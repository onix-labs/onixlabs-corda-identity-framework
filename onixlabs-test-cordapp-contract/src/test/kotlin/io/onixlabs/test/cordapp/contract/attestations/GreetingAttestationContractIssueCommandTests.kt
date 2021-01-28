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

import io.onixlabs.corda.identityframework.v1.contract.AttestationContract
import io.onixlabs.corda.identityframework.v1.contract.AttestationStatus
import io.onixlabs.test.cordapp.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class GreetingAttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On greeting attestation issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val attestation1 = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                output(GreetingAttestationContract.ID, attestation1)
                reference(issuedClaim1.ref)
                fails()
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On greeting attestation issuing, only one greeting state must be referenced`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val issuedClaim2 = issue(GREETING_CLAIM)
                val attestation1 = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                output(GreetingAttestationContract.ID, attestation1)
                reference(issuedClaim1.ref)
                reference(issuedClaim2.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                failsWith(GreetingAttestationContract.Issue.CONTRACT_RULE_REFERENCES)
            }
        }
    }

    @Test
    fun `On greeting attestation issuing, the attestation pointer must point to the referenced greeting state`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(GREETING_CLAIM)
                val issuedClaim2 = issue(GREETING_CLAIM)
                val attestation1 = GreetingAttestation(IDENTITY_C.party, issuedClaim1, AttestationStatus.ACCEPTED)
                output(GreetingAttestationContract.ID, attestation1)
                reference(issuedClaim2.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                failsWith(GreetingAttestationContract.Issue.CONTRACT_RULE_POINTER)
            }
        }
    }
}
