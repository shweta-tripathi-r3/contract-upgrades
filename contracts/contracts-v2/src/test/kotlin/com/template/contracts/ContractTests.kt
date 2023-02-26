package com.template.contracts

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.template.states.DepositState

class ContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
    var bankA = TestIdentity(CordaX500Name("BankA", "TestLand", "US"))
    var treasury = TestIdentity(CordaX500Name("Treasury", "TestLand", "US"))

    @Test
    fun dummytest() {
        val state = DepositState( bankA.party, treasury.party,100,"USD","ref123")
        ledgerServices.ledger {
            // Should fail bid price is equal to previous highest bid
            transaction {
                //failing transaction
                input(DepositContract.ID, state)
                output(DepositContract.ID, state)
                command(bankA.publicKey, DepositContract.Commands.Create())
                fails()
            }
            //pass
            transaction {
                //passing transaction
                output(DepositContract.ID, state)
                command(bankA.publicKey, DepositContract.Commands.Create())
                verifies()
            }
        }
    }
}
