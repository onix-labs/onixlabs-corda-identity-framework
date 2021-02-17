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

package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.core.workflow.DEFAULT_PAGE_SPECIFICATION
import io.onixlabs.corda.core.workflow.FindStateFlow
import io.onixlabs.corda.core.workflow.andWithExpressions
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationPointer
import io.onixlabs.corda.identityframework.contract.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.QueryCriteria.VaultQueryCriteria

/**
 * Represents the flow for finding an attestation in the vault.
 *
 * @param attestationClass The class of the underlying attestation.
 * @param linearId The linear ID to include in the query.
 * @param externalId The external ID to include in the query.
 * @param attestor The attestor to include in the query.
 * @param pointer The attestation pointer to include in the query.
 * @param pointerStateRef The pointer state reference to include in the query.
 * @param pointerStateClass The pointer state class to include in the query.
 * @param pointerStateLinearId The pointer state linear ID to include in the query.
 * @param pointerHash The pointer hash to include in the query.
 * @param status The attestation status to include in the query.
 * @param previousStateRef The state reference of the previous attestation to include in the query.
 * @param hash The hash to include in the query.
 * @param stateStatus The state status to include in the query.
 * @param relevancyStatus The relevancy status to include in the query.
 * @property pageSpecification The page specification of the query.
 */
@StartableByRPC
@StartableByService
class FindAttestationFlow(
    attestationClass: Class<out Attestation<*>>? = null,
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    attestor: AbstractParty? = null,
    pointer: AttestationPointer<*>? = null,
    pointerStateRef: StateRef? = null,
    pointerStateClass: Class<out ContractState>? = null,
    pointerStateLinearId: UniqueIdentifier? = null,
    pointerHash: SecureHash? = null,
    status: AttestationStatus? = null,
    previousStateRef: StateRef? = null,
    hash: SecureHash? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION
) : FindStateFlow<Attestation<*>>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(attestationClass ?: contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        attestationClass?.let { AttestationEntity::attestationClass.equal(it.canonicalName) },
        linearId?.let { AttestationEntity::linearId.equal(it.id) },
        externalId?.let { AttestationEntity::externalId.equal(it) },
        attestor?.let { AttestationEntity::attestor.equal(it) },
        pointer?.let { AttestationEntity::pointerHash.equal(it.hash.toString()) },
        pointerStateRef?.let { AttestationEntity::pointerStateRef.equal(it.toString()) },
        pointerStateClass?.let { AttestationEntity::pointerStateClass.equal(it.canonicalName) },
        pointerStateLinearId?.let { AttestationEntity::pointerStateLinearId.equal(it.id) },
        pointerHash?.let { AttestationEntity::pointerHash.equal(it.toString()) },
        status?.let { AttestationEntity::status.equal(it) },
        previousStateRef?.let { AttestationEntity::previousStateRef.equal(it.toString()) },
        hash?.let { AttestationEntity::hash.equal(it.toString()) }
    )
}
