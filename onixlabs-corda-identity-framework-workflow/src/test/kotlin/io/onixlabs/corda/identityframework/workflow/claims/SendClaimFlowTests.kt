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
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.Pipeline
import io.onixlabs.corda.identityframework.workflow.SendClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class SendClaimFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var claim: StateAndRef<CordaClaim<String>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeB) {
                IssueClaimFlow.Initiator(CLAIM_2)
            }
            .run(nodeB) {
                claim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                SendClaimFlow.Initiator(claim, observers = setOf(partyA, partyC))

            }
            .finally { transaction = it }
    }

    @Test
    fun `SendClaimFlow should record a transaction for the claim issuer, claim holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `SendClaimFlow should record a CordaClaim for the claim issuer, claim holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedClaim = recordedTransaction
                    .tx.outRefsOfType<CordaClaim<String>>().singleOrNull()
                    ?: fail("Failed to find a recorded claim.")

                assertEquals(claim, recordedClaim)
            }
        }
    }
}
