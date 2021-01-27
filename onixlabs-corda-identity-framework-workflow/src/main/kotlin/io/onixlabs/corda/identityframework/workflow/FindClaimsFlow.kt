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
import io.onixlabs.corda.core.workflow.DEFAULT_SORTING
import io.onixlabs.corda.core.workflow.FindStatesFlow
import io.onixlabs.corda.core.workflow.andWithExpressions
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema.CordaClaimEntity
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
import net.corda.core.node.services.vault.Sort

/**
 * Represents the flow for finding claims in the vault.
 *
 * @param linearId The linear ID to include in the query.
 * @param externalId The external ID to include in the query.
 * @param issuer The issuer to include in the query.
 * @param holder The holder to include in the query.
 * @param property The property to include in the query.
 * @param value The value to include in the query.
 * @param previousStateRef The state reference of the previous claim to include in the query.
 * @param isSelfIssued The is-self-issued status to include in the query.
 * @param hash The hash to include in the query.
 * @param stateStatus The state status to include in the query.
 * @param relevancyStatus The relevancy status to include in the query.
 * @property pageSpecification The page specification of the query.
 */
@StartableByRPC
@StartableByService
class FindClaimsFlow<T : CordaClaim<*>>(
    linearId: UniqueIdentifier? = null,
    externalId: String? = null,
    issuer: AbstractParty? = null,
    holder: AbstractParty? = null,
    property: String? = null,
    value: Any? = null,
    previousStateRef: StateRef? = null,
    isSelfIssued: Boolean? = null,
    hash: SecureHash? = null,
    stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
    relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
    override val pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
    override val sorting: Sort = DEFAULT_SORTING
) : FindStatesFlow<T>() {
    override val criteria: QueryCriteria = VaultQueryCriteria(
        contractStateTypes = setOf(contractStateType),
        relevancyStatus = relevancyStatus,
        status = stateStatus
    ).andWithExpressions(
        linearId?.let { CordaClaimEntity::linearId.equal(it.id) },
        externalId?.let { CordaClaimEntity::externalId.equal(it) },
        issuer?.let { CordaClaimEntity::issuer.equal(it) },
        holder?.let { CordaClaimEntity::holder.equal(it) },
        property?.let { CordaClaimEntity::property.equal(it) },
        value?.let { CordaClaimEntity::value.equal(it.toString()) },
        previousStateRef?.let { CordaClaimEntity::previousStateRef.equal(it.toString()) },
        isSelfIssued?.let { CordaClaimEntity::isSelfIssued.equal(it) },
        hash?.let { CordaClaimEntity::hash.equal(it.toString()) }
    )
}
