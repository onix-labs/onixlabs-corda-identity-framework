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

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.identityframework.v1.contract.CordaClaim
import io.onixlabs.corda.identityframework.v1.contract.amend
import io.onixlabs.corda.identityframework.workflow.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.FindClaimsFlow
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
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            linearId = GREETING_CLAIM.linearId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            externalId = GREETING_CLAIM.linearId.externalId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            issuer = GREETING_CLAIM.issuer,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            holder = GREETING_CLAIM.holder,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            property = GREETING_CLAIM.property,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            value = GREETING_CLAIM.value,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<CordaClaim<*>>(
                            previousStateRef = claim.state.data.previousStateRef,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            isSelfIssued = true,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(0, it.size) }
            }
        }
    }

    @Test
    fun `FindClaimsFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimsFlow<GreetingClaim>(
                            hash = GREETING_CLAIM.hash,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }
}
