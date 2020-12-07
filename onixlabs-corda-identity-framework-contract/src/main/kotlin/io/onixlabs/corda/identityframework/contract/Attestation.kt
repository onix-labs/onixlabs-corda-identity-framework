/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.identityframework.contract.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.contract.AttestationSchema.AttestationSchemaV1
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

/**
 * Represents an attestation; a proof that a particular state has been witnessed.
 *
 * @param T The underlying [ContractState] type being attested.
 * @property attestor The party who is attesting to the witnessed state.
 * @property attestees The parties of this attestation, usually the participants of the attested state.
 * @property pointer The pointer to the attested state.
 * @property status The status of the attestation.
 * @property metadata Additional information about the attestation.
 * @property linearId The unique identifier of the attestation.
 * @property previousStateRef The state reference of the previous state in the chain.
 * @property hash The unique hash which represents this attestation.
 * @property participants The participants of this attestation; namely the attestor and attestees.
 */
@BelongsToContract(AttestationContract::class)
open class Attestation<T : ContractState>(
    val attestor: AbstractParty,
    val attestees: Set<AbstractParty>,
    val pointer: AttestationPointer<T>,
    val status: AttestationStatus,
    val metadata: Map<String, String>,
    override val linearId: UniqueIdentifier,
    override val previousStateRef: StateRef?
) : LinearState, QueryableState, ChainState, Hashable {

    init {
        check(metadata.size <= 10) { "The number of metadata entries cannot exceed 10." }
        check(metadata.keys.all { it.length <= 256 }) { "The maximum key length for a metadata entry is 256." }
        check(metadata.values.all { it.length <= 1024 }) { "The maximum value length for a metadata entry is 1024." }
    }

    override val hash: SecureHash
        get() = SecureHash.sha256("$attestor$pointer$previousStateRef")

    override val participants: List<AbstractParty>
        get() = (attestees + attestor).toList()

    /**
     * Amends this attestation.
     *
     * @property previousStateRef The state reference of the previous state in the chain.
     * @param status The amended attestation status.
     * @param pointer The pointer to the attested state, if a new version of the state is being attested.
     * @param metadata Additional information about the attestation.
     * @return Returns a new, amended version of this attestation state.
     */
    open fun amend(
        previousStateRef: StateRef,
        status: AttestationStatus,
        pointer: AttestationPointer<T> = this.pointer,
        metadata: Map<String, String> = emptyMap()
    ): Attestation<T> {
        return Attestation(attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
    }

    /**
     * Generates a persistent state entity from this contract state.
     *
     * @param schema The mapped schema from which to generate a persistent state entity.
     * @return Returns a persistent state entity.
     */
    override fun generateMappedObject(schema: MappedSchema): PersistentState = when (schema) {
        is AttestationSchemaV1 -> AttestationEntity(
            linearId = linearId.id,
            externalId = linearId.externalId,
            attestor = attestor,
            pointerStateRef = pointer.stateRef.toString(),
            pointerStateClass = pointer.stateClass.canonicalName,
            pointerStateLinearId = pointer.stateLinearId?.id,
            pointerHash = pointer.hash.toString(),
            status = status,
            previousStateRef = previousStateRef?.toString(),
            hash = hash.toString()
        )
        else -> throw IllegalArgumentException("Unrecognised schema: $schema.")
    }

    /**
     * Gets the supported schemas of this state.
     *
     * @return Returns the supported schemas of this state.
     */
    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(AttestationSchemaV1)
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Attestation<*>
                && other.attestor == attestor
                && other.attestees == attestees
                && other.pointer == pointer
                && other.status == status
                && other.metadata == metadata
                && other.previousStateRef == previousStateRef
                && other.linearId == linearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
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
     * Ensures that the immutable properties of this attestation have not changed.
     *
     * @param other The attestation to compare with the current attestation.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    internal fun internalImmutableEquals(other: Attestation<*>): Boolean {
        return attestor == other.attestor
                && linearId == other.linearId
                && pointer.immutableEquals(other.pointer)
                && immutableEquals(other)
    }

    /**
     * Ensures immutable property checks in derived classes.
     *
     * @param other The attestation to compare with the current attestation.
     * @return Returns true if the immutable properties have not changed; otherwise, false.
     */
    protected open fun immutableEquals(other: Attestation<*>): Boolean = true
}
