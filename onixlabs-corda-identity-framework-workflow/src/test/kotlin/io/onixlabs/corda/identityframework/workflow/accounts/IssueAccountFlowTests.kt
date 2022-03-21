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

package io.onixlabs.corda.identityframework.workflow.accounts

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.Pipeline
import net.corda.core.transactions.SignedTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals

class IssueAccountFlowTests : FlowTest() {

    private lateinit var transaction: SignedTransaction
    private lateinit var account: Account

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                account = ACCOUNT_1_FOR_PARTY_A
                IssueAccountFlow.Initiator(account, observers = setOf(partyB, partyC))
            }
            .finally { transaction = it }
    }

    @Test
    fun `IssueAccountFlow transaction should be signed by the initiator`() {
        transaction.verifyRequiredSignatures()
    }

    @Test
    fun `IssueAccountFlow should record a transaction for the account holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: {${transaction.id}.")

                assertEquals(transaction, recordedTransaction)
            }
        }
    }

    @Test
    fun `IssueAccountFlow should record an Account for the account holder and observers`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            it.transaction {
                val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id)
                    ?: fail("Failed to find a recorded transaction with id: ${transaction.id}.")

                val recordedAccount = recordedTransaction.tx.outputsOfType<Account>().singleOrNull()
                    ?: fail("Failed to find a recorded account.")

                assertEquals(account, recordedAccount)
                assertEquals(5, recordedAccount.claims.size)
                assert(recordedAccount.claims.containsAll(ACCOUNT_CLAIMS))
            }
        }
    }
}
