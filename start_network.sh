#!/bin/bash
#
# Author: Dominik Nuszkiewicz
# Script that brings up a HL Fabric network 2.2.14 in order to implement
# system for engineering thesis



# Obtain CONTAINER_IDS and remove them
## This function is called when you bring a network down
function clearContainers() {
  CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /hyperledger.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == " " ]; then
    echo "---- No containers available to delete ----"
  else
    docker rm -f $CONTAINER_IDS
  fi
}

# Delete any images that were generated as a part of this setup
# specifically the following images are often left behind:
## This function is called when you bring the network down
function removeUnwantedImages() {
  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /hyperledger.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    echo "---- No images available for deletion ----"
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi
}

# Do some basic sanity checking to make sure that the appropriate versions of fabric
# binaries/images are available. In the future, additional checking for the presence
# of go or other items could be added.
function checkPrereqs() {
  ## Check if your have cloned the peer binaries and configuration files.
  peer version > /dev/null 2>&1
  if [[ $? -ne 0 || ! -d "./config" ]]; then
    echo "Peer binary and configuration files not found.."
    echo
    echo "Follow the instructions in the Fabric docs to install the Fabric Binaries:"
    echo "https://hyperledger-fabric.readthedocs.io/en/latest/install.html"
    exit 1
  fi

  # use the fabric tools container to see if the samples and binaries match your
  # docker images
  LOCAL_VERSION=$(peer version | sed -ne 's/ Version: //p')
  DOCKER_IMAGE_VERSION=$(docker run --rm hyperledger/fabric-tools:$IMAGETAG peer version | sed -ne 's/ Version: //p' | head -1)
  echo "LOCAL_VERSION=$LOCAL_VERSION"
  echo "DOCKER_IMAGE_VERSION=$DOCKER_IMAGE_VERSION"

  if [ "$LOCAL_VERSION" != "$DOCKER_IMAGE_VERSION" ]; then
    echo "Local fabric binaries and docker images are out of sync. This may cause problems."
  else
    echo "---- LOCAL_VERSION OK ----"
  fi

  # Check for fabric-ca
  fabric-ca-client version > /dev/null 2>&1
  if [[ $? -ne 0 ]]; then
    echo "fabric-ca-client binary not found.."
    echo
    echo "Follow the instructions in the Fabric docs to install the Fabric Binaries:"
    echo "https://hyperledger-fabric.readthedocs.io/en/latest/install.html"
    exit 1
  fi
  CA_LOCAL_VERSION=$(fabric-ca-client version | sed -ne 's/ Version: //p')
  CA_DOCKER_IMAGE_VERSION=$(docker run --rm hyperledger/fabric-ca:$CA_IMAGETAG fabric-ca-client version | sed -ne 's/ Version: //p' | head -1)
  echo "CA_LOCAL_VERSION=$CA_LOCAL_VERSION"
  echo "CA_DOCKER_IMAGE_VERSION=$CA_DOCKER_IMAGE_VERSION"

  if [ "$CA_LOCAL_VERSION" != "$CA_DOCKER_IMAGE_VERSION" ]; then
    echo "Local fabric-ca binaries and docker images are out of sync. This may cause problems."
  else
    echo "---- CA_LOCAL_VERSION OK ----"
  fi
}


# Bring up the peer and orderer nodes using docker compose.
function networkUp() {

  checkPrereqs
  # generate artifacts if they don't exist
  if [ ! -d "organizations/peerOrganizations" ]; then
    createOrgs
    # createConsortium
  fi

  # COMPOSE_FILES="-f ${COMPOSE_FILE_BASE}"

  # if [ "${DATABASE}" == "couchdb" ]; then
  #   COMPOSE_FILES="${COMPOSE_FILES} -f ${COMPOSE_FILE_COUCH}"
  # fi

  # IMAGE_TAG=$IMAGETAG docker compose --env-file ./.env ${COMPOSE_FILES} up -d 2>&1

  # docker compose --env-file ./.env -f "${COMPOSE_FILE_REDIS}" up -d

  # docker ps -a
  # if [ $? -ne 0 ]; then
  #   fatalln "Unable to start network"
  # fi
}

# Create Organization crypto material using CAs
function createOrgs() {

  #what is the reason to remove folders if it has been generated previously?
  if [ -d "organizations/peerOrganizations" ]; then
    rm -Rf organizations/peerOrganizations && rm -Rf organizations/ordererOrganizations
  fi

  # Create crypto material using Fabric CAs
    echo "Generate certificates using Fabric CA's"

    IMAGE_TAG=${CA_IMAGETAG} docker compose -f $COMPOSE_FILE_CA up -d

    . ./organizations/fabric-ca/registerEnroll.sh

  while :
    do
      if [ ! -f "organizations/fabric-ca/hosp1/tls-cert.pem" ]; then
        sleep 1
      else
        break
      fi
  done

  echo "---- Create hosp1 Identities ----"

  createHosp1

  echo "---- Create hosp2 Identities ----"

  createHosp2

  echo "---- Create orderer Identities ----"

  createOrderer



  # infoln "Generate CCP files for Hospital 1 and Hospital 2"
  # ./organizations/ccp-generate.sh

  #TUTAJ SKONCZONE 2:14 26.11.2023
}

. .env
#env variables
COMPOSE_FILE_BASE=docker/docker-compose-main.yaml
# certificate authorities compose file
COMPOSE_FILE_CA=docker/docker-compose-ca.yaml
# default image tag
IMAGETAG=$FABRIC_VERSION
# default ca image tag
CA_IMAGETAG=$FABRIC_CA_VERSION






clearContainers;
# removeUnwantedImages;
networkUp;
