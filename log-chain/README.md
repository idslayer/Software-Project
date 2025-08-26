- Monitor Smart Contract:

./monitordocker.sh fabric_test


- On Fabric Server:
sudo bash anchor_script/down.sh
sudo bash anchor_script/init_network.sh
sudo chmod -R 777 /home/tik/Desktop/log-chain/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com

- On Local Machine:
scp -r tik@192.168.1.7:/home/tik/Desktop/log-chain/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/ ./orgs