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
data class DepositState(
    val owner: Party,
    val treasury: Party,
    val amount: Int,
    val currency: String,
    val accountId: String,
    val currentOwner: Party? = null,
    override var participants: List<AbstractParty> = listOf(owner,treasury)
) : ContractState
