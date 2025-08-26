# Set environment for Org1 peer (from test-network docs)
export FABRIC_DIR=$PWD/fabric-samples
export PATH=$FABRIC_DIR/bin:$PATH
export FABRIC_CFG_PATH=$FABRIC_DIR/config/
export FABRIC_NET=$FABRIC_DIR/test-network
. ./organizations/peerOrganizations/org1.example.com/envVar.sh

# PutAnchor
peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com \
 --tls --cafile "${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
 -C audit -n anchor \
 --peerAddresses localhost:7051 \
 --tlsRootCertFiles "${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
 -c '{"Args":["PutAnchor","batch-0001","0xabc123...", "1000", "1723600000000000000","1723600000999999999","0xprev...","first batch"]}'

# GetAnchor
peer chaincode query -C log-chain -n anchor -c '{"Args":["GetAnchor","batch-0001"]}'

# LatestAnchor
peer chaincode query -C audit -n anchor -c '{"Args":["LatestAnchor"]}'
