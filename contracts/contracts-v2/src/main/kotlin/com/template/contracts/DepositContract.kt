package com.template.contracts

import com.template.states.DepositState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class DepositContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.DepositContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val (value) = tx.getCommand<CommandData>(0)
        val output = tx.outputsOfType<DepositState>().first()
        when (value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when creating a Deposit state.".using(tx.inputStates.isEmpty())
                "The deposit amount should be positive".using(output.amount > 0)
            }
            is Commands.Transfer -> requireThat {
                "Deposit cannot be transferred to self".using(output.owner != output.currentOwner)
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Transfer : Commands
    }
}