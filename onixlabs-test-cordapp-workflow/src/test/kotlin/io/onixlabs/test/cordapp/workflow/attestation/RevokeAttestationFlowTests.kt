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
import io.onixlabs.corda.identityframework.workflow.IssueAttestationFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.RevokeAttestationFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeAttestationFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeC) {
                val issuedClaim = it.tx.outRefsOfType<GreetingClaim>().single()
                val attestation = issuedClaim.accept(partyC)
                IssueAttestationFlow.Initiator(attestation)
            }
            .run(nodeC) {
                val attestation = it.tx.outRefsOfType<Attestation<GreetingClaim>>().single()
                RevokeAttestationFlow.Initiator(attestation)
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeAttestationFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeAttestationFlow should record a transaction for the attestor and state participants`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}
