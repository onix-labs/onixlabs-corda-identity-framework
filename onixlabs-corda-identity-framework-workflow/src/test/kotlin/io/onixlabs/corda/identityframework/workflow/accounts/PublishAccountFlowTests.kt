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

package io.onixlabs.corda.identityframework.workflow.accounts

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class PublishAccountFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var account: StateAndRef<Account>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueAccountFlow.Initiator(ACCOUNT_1_FOR_PARTY_A)
            }
            .run(nodeA) {
                account = it.tx.outRefsOfType<Account>().single()
                PublishAccountFlow.Initiator(account, observers = setOf(partyB, partyC))
            }
            .finally { transaction = it }
    }

    @Test
    fun `PublishAccountFlow should record a transaction for the account holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: {${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `PublishAccountFlow should record an Account for the account holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedAccount = recordedTransaction.tx.outRefsOfType<Account>().singleOrNull()
                    ?: fail("Failed to find a recorded account.")

                assertEquals(account, recordedAccount)
            }
        }
    }
}
