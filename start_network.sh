#!/bin/bash
#
# Author: Dominik Nuszkiewicz
# Script that brings up a HL Fabric network 2.2.14 in order to implement
# system for engineering thesis

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/config
export VERBOSE=false

# Obtain CONTAINER_IDS and remove them
## This function is called when you bring a network down
function clearContainers() {
  CONTAINER_IDS=$(docker ps -a | awk '($2 ~ /dev-peer.*/) {print $1}')
  if [ -z "$CONTAINER_IDS" -o "$CONTAINER_IDS" == " " ]; then
    infoln "No containers available for deletion"
  else
    docker rm -f $CONTAINER_IDS
  fi
}

# Delete any images that were generated as a part of this setup
# specifically the following images are often left behind:
## This function is called when you bring the network down
function removeUnwantedImages() {
  DOCKER_IMAGE_IDS=$(docker images | awk '($1 ~ /dev-peer.*/) {print $3}')
  if [ -z "$DOCKER_IMAGE_IDS" -o "$DOCKER_IMAGE_IDS" == " " ]; then
    infoln "No images available for deletion"
  else
    docker rmi -f $DOCKER_IMAGE_IDS
  fi
}

# Tear down running network
function networkDown() {
  # docker compose --env-file ./.env -f $COMPOSE_FILE_BASE -f $COMPOSE_FILE_COUCH -f $COMPOSE_FILE_CA -f $COMPOSE_FILE_REDIS down --volumes --remove-orphans
  docker compose --env-file ./.env -f $COMPOSE_FILE_BASE -f $COMPOSE_FILE_COUCH -f $COMPOSE_FILE_CA down --volumes --remove-orphans
  # Don't remove the generated artifacts -- note, the ledgers are always removed
  if [ "$MODE" != "restart" ]; then
    # Bring down the network, deleting the volumes
    #Cleanup the chaincode containers
    clearContainers
    #Cleanup images
    removeUnwantedImages
    # # remove orderer block and other channel configuration transactions and certs
    # docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf system-genesis-block/*.block organizations/peerOrganizations organizations/ordererOrganizations'
    # ## remove fabric ca artifacts
    # docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/hosp1/msp organizations/fabric-ca/hosp1/tls-cert.pem organizations/fabric-ca/hosp1/ca-cert.pem organizations/fabric-ca/hosp1/IssuerPublicKey organizations/fabric-ca/hosp1/IssuerRevocationPublicKey organizations/fabric-ca/hosp1/fabric-ca-server.db'
    # docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/hosp2/msp organizations/fabric-ca/hosp2/tls-cert.pem organizations/fabric-ca/hosp2/ca-cert.pem organizations/fabric-ca/hosp2/IssuerPublicKey organizations/fabric-ca/hosp2/IssuerRevocationPublicKey organizations/fabric-ca/hosp2/fabric-ca-server.db'
    # docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf organizations/fabric-ca/ordererOrg/msp organizations/fabric-ca/ordererOrg/tls-cert.pem organizations/fabric-ca/ordererOrg/ca-cert.pem organizations/fabric-ca/ordererOrg/IssuerPublicKey organizations/fabric-ca/ordererOrg/IssuerRevocationPublicKey organizations/fabric-ca/ordererOrg/fabric-ca-server.db'
    # # remove channel and script artifacts
    # docker run --rm -v $(pwd):/data busybox sh -c 'cd /data && rm -rf channel-artifacts log.txt *.tar.gz'

  fi

  # # Remove files from wallet
  # rm -rf ..wallet
  # echo "The wallet has cleared."
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
  DOCKER_IMAGE_VERSION=$(docker run --rm hyperledger/fabric-tools:2.2.14 peer version | sed -ne 's/ Version: //p' | head -1)
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
    createConsortium
  fi

  COMPOSE_FILES="-f ${COMPOSE_FILE_BASE}"

  if [ "${DATABASE}" == "couchdb" ]; then
    COMPOSE_FILES="${COMPOSE_FILES} -f ${COMPOSE_FILE_COUCH}"
  fi

  IMAGE_TAG=$IMAGETAG docker compose --env-file ./.env ${COMPOSE_FILES} up -d 2>&1

  # docker compose --env-file ./.env -f "${COMPOSE_FILE_REDIS}" up -d

  docker ps -a
  if [ $? -ne 0 ]; then
    echo "Unable to start network"
  fi
}

# Create Organization crypto material using CAs
function createOrgs() {

  #what is the reason to remove folders if it has been generated previously?
  if [ -d "organizations/peerOrganizations" ]; then
    rm -Rf organizations/peerOrganizations && rm -Rf organizations/ordererOrganizations
  fi

  # Create crypto material using Fabric CAs
    echo "Generate certificates using Fabric CA's"

    IMAGE_TAG=${CA_IMAGETAG} docker compose -f $COMPOSE_FILE_CA up -d 2>&1

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

  echo "---- Generate CCP files for Hospital 1 and Hospital 2 ----"
  ./organizations/ccp-generate.sh
}

