package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.DepositContract
import com.template.states.DepositState
import net.corda.core.flows.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.util.stream.Collectors


@InitiatingFlow
@StartableByRPC
class CreateDepositFlowInitiator(
    private val bank: Party,
    private val treasury: Party,
    private val amount: Double,
    private val currency: String,
    private val ref: String
) : FlowLogic<SignedTransaction>() {

    companion object {
        object OBTAINING_NOTARY : ProgressTracker.Step("Getting Notary from Network")
        object GENERATING_OUTPUT_STATE : ProgressTracker.Step("Generating output state")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
//        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
//            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
//        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            OBTAINING_NOTARY,
            GENERATING_OUTPUT_STATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network and our key pair.
        progressTracker.currentStep = OBTAINING_NOTARY
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

        //Compose the State that carries the Hello World message
        progressTracker.currentStep = GENERATING_OUTPUT_STATE
        val output = DepositState(amount, bank, treasury, currency, ref)

        // Step 3. Create a new TransactionBuilder object.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)
            .addCommand(DepositContract.Commands.Create(), listOf(bank.owningKey,treasury.owningKey))
            .addOutputState(output)

        // Step 4. Verify and sign it with our KeyPair.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

//        //Initiate Flow with owner
//        val ownerSession = initiateFlow(bank)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val otherParties: MutableList<Party> =
            output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 6. Assuming no exceptions, we can now finalise the transaction
        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow<SignedTransaction>(FinalityFlow(stx, sessions))
    }
}