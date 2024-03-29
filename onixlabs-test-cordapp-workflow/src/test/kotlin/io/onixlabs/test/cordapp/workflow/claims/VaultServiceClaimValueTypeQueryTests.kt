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

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import io.onixlabs.corda.identityframework.workflow.claimProperty
import io.onixlabs.corda.identityframework.workflow.claimType
import io.onixlabs.corda.identityframework.workflow.claimValueType
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VaultServiceClaimValueTypeQueryTests : FlowTest() {

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                val claim = CordaClaim(partyA, partyB, "greeting", "abc")
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = GREETING_CLAIM
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = CordaClaim(partyA, "greeting", 12345)
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = CordaClaim(partyA, "greeting", false)
                IssueClaimFlow.Initiator(claim)
            }
    }

    @Test
    fun `VaultService equalTo should only return only the claims where the claim type is specified`() {
        val results = nodeA.services.vaultServiceFor<CordaClaim<String>>().filter {
            stateStatus(Vault.StateStatus.ALL)
            claimProperty("greeting")
            claimType(CordaClaim::class.java)
        }

        assertEquals(3, results.count())
    }

    @Test
    fun `VaultService equalTo should the correct claim where the claim value type is String`() {
        val results = nodeA.services.vaultServiceFor<CordaClaim<*>>().filter {
            stateStatus(Vault.StateStatus.ALL)
            claimProperty("greeting")
            claimType(CordaClaim::class.java)
            claimValueType(String::class.java)
        }

        assertEquals(1, results.count())
        assertEquals("abc", results.single().state.data.value)
    }

    @Test
    fun `VaultService equalTo should the correct claim where the claim type is GreetingClaim`() {
        val results = nodeA.services.vaultServiceFor<GreetingClaim>().filter {
            stateStatus(Vault.StateStatus.ALL)
            claimProperty("greeting")
        }

        assertEquals(1, results.count())
        assertEquals("Hello, World!", results.single().state.data.value)
    }
}