# Generate orderer system channel genesis block.
function createConsortium() {

  which configtxgen
  if [ "$?" -ne 0 ]; then
    echo "---- configtxgen tool not found. ----"
  fi

  echo "---- Generating Orderer Genesis block ----"

  # Note: For some unknown reason (at least for now) the block file can't be
  # named orderer.genesis.block or the orderer will fail to launch!
  set -x
  configtxgen -configPath $FABRIC_CFG_PATH -profile TwoOrgsOrdererGenesis -channelID system-channel -outputBlock ./system-genesis-block/genesis.block
  res=$?
  { set +x; } 2>/dev/null
  if [ $res -ne 0 ]; then
    echo "---- Failed to generate orderer genesis block... ----"
  fi
}

## call the script to join create the channel and join the peers of org1 and org2
function createChannel() {

## Bring up the network if it is not already up.

  if [ ! -d "organizations/peerOrganizations" ]; then
    echo "Bringing up network"
    networkUp
  fi

  # now run the script that creates a channel. This script uses configtxgen once
  # more to create the channel creation transaction and the anchor peer updates.
  # configtx.yaml is mounted in the cli container, which allows us to use it to
  # create the channel artifacts
  scripts/createChannel.sh $CHANNEL_NAME $CLI_DELAY $MAX_RETRY $VERBOSE
  if [ $? -ne 0 ]; then
    echo "Create channel failed"
  fi

}


## Call the script to deploy a chaincode to the channel
function deployCC() {

  scripts/deployCC.sh $CHANNEL_NAME $CC_NAME $CC_SRC_PATH $CC_SRC_LANGUAGE $CC_VERSION $CC_SEQUENCE $CC_INIT_FCN $CC_END_POLICY $CC_COLL_CONFIG $CLI_DELAY $MAX_RETRY $VERBOSE "false"

  if [ $? -ne 0 ]; then
    echo "Deploying chaincode failed"
  fi

  exit 0
}

. .env
export FABRIC_CFG_PATH
#env variables
COMPOSE_FILE_BASE=docker/docker-compose-network.yaml
# certificate authorities compose file
COMPOSE_FILE_CA=docker/docker-compose-ca.yaml
# default image tag
IMAGETAG=$FABRIC_VERSION
# default ca image tag
CA_IMAGETAG=$FABRIC_CA_VERSION
# docker compose.yaml file if you are using couchdb
COMPOSE_FILE_COUCH=docker/docker-compose-couch.yaml
# default database
DATABASE="couchdb"

# main channel name
CHANNEL_NAME="hospitalchannel"
# chaincode name defaults to "basic"
CC_NAME="healthchainCC1"
# chaincode path defaults to "NA"
CC_SRC_PATH="NA"
#language for chaincode
CC_SRC_LANGUAGE="javascript"
# Chaincode version
CC_VERSION="4.0"
# Chaincode definition sequence
CC_SEQUENCE=3
# chaincode init function, default is "NA"
CC_INIT_FCN="initLedger"
# endorsement policy defaults to "NA". This would allow chaincodes to use the majority default policy.
CC_END_POLICY="NA"
# collection configuration defaults to "NA"
# CC_COLL_CONFIG="../private-collections/private-collections.json"
CC_COLL_CONFIG="NA"
# default for delay between commands
CLI_DELAY=3
# timeout duration - the duration the CLI should wait for a response from
# another container before giving up
MAX_RETRY=5
VERBOSE=false

## Parse mode
if [[ $# -lt 1 ]] ; then
  exit 0
else
  MODE=$1
  shift
fi

# Determine mode of operation and printing out what we asked for
if [ "$MODE" == "up" ]; then
  echo "Starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds and using database '${DATABASE}' ${CRYPTO_MODE}"
elif [ "$MODE" == "createChannel" ]; then
  echo "Creating channel '${CHANNEL_NAME}'."
  echo "If network is not up, starting nodes with CLI timeout of '${MAX_RETRY}' tries and CLI delay of '${CLI_DELAY}' seconds and using database '${DATABASE} ${CRYPTO_MODE}"
elif [ "$MODE" == "deployCC" ]; then
  echo "deploying chaincode on channel '${CHANNEL_NAME}'"
elif [ "$MODE" == "down" ]; then
  echo "Stopping network"
elif [ "$MODE" == "restart" ]; then
  echo "Restarting network"
else
  exit 1
fi

if [ "${MODE}" == "up" ]; then
  networkUp
elif [ "${MODE}" == "createChannel" ]; then
  createChannel
elif [ "${MODE}" == "deployCC" ]; then
  deployCC
elif [ "${MODE}" == "down" ]; then
  networkDown
elif [ "${MODE}" == "restart" ]; then
  networkDown
else
  exit 1
fi