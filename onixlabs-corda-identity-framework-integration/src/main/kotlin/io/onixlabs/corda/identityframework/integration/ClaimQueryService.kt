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
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.workflow.FindClaimFlow
import io.onixlabs.corda.identityframework.workflow.FindClaimsFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.utilities.getOrThrow
import java.time.Duration
import java.time.Instant

class ClaimQueryService(rpc: CordaRPCOps) : RPCService(rpc) {

    /**
     * Finds a single claim.
     *
     * @param T The underlying [CordaClaim] type.
     * @param claimClass The class of the underlying corda claim.
     * @param valueClass The class of the underlying corda claim value.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param issuer The issuer to include in the query.
     * @param holder The holder to include in the query.
     * @param property The property to include in the query.
     * @param value The value to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param isSelfIssued The is-self-issued status to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns a claim that matches the query, or null if no claim was found.
     */
    fun <T : CordaClaim<*>> findClaim(
        claimClass: Class<T>,
        valueClass: Class<*>? = null,
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        issuer: AbstractParty? = null,
        holder: AbstractParty? = null,
        property: String? = null,
        value: Any? = null,
        timestamp: Instant? = null,
        isSelfIssued: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<T>? {
        return rpc.startFlowDynamic(
            FindClaimFlow::class.java,
            claimClass,
            valueClass,
            linearId,
            externalId,
            issuer,
            holder,
            property,
            value,
            timestamp,
            isSelfIssued,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)?.cast(claimClass)
    }

    /**
     * Finds a single claim.
     *
     * @param T The underlying [CordaClaim] type.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param issuer The issuer to include in the query.
     * @param holder The holder to include in the query.
     * @param property The property to include in the query.
     * @param value The value to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param isSelfIssued The is-self-issued status to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns a claim that matches the query, or null if no claim was found.
     */
    inline fun <reified T : CordaClaim<*>> findClaim(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        issuer: AbstractParty? = null,
        holder: AbstractParty? = null,
        property: String? = null,
        value: Any? = null,
        timestamp: Instant? = null,
        isSelfIssued: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): StateAndRef<T>? {
        val claimType = object : TypeReference<T>() {}
        val claimClass = claimType.type.toClass()
        val valueClass = claimType.arguments[0].toClass()
        return rpc.startFlowDynamic(
            FindClaimFlow::class.java,
            claimClass,
            valueClass,
            linearId,
            externalId,
            issuer,
            holder,
            property,
            value,
            timestamp,
            isSelfIssued,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout)?.cast()
    }

    /**
     * Finds multiple claims.
     *
     * @param T The underlying [CordaClaim] type.
     * @param claimClass The class of the underlying corda claim.
     * @param valueClass The class of the underlying corda claim value.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param issuer The issuer to include in the query.
     * @param holder The holder to include in the query.
     * @param property The property to include in the query.
     * @param value The value to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param isSelfIssued The is-self-issued status to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns claims that matches the query, or null if no claims are found.
     */
    fun <T : CordaClaim<*>> findClaims(
        claimClass: Class<T>,
        valueClass: Class<*>? = null,
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        issuer: AbstractParty? = null,
        holder: AbstractParty? = null,
        property: String? = null,
        value: Any? = null,
        timestamp: Instant? = null,
        isSelfIssued: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<T>> {
        return rpc.startFlowDynamic(
            FindClaimsFlow::class.java,
            claimClass,
            valueClass,
            linearId,
            externalId,
            issuer,
            holder,
            property,
            value,
            timestamp,
            isSelfIssued,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout).cast(claimClass)
    }

    /**
     * Finds multiple claims.
     *
     * @param T The underlying [CordaClaim] type.
     * @param linearId The linear ID to include in the query.
     * @param externalId The external ID to include in the query.
     * @param issuer The issuer to include in the query.
     * @param holder The holder to include in the query.
     * @param property The property to include in the query.
     * @param value The value to include in the query.
     * @param timestamp The timestamp to include in the query.
     * @param isSelfIssued The is-self-issued status to include in the query.
     * @param hash The hash to include in the query.
     * @param stateStatus The state status to include in the query.
     * @param relevancyStatus The relevancy status to include in the query.
     * @property pageSpecification The page specification of the query.
     * @param flowTimeout The amount of time that the flow will be allowed to execute before failing.
     * @return Returns claims that matches the query, or null if no claims are found.
     */
    inline fun <reified T : CordaClaim<*>> findClaims(
        linearId: UniqueIdentifier? = null,
        externalId: String? = null,
        issuer: AbstractParty? = null,
        holder: AbstractParty? = null,
        property: String? = null,
        value: Any? = null,
        timestamp: Instant? = null,
        isSelfIssued: Boolean? = null,
        hash: SecureHash? = null,
        stateStatus: Vault.StateStatus = Vault.StateStatus.UNCONSUMED,
        relevancyStatus: Vault.RelevancyStatus = Vault.RelevancyStatus.ALL,
        pageSpecification: PageSpecification = DEFAULT_PAGE_SPECIFICATION,
        flowTimeout: Duration = Duration.ofSeconds(30)
    ): List<StateAndRef<T>> {
        val claimType = object : TypeReference<T>() {}
        val claimClass = claimType.type.toClass()
        val valueClass = claimType.arguments[0].toClass()
        return rpc.startFlowDynamic(
            FindClaimsFlow::class.java,
            claimClass,
            valueClass,
            linearId,
            externalId,
            issuer,
            holder,
            property,
            value,
            timestamp,
            isSelfIssued,
            hash,
            stateStatus,
            relevancyStatus,
            pageSpecification
        ).returnValue.getOrThrow(flowTimeout).cast()
    }
}
