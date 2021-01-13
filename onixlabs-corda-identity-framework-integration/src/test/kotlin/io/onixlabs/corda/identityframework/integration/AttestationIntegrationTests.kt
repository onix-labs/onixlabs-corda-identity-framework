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

import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import io.onixlabs.corda.identityframework.contract.CordaClaim
import net.corda.core.node.services.Vault
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class AttestationIntegrationTests : IntegrationTest() {

    @Test
    fun `Attestation integration service tests`() = start {

        // Issue a claim
        nodeA.claims.commandService.issueClaim(
            property = "example",
            value = "Hello, World!",
            linearId = ID,
            observers = setOf(partyC)
        ).returnValue.getOrThrow()

        // Find the issued claim
        val issuedClaim = nodeC.claims.queryService.findClaim<CordaClaim<String>>(
            linearId = ID,
            stateStatus = Vault.StateStatus.UNCONSUMED
        ) ?: fail("Failed to find issued claim.")

        // Issue an attestation
        nodeC.attestations.commandService.issueAttestation(
            state = issuedClaim
        ).returnValue.getOrThrow()

        // Find the issued attestation
        val issuedAttestation = nodeC.attestations.queryService.findAttestation<Attestation<CordaClaim<String>>>(
            pointerStateRef = issuedClaim.ref,
            stateStatus = Vault.StateStatus.UNCONSUMED
        ) ?: fail("Failed to find issued attestation.")

        // Amend the issued attestation
        nodeC.attestations.commandService.amendAttestation(
            attestation = issuedAttestation,
            state = issuedClaim,
            status = AttestationStatus.ACCEPTED
        ).returnValue.getOrThrow()

        // Find the amended attestation
        val amendedAttestation = nodeA.attestations.queryService.findAttestation<Attestation<CordaClaim<String>>>(
            pointerStateRef = issuedClaim.ref,
            stateStatus = Vault.StateStatus.UNCONSUMED
        ) ?: fail("Failed to find amended attestation.")

        // Publish the amended attestation
        nodeA.attestations.commandService.publishAttestation(
            attestation = amendedAttestation,
            observers = setOf(partyB)
        ).returnValue.getOrThrow()

        // Find the published attestation
        listOf(nodeA, nodeB, nodeC).forEach {
            it.attestations.queryService.findAttestation<Attestation<CordaClaim<String>>>(
                pointerStateRef = issuedClaim.ref,
                stateStatus = Vault.StateStatus.UNCONSUMED
            ) ?: fail("Failed to find published attestation.")
        }

        // Revoke the attestation
        nodeC.attestations.commandService.revokeAttestation(
            attestation = amendedAttestation,
            observers = setOf(partyB)
        ).returnValue.getOrThrow()
    }
}
