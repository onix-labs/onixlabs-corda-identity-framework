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

package io.onixlabs.corda.identityframework.workflow.claims

import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema.CordaClaimEntity
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.AmendClaimFlow
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.Pipeline
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindClaimFlowTests : FlowTest() {

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
    fun `FindClaimFlow should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                linearIds(claim.state.data.linearId)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::externalId equalTo claim.state.data.linearId.externalId)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::issuer equalTo claim.state.data.issuer)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::holder equalTo claim.state.data.holder)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::property equalTo claim.state.data.property)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::value equalTo claim.state.data.value)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::previousStateRef equalTo claim.state.data.previousStateRef.toString())
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::isSelfIssued equalTo claim.state.data.isSelfIssued)
            }

            assertEquals(claim, result)
        }
    }

    @Test
    fun `FindClaimFlow should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val result = it.services.vaultServiceFor<CordaClaim<String>>().singleOrNull {
                stateStatus(Vault.StateStatus.UNCONSUMED)
                expression(CordaClaimEntity::hash equalTo claim.state.data.hash.toString())
            }

            assertEquals(claim, result)
        }
    }
}
