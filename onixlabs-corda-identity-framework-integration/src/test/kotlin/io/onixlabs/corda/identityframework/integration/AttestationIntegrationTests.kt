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

package io.onixlabs.corda.identityframework.integration

import io.onixlabs.corda.core.services.singleOrNull
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.workflow.attestationPointer
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class AttestationIntegrationTests : IntegrationTest() {

    @Test
    fun `Attestation integration service tests`() = start {

        // Issue a claim
        nodeA.claimService.issueClaim(
            property = "example",
            value = "Hello, World!",
            linearId = ID,
            observers = setOf(partyC)
        ).returnValue.getOrThrow()

        // Find the issued claim
        val issuedClaim = nodeC.rpc.vaultServiceFor<CordaClaim<String>>().singleOrNull {
            linearIds(ID)
        } ?: fail("Failed to find issued claim.")

        // Issue an attestation
        nodeC.attestationService.issueStaticAttestation(
            state = issuedClaim
        ).returnValue.getOrThrow()

        // Find the issued attestation
        val issuedAttestation = nodeC.rpc.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
            attestationPointer(issuedClaim.ref)
        } ?: fail("Failed to find issued attestation.")

        // Amend the issued attestation
        nodeC.attestationService.amendStaticAttestation(
            oldAttestation = issuedAttestation,
            state = issuedClaim,
            status = AttestationStatus.ACCEPTED
        ).returnValue.getOrThrow()

        // Find the amended attestation
        val amendedAttestation = nodeC.rpc.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
            attestationPointer(issuedClaim.ref)
        } ?: fail("Failed to find amended attestation.")

        // Publish the amended attestation
        val tx = nodeA.attestationService.publishAttestation(
            attestation = amendedAttestation,
            observers = setOf(partyB)
        ).returnValue.getOrThrow()

        // Find the published attestation
        listOf(nodeA, nodeB, nodeC).forEach {
            it.waitForTransaction(tx.id)
            it.rpc.vaultServiceFor<Attestation<CordaClaim<String>>>().singleOrNull {
                attestationPointer(issuedClaim.ref)
            } ?: fail("Failed to find published attestation.")
        }

        // Revoke the attestation
        nodeC.attestationService.revokeAttestation(
            attestation = amendedAttestation,
            observers = setOf(partyB)
        ).returnValue.getOrThrow()
    }
}
