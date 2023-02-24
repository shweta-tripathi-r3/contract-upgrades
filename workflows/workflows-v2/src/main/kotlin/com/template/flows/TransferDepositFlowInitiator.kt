package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.DepositContract
import com.template.states.DepositState
import net.corda.core.contracts.StateAndRef
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
class TransferDepositFlowInitiator(
    private val newOwner: Party,
    private val accountId: String
) : FlowLogic<SignedTransaction>() {
    companion object {
        object OBTAINING_NOTARY : ProgressTracker.Step("Getting Notary from Network")
        object OBTAINING_INPUT_STATE : ProgressTracker.Step("Getting Input Deposit State")
        object GENERATING_OUTPUT_STATE : ProgressTracker.Step("Generating output state")
        object GENERATING_TRANSACTION : ProgressTracker.Step("Generating transaction")
        object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
        object GATHERING_SIGS : ProgressTracker.Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            OBTAINING_NOTARY,
            OBTAINING_INPUT_STATE,
            GENERATING_OUTPUT_STATE,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network.
        progressTracker.currentStep = OBTAINING_NOTARY
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

        progressTracker.currentStep = OBTAINING_INPUT_STATE

        //Use vaultQuery to fetch all DepositStates and then use a filter to find the DepositStates corresponding to the accountId.
        //This resulting state will be used as input for our deposit transfer transaction.
        val stateStateAndRef: List<StateAndRef<DepositState>> = serviceHub.vaultService.queryBy(
            DepositState::class.java
        ).states
        val (state) = stateStateAndRef.stream().filter { (state): StateAndRef<DepositState> ->
            val depositState: DepositState = state.data
            depositState.accountId == accountId
        }.findAny().orElseThrow<IllegalArgumentException> {
            IllegalArgumentException(
                "Deposit Not Found"
            )
        }
        val inputState: DepositState = state.data
        //Compose the State that carries the Hello World message
        progressTracker.currentStep = GENERATING_OUTPUT_STATE
        val output = DepositState(
            inputState.amount,
            inputState.owner,
            inputState.treasury,
            inputState.currency,
            accountId,
            newOwner
        )

        // Step 3. Create a new TransactionBuilder object.
        progressTracker.currentStep = GENERATING_TRANSACTION
        val builder = TransactionBuilder(notary)
            .addCommand(
                DepositContract.Commands.Transfer(),
                listOf(inputState.owner.owningKey, inputState.treasury.owningKey, newOwner.owningKey)
            )
            .addOutputState(output)

        output.participants = listOf(inputState.owner, inputState.treasury, newOwner)

        // Step 4. Verify and sign it with our KeyPair.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        builder.verify(serviceHub)

        // Step 5. Sign it with our KeyPair.
        progressTracker.currentStep = SIGNING_TRANSACTION
        val ptx = serviceHub.signInitialTransaction(builder)

        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        progressTracker.currentStep = GATHERING_SIGS
        val otherParties: MutableList<Party> =
            output.participants.stream().map { el: AbstractParty? -> el as Party? }.collect(Collectors.toList())
        otherParties.remove(ourIdentity)
        otherParties.add(newOwner)
        val sessions = otherParties.stream().map { el: Party? -> initiateFlow(el!!) }.collect(Collectors.toList())

        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(stx, sessions))
    }
}