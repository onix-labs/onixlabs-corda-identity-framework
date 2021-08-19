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

package io.onixlabs.corda.identityframework.workflow.attestations

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.*
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceAttestationsQueryTests : FlowTest() {

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
                val attestation =
                    claim.createAcceptedStaticAttestation(partyC, linearId = UniqueIdentifier("attestation"))
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.rejectAttestation()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.acceptAttestation()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                linearIds(attestation.state.data.linearId)
                attestationType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                externalIds(attestation.state.data.linearId.externalId)
                attestationType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationAttestor(attestation.state.data.attestor)
                attestationType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationPointer(claim.ref)
                attestationType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointerHash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationPointerHash(attestation.state.data.pointer.hash)
                attestationType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by status (ACCEPTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationStatus(AttestationStatus.ACCEPTED)
                attestationType()
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by status (REJECTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationStatus(AttestationStatus.REJECTED)
                attestationType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationPreviousStateRef(attestation.state.data.previousStateRef)
                attestationType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                attestationHash(attestation.state.data.hash)
                attestationType()
            }

            assertEquals(1, results.count())
        }
    }
}
