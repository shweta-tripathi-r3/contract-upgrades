package com.template.states


import com.template.contracts.DepositContract
import com.template.schema.DepositSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

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
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is DepositSchemaV1 ->
                DepositSchemaV1.PersistentDeposit(
                    owner = owner.name.toString(),
                    treasury = treasury.name.toString(),
                    amount = amount,
                    currency = currency,
                    accountId = accountId,
                    currentOwner = if (currentOwner != null) currentOwner!!.name.toString() else ""
                )

            else -> error("Unsupported schema ${schema.name}")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(DepositSchemaV1())

}