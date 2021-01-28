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

package io.onixlabs.test.cordapp.workflow.attestation

import io.onixlabs.corda.identityframework.v1.contract.Attestation
import io.onixlabs.corda.identityframework.v1.contract.accept
import io.onixlabs.corda.identityframework.workflow.FindAttestationFlow
import io.onixlabs.corda.identityframework.workflow.IssueAttestationFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindAttestationFlowTests : FlowTest() {

    private lateinit var claim: StateAndRef<GreetingClaim>
    private lateinit var attestation: StateAndRef<Attestation<GreetingClaim>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeC) {
                claim = it.tx.outRefsOfType<GreetingClaim>().single()
                val attestation = claim.accept(partyC)
                IssueAttestationFlow.Initiator(attestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<GreetingClaim>>().single() }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            linearId = attestation.state.data.linearId
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            externalId = attestation.state.data.linearId.externalId
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            attestor = attestation.state.data.attestor
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by pointer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            pointer = attestation.state.data.pointer
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by pointerStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            pointerStateRef = claim.ref
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by pointerStateClass`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            pointerStateClass = claim.state.data.javaClass
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by pointerStateLinearId`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            pointerStateLinearId = claim.state.data.linearId
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by pointerHash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            pointerHash = attestation.state.data.pointer.hash
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by status`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            status = attestation.state.data.status
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            previousStateRef = attestation.state.data.previousStateRef
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }

    @Test
    fun `FindAttestationFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationFlow<Attestation<GreetingClaim>>(
                            hash = attestation.state.data.hash
                        )
                    }
                    .finally { assertEquals(attestation, it) }
            }
        }
    }
}
