#!/bin/bash
set -e

FABRIC_VERSION=2.5.5
CA_VERSION=1.5.9
FABRIC_DIR=$PWD/fabric-samples

# Deploy chaincode
export CHANNEL=${CHANNEL:-"log-chain"}
export CC_NAME=${CC_NAME:-"anchor-contract"}
export CC_LABEL=${CC_LABEL:-"anchor_1"}
export CC_PATH=${CC_PATH:-"$PWD/anchor-contract"}


# Check if fabric-samples directory exists
if [ ! -d "$FABRIC_DIR" ]; then
    echo ">>> Downloading Fabric binaries & samples..."
    curl -sSL https://bit.ly/2ysbOFE | bash -s -- $FABRIC_VERSION $CA_VERSION
fi

echo ">>> Adding fabric binaries to PATH..."
export PATH=$PATH:$FABRIC_DIR/bin
echo "export PATH=\$PATH:$FABRIC_DIR/bin" >> ~/.bashrc

cd $FABRIC_DIR/test-network

echo "Stopping any existing network..."
./network.sh down

echo "Starting Fabric network with channel '$CHANNEL'..."
./network.sh up createChannel -c $CHANNEL

echo "Deploying chaincode 'anchor'..."
./network.sh deployCC -c $CHANNEL -ccn $CC_NAME -ccp $CC_PATH -ccl java

# ./network.sh deployCC -c log-chain -ccn basic -ccp $PWD/../asset-transfer-basic/chaincode-java -ccl java

echo "Network ready!"
echo "Fabric binaries are in $FABRIC_DIR/bin"
echo "Channel: $CHANNEL"
echo "Chaincode: $CC_NAME"
