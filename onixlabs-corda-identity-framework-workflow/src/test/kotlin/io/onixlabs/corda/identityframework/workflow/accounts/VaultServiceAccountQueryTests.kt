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

import io.onixlabs.corda.core.services.isNull
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountSchema.AccountEntity
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.Pipeline
import io.onixlabs.corda.identityframework.workflow.accountOwner
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceAccountQueryTests : FlowTest() {

    private lateinit var account: StateAndRef<Account>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) { IssueAccountFlow.Initiator(ACCOUNT_1_FOR_PARTY_A, observers = setOf(partyB, partyC)) }
            .finally { account = it.tx.outRefsOfType<Account>().single() }
    }

    @Test
    fun `VaultService should find the expected account by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                linearIds(account.state.data.linearId)
            }

            assertEquals(account, result)
        }
    }

    @Test
    fun `VaultService should find the expected account by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(AccountEntity::externalId.isNull())
            }

            assertEquals(account, result)
        }
    }

    @Test
    fun `VaultService should find the expected account by owner`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountOwner(partyA)
            }

            assertEquals(account, result)
        }
    }
}
