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

package io.onixlabs.test.cordapp.workflow.attestation

import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationSchema
import io.onixlabs.corda.identityframework.contract.createAcceptedLinearAttestation
import io.onixlabs.corda.identityframework.workflow.IssueAttestationFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceAttestationQueryTests : FlowTest() {

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
                val attestation = claim.createAcceptedLinearAttestation(partyC)
                IssueAttestationFlow.Initiator(attestation)
            }
            .finally { attestation = it.tx.outRefsOfType<Attestation<GreetingClaim>>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                linearIds(attestation.state.data.linearId)
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService isNull should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::externalId.isNull())
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by attestor`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::attestor equalTo attestation.state.data.attestor)
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointer hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::pointerHash equalTo attestation.state.data.pointer.hash.toString())
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by pointer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::pointer equalTo attestation.state.data.pointer.statePointer.toString())
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by status`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::status equalTo attestation.state.data.status)
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::previousStateRef.isNull())
            }

            assertEquals(attestation, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Attestation<*>>().singleOrNull {
                expression(AttestationSchema.AttestationEntity::hash equalTo attestation.state.data.hash.toString())
            }

            assertEquals(attestation, result)
        }
    }
}

