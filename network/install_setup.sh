#!/bin/bash
#
# Author: Dominik Nuszkiewicz
# Script that install prerequisites of Hyperledger Fabric 2.1.0
# to carry out engineering thesis system
#prereqs needed: 
# - install Oracle VM VirtualBox - Version 7.0.8 on windows10
# - install vagrant on windows10 -> https://developer.hashicorp.com/vagrant/install 
#1.1 go
#2.1 curl
#2.2 docker (5:24.0.7-1~ubuntu.20.04~focal)
#2.3 docker-compose (Docker Compose version v2.21.0)
#2.4 jq
#2.5 git
#3.1 fabric 2.2.14


if [ -n "$SUDO_COMMAND" ]
then
    echo "---- Please don't run this script with sudo. ----"
    exit
fi

export DOCKER_VERSION=5:24.0.7-1~ubuntu.20.04~focal
export SUDO_USER=vagrant
. .env
export FABRIC_CFG_PATH=${FABRIC_CFG_PATH}

#1. GO
# Get the version 1.13 from google
wget https://dl.google.com/go/go1.13.3.linux-amd64.tar.gz
act='ttyout="*"'
sudo tar -xf go1.13.3.linux-amd64.tar.gz --checkpoint --checkpoint-action=$act -C /usr/local 
rm go1.13.3.linux-amd64.tar.gz

# If GOROOT already set then DO Not set it again
if [ -z $GOROOT ]
then
    echo "export GOROOT=/usr/local/go" >> ~/.profile
    echo "export PATH=$PATH:/usr/local/go/bin" >> ~/.profile

    GOPATH=$PWD/../gopath
    GOPATH="$(cd "$(dirname "$GOPATH")"; pwd)/$(basename "$GOPATH")"

    echo "export GOPATH=$GOPATH" >> ~/.profile
    echo "======== Updated .profile with GOROOT/GOPATH/PATH===="

    echo "export GOROOT=/usr/local/go" >> ~/.bashrc
    echo "export GOPATH=$GOPATH" >> ~/.bashrc
    echo "======== Updated .profile with GOROOT/GOPATH/PATH===="

    export PATH=$PATH:/usr/local/go/bin

    GOCACHE="$HOME/.go-cache"
    echo "export GOCACHE=$HOME/.go-cache" >> $HOME/.bashrc
    sudo mkdir -p $GOCACHE
    chown -R $USER $GOCACHE

else
    echo "======== No Change made to .profile ====="
fi

echo "======= Done. PLEASE LOG OUT & LOG Back In ===="
echo "Then validate by executing    'go version'"

#2. others
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates git curl software-properties-common jq
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get install -y "docker-ce=${DOCKER_VERSION}" 
sudo apt-get install -y "docker-ce-cli=${DOCKER_VERSION}" 
sudo apt-get install -y containerd.io docker-buildx-plugin docker-compose-plugin
sudo docker info
echo "---- Adding $SUDO_USER to the docker group ----"
sudo usermod -aG docker vagrant

#start docker
sudo service docker restart
sudo systemctl daemon-reload
sudo systemctl restart docker

#3. fabric
SETUP_FOLDER=$PWD
export PATH=$PATH:$GOROOT/bin

echo "GOPATH=$GOPATH"
echo "GOROOT=$GOROOT"

sudo mkdir $GOPATH

rm -rf ./temp 2> /dev/null
# create temp directory
sudo mkdir temp  &> /dev/null
cd temp

echo "---- Starting to Download Fabric ----"
sudo curl -sSL https://bit.ly/2ysbOFE |bash -s $FABRIC_VERSION $FABRIC_CA_VERSION
BIN_PATH=/usr/local/bin
echo "---- Copying the binaries to /usr/local/bin ----"
sudo cp -r ./fabric-samples/bin/* $BIN_PATH
sudo rm -rf ./fabric-samples/bin
# Clean up
cd ..
sudo rm -rf temp

echo "export PATH=$BIN_PATH:$PATH" >> $HOME/.profile
echo "export PATH=$BIN_PATH:$PATH" >> $HOME/.bashrc

sudo chmod u+x $BIN_PATH/*

echo "---- Done. ----"

