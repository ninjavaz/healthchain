#!bin/bash
. .env
cd ${HOME_PATH}
rm -rf organizations/ordererOrganizations
rm -rf organizations/peerOrganizations
rm -rf fabric-ca/hosp1
rm -rf fabric-ca/hosp2
rm -rf fabric-ca/ordererOrg
rm -rf channel-artifacts
rm -rf system-genesis-block

echo "---- Deleted all identities and blocks files ----"
