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

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.claims.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.RevokeClaimFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class RevokeClaimFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(GREETING_CLAIM, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<GreetingClaim>().single()
                val claim = oldClaim.amend("Goodbye, World!")
                AmendClaimFlow.Initiator(oldClaim, claim, observers = setOf(partyC))
            }
            .run(nodeA) {
                val claim = it.tx.outRefsOfType<GreetingClaim>().single()
                RevokeClaimFlow.Initiator(claim, observers = setOf(partyC))
            }
            .finally { transaction = it }
    }

    @Test
    fun `RevokeClaimFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `RevokeClaimFlow should record a transaction for the claim issuer, claim holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }
}
