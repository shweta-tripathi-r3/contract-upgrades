package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.DepositContract
import com.template.states.DepositState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.stream.Collectors


// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class Initiator(
    private val bank: Party,
    private val treasury: Party,
    private val amount: Double,
    private val currency: String,
    private val ref: String
) : FlowLogic<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

        //Compose the State that carries the Hello World message
        val output = DepositState(amount,bank,treasury,currency,ref)

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary)
            .addCommand(DepositContract.Commands.Create(), listOf(bank.owningKey, treasury.owningKey))
            .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)


        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> =
            output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow<SignedTransaction>(FinalityFlow(stx, sessions))
    }
}

@InitiatedBy(Initiator::class)
class Responder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                //Addition checks
            }
        }
        val txId = subFlow(signTransactionFlow).id
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

