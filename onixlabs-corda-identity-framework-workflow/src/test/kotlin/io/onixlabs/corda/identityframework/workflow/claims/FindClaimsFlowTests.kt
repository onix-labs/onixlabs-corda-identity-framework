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

package io.onixlabs.corda.identityframework.workflow.claims

import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class FindClaimsFlowTests : FlowTest() {

    private lateinit var claim: StateAndRef<CordaClaim<String>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Amended Value 1")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Amended Value 2")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .run(nodeB) {
                IssueClaimFlow.Initiator(CLAIM_2, observers = setOf(partyA, partyC))
            }
            .run(nodeB) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Int>>().single()
                val newClaim = oldClaim.amend(456)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyC))
            }
            .run(nodeB) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Int>>().single()
                val newClaim = oldClaim.amend(789)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyC))
            }
            .run(nodeC) {
                IssueClaimFlow.Initiator(CLAIM_3, observers = setOf(partyA, partyB))
            }
            .run(nodeC) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Instant>>().single()
                val newClaim = oldClaim.amend(Instant.MIN)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyB))
            }
            .run(nodeC) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Instant>>().single()
                val newClaim = oldClaim.amend(Instant.MAX)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyB))
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
                        FindClaimsFlow(
                            linearId = CLAIM_1.linearId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
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
                        FindClaimsFlow(
                            externalId = CLAIM_1.linearId.externalId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
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
                        FindClaimsFlow(
                            issuer = CLAIM_1.issuer,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
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
                        FindClaimsFlow(
                            holder = CLAIM_1.holder,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(6, it.size) }
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
                        FindClaimsFlow(
                            property = CLAIM_1.property,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
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
                        FindClaimsFlow(
                            value = CLAIM_1.value,
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
                        FindClaimsFlow(
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
                        FindClaimsFlow(
                            isSelfIssued = true,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(6, it.size) }
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
                        FindClaimsFlow(
                            hash = CLAIM_1.hash,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }
}
