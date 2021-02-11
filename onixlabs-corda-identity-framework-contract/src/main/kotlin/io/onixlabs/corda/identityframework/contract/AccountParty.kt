package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.core.contract.Resolvable
import io.onixlabs.corda.core.contract.TransactionResolution
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.PartyAndReference
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria.LinearStateQueryCriteria
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.utilities.OpaqueBytes
import java.security.PublicKey

/**
 * Represents a party that resolves back to an account.
 *
 * @property owningKey The public key of the account owner.
 * @property name The name of the account owner; or null if the account owner is unknown.
 * @property accountClass The class of the account.
 * @property accountLinearId The linearId of the account.
 */
class AccountParty internal constructor(
    owningKey: PublicKey,
    private val name: CordaX500Name?,
    private val accountClass: Class<out Account>,
    private val accountLinearId: UniqueIdentifier
) : AbstractParty(owningKey), Resolvable<Account> {

    private val criteria = LinearStateQueryCriteria(
        contractStateTypes = setOf(accountClass),
        relevancyStatus = Vault.RelevancyStatus.ALL,
        status = Vault.StateStatus.UNCONSUMED,
        linearId = listOf(accountLinearId)
    )

    /**
     * Gets the name of the account owner; or null if the account owner is unknown.
     *
     * @return Returns the name of the account owner; or null if the account owner is unknown.
     */
    override fun nameOrNull(): CordaX500Name? {
        return name
    }

    /**
     * Builds a reference to an object being stored or issued by a party.
     *
     * @param bytes The bytes of the object being referenced.
     * @return Returns a party and reference of this object instance.
     */
    override fun ref(bytes: OpaqueBytes): PartyAndReference {
        return PartyAndReference(this, bytes)
    }

    /**
     * Determines whether this [Resolvable] is pointing to the specified [StateAndRef] instance.
     *
     * @param stateAndRef The [StateAndRef] to determine being pointed to.
     * @return Returns true if this [Resolvable] is pointing to the specified [StateAndRef]; otherwise, false.
     */
    override fun isPointingTo(stateAndRef: StateAndRef<Account>): Boolean {
        return accountLinearId == stateAndRef.state.data.linearId
    }

    /**
     * Resolves a [ContractState] using a [CordaRPCOps] instance.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(cordaRPCOps: CordaRPCOps): StateAndRef<Account>? {
        return cordaRPCOps.vaultQueryByCriteria(criteria, accountClass).states.singleOrNull()
    }

    /**
     * Resolves a [ContractState] using a [ServiceHub] instance.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the state.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(serviceHub: ServiceHub): StateAndRef<Account>? {
        return serviceHub.vaultService.queryBy(accountClass, criteria).states.singleOrNull()
    }

    /**
     * Resolves a [ContractState] using a [LedgerTransaction] instance.
     *
     * @param transaction The [LedgerTransaction] instance to use to resolve the state.
     * @param resolution The transaction resolution method to use to resolve the [ContractState] instance.
     * @return Returns the resolved [ContractState], or null if no matching state is found.
     */
    override fun resolve(transaction: LedgerTransaction, resolution: TransactionResolution): StateAndRef<Account>? {
        val states: List<StateAndRef<Account>> = when (resolution) {
            TransactionResolution.INPUT -> transaction.inRefsOfType(accountClass)
            TransactionResolution.OUTPUT -> transaction.outRefsOfType(accountClass)
            TransactionResolution.REFERENCE -> transaction.referenceInputRefsOfType(accountClass)
        }

        return states.singleOrNull { isPointingTo(it) }
    }
}
