// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

interface IEnergyTradingPlatform {
    function createOrder(uint256 _amount, uint256 _price) external returns (uint256);
}

contract SellerReceiverMock {
    uint256 public receiveCount;

    function createOrderOnPlatform(address platform, uint256 amount, uint256 price) external returns (uint256) {
        return IEnergyTradingPlatform(platform).createOrder(amount, price);
    }

    receive() external payable {
        receiveCount += 1;
    }
}
