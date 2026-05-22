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
    bool private locked;

    // --- 新增 PBFT 共识相关变量 ---
    enum ConsensusPhase { NONE, PRE_PREPARE, PREPARE, COMMIT, SUCCESS }
    
    struct PBFTState {
        ConsensusPhase phase;
        uint256 prepareCount;
        uint256 commitCount;
        mapping(address => bool) hasPrepared;
        mapping(address => bool) hasCommitted;
    }

    mapping(uint256 => PBFTState) private pbftStates;
    mapping(address => bool) public isValidator;
    uint256 public validatorCount;
    uint256 public constant MIN_QUORUM = 3; // 假设 4 个节点，2f+1=3

    event PBFTPhaseChanged(uint256 orderId, ConsensusPhase phase);
    event ValidatorAdded(address validator);
    // --------------------------

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

    modifier nonReentrant() {
        require(!locked, "Reentrancy detected");
        locked = true;
        _;
        locked = false;
    }

    modifier orderExists(uint256 orderId) {
        require(orderId < orderIdCounter, "Order does not exist");
        _;
    }

    modifier transactionExists(uint256 transactionId) {
        require(transactionId < transactionIdCounter, "Transaction does not exist");
        _;
    }

    function safeTransferETH(address to, uint256 amount) internal {
        if (amount == 0) {
            return;
        }

        (bool success, ) = payable(to).call{value: amount}("");
        require(success, "ETH transfer failed");
    }

    function deposit() external payable {
        require(msg.value > 0, "Deposit amount must be greater than 0");
        availableBalances[msg.sender] += msg.value;
        emit Deposit(msg.sender, msg.value);
    }

    function freezeBalance(uint256 amount) external hasEnoughBalance(amount) {
        require(amount > 0, "Freeze amount must be greater than 0");
        freezeFunds(amount);
    }

    function unfreezeBalance(uint256 amount) external {
        require(amount > 0, "Unfreeze amount must be greater than 0");
        unfreezeFunds(msg.sender, amount);
    }

    function withdraw(uint256 amount) external hasEnoughBalance(amount) nonReentrant {
        availableBalances[msg.sender] -= amount;
        safeTransferETH(msg.sender, amount);
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
        
        // 初始化该订单的投票 (重置)
        tradeVotes[orderId] = 0;
        
        emit OrderCreated(orderId, msg.sender, _amount, _price);
        return orderId;
    }

    // --- 新增 PBFT 核心函数 ---
    
    function addValidator(address _v) external {
        if (!isValidator[_v]) {
            isValidator[_v] = true;
            validatorCount++;
            emit ValidatorAdded(_v);
        }
    }

    /**
     * @dev PBFT 第一阶段：Pre-prepare (由主节点发起提案)
     */
    function prePrepare(uint256 _orderId) external {
        require(isValidator[msg.sender], "Only validator can propose");
        require(pbftStates[_orderId].phase == ConsensusPhase.NONE, "Already proposed");
        
        pbftStates[_orderId].phase = ConsensusPhase.PRE_PREPARE;
        emit PBFTPhaseChanged(_orderId, ConsensusPhase.PRE_PREPARE);
    }

    /**
     * @dev PBFT 第二阶段：Prepare (各节点广播验证结果)
     */
    function prepare(uint256 _orderId) external {
        require(isValidator[msg.sender], "Only validator can prepare");
        PBFTState storage state = pbftStates[_orderId];
        require(state.phase == ConsensusPhase.PRE_PREPARE || state.phase == ConsensusPhase.PREPARE, "Invalid phase");
        require(!state.hasPrepared[msg.sender], "Already prepared");

        state.hasPrepared[msg.sender] = true;
        state.prepareCount++;

        if (state.prepareCount >= MIN_QUORUM) {
            state.phase = ConsensusPhase.PREPARE;
            emit PBFTPhaseChanged(_orderId, ConsensusPhase.PREPARE);
        }
    }

    /**
     * @dev PBFT 第三阶段：Commit (各节点确认达成一致)
     */
    function commit(uint256 _orderId) external {
        require(isValidator[msg.sender], "Only validator can commit");
        PBFTState storage state = pbftStates[_orderId];
        require(state.phase == ConsensusPhase.PREPARE || state.phase == ConsensusPhase.COMMIT, "Invalid phase");
        require(!state.hasCommitted[msg.sender], "Already committed");

        state.hasCommitted[msg.sender] = true;
        state.commitCount++;

        if (state.commitCount >= MIN_QUORUM) {
            state.phase = ConsensusPhase.SUCCESS;
            emit PBFTPhaseChanged(_orderId, ConsensusPhase.SUCCESS);
        }
    }
    // ----------------------

    function buyEnergy(uint256 _orderId, uint256 _amount) external payable nonReentrant orderExists(_orderId) {
        Order storage order = orders[_orderId];
        require(order.status == OrderStatus.OPEN, "Order status is not correct");
        
        // --- 强制 PBFT 共识校验 ---
        require(
            pbftStates[_orderId].phase == ConsensusPhase.SUCCESS,
            "PBFT Consensus not reached: transaction must be in SUCCESS phase"
        );
        // ------------------

        require(_amount > 0, "Purchase amount must be greater than 0");
        require(_amount <= order.amount, "Purchase amount cannot exceed order amount");
        
        uint256 totalPrice = _amount * order.price;

        if (msg.value < totalPrice) {
            uint256 remaining = totalPrice - msg.value;
            require(frozenBalances[msg.sender] >= remaining, "Insufficient frozen balance");
        }

        // Effects: update order state and persist transaction before any external transfer.
        order.amount -= _amount;
        if (order.amount == 0) {
            order.status = OrderStatus.FILLED;
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
        
        if (msg.value >= totalPrice) {
            if (msg.value > totalPrice) {
                safeTransferETH(msg.sender, msg.value - totalPrice);
            }
            safeTransferETH(order.seller, totalPrice);
        } else {
            uint256 remaining = totalPrice - msg.value;
            frozenBalances[msg.sender] -= remaining;
            availableBalances[order.seller] += totalPrice;
        }

        emit EnergyBought(transactionId, _orderId, msg.sender, _amount, totalPrice);
    }

    function cancelOrder(uint256 _orderId) external orderExists(_orderId) {
        Order storage order = orders[_orderId];
        require(order.seller == msg.sender, "Only seller can cancel order");
        require(order.status == OrderStatus.OPEN, "Can only cancel open orders");
        
        order.status = OrderStatus.CANCELLED;
        emit OrderCancelled(_orderId);
    }

    function getOrder(uint256 _orderId) external view orderExists(_orderId) returns (
        uint256 id,
        address seller,
        uint256 amount,
        uint256 price,
        OrderStatus status,
        uint256 createdAt
    ) {
        Order storage order = orders[_orderId];
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

    function getTransaction(uint256 _transactionId) external view transactionExists(_transactionId) returns (
        uint256 id,
        uint256 orderId,
        address buyer,
        address seller,
        uint256 amount,
        uint256 totalPrice,
        uint256 timestamp
    ) {
        Transaction storage transaction = transactions[_transactionId];
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
