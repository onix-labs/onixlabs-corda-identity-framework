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

package io.onixlabs.corda.identityframework.workflow.claims

import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceClaimQueryTests : FlowTest() {

    private lateinit var claim: StateAndRef<CordaClaim<String>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Goodbye, World!")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .finally { claim = it.tx.outRefsOfType<CordaClaim<String>>().single() }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                linearIds(claim.state.data.linearId)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                externalIds(claim.state.data.linearId.externalId!!)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimIssuer(claim.state.data.issuer)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimHolder(claim.state.data.holder)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimProperty(claim.state.data.property)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimValue(claim.state.data.value)
                claimType(CordaClaim::class.java)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value and value type`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimValue(claim.state.data.value)
                claimValueType(String::class.java)
                claimType(CordaClaim::class.java)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value and value type (inline)`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimValue(claim.state.data.value)
                claimValueType<String>()
                claimType(CordaClaim::class.java)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimPreviousStateRef(claim.state.data.previousStateRef)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimIsSelfIssued(claim.state.data.isSelfIssued)
                claimType()
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                claimHash(claim.state.data.hash)
                claimType()
            }

            assertEquals(claim, result)
        }
    }
}
