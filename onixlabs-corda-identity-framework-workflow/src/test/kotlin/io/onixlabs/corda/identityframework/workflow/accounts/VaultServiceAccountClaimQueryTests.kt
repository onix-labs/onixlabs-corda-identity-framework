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

import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.claims.Claim
import io.onixlabs.corda.identityframework.contract.filterByProperty
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class VaultServiceAccountClaimQueryTests : FlowTest() {

    private lateinit var account: StateAndRef<Account>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) { IssueAccountFlow.Initiator(ACCOUNT_1_FOR_PARTY_A, observers = setOf(partyB, partyC)) }
            .run(nodeA) { IssueAccountFlow.Initiator(ACCOUNT_2_FOR_PARTY_A, observers = setOf(partyB, partyC)) }
            .finally { account = it.tx.outRefsOfType<Account>().single() }
    }

    @Test
    fun `VaultService should find the expected account by claim (string)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaim(Claim("string", "abc"))
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("string").single().value, "abc")
        }
    }

    @Test
    fun `VaultService should find the expected account by claim (integer)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaim(Claim("integer", 123))
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("integer").single().value, 123)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim (decimal)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaim(Claim("decimal", (123.45).toBigDecimal()))
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("decimal").single().value, (123.45).toBigDecimal())
        }
    }

    @Test
    fun `VaultService should find the expected account by claim (boolean)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaim(Claim("boolean", true))
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("boolean").single().value, true)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim (instant)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaim(Claim("instant", Instant.MIN))
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("instant").single().value, Instant.MIN)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim property (string)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimProperty("string")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("string").single().value, "abc")
        }
    }

    @Test
    fun `VaultService should find the expected account by claim property (integer)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimProperty("integer")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("integer").single().value, 123)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim property (decimal)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimProperty("decimal")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("decimal").single().value, (123.45).toBigDecimal())
        }
    }

    @Test
    fun `VaultService should find the expected account by claim property (boolean)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimProperty("boolean")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("boolean").single().value, true)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim property (instant)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimProperty("instant")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("instant").single().value, Instant.MIN)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim value (string)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimValue("abc")
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("string").single().value, "abc")
        }
    }

    @Test
    fun `VaultService should find the expected account by claim value (integer)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimValue(123)
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("integer").single().value, 123)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim value (decimal)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimValue((123.54).toBigDecimal())
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("decimal").single().value, (123.45).toBigDecimal())
        }
    }

    @Test
    fun `VaultService should find the expected account by claim value (boolean)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimValue(true)
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("boolean").single().value, true)
        }
    }

    @Test
    fun `VaultService should find the expected account by claim value (instant)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<Account>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                accountClaimValue(Instant.MIN)
            }

            assertEquals(account, result)
            assertEquals(result!!.state.data.claims.filterByProperty("instant").single().value, Instant.MIN)
        }
    }
}
