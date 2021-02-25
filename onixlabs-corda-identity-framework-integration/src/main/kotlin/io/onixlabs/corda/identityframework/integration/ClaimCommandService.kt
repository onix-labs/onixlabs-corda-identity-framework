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

package io.onixlabs.corda.identityframework.integration

import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.PublishClaimFlow
import io.onixlabs.corda.identityframework.workflow.RevokeClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

/**
 * Represents the claim command service.
 *
 * @param rpc The Corda RPC instance that the service will bind to.
 */
class ClaimCommandService(rpc: CordaRPCOps) : RPCService(rpc) {

    /**
     * Issues a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to issue.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> issueClaim(
        claim: CordaClaim<T>,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueClaimFlow::Initiator,
            claim,
            notary,
            observers
        )
    }

    /**
     * Issues a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to issue.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> issueClaim(
        claim: CordaClaim<T>,
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueClaimFlow::Initiator,
            claim,
            notary,
            observers
        )
    }

    /**
     * Issues a claim.
     *
     * @param T The underlying claim value type.
     * @param property The property of the claim.
     * @param value The value of the claim.
     * @param issuer The issuer of the claim.
     * @param holder The holder of the claim.
     * @param linearId The unique identifier of the claim.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> issueClaim(
        property: String,
        value: T,
        issuer: AbstractParty = ourIdentity,
        holder: AbstractParty = ourIdentity,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val claim = CordaClaim(issuer, holder, property, value, linearId)
        return issueClaim(claim, notary, observers)
    }

    /**
     * Issues a claim.
     *
     * @param T The underlying claim value type.
     * @param property The property of the claim.
     * @param value The value of the claim.
     * @param issuer The issuer of the claim.
     * @param holder The holder of the claim.
     * @param linearId The unique identifier of the claim.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> issueClaim(
        property: String,
        value: T,
        issuer: AbstractParty = ourIdentity,
        holder: AbstractParty = ourIdentity,
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        val claim = CordaClaim(issuer, holder, property, value, linearId)
        return issueClaim(claim, notary, observers, clientId)
    }

    /**
     * Amends a claim.
     *
     * @param T The underlying claim value type.
     * @param oldClaim The claim to be consumed.
     * @param newClaim The claim to be created.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> amendClaim(
        oldClaim: StateAndRef<CordaClaim<T>>,
        newClaim: CordaClaim<T>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendClaimFlow::Initiator,
            oldClaim,
            newClaim,
            observers
        )
    }

    /**
     * Amends a claim.
     *
     * @param T The underlying claim value type.
     * @param oldClaim The claim to be consumed.
     * @param newClaim The claim to be created.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> amendClaim(
        oldClaim: StateAndRef<CordaClaim<T>>,
        newClaim: CordaClaim<T>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendClaimFlow::Initiator,
            oldClaim,
            newClaim,
            observers
        )
    }

    /**
     * Amends a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param value The amended value of the claim.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> amendClaim(
        claim: StateAndRef<CordaClaim<T>>,
        value: T,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return amendClaim(claim, claim.amend(value), observers)
    }

    /**
     * Amends a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param value The amended value of the claim.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> amendClaim(
        claim: StateAndRef<CordaClaim<T>>,
        value: T,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return amendClaim(claim, claim.amend(value), observers, clientId)
    }

    /**
     * Revokes a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> revokeClaim(
        claim: StateAndRef<CordaClaim<T>>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeClaimFlow::Initiator,
            claim,
            observers
        )
    }

    /**
     * Revokes a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> revokeClaim(
        claim: StateAndRef<CordaClaim<T>>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeClaimFlow::Initiator,
            claim,
            observers
        )
    }

    /**
     * Publishes a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be published.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Any> publishClaim(
        claim: StateAndRef<CordaClaim<T>>,
        observers: Set<Party>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            PublishClaimFlow::Initiator,
            claim,
            observers
        )
    }

    /**
     * Publishes a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be published.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun <T : Any> publishClaim(
        claim: StateAndRef<CordaClaim<T>>,
        observers: Set<Party>,
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            PublishClaimFlow::Initiator,
            claim,
            observers
        )
    }
}
