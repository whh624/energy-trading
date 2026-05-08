const EnergyTradingPlatform = artifacts.require("EnergyTradingPlatform");

module.exports = function(deployer) {
  deployer.deploy(EnergyTradingPlatform);
};
