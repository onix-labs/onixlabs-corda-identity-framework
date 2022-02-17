/*
 * Copyright 2020-2022 ONIXLabs
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

import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimSchema
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.identityframework.workflow.claims.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceClaimQueryTests : FlowTest() {

    private lateinit var claim: StateAndRef<GreetingClaim>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<GreetingClaim>().single()
                val newClaim = oldClaim.amend("Goodbye, World!")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .finally { claim = it.tx.outRefsOfType<GreetingClaim>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                linearIds(claim.state.data.linearId)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService isNull should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                expression(CordaClaimSchema.CordaClaimEntity::externalId.isNull())
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimIssuer(claim.state.data.issuer)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimHolder(claim.state.data.holder)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimProperty(claim.state.data.property)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimValue(claim.state.data.value)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimPreviousStateRef(claim.state.data.previousStateRef)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimIsSelfIssued(claim.state.data.isSelfIssued)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<GreetingClaim>().singleOrNull {
                claimHash(claim.state.data.hash)
                claimType()
            }

            assertEquals(claim, result)
        }
    }
}
