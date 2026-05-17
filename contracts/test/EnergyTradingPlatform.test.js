const EnergyTradingPlatform = artifacts.require("EnergyTradingPlatform");
const SellerReceiverMock = artifacts.require("SellerReceiverMock");

contract("EnergyTradingPlatform", (accounts) => {
    const buyer = accounts[1];
    const seller = accounts[2];

    let platform;

    beforeEach(async () => {
        platform = await EnergyTradingPlatform.new();
    });

    async function expectRevert(promise, expectedReason) {
        try {
            await promise;
            assert.fail("Expected revert not received");
        } catch (error) {
            assert(
                error.message.includes("revert") && error.message.includes(expectedReason),
                `Expected revert containing "${expectedReason}", got "${error.message}"`
            );
        }
    }

    it("rejects reads for non-existent orders and transactions", async () => {
        await expectRevert(platform.getOrder(0), "Order does not exist");
        await expectRevert(platform.getTransaction(0), "Transaction does not exist");
    });

    it("completes a purchase and pays contract sellers through call", async () => {
        const sellerContract = await SellerReceiverMock.new();
        await sellerContract.createOrderOnPlatform(platform.address, 5, 2);

        await platform.buyEnergy(0, 5, {
            from: buyer,
            value: 10
        });

        const order = await platform.getOrder(0);
        const transaction = await platform.getTransaction(0);
        const receiveCount = await sellerContract.receiveCount();
        const buyerTransactions = await platform.getUserTransactions(buyer);

        assert.equal(order.amount.toString(), "0", "Order amount should be fully consumed");
        assert.equal(order.status.toString(), "1", "Order should be FILLED");
        assert.equal(transaction.totalPrice.toString(), "10", "Transaction should persist total price");
        assert.equal(transaction.seller, sellerContract.address, "Transaction should record seller contract");
        assert.equal(receiveCount.toString(), "1", "Seller contract should receive ETH exactly once");
        assert.equal(buyerTransactions.length, 1, "Buyer should have one transaction record");
    });

    it("supports purchases funded by frozen contract balance", async () => {
        await platform.deposit({
            from: buyer,
            value: 20
        });
        await platform.freezeBalance(12, { from: buyer });
        await platform.createOrder(3, 4, { from: seller });

        await platform.buyEnergy(0, 3, {
            from: buyer,
            value: 0
        });

        const buyerBalance = await platform.getBalance(buyer);
        const sellerBalance = await platform.getBalance(seller);
        const transaction = await platform.getTransaction(0);

        assert.equal(buyerBalance.available.toString(), "8", "Buyer available balance should decrease after freezing");
        assert.equal(buyerBalance.frozen.toString(), "0", "Buyer frozen balance should be fully consumed");
        assert.equal(sellerBalance.available.toString(), "12", "Seller should receive internal balance credit");
        assert.equal(transaction.totalPrice.toString(), "12", "Transaction should record frozen-balance payment");
    });
});
