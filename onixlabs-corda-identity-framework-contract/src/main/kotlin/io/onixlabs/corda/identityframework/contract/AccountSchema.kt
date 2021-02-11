package io.onixlabs.corda.identityframework.contract

import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object AccountSchema {

    object AccountSchemaV1 : MappedSchema(AccountSchema.javaClass, 1, listOf(AccountEntity::class.java)) {
        override val migrationResource: String = "account-schema.changelog-master"
    }

    @Entity
    @Table(name = "accounts")
    class AccountEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "owner", nullable = false)
        val owner: AbstractParty
    ) : PersistentState()
}
