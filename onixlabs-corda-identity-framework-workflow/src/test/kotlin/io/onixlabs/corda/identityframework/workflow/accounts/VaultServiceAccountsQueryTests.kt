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

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountSchema.AccountEntity
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.Pipeline
import io.onixlabs.corda.identityframework.workflow.accountOwner
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceAccountsQueryTests : FlowTest() {

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) { IssueAccountFlow.Initiator(ACCOUNT_1_FOR_PARTY_A, observers = setOf(partyB, partyC)) }
            .run(nodeA) { IssueAccountFlow.Initiator(ACCOUNT_2_FOR_PARTY_A, observers = setOf(partyB, partyC)) }
            .run(nodeB) { IssueAccountFlow.Initiator(ACCOUNT_1_FOR_PARTY_B, observers = setOf(partyA, partyC)) }
            .run(nodeB) { IssueAccountFlow.Initiator(ACCOUNT_2_FOR_PARTY_B, observers = setOf(partyA, partyC)) }
    }

    @Test
    fun `VaultService should find the expected account by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Account>().filter {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                linearIds(ACCOUNT_1_FOR_PARTY_A.linearId)
            }

            assertEquals(1, results.count())
            assertEquals(ACCOUNT_1_FOR_PARTY_A, results.single().state.data)
        }
    }

    @Test
    fun `VaultService should find the expected account by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Account>().filter {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(AccountEntity::externalId.isNull())
            }

            assertEquals(4, results.count())
        }
    }

    @Test
    fun `VaultService should find the expected account by owner (Party A)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Account>().filter {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountOwner(partyA)
            }

            assertEquals(2, results.count())
        }
    }

    @Test
    fun `VaultService should find the expected account by owner (Party B)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<Account>().filter {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountOwner(partyB)
            }

            assertEquals(2, results.count())
        }
    }
}
