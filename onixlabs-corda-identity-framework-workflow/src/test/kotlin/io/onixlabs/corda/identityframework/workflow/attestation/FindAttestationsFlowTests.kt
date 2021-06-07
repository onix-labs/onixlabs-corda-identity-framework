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

package io.onixlabs.corda.identityframework.workflow.attestation

import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultQuery
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.*
import io.onixlabs.corda.identityframework.contract.AttestationSchema.AttestationEntity
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
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
                val attestation = claim.acceptLinearState(partyC, linearId = UniqueIdentifier("attestation"))
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.rejectState()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .run(nodeC) {
                val oldAttestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single()
                val newAttestation = oldAttestation.acceptState()
                AmendAttestationFlow.Initiator(oldAttestation, newAttestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<CordaClaim<String>>>().single() }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                linearIds(attestation.state.data.linearId)
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::externalId equalTo attestation.state.data.linearId.externalId)
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::attestor equalTo attestation.state.data.attestor)
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::pointerStateRef equalTo claim.ref.toString())
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerStateLinearId`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::pointerStateLinearId equalTo claim.state.data.linearId.id)
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by pointerHash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::pointerHash equalTo attestation.state.data.pointer.hash.toString())
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by status (ACCEPTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::status equalTo AttestationStatus.ACCEPTED)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by status (REJECTED)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::status equalTo AttestationStatus.REJECTED)
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::previousStateRef equalTo attestation.state.data.previousStateRef?.toString())
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `FindAttestationsFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Attestation<CordaClaim<String>>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                where(AttestationEntity::hash equalTo attestation.state.data.hash.toString())
            }

            assertEquals(1, results.count())
        }
    }
}
