export FABRIC_DIR=$PWD/fabric-samples

export PATH=$FABRIC_DIR/bin:$PATH
export FABRIC_CFG_PATH=$FABRIC_DIR/config/

export FABRIC_NET=$FABRIC_DIR/test-network

export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${FABRIC_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${FABRIC_NET}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

export CHANNEL=log-chain
export CC_NAME=anchor-contract

peer chaincode query -C $CHANNEL -n $CC_NAME -c '{"Args":["LatestAnchor"]}'