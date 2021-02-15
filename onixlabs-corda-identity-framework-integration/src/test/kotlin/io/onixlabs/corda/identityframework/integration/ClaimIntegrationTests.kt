/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.integration

import io.onixlabs.corda.core.contract.cast
import io.onixlabs.corda.identityframework.contract.CordaClaim
import net.corda.core.node.services.Vault
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ClaimIntegrationTests : IntegrationTest() {

    @Test
    fun `Claim integration service tests`() = start {

        // Issue a claim
        nodeA.claims.commandService.issueClaim(
            property = "example",
            value = "Hello, World!",
            linearId = ID,
            observers = setOf(partyC)
        ).returnValue.getOrThrow()

        // Find the issued claim
        val issuedClaim = nodeA.claims.queryService.findClaim(
            linearId = ID,
            stateStatus = Vault.StateStatus.UNCONSUMED
        )?.cast<CordaClaim<String>>() ?: fail("Failed to find issued claim.")

        // Amend the claim
        nodeA.claims.commandService.amendClaim(
            claim = issuedClaim,
            value = "Goodbye, World!"
        ).returnValue.getOrThrow()

        // Find the amended claim
        val amendedClaim = nodeA.claims.queryService.findClaim(
            linearId = ID,
            stateStatus = Vault.StateStatus.UNCONSUMED
        )?.cast<CordaClaim<String>>() ?: fail("Failed to find amended claim.")

        // Publish the amended claim
        nodeA.claims.commandService.publishClaim(
            claim = amendedClaim,
            observers = setOf(partyB, partyC)
        ).returnValue.getOrThrow()

        // Find the published claim
        listOf(nodeA, nodeB, nodeC).forEach {
            it.claims.queryService.findClaim(
                linearId = ID,
                stateStatus = Vault.StateStatus.UNCONSUMED
            )?.cast<CordaClaim<String>>() ?: fail("Failed to find sent claim.")
        }

        // Revoke the claim
        nodeA.claims.commandService.revokeClaim(
            claim = amendedClaim,
            observers = setOf(partyB, partyC)
        ).returnValue.getOrThrow()
    }
}
