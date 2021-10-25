/*
 * Copyright 2020-2021 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimSchema
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.identityframework.workflow.claims.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceClaimsQueryTests : FlowTest() {

    private lateinit var claim: StateAndRef<CordaClaim<String>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<GreetingClaim>().single()
                val claim = oldClaim.amend("Goodbye, World!")
                AmendClaimFlow.Initiator(oldClaim, claim, observers = setOf(partyC))
            }
            .finally { claim = it.tx.outRefsOfType<CordaClaim<String>>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                linearIds(GREETING_CLAIM.linearId)
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService isNull should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::externalId.isNull())
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimIssuer(GREETING_CLAIM.issuer)
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimHolder(GREETING_CLAIM.holder)
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimProperty(GREETING_CLAIM.property)
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimValue(GREETING_CLAIM.value)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimPreviousStateRef(GREETING_CLAIM.previousStateRef)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimIsSelfIssued(GREETING_CLAIM.isSelfIssued)
                claimType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimHash(GREETING_CLAIM.hash)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }
}
