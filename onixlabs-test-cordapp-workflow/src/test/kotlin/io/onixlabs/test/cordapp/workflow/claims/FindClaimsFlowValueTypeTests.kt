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

package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.filter
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import net.corda.core.node.services.Vault
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindClaimsFlowValueTypeTests : FlowTest() {

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
    fun `FindClaimsFlow should only return only the claims where the claim type is specified`() {
        val results = nodeA.services.vaultServiceFor<CordaClaim<String>>().filter {
            stateStatus(Vault.StateStatus.ALL)
            expression(CordaClaimSchema.CordaClaimEntity::property equalTo "greeting")
            expression(CordaClaimSchema.CordaClaimEntity::claimType equalTo CordaClaim::class.java.canonicalName)
        }

        assertEquals(3, results.count())
    }

    @Test
    fun `FindClaimsFlow should the correct claim where the claim value type is String`() {
        val results = nodeA.services.vaultServiceFor<CordaClaim<*>>().filter {
            stateStatus(Vault.StateStatus.ALL)
            expression(CordaClaimSchema.CordaClaimEntity::property equalTo "greeting")
            expression(CordaClaimSchema.CordaClaimEntity::claimType equalTo CordaClaim::class.java.canonicalName)
            expression(CordaClaimSchema.CordaClaimEntity::valueType equalTo String::class.java.canonicalName)
        }

        assertEquals(1, results.count())
        assertEquals("abc", results.single().state.data.value)
    }

    @Test
    fun `FindClaimsFlow should the correct claim where the claim type is GreetingClaim`() {
        val results = nodeA.services.vaultServiceFor<GreetingClaim>().filter {
            stateStatus(Vault.StateStatus.ALL)
            expression(CordaClaimSchema.CordaClaimEntity::property equalTo "greeting")
        }

        assertEquals(1, results.count())
        assertEquals("Hello, World!", results.single().state.data.value)
    }
}
