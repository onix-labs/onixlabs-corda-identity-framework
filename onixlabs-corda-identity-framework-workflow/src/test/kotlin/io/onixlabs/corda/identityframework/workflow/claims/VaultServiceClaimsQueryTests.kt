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

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.corda.identityframework.workflow.*
import net.corda.core.contracts.StateRef
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals

class VaultServiceClaimsQueryTests : FlowTest() {

    private var previousStateRef: StateRef? = null

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Amended Value 1")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .run(nodeA) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<String>>().single()
                val newClaim = oldClaim.amend("Amended Value 2")
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyC))
            }
            .run(nodeB) {
                IssueClaimFlow.Initiator(CLAIM_2, observers = setOf(partyA, partyC))
            }
            .run(nodeB) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Int>>().single()
                val newClaim = oldClaim.amend(456)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyC))
            }
            .run(nodeB) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Int>>().single()
                val newClaim = oldClaim.amend(789)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyC))
            }
            .run(nodeC) {
                IssueClaimFlow.Initiator(CLAIM_3, observers = setOf(partyA, partyB))
            }
            .run(nodeC) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Instant>>().single()
                val newClaim = oldClaim.amend(Instant.MIN)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyB))
            }
            .run(nodeC) {
                val oldClaim = it.tx.outRefsOfType<CordaClaim<Instant>>().single()
                val newClaim = oldClaim.amend(Instant.MAX)
                AmendClaimFlow.Initiator(oldClaim, newClaim, observers = setOf(partyA, partyB))
            }
            .finally {
                previousStateRef = it
                    .tx.outRefsOfType<CordaClaim<Instant>>()
                    .single().state.data.previousStateRef
            }
    }

    @Test
    fun `VaultService should find the expected claim by linear ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                linearIds(CLAIM_1.linearId)
                claimType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by external ID`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                externalIds(CLAIM_1.linearId.externalId)
                claimType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by issuer`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimIssuer(CLAIM_1.issuer)
                claimType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by holder`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimHolder(CLAIM_1.holder)
                claimType()
            }

            assertEquals(6, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by property`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimProperty(CLAIM_1.property)
                claimType()
            }

            assertEquals(3, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by value`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimValue(CLAIM_1.value)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by previousStateRef`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimPreviousStateRef(previousStateRef)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by isSelfIssued`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimIsSelfIssued()
                claimType()
            }

            assertEquals(6, results.count())
        }
    }

    @Test
    fun `VaultService equalTo should find the expected claim by hash`() {
        listOf(nodeA, nodeB, nodeC).forEach {
            val results = it.services.vaultServiceFor<CordaClaim<*>>().filter {
                stateStatus(Vault.StateStatus.ALL)
                claimHash(CLAIM_1.hash)
                claimType()
            }

            assertEquals(1, results.count())
        }
    }
}
