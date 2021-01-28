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

package io.onixlabs.corda.identityframework.v1.contract

import io.onixlabs.corda.core.contract.Hashable
import io.onixlabs.corda.core.contract.Resolvable
import io.onixlabs.corda.core.contract.TransactionResolution
import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction
import java.util.*

/**
 * Represents a pointer to a witnessed state.
 *
 * @param T The underlying [ContractState] type.
 * @property stateRef The [StateRef] of the witnessed state being attested.
 * @property stateClass The [Class] of the witnessed state being attested.
 * @property stateLinearId The [UniqueIdentifier] of the witnessed state being attested, only if it's a linear state.
 */
@CordaSerializable
class AttestationPointer<T : ContractState>(
    val stateRef: StateRef,
    val stateClass: Class<T>,
    val stateLinearId: UniqueIdentifier? = null
) : Resolvable<T>, Hashable {

    constructor(stateAndRef: StateAndRef<T>) : this(
        stateAndRef.ref,
        stateAndRef.state.data.javaClass,
        if (stateAndRef.state.data is LinearState) (stateAndRef.state.data as LinearState).linearId else null
    )

    override val hash: SecureHash
        get() = SecureHash.sha256("$stateRef$stateClass$stateLinearId")

    private val criteria: VaultQueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(stateClass),
        status = Vault.StateStatus.ALL,
        relevancyStatus = Vault.RelevancyStatus.ALL,
        stateRefs = listOf(stateRef)
    )

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is AttestationPointer<*>
                && other.stateRef == stateRef
                && other.stateClass == stateClass
                && other.stateLinearId == stateLinearId)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(stateRef, stateClass)
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
        return getOrThrow(cordaRPCOps.vaultQueryByCriteria(criteria, stateClass).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T>? {
        return getOrThrow(serviceHub.vaultService.queryBy(stateClass, criteria).states.singleOrNull())
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
            TransactionResolution.INPUT -> transaction.inRefsOfType(stateClass)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(stateClass)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(stateClass)
        }

        return getOrThrow(states.singleOrNull { isPointingTo(it) })
    }

    /**
     * Determines whether this [Resolvable] is pointing to the specified [StateAndRef] instance.
     *
     * @param stateAndRef The [StateAndRef] to determine being pointed to.
     * @return Returns true if this [Resolvable] is pointing to the specified [StateAndRef]; otherwise, false.
     */
    override fun isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
        return stateAndRef.ref == stateRef
    }

    /**
     * Determines whether any immutable properties of this object have changed.
     *
     * @param pointer The pointer to compare to this one.
     * @return Returns true if the immutable properties remain unchanged; otherwise, false.
     */
    internal fun immutableEquals(pointer: AttestationPointer<*>): Boolean {
        return pointer.stateClass == stateClass && pointer.stateLinearId == stateLinearId
    }

    /**
     * Checks the claim and value class types of the specified state to ensure they match the expected types.
     *
     * @param stateAndRef The [StateAndRef] to check.
     * @return Returns the [StateAndRef] if the claim and value class types match.
     * @throws IllegalStateException if the claim or value class types do not match the expected types.
     */
    private fun getOrThrow(stateAndRef: StateAndRef<T>?): StateAndRef<T>? {
        return stateAndRef?.let {
            check(stateClass == stateAndRef.state.data.javaClass) {
                "Invalid state class. Expected '$stateClass' but got '${stateAndRef.state.data.javaClass}'."
            }

            stateAndRef
        }
    }
}
