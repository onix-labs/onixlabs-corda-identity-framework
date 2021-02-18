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

package io.onixlabs.corda.identityframework.workflow.attestation

import io.onixlabs.corda.identityframework.contract.*
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindAttestationsFlowTests : FlowTest() {

    private lateinit var claim: StateAndRef<CordaClaim<String>>
    private lateinit var attestation: StateAndRef<Attestation<CordaClaim<String>>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeC) {
                claim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val attestation = claim.accept(partyC, linearId = UniqueIdentifier("attestation"))
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.reject()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.accept()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single() }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            linearId = attestation.state.data.linearId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            externalId = attestation.state.data.linearId.externalId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            attestor = attestation.state.data.attestor,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            pointer = attestation.state.data.pointer,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            pointerStateRef = claim.ref,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerStateClass`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            pointerStateClass = claim.state.data.javaClass,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerStateLinearId`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            pointerStateLinearId = claim.state.data.linearId,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerHash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            pointerHash = attestation.state.data.pointer.hash,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(3, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by status (ACCEPTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            status = AttestationStatus.ACCEPTED,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(2, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by status (REJECTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            status = AttestationStatus.REJECTED,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            previousStateRef = attestation.state.data.previousStateRef,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                Pipeline
                    .create(network)
                    .run(it) {
                        FindAttestationsFlow(
                            hash = attestation.state.data.hash,
                            stateStatus = Vault.StateStatus.ALL
                        )
                    }
                    .finally { assertEquals(1, it.size) }
            }
        }
    }
}
