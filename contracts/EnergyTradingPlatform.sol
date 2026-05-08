// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract EnergyTradingPlatform {
    enum OrderStatus {
        OPEN,
        FILLED,
        CANCELLED
    }

    struct Order {
        uint256 id;
        address seller;
        uint256 amount;
        uint256 price;
        OrderStatus status;
        uint256 createdAt;
    }

    struct Transaction {
        uint256 id;
        uint256 orderId;
        address buyer;
        address seller;
        uint256 amount;
        uint256 totalPrice;
        uint256 timestamp;
    }

    uint256 private orderIdCounter;
    uint256 private transactionIdCounter;

    mapping(uint256 => Order) private orders;
    mapping(uint256 => Transaction) private transactions;
    mapping(address => uint256[]) private userTransactions;
    
    mapping(address => uint256) private frozenBalances;
    mapping(address => uint256) private availableBalances;

    event OrderCreated(uint256 orderId, address seller, uint256 amount, uint256 price);
    event EnergyBought(uint256 transactionId, uint256 orderId, address buyer, uint256 amount, uint256 totalPrice);
    event OrderCancelled(uint256 orderId);
    event FundsFrozen(address user, uint256 amount);
    event FundsUnfrozen(address user, uint256 amount);
    event Deposit(address user, uint256 amount);
    event Withdrawal(address user, uint256 amount);

    modifier hasEnoughBalance(uint256 amount) {
        require(availableBalances[msg.sender] >= amount, "Insufficient available balance");
        _;
    }

    function deposit() external payable {
        require(msg.value > 0, "Deposit amount must be greater than 0");
        availableBalances[msg.sender] += msg.value;
        emit Deposit(msg.sender, msg.value);
    }

    function withdraw(uint256 amount) external hasEnoughBalance(amount) {
        availableBalances[msg.sender] -= amount;
        payable(msg.sender).transfer(amount);
        emit Withdrawal(msg.sender, amount);
    }

    function freezeFunds(uint256 amount) internal {
        require(availableBalances[msg.sender] >= amount, "Insufficient available balance");
        availableBalances[msg.sender] -= amount;
        frozenBalances[msg.sender] += amount;
        emit FundsFrozen(msg.sender, amount);
    }

    function unfreezeFunds(address user, uint256 amount) internal {
        require(frozenBalances[user] >= amount, "Insufficient frozen balance");
        frozenBalances[user] -= amount;
        availableBalances[user] += amount;
        emit FundsUnfrozen(user, amount);
    }

    function createOrder(uint256 _amount, uint256 _price) external returns (uint256) {
        require(_amount > 0, "Amount must be greater than 0");
        require(_price > 0, "Price must be greater than 0");
        
        uint256 orderId = orderIdCounter++;
        orders[orderId] = Order({
            id: orderId,
            seller: msg.sender,
            amount: _amount,
            price: _price,
            status: OrderStatus.OPEN,
            createdAt: block.timestamp
        });
        
        emit OrderCreated(orderId, msg.sender, _amount, _price);
        return orderId;
    }

    function buyEnergy(uint256 _orderId, uint256 _amount) external payable {
        Order storage order = orders[_orderId];
        require(order.id == _orderId, "Order does not exist");
        require(order.status == OrderStatus.OPEN, "Order status is not correct");
        require(_amount > 0, "Purchase amount must be greater than 0");
        require(_amount <= order.amount, "Purchase amount cannot exceed order amount");
        
        uint256 totalPrice = _amount * order.price;
        
        if (msg.value >= totalPrice) {
            if (msg.value > totalPrice) {
                payable(msg.sender).transfer(msg.value - totalPrice);
            }
            payable(order.seller).transfer(totalPrice);
        } else {
            uint256 remaining = totalPrice - msg.value;
            require(frozenBalances[msg.sender] >= remaining, "Insufficient frozen balance");
            frozenBalances[msg.sender] -= remaining;
            availableBalances[order.seller] += totalPrice;
        }
        
        uint256 transactionId = transactionIdCounter++;
        Transaction memory transaction = Transaction({
            id: transactionId,
            orderId: _orderId,
            buyer: msg.sender,
            seller: order.seller,
            amount: _amount,
            totalPrice: totalPrice,
            timestamp: block.timestamp
        });
        transactions[transactionId] = transaction;
        userTransactions[msg.sender].push(transactionId);
        userTransactions[order.seller].push(transactionId);
        
        order.amount -= _amount;
        if (order.amount == 0) {
            order.status = OrderStatus.FILLED;
        }
        
        emit EnergyBought(transactionId, _orderId, msg.sender, _amount, totalPrice);
    }

    function cancelOrder(uint256 _orderId) external {
        Order storage order = orders[_orderId];
        require(order.id == _orderId, "Order does not exist");
        require(order.seller == msg.sender, "Only seller can cancel order");
        require(order.status == OrderStatus.OPEN, "Can only cancel open orders");
        
        order.status = OrderStatus.CANCELLED;
        emit OrderCancelled(_orderId);
    }

    function getOrder(uint256 _orderId) external view returns (
        uint256 id,
        address seller,
        uint256 amount,
        uint256 price,
        OrderStatus status,
        uint256 createdAt
    ) {
        Order storage order = orders[_orderId];
        require(order.id == _orderId, "Order does not exist");
        return (
            order.id,
            order.seller,
            order.amount,
            order.price,
            order.status,
            order.createdAt
        );
    }

    function getUserTransactions(address _user) external view returns (uint256[] memory) {
        return userTransactions[_user];
    }

    function getTransaction(uint256 _transactionId) external view returns (
        uint256 id,
        uint256 orderId,
        address buyer,
        address seller,
        uint256 amount,
        uint256 totalPrice,
        uint256 timestamp
    ) {
        Transaction storage transaction = transactions[_transactionId];
        require(transaction.id == _transactionId, "Transaction does not exist");
        return (
            transaction.id,
            transaction.orderId,
            transaction.buyer,
            transaction.seller,
            transaction.amount,
            transaction.totalPrice,
            transaction.timestamp
        );
    }

    function getOpenOrders() external view returns (uint256[] memory) {
        uint256 count = 0;
        for (uint256 i = 0; i < orderIdCounter; i++) {
            if (orders[i].status == OrderStatus.OPEN) {
                count++;
            }
        }
        
        uint256[] memory openOrders = new uint256[](count);
        uint256 index = 0;
        for (uint256 i = 0; i < orderIdCounter; i++) {
            if (orders[i].status == OrderStatus.OPEN) {
                openOrders[index] = i;
                index++;
            }
        }
        
        return openOrders;
    }

    function getUserOrders(address _user) external view returns (uint256[] memory) {
        uint256 count = 0;
        for (uint256 i = 0; i < orderIdCounter; i++) {
            if (orders[i].seller == _user) {
                count++;
            }
        }
        
        uint256[] memory userOrders = new uint256[](count);
        uint256 index = 0;
        for (uint256 i = 0; i < orderIdCounter; i++) {
            if (orders[i].seller == _user) {
                userOrders[index] = i;
                index++;
            }
        }
        
        return userOrders;
    }

    function getBalance(address _user) external view returns (uint256 available, uint256 frozen) {
        return (availableBalances[_user], frozenBalances[_user]);
    }

    function getTotalBalance(address _user) external view returns (uint256) {
        return availableBalances[_user] + frozenBalances[_user];
    }
}
