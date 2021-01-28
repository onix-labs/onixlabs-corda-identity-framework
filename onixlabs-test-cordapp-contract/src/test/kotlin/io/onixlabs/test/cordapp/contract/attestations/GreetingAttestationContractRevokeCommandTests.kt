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

package io.onixlabs.test.cordapp.contract.attestations

import io.onixlabs.corda.identityframework.contract.AttestationContract
import io.onixlabs.corda.identityframework.contract.AttestationStatus
import io.onixlabs.test.cordapp.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class GreetingAttestationContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On greeting attestation revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val attestation = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                val issuedAttestation1 = issue(attestation, issuedClaim1)
                input(issuedAttestation1.ref)
                reference(issuedClaim1.ref)
                fails()
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On greeting attestation revoking, only one greeting state must be referenced`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val issuedClaim2 = issue(GREETING_CLAIM)
                val attestation = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                val issuedAttestation1 = issue(attestation, issuedClaim1)
                input(issuedAttestation1.ref)
                reference(issuedClaim1.ref)
                reference(issuedClaim2.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                failsWith(GreetingAttestationContract.Revoke.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On greeting attestation revoking, the attestation pointer must point to the referenced greeting state`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val issuedClaim2 = issue(GREETING_CLAIM)
                val attestation = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                val issuedAttestation1 = issue(attestation, issuedClaim1)
                input(issuedAttestation1.ref)
                reference(issuedClaim2.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                failsWith(GreetingAttestationContract.Revoke.CONTRACT_RULE_POINTER)
            }
        }
    }

    @Test
    fun `On greeting attestation revoking, the attestation status must be ACCEPTED`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val attestation = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.REJECTED)
                val issuedAttestation1 = issue(attestation, issuedClaim1)
                input(issuedAttestation1.ref)
                reference(issuedClaim1.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                failsWith(GreetingAttestationContract.Revoke.CONTRACT_RULE_STATUS)
            }
        }
    }
}
