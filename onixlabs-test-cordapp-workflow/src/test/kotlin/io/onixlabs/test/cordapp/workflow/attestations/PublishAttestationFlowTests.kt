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

package io.onixlabs.test.cordapp.workflow.attestations

import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.createAcceptedLinearAttestation
import io.onixlabs.corda.identityframework.workflow.attestations.IssueAttestationFlow
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.attestations.PublishAttestationFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class PublishAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var attestation: StateAndRef<Attestation<GreetingClaim>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeB) {
                val issuedClaim = it.tx.outRefsOfType<GreetingClaim>().single()
                val attestation = issuedClaim.createAcceptedLinearAttestation(partyB)
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeB) {
                attestation = it.tx.outRefsOfType<Attestation<GreetingClaim>>().single()
                PublishAttestationFlow.Initiator(attestation, setOf(partyC))
            }
            .finally { transaction = it }
    }

    @Test
    fun `AmendAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `AmendAttestationFlow should record a transaction for the attestor and state participants`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `AmendAttestationFlow should record an Attestation for the attestor and state participants`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedAttestation = recordedTransaction
                    .tx.outRefsOfType<Attestation<GreetingClaim>>().singleOrNull()
                    ?: fail("Failed to find a recorded attestation.")

                assertEquals(attestation, recordedAttestation)
            }
        }
    }
}
