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

import io.onixlabs.corda.core.contract.cast
import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationPointer
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import io.onixlabs.corda.identityframework.workflow.FindAttestationFlow
import io.onixlabs.corda.identityframework.workflow.FindAttestationsFlow
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration
import java.time.Instant

/**
 * Represents the attestation query service.
 *
 * @param rpc The Corda RPC instance that the service will bind to.
 */
class AttestationQueryService(rpc: CordaRPCOps) : RPCService(rpc) {

    /**
     * Finds a single attestation.
     *
     * @param T The underlying [Attestation] type.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param attestor The attestor to include in the query.
     * @param pointer The attestation pointer to include in the query.
     * @param pointerStateRef The pointer state reference to include in the query.
     * @param pointerStateClass The pointer state class to include in the query.
     * @param pointerStateLinearId The pointer state linear ID to include in the query.
     * @param pointerHash The pointer hash to include in the query.
     * @param status The attestation status to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns an attestation that matches the query, or null if no attestation was found.
     */
    inline fun <reified T : Attestation<*>> findAttestation(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        attestor: AbstractParty? = null,
        pointer: AttestationPointer<*>? = null,
        pointerStateRef: StateRef? = null,
        pointerStateClass: Class<out ContractState>? = null,
        pointerStateLinearId: UniqueIdentifier? = null,
        pointerHash: SecureHash? = null,
        status: AttestationStatus? = null,
        timestamp: Instant? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<T>? {
        return rpc.startFlowDynamic(
            FindAttestationFlow::class.java,
            T::class.java,
            linearId,
            externalId,
            attestor,
            pointer,
            pointerStateRef,
            pointerStateClass,
            pointerStateLinearId,
            pointerHash,
            status,
            timestamp,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)?.cast()
    }

    /**
     * Finds multiple attestations.
     *
     * @param T The underlying [Attestation] type.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param attestor The attestor to include in the query.
     * @param pointer The attestation pointer to include in the query.
     * @param pointerStateRef The pointer state reference to include in the query.
     * @param pointerStateClass The pointer state class to include in the query.
     * @param pointerStateLinearId The pointer state linear ID to include in the query.
     * @param pointerHash The pointer hash to include in the query.
     * @param status The attestation status to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns attestations that match the query, or null if no attestations are found.
     */
    inline fun <reified T : Attestation<*>> findAttestations(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        attestor: AbstractParty? = null,
        pointer: AttestationPointer<*>? = null,
        pointerStateRef: StateRef? = null,
        pointerStateClass: Class<out ContractState>? = null,
        pointerStateLinearId: UniqueIdentifier? = null,
        pointerHash: SecureHash? = null,
        status: AttestationStatus? = null,
        timestamp: Instant? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<T>> {
        return rpc.startFlowDynamic(
            FindAttestationsFlow::class.java,
            T::class.java,
            linearId,
            externalId,
            attestor,
            pointer,
            pointerStateRef,
            pointerStateClass,
            pointerStateLinearId,
            pointerHash,
            status,
            timestamp,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout).cast()
    }
}
