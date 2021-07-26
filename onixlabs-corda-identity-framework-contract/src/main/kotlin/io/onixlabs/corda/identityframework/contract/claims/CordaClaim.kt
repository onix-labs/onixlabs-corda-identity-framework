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

package io.onixlabs.corda.identityframework.contract.claims

import io.onixlabs.corda.core.contract.ChainState
import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.identityframework.contract.accountLinearId
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimSchema.CordaClaimEntity
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimSchema.CordaClaimSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

/**
 * Represents a claim that is implemented as a Corda state.
 *
 * @param T The underlying claim value type.
 * @property issuer The issuer of the claim.
 * @property holder The holder of the claim.
 * @property property The property of the claim.
 * @property value The value of the claim.
 * @property linearId The unique identifier of the claim.
 * @property previousStateRef The state reference of the previous state in the chain.
 * @property isSelfIssued Determines whether the claim has been self-issued.
 * @property hash The unique hash which represents this claim.
 * @property participants The participants of this claim; namely the issuer and the holder.
 */
@BelongsToContract(CordaClaimContract::class)
open class CordaClaim<T : Any>(
    val issuer: AbstractParty,
    val holder: AbstractParty,
    override val property: String,
    override val value: T,
    override val linearId: UniqueIdentifier,
    override val previousStateRef: StateRef?
) : AbstractClaim<T>(), LinearState, QueryableState, ChainState, Hashable {

    constructor(
        issuer: AbstractParty,
        holder: AbstractParty,
        property: String,
        value: T,
        linearId: UniqueIdentifier = UniqueIdentifier()
    ) : this(issuer, holder, property, value, linearId, null)

    constructor(
        issuer: AbstractParty,
        property: String,
        value: T,
        linearId: UniqueIdentifier = UniqueIdentifier()
    ) : this(issuer, issuer, property, value, linearId, null)

    val isSelfIssued: Boolean
        get() = issuer == holder

    final override val hash: SecureHash
        get() = SecureHash.sha256("$issuer$holder$property$value${value.javaClass}$previousStateRef")

    override val participants: List<AbstractParty>
        get() = setOf(issuer, holder).toList()

    /**
     * Amends the claim value.
     *
     * @property previousStateRef The state reference of the previous state in the chain.
     * @param value The amended claim value.
     * @return Returns an amended claim.
     */
    open fun amend(previousStateRef: StateRef, value: T): CordaClaim<T> {
        return CordaClaim(issuer, holder, property, value, linearId, previousStateRef)
    }

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is CordaClaimSchemaV1 -> CordaClaimEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            issuer = issuer,
            issuerAccountLinearId = issuer.accountLinearId?.id,
            issuerAccountExternalId = issuer.accountLinearId?.externalId,
            holder = holder,
            holderAccountLinearId = holder.accountLinearId?.id,
            holderAccountExternalId = holder.accountLinearId?.externalId,
            property = property,
            value = value.toString(),
            valueType = value.javaClass.canonicalName,
            previousStateRef = previousStateRef?.toString(),
            isSelfIssued = isSelfIssued,
            hash = hash.toString(),
            claimType = javaClass.canonicalName
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(CordaClaimSchemaV1)
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is CordaClaim<*>
                && other.javaClass == javaClass
                && other.issuer == issuer
                && other.holder == holder
                && other.property == property
                && other.value == value
                && other.previousStateRef == previousStateRef
                && other.linearId == linearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(issuer, holder, property, value, linearId, previousStateRef)
    }

    /**
     * Ensures that the properties considered immutable across state transitions do not change.
     *
     * @param other The Corda claim to compare with the current Corda claim.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    internal fun internalImmutableEquals(other: CordaClaim<*>): Boolean {
        return this === other || (other.value.javaClass == value.javaClass
                && other.issuer == issuer
                && other.holder == holder
                && other.property == property
                && other.linearId == linearId
                && immutableEquals(other))
    }

    /**
     * Ensures that the properties considered immutable across state transitions do not change.
     *
     * @param other The Corda claim to compare with the current Corda claim.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    protected open fun immutableEquals(other: CordaClaim<*>): Boolean = true
}
