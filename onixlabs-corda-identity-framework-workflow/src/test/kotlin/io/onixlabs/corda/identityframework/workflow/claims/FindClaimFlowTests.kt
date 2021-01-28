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
import kotlin.test.assertEquals

class FindClaimFlowTests : FlowTest() {

    private lateinit var claim: StateAndRef<CordaClaim<String>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Goodbye, World!")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .finally { claim = it.tx.outRefsOfType<CordaClaim<String>>().single() }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            linearId = claim.state.data.linearId,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            externalId = claim.state.data.linearId.externalId,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            issuer = claim.state.data.issuer,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            holder = claim.state.data.holder,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            property = claim.state.data.property,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            value = claim.state.data.value,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            previousStateRef = claim.state.data.previousStateRef,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            isSelfIssued = claim.state.data.isSelfIssued,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindClaimFlow<CordaClaim<String>>(
                            hash = claim.state.data.hash,
                            stateStatus = Vault.StateStatus.UNCONSUMED
                        )
                    }
                    .finally { assertEquals(claim, it) }
            }
        }
    }
}
