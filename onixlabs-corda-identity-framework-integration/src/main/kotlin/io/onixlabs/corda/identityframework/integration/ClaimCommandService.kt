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
import io.onixlabs.corda.identityframework.workflow.RevokeClaimFlow
import io.onixlabs.corda.identityframework.workflow.SendClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

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
     * @param property The property of the claim.
     * @param value The value of the claim.
     * @param issuer The issuer of the claim.
     * @param holder The holder of the claim.
     * @param linearId The unique identifier of the claim.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
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
        return rpc.startTrackedFlow(
            IssueClaimFlow::Initiator,
            CordaClaim(issuer, holder, property, value, linearId),
            notary,
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
     */
    fun <T : Any> amendClaim(
        claim: StateAndRef<CordaClaim<T>>,
        value: T,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendClaimFlow::Initiator,
            claim,
            claim.amend(value),
            observers
        )
    }

    /**
     * Revokes a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param observers Additional observers of the transaction.
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
     * Sends a claim.
     *
     * @param T The underlying claim value type.
     * @param claim The claim to be consumed.
     * @param observers Additional observers of the transaction.
     */
    fun <T : Any> sendClaim(
        claim: StateAndRef<CordaClaim<T>>,
        observers: Set<Party>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            SendClaimFlow::Initiator,
            claim,
            observers
        )
    }
}
