# CorDapp Upgrades

This sample shows how to upgrade CorDapps using implicit approach.
Signature Constraint (Implicit-Upgrades) introduced in Corda 4 is however the recommended approach to perform upgrades in Corda, since it doesn't
require the heavyweight process of creating upgrade transactions for every state on the ledger of all parties.

## Corda Network
`BankA`
`BankB`
`Treasury`
`Notary`

## Contract and Flow Version
**Version 1 - Contract and Flow**
`Create Deposit` between `BankA` and `Treasury`

**Version 2 - Contract and Flow**
`Transfer Deposit` from `BankA` to `BankB`


# Demo Scenarios
## Initial Scenario
*Step1*
Build `./gradlew build` & Deploy version 1 of contracts and flows by running `./gradlew deployNodes`.
Now run the nodes using `./build/nodes/runnodes`.
This would create a network of 3 nodes and a notary all running version 1 of contracts and flows.

*Step2*
Create Deposit by `BankA` with `Treasury`

`start CreateDepositFlowInitiator bank: BankA, treasury: Treasury, amount: 100, currency: USD, ref: ref123`

*Step3*
Check created states
`run vaultQuery contractStateType: com.template.states.DepositState`

## Upgrade Scenario
*Step1*
Drain nodes - `run setFlowsDrainingModeEnabled enabled: true` and do graceful shutdown `run gracefulShutdown`.
Upgrade flows and contracts to version 2 for all nodes.
Upgrade can be done by using the below script, which would copy workflows-v2.jar to cordapps directory of the 3 nodes.

`cd script
./upgrade.sh --node=Treasury,BankA,BankB --workflow=2 --contract=2`

*Step2*
Go to the `build/nodes/<Node>` folder and open a command prompt
run `java -jar corda.jar run-migration-scripts --core-schemas --app-schemas`
Do this for all nodes.

*Step3*
Restart the nodes. `./build/nodes/runnodes`
Stop nodes from draining - `run setFlowsDrainingModeEnabled enabled: false`

*Step4*
Transfer the previously created Deposit State from `BankA` to `BankB`

`start TransferDepositFlowInitiator newOwner: BankB, accountId: ref123`

*Step5*
Check Vault of `BankA`, `BankB` and `Treasury`
`run vaultQuery contractStateType: com.template.states.DepositState`

For `BankA` and `Treasury` the previously created Deposit State will have the new field as null 