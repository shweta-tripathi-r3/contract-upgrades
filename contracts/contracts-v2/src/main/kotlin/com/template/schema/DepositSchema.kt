package com.template.schema

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState

object DepositSchema

class DepositSchemaV1 : MappedSchema(
    schemaFamily = DepositSchema.javaClass,
    version = 1,
    mappedTypes = listOf(
        PersistentDeposit::class.java
    )
) {
    override val migrationResource: String?
        get() = "deposit-changelog-master";

    @Entity
    @Table(name = "DEPOSITS")
    class PersistentDeposit(
        @Column(name = "owner")
        val owner: String,

        @Column(name = "treasury")
        val treasury: String,

        @Column(name = "amount")
        val amount: Int,

        @Column(name = "currency")
        val currency: String,

        @Column(name = "account_id")
        val accountId: String,

        @Column(name = "current_owner")
        val currentOwner: String

    ) : PersistentState(){
        constructor() : this("","",0,"","","")
    }
}