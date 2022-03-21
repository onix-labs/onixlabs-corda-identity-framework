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

package io.onixlabs.corda.identityframework.workflow.attestations

import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.createAcceptedStaticAttestation
import io.onixlabs.corda.identityframework.contract.rejectAttestation
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceAttestationQueryTests : FlowTest() {

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
                val attestation = claim.createAcceptedStaticAttestation(partyC)
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.rejectAttestation()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                linearIds(attestation.state.data.linearId)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService isNull should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(AttestationEntity::externalId.isNull())
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationAttestor(attestation.state.data.attestor)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationPointer(claim.ref)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointerHash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationPointerHash(attestation.state.data.pointer.hash)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by status`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationStatus(attestation.state.data.status)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationPreviousStateRef(attestation.state.data.previousStateRef)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                attestationHash(attestation.state.data.hash)
                attestationType()
            }

            assertEquals(attestation, result)
        }
    }
}
