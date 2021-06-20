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

package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.core.contract.SingularResolvable
import io.onixlabs.corda.core.contract.TransactionResolution
import io.onixlabs.corda.core.services.vaultQuery
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.util.*

/**
 * Represents the base class for attestation pointer implementations.
 *
 * @param T The underlying [ContractState] type.
 * @property stateRef The [StateRef] of the witnessed state being attested.
 * @property stateType The [Class] of the witnessed state being attested.
 * @property hash The unique hash of the attestation pointer.
 */
@CordaSerializable
sealed class AttestationPointer<T : ContractState> : SingularResolvable<T>, Hashable {
    abstract val stateType: Class<T>
    abstract val stateRef: StateRef

    /**
     * Determines whether any immutable properties of this object have changed.
     *
     * @param other The pointer to compare to this one.
     * @return Returns true if the immutable properties remain unchanged; otherwise, false.
     */
    internal abstract fun immutableEquals(other: AttestationPointer<T>): Boolean

    /**
     * Determines whether this [SingularResolvable] is pointing to the specified [StateAndRef] instance.
     *
     * @param stateAndRef The [StateAndRef] to determine being pointed to.
     * @return Returns true if this [SingularResolvable] is pointing to the specified [StateAndRef]; otherwise, false.
     */
    override fun isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
        return stateAndRef.ref == stateRef
    }

    /**
     * Checks the claim and value class types of the specified state to ensure they match the expected types.
     *
     * @param stateAndRef The [StateAndRef] to check.
     * @return Returns the [StateAndRef] if the claim and value class types match.
     * @throws IllegalStateException if the claim or value class types do not match the expected types.
     */
    protected fun getOrThrow(stateAndRef: StateAndRef<T>?): StateAndRef<T>? {
        return stateAndRef?.apply {
            check(stateType == stateAndRef.state.data.javaClass) {
                "Invalid state class. Expected '$stateType' but got '${stateAndRef.state.data.javaClass}'."
            }
        }
    }

    /**
     * Gets the state linearId if this [AttestationPointer] is a [LinearAttestationPointer]; otherwise, null.
     * @return Returns the state linearId if this [AttestationPointer] is a [LinearAttestationPointer]; otherwise, null.
     */
    internal fun getLinearId(): UniqueIdentifier? {
        return if (this is LinearAttestationPointer<*>) stateLinearId else null
    }
}

/**
 * Represents a linear attestation pointer to a [LinearState].
 *
 * @param T The underlying [LinearState] type.
 *
 * @property stateType The [Class] of the witnessed state being attested.
 * @property stateRef The [StateRef] of the witnessed state being attested.
 * @property stateLinearId The [UniqueIdentifier] of the witnessed state being attested.
 * @property hash The unique hash of the attestation pointer.
 */
class LinearAttestationPointer<T : LinearState> internal constructor(
    override val stateType: Class<T>,
    override val stateRef: StateRef,
    val stateLinearId: UniqueIdentifier
) : AttestationPointer<T>() {

    constructor(stateAndRef: StateAndRef<T>) : this(
        stateType = stateAndRef.state.data.javaClass,
        stateRef = stateAndRef.ref,
        stateLinearId = stateAndRef.state.data.linearId
    )

    private val criteria: QueryCriteria = vaultQuery(stateType) {
        stateStatus(Vault.StateStatus.ALL)
        relevancyStatus(Vault.RelevancyStatus.ALL)
        stateRefs(stateRef)
        linearIds(stateLinearId)
    }

    override val hash: SecureHash
        get() = SecureHash.sha256("$stateType$stateRef$stateLinearId")

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is LinearAttestationPointer<*>
                && other.stateType == stateType
                && other.stateRef == stateRef
                && other.stateLinearId == stateLinearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(stateType, stateRef, stateLinearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun toString(): String {
        return toDataClassString()
    }

    /**
     * Resolves a [ContractState] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T>? {
        return getOrThrow(cordaRPCOps.vaultQueryByCriteria(criteria, stateType).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T>? {
        return getOrThrow(serviceHub.vaultService.queryBy(stateType, criteria).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [LedgerTransaction] instance.
     *
     * @param transaction The [LedgerTransaction] instance to use to resolve the state.
     * @param resolution The transaction resolution method to use to resolve the [ContractState] instance.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(transaction: LedgerTransaction, resolution: TransactionResolution): StateAndRef<T>? {
        val states: List<StateAndRef<T>> = when (resolution) {
            TransactionResolution.INPUT -> transaction.inRefsOfType(stateType)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(stateType)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(stateType)
        }

        return getOrThrow(states.singleOrNull { isPointingTo(it) })
    }

    /**
     * Determines whether any immutable properties of this object have changed.
     *
     * @param other The pointer to compare to this one.
     * @return Returns true if the immutable properties remain unchanged; otherwise, false.
     */
    override fun immutableEquals(other: AttestationPointer<T>): Boolean {
        return other is LinearAttestationPointer<*>
                && other.stateType == stateType
                && other.stateLinearId == stateLinearId
    }
}

/**
 * Represents a static attestation pointer to a [ContractState].
 *
 * @param T The underlying [LinearState] type.
 *
 * @property stateType The [Class] of the witnessed state being attested.
 * @property stateRef The [StateRef] of the witnessed state being attested.
 * @property hash The unique hash of the attestation pointer.
 */
class StaticAttestationPointer<T : ContractState> internal constructor(
    override val stateType: Class<T>,
    override val stateRef: StateRef
) : AttestationPointer<T>() {

    constructor(stateAndRef: StateAndRef<T>) : this(
        stateType = stateAndRef.state.data.javaClass,
        stateRef = stateAndRef.ref
    )

    private val criteria: QueryCriteria = vaultQuery(stateType) {
        stateStatus(Vault.StateStatus.ALL)
        relevancyStatus(Vault.RelevancyStatus.ALL)
        stateRefs(stateRef)
    }

    override val hash: SecureHash
        get() = SecureHash.sha256("$stateType$stateRef")

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is StaticAttestationPointer<*>
                && other.stateType == stateType
                && other.stateRef == stateRef)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(stateType, stateRef)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun toString(): String {
        return toDataClassString()
    }

    /**
     * Resolves a [ContractState] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T>? {
        return getOrThrow(cordaRPCOps.vaultQueryByCriteria(criteria, stateType).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T>? {
        return getOrThrow(serviceHub.vaultService.queryBy(stateType, criteria).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [LedgerTransaction] instance.
     *
     * @param transaction The [LedgerTransaction] instance to use to resolve the state.
     * @param resolution The transaction resolution method to use to resolve the [ContractState] instance.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(transaction: LedgerTransaction, resolution: TransactionResolution): StateAndRef<T>? {
        val states: List<StateAndRef<T>> = when (resolution) {
            TransactionResolution.INPUT -> transaction.inRefsOfType(stateType)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(stateType)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(stateType)
        }

        return getOrThrow(states.singleOrNull { isPointingTo(it) })
    }

    /**
     * Determines whether any immutable properties of this object have changed.
     *
     * @param other The pointer to compare to this one.
     * @return Returns true if the immutable properties remain unchanged; otherwise, false.
     */
    override fun immutableEquals(other: AttestationPointer<T>): Boolean {
        return other is StaticAttestationPointer<*>
                && other.stateType == stateType
    }
}
