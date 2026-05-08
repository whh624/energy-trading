const Web3 = require('web3');
const fs = require('fs');
const path = require('path');

async function deploy() {
    console.log('Starting contract deployment...');
    
    const web3 = new Web3('http://127.0.0.1:8545');
    
    const accounts = await web3.eth.getAccounts();
    console.log('Available accounts:', accounts);
    
    const deployerAccount = accounts[0];
    console.log('Deploying from account:', deployerAccount);
    
    const contractABI = [
        {
            "inputs": [],
            "name": "deposit",
            "outputs": [],
            "stateMutability": "payable",
            "type": "function"
        },
        {
            "inputs": [{"name": "amount", "type": "uint256"}],
            "name": "withdraw",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {"name": "_amount", "type": "uint256"},
                {"name": "_price", "type": "uint256"}
            ],
            "name": "createOrder",
            "outputs": [{"name": "", "type": "uint256"}],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {"name": "_orderId", "type": "uint256"},
                {"name": "_amount", "type": "uint256"}
            ],
            "name": "buyEnergy",
            "outputs": [],
            "stateMutability": "payable",
            "type": "function"
        },
        {
            "inputs": [{"name": "_orderId", "type": "uint256"}],
            "name": "cancelOrder",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [{"name": "_orderId", "type": "uint256"}],
            "name": "getOrder",
            "outputs": [
                {"name": "id", "type": "uint256"},
                {"name": "seller", "type": "address"},
                {"name": "amount", "type": "uint256"},
                {"name": "price", "type": "uint256"},
                {"name": "status", "type": "uint8"},
                {"name": "createdAt", "type": "uint256"}
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "getOpenOrders",
            "outputs": [{"name": "", "type": "uint256[]"}],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [{"name": "_user", "type": "address"}],
            "name": "getBalance",
            "outputs": [
                {"name": "available", "type": "uint256"},
                {"name": "frozen", "type": "uint256"}
            ],
            "stateMutability": "view",
            "type": "function"
        }
    ];
    
    const contractBytecode = '0x6080604052346000';
    
    const contract = new web3.eth.Contract(contractABI);
    
    try {
        const deployedContract = await contract.deploy({
            data: '0x' + contractBytecode,
            arguments: []
        }).send({
            from: deployerAccount,
            gas: 5000000
        });
        
        console.log('\n========================================');
        console.log('Contract deployed successfully!');
        console.log('Contract Address:', deployedContract.options.address);
        console.log('========================================\n');
        
        const config = {
            contractAddress: deployedContract.options.address,
            deployerAddress: deployerAccount,
            networkId: 1337,
            rpcUrl: 'http://127.0.0.1:8545'
        };
        
        const configPath = path.join(__dirname, 'deploy-config.json');
        fs.writeFileSync(configPath, JSON.stringify(config, null, 2));
        console.log('Deployment config saved to:', configPath);
        
        return deployedContract.options.address;
    } catch (error) {
        console.error('Deployment failed:', error);
        throw error;
    }
}

deploy().then(address => {
    console.log('\nNext steps:');
    console.log('1. Copy the contract address to application.properties');
    console.log('2. Update web3j.contract.address=' + address);
    process.exit(0);
}).catch(err => {
    console.error(err);
    process.exit(1);
});
