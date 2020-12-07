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

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria
import net.corda.core.transactions.LedgerTransaction
import java.util.*

/**
 * Represents the base class for claim pointer implementations.
 * Claim pointers are themselves claims that point to other [CordaClaim] states.
 *
 * @param T The underlying [CordaClaim] state type.
 * @property property The property of the claim being pointed to.
 * @property value The value of the claim pointer, which is the identifier used to resolve a [CordaClaim] state.
 * @property issuer The issuer of the [CordaClaim] state.
 * @property holder The holder of the [CordaClaim] state.
 * @property claimClass The class of the [CordaClaim] being pointed to.
 * @property valueClass The class of the value of the [CordaClaim] being pointed to.
 */
sealed class ClaimPointer<T : CordaClaim<*>> : AbstractClaim<Any>(), Resolvable<T> {
    abstract val issuer: AbstractParty
    abstract val holder: AbstractParty
    protected abstract val claimClass: Class<T>
    protected abstract val valueClass: Class<*>

    /**
     * Checks the claim and value class types of the specified state to ensure they match the expected types.
     *
     * @param stateAndRef The [StateAndRef] to check.
     * @return Returns the [StateAndRef] if the claim and value class types match.
     * @throws IllegalStateException if the claim or value class types do not match the expected types.
     */
    protected fun getOrThrow(stateAndRef: StateAndRef<T>?): StateAndRef<T>? {
        return stateAndRef?.let {
            check(claimClass == stateAndRef.state.data.javaClass) {
                "Invalid claim class. Expected '$claimClass' but got '${stateAndRef.state.data.javaClass}'."
            }

            check(valueClass == stateAndRef.state.data.value.javaClass) {
                "Invalid claim value class. Expected '$valueClass' but got '${stateAndRef.state.data.value.javaClass}'."
            }

            stateAndRef
        }
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ClaimPointer<*>
                && other.javaClass == javaClass
                && other.claimClass == claimClass
                && other.valueClass == valueClass
                && other.property == property
                && other.value == value
                && other.issuer == issuer
                && other.holder == holder)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(issuer, holder, property, value, claimClass, valueClass)
    }
}

/**
 * Represents a claim pointer that points to a [CordaClaim] by its linear ID.
 * A linear claim pointer always points to the latest witnessed version of the specified [CordaClaim].
 *
 * @param T The underlying [CordaClaim] state type.
 * @property issuer The issuer of the [CordaClaim] state.
 * @property holder The holder of the [CordaClaim] state.
 * @property property The property of the claim being pointed to.
 * @property value The value of the claim pointer, which is the identifier used to resolve a [CordaClaim] state.
 * @property claimClass The class of the [CordaClaim] being pointed to.
 * @property valueClass The class of the value of the [CordaClaim] being pointed to.
 */
class LinearClaimPointer<T : CordaClaim<*>> private constructor(
    override val issuer: AbstractParty,
    override val holder: AbstractParty,
    override val property: String,
    override val value: UniqueIdentifier,
    override val claimClass: Class<T>,
    override val valueClass: Class<*>
) : ClaimPointer<T>() {

    /**
     * Creates a new instance of a [LinearClaimPointer].
     *
     * @param claim The [CordaClaim] from which to create a [LinearClaimPointer] instance.
     */
    constructor(claim: T) : this(
        issuer = claim.issuer,
        holder = claim.holder,
        property = claim.property,
        value = claim.linearId,
        claimClass = claim.javaClass,
        valueClass = claim.value.javaClass
    )

    private val criteria = LinearStateQueryCriteria(
        contractStateTypes = setOf(claimClass),
        status = Vault.StateStatus.UNCONSUMED,
        relevancyStatus = Vault.RelevancyStatus.ALL,
        linearId = listOf(value)
    )

    /**
     * Resolves a [ContractState] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T>? {
        return getOrThrow(cordaRPCOps.vaultQueryByCriteria(criteria, claimClass).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T>? {
        return getOrThrow(serviceHub.vaultService.queryBy(claimClass, criteria).states.singleOrNull())
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
            TransactionResolution.INPUT -> transaction.inRefsOfType(claimClass)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(claimClass)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(claimClass)
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
        return stateAndRef.state.data.linearId == value
    }
}

/**
 * Represents a claim pointer that points to a [CordaClaim] by its state reference.
 * A static claim pointer always points to a specific witnessed version of the specified [CordaClaim].
 *
 * @param T The underlying [CordaClaim] state type.
 * @property issuer The issuer of the [CordaClaim] state.
 * @property holder The holder of the [CordaClaim] state.
 * @property property The property of the claim being pointed to.
 * @property value The value of the claim pointer, which is the identifier used to resolve a [CordaClaim] state.
 * @property claimClass The class of the [CordaClaim] being pointed to.
 * @property valueClass The class of the value of the [CordaClaim] being pointed to.
 */
class StaticClaimPointer<T : CordaClaim<*>> private constructor(
    override val issuer: AbstractParty,
    override val holder: AbstractParty,
    override val property: String,
    override val value: StateRef,
    override val claimClass: Class<T>,
    override val valueClass: Class<*>
) : ClaimPointer<T>() {

    /**
     * Creates a new instance of a [StaticClaimPointer].
     *
     * @param claim The [CordaClaim] from which to create a [StaticClaimPointer] instance.
     */
    constructor(claim: StateAndRef<T>) : this(
        issuer = claim.state.data.issuer,
        holder = claim.state.data.holder,
        property = claim.state.data.property,
        value = claim.ref,
        claimClass = claim.state.data.javaClass,
        valueClass = claim.state.data.value.javaClass
    )

    private val criteria = VaultQueryCriteria(
        contractStateTypes = setOf(claimClass),
        status = Vault.StateStatus.ALL,
        relevancyStatus = Vault.RelevancyStatus.ALL,
        stateRefs = listOf(value)
    )

    /**
     * Resolves a [ContractState] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<T>? {
        return getOrThrow(cordaRPCOps.vaultQueryByCriteria(criteria, claimClass).states.singleOrNull())
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<T>? {
        return getOrThrow(serviceHub.vaultService.queryBy(claimClass, criteria).states.singleOrNull())
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
            TransactionResolution.INPUT -> transaction.inRefsOfType(claimClass)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(claimClass)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(claimClass)
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
        return stateAndRef.ref == value
    }
}
