#!bin/bash
. .env
cd ${HOME_PATH}
rm -rf organizations/ordererOrganizations
rm -rf organizations/peerOrganizations
rm -rf organizations/fabric-ca/hosp1
rm -rf organizations/fabric-ca/hosp2
rm -rf organizations/fabric-ca/ordererOrg
rm -rf channel-artifacts
rm -rf system-genesis-block
rm -rf ../backend/wallet/*

echo "---- Deleted all identities and blocks files ----"
