#!/bin/bash
set -e

FABRIC_DIR=$PWD/fabric-samples
cd $FABRIC_DIR/test-network

echo "Stopping any existing network..."
./network.sh down