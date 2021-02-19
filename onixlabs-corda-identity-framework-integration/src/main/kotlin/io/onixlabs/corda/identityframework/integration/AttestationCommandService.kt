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
import io.onixlabs.corda.identityframework.contract.*
import io.onixlabs.corda.identityframework.workflow.AmendAttestationFlow
import io.onixlabs.corda.identityframework.workflow.IssueAttestationFlow
import io.onixlabs.corda.identityframework.workflow.PublishAttestationFlow
import io.onixlabs.corda.identityframework.workflow.RevokeAttestationFlow
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowProgressHandle
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.transactions.SignedTransaction

/**
 * Represents the attestation command service.
 *
 * @param rpc The Corda RPC instance that the service will bind to.
 */
class AttestationCommandService(rpc: CordaRPCOps) : RPCService(rpc) {

    /**
     * Issues an attestation.
     *
     * @param T The [Attestation] type.
     * @param attestation The attestation to issue.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Attestation<*>> issueAttestation(
        attestation: T,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(IssueAttestationFlow::Initiator, attestation, notary, observers)
    }

    /**
     * Issues an attestation.
     *
     * @param T The underlying [ContractState] type.
     * @param state The state being attested.
     * @param attestor The attestor of the witnessed state.
     * @param status The status of the attestation.
     * @param metadata Additional information about the attestation.
     * @param linearId The unique identifier of the attestation.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : ContractState> issueAttestation(
        state: StateAndRef<T>,
        attestor: AbstractParty = ourIdentity,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier(),
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val attestation = state.attest(attestor, status, metadata, linearId)
        return issueAttestation(attestation, notary, observers)
    }

    /**
     * Amends an attestation.
     *
     * @param T The [Attestation] type.
     * @param oldAttestation The old attestation to be consumed.
     * @param newAttestation The new attestation to be created.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Attestation<*>> amendAttestation(
        oldAttestation: StateAndRef<T>,
        newAttestation: T,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(AmendAttestationFlow::Initiator, oldAttestation, newAttestation, observers)
    }

    /**
     * Amends an attestation.
     *
     * @param T The underlying [ContractState] type.
     * @param oldAttestation The old attestation to be consumed.
     * @param status The status of the attestation.
     * @param metadata Additional information about the attestation.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : ContractState> amendAttestation(
        oldAttestation: StateAndRef<Attestation<T>>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val newAttestation = oldAttestation.amend(status, metadata = metadata)
        return amendAttestation(oldAttestation, newAttestation, observers)
    }

    /**
     * Amends an attestation.
     *
     * @param T The underlying [ContractState] type.
     * @param oldAttestation The old attestation to be consumed.
     * @param state The state being attested.
     * @param status The status of the attestation.
     * @param metadata Additional information about the attestation.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : ContractState> amendAttestation(
        oldAttestation: StateAndRef<Attestation<T>>,
        state: StateAndRef<T>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        val pointer = state.toAttestationPointer()
        val newAttestation = oldAttestation.amend(status, pointer, metadata)
        return amendAttestation(oldAttestation, newAttestation, observers)
    }

    /**
     * Revokes an attestation.
     *
     * @param T The [Attestation] type.
     * @param attestation The attestation to be consumed.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun <T : Attestation<*>> revokeAttestation(
        attestation: StateAndRef<T>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(RevokeAttestationFlow::Initiator, attestation, observers)
    }

    /**
     * Publishes an attestation.
     *
     * @param T The [Attestation] type.
     * @param attestation The attestation to be published.
     * @param observers Observers of the attestation.
     * @return Returns a flow process handle.
     */
    fun <T : Attestation<*>> publishAttestation(
        attestation: StateAndRef<T>,
        observers: Set<Party>
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(PublishAttestationFlow::Initiator, attestation, observers)
    }
}
