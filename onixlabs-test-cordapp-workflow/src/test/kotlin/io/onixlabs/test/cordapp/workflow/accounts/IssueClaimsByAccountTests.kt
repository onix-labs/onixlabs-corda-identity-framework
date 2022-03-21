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

package io.onixlabs.test.cordapp.workflow.accounts

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.single
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.workflow.accounts.IssueAccountFlow
import io.onixlabs.corda.identityframework.workflow.accounts.PublishAccountFlow
import io.onixlabs.corda.identityframework.workflow.claimHolderAccount
import io.onixlabs.corda.identityframework.workflow.claimIssuerAccount
import io.onixlabs.corda.identityframework.workflow.claimType
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.claims.PublishClaimFlow
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IssueClaimsByAccountTests : FlowTest() {

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueAccountFlow.Initiator(ACCOUNT_FOR_PARTY_A)
            }
            .run(nodeA) {
                val account = nodeA.services.vaultServiceFor<Account>().single {
                    linearIds(ACCOUNT_FOR_PARTY_A.linearId)
                }

                PublishAccountFlow.Initiator(account, setOf(partyB, partyC))
            }
            .run(nodeB) {
                IssueAccountFlow.Initiator(ACCOUNT_FOR_PARTY_B)
            }
            .run(nodeB) {
                val account = nodeB.services.vaultServiceFor<Account>().single {
                    linearIds(ACCOUNT_FOR_PARTY_B.linearId)
                }

                PublishAccountFlow.Initiator(account, setOf(partyA, partyC))
            }
            .run(nodeA) {
                val claim = GreetingClaim(ACCOUNT_FOR_PARTY_A.toAccountParty(), ACCOUNT_FOR_PARTY_A.toAccountParty())
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = it.tx.outRefsOfType<GreetingClaim>().single()
                PublishClaimFlow.Initiator(claim, setOf(partyB, partyC))
            }
            .run(nodeA) {
                val claim = GreetingClaim(ACCOUNT_FOR_PARTY_A.toAccountParty(), ACCOUNT_FOR_PARTY_B.toAccountParty())
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = it.tx.outRefsOfType<GreetingClaim>().single()
                PublishClaimFlow.Initiator(claim, setOf(partyC))
            }
    }

    @Test
    fun `IssueClaimFlow should record all claims where the claim issuer resolves to the account for party A`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val claims = it.services.vaultServiceFor<GreetingClaim>().filter {
                claimType()
                claimIssuerAccount(ACCOUNT_FOR_PARTY_A)
            }.toList()

            assertEquals(2, claims.size)
        }
    }

    @Test
    fun `IssueClaimFlow should record all claims where the claim holder resolves to the account for party A`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val claims = it.services.vaultServiceFor<GreetingClaim>().filter {
                claimType()
                claimHolderAccount(ACCOUNT_FOR_PARTY_A)
            }.toList()

            assertEquals(1, claims.size)
        }
    }

    @Test
    fun `IssueClaimFlow should record all claims where the claim holder resolves to the account for party B`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val claims = it.services.vaultServiceFor<GreetingClaim>().filter {
                claimType()
                claimHolderAccount(ACCOUNT_FOR_PARTY_B)
            }.toList()

            assertEquals(1, claims.size)
        }
    }
}
