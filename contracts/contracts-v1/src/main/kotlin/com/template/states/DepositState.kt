package com.template.states


import com.template.contracts.DepositContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

// *********
// * State *
// *********
@BelongsToContract(DepositContract::class)
data class DepositState(val amount: Double,
                        val bank: Party,
                        val treasury: Party,
                        val currency: String,
                        val accountId: String,
                        override val participants: List<AbstractParty> = listOf(bank,treasury)
) : ContractState
