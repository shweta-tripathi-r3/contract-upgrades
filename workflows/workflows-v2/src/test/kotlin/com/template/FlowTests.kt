package com.template

import com.template.flows.CreateDepositFlowInitiator
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.template.states.DepositState
import java.util.concurrent.Future;
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault.StateStatus


class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))))
        a = network.createPartyNode()
        b = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun `DummyTest`() {
        val flow = CreateDepositFlowInitiator(b.info.legalIdentities[0],a.info.legalIdentities[0],100,"USD", "ref123")
        val future: Future<SignedTransaction> = a.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.
         val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
         val states = b.services.vaultService.queryBy(DepositState::class.java, inputCriteria).states[0].state.data
    }
}