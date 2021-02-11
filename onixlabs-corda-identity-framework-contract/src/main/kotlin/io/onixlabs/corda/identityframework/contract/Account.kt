package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.identityframework.contract.AccountSchema.AccountEntity
import io.onixlabs.corda.identityframework.contract.AccountSchema.AccountSchemaV1
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * Represents an account.
 *
 * @property owner The owner of the account.
 * @property linearId The unique identifier of the account.
 * @property participants The participants of this account; namely the owner.
 */
@BelongsToContract(AccountContract::class)
open class Account(
    override val owner: AbstractParty,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, OwnableState, QueryableState {

    override val participants: List<AbstractParty>
        get() = listOf(owner)

    /**
     * Copies the account, replacing the owner field with this new value and leaving the rest alone.
     *
     * @param newOwner The new owner of this state.
     * @return Returns the account, replacing the owner field with this new value and leaving the rest alone.
     */
    final override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        throw IllegalStateException("Cannot change ownership of accounts.")
    }

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is AccountSchemaV1 -> AccountEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            owner = owner
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(AccountSchemaV1)
    }

    /**
     * Creates an account party from this account.
     * @return Returns an account party that resolves this account.
     */
    fun toAccountParty(): AccountParty {
        return AccountParty(owner.owningKey, owner.nameOrNull(), javaClass, linearId)
    }
}
