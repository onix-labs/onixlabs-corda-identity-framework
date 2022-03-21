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

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.PublishClaimFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class PublishClaimFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var claim: StateAndRef<GreetingClaim>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM)
            }
            .run(nodeA) {
                claim = it.tx.outRefsOfType<GreetingClaim>().single()
                PublishClaimFlow.Initiator(claim, observers = setOf(partyC))

            }
            .finally { transaction = it }
    }

    @Test
    fun `PublishClaimFlow should record a transaction for the claim issuer, claim holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `PublishClaimFlow should record a CordaClaim for the claim issuer, claim holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedClaim = recordedTransaction
                    .tx.outRefsOfType<GreetingClaim>().singleOrNull()
                    ?: fail("Failed to find a recorded claim.")

                assertEquals(claim, recordedClaim)
            }
        }
    }
}
