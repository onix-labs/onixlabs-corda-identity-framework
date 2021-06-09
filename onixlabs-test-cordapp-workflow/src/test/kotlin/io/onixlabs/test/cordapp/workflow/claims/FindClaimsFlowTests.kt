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

import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindClaimsFlowTests : FlowTest() {

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
    fun `FindClaimsFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                linearIds(GREETING_CLAIM.linearId)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::externalId.isNull())
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::issuer equalTo GREETING_CLAIM.issuer)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::holder equalTo GREETING_CLAIM.holder)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::property equalTo GREETING_CLAIM.property)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::value equalTo GREETING_CLAIM.value)
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::previousStateRef.isNull())
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::isSelfIssued equalTo GREETING_CLAIM.isSelfIssued)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<GreetingClaim>().filter {
                stateStatus(Vault.StateStatus.ALL)
                expression(CordaClaimSchema.CordaClaimEntity::hash equalTo GREETING_CLAIM.hash.toString())
            }

            assertEquals(1, results.count())
        }
    }
}
