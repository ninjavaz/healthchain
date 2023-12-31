#!/bin/bash
# This is a collection of bash functions used by different scripts

. /vagrant/network/.env
export HOME_PATH=${HOME_PATH}
export CORE_PEER_TLS_ENABLED=true
export ORDERER_CA=${HOME_PATH}/organizations/ordererOrganizations/healthchain.com/orderers/orderer.healthchain.com/msp/tlscacerts/tlsca.healthchain.com-cert.pem
export PEER0_HOSP1_CA=${HOME_PATH}/organizations/peerOrganizations/hosp1.healthchain.com/peers/peer0.hosp1.healthchain.com/tls/ca.crt
export PEER0_HOSP2_CA=${HOME_PATH}/organizations/peerOrganizations/hosp2.healthchain.com/peers/peer0.hosp2.healthchain.com/tls/ca.crt

# Set OrdererOrg.Admin globals
setOrdererGlobals() {
  export CORE_PEER_LOCALMSPID="OrdererMSP"
  export CORE_PEER_TLS_ROOTCERT_FILE=${HOME_PATH}/organizations/ordererOrganizations/healthchain.com/orderers/orderer.healthchain.com/msp/tlscacerts/tlsca.healthchain.com-cert.pem
  export CORE_PEER_MSPCONFIGPATH=${HOME_PATH}/organizations/ordererOrganizations/healthchain.com/users/Admin@healthchain.com/msp
}

# Set environment variables for the peer org
setGlobals() {
  local USING_ORG=""
  if [ -z "$OVERRIDE_ORG" ]; then
    USING_ORG=$1
  else
    USING_ORG="${OVERRIDE_ORG}"
  fi
  echo "Using Hospital ${USING_ORG}"
  if [ $USING_ORG -eq 1 ]; then
    export CORE_PEER_LOCALMSPID="hosp1MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${HOME_PATH}/organizations/peerOrganizations/hosp1.healthchain.com/peers/peer0.hosp1.healthchain.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${HOME_PATH}/organizations/peerOrganizations/hosp1.healthchain.com/users/Admin@hosp1.healthchain.com/msp
    # export CORE_PEER_MSPCONFIGPATH=${HOME_PATH}/organizations/peerOrganizations/hosp1.healthchain.com/peers/peer0.hosp1.healthchain.com/msp
    export CORE_PEER_ADDRESS=localhost:7051
  elif [ $USING_ORG -eq 2 ]; then
    export CORE_PEER_LOCALMSPID="hosp2MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${HOME_PATH}/organizations/peerOrganizations/hosp2.healthchain.com/peers/peer0.hosp2.healthchain.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${HOME_PATH}/organizations/peerOrganizations/hosp2.healthchain.com/users/Admin@hosp2.healthchain.com/msp
    # export CORE_PEER_MSPCONFIGPATH=${HOME_PATH}/organizations/peerOrganizations/hosp2.healthchain.com/peers/peer0.hosp2.healthchain.com/msp
    export CORE_PEER_ADDRESS=localhost:9051
  else
    echo "hosp Unknown"
  fi

  if [ "$VERBOSE" == "true" ]; then
    env | grep CORE
  fi
}

# parsePeerConnectionParameters $@
# Helper function that sets the peer connection parameters for a chaincode
# operation
parsePeerConnectionParameters() {

  PEER_CONN_PARMS=""
  PEERS=""
  while [ "$#" -gt 0 ]; do
    setGlobals $1
    PEER="peer0.hosp$1"
    ## Set peer addresses
    PEERS="$PEERS $PEER"
    PEER_CONN_PARMS="$PEER_CONN_PARMS --peerAddresses $CORE_PEER_ADDRESS"
    ## Set path to TLS certificate
    TLSINFO=$(eval echo "--tlsRootCertFiles \$PEER0_HOSP$1_CA")
    PEER_CONN_PARMS="$PEER_CONN_PARMS $TLSINFO"
    # shift by one to get to the next organization
    shift
  done
  # remove leading space for output
  PEERS="$(echo -e "$PEERS" | sed -e 's/^[[:space:]]*//')"
}

verifyResult() {
  if [ $1 -ne 0 ]; then
    echo "$2"
  fi
}
