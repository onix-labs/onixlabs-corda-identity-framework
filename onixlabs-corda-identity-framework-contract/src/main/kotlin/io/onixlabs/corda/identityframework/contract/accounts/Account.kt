/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.contract.accounts

import io.onixlabs.corda.identityframework.contract.accounts.AccountSchema.AccountEntity
import io.onixlabs.corda.identityframework.contract.accounts.AccountSchema.AccountSchemaV1
import io.onixlabs.corda.identityframework.contract.toDataClassString
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

/**
 * Represents an account, which allows contract states to be partitioned within a single node.
 *
 * @property owner The owner of the account.
 * @property linearId The unique identifier of the account.
 * @property participants The participants of this account; namely the owner.
 */
@BelongsToContract(AccountContract::class)
open class Account(
    val owner: AbstractParty,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override val participants: List<AbstractParty>
        get() = listOf(owner)

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
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Account
                && other.javaClass == javaClass
                && other.owner == owner
                && other.linearId == linearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(owner, linearId, javaClass)
    }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String {
        return toDataClassString()
    }

    /**
     * Creates an account party from this account.
     * @return Returns an account party that resolves this account.
     */
    fun toAccountParty(): AccountParty {
        return AccountParty(owner, linearId, javaClass)
    }
}
