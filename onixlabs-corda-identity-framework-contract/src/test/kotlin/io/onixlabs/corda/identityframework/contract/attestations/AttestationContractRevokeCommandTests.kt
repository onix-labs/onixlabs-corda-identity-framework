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

package io.onixlabs.corda.identityframework.contract.attestations

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class AttestationContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On attestation revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.createAcceptedLinearAttestation(IDENTITY_C.party))
                input(issuedAttestation1.ref)
                fails()
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On evolvable attestation revoking, only one attestation state must be consumed`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.createAcceptedLinearAttestation(IDENTITY_C.party))
                val issuedAttestation2 = issue(issuedClaim1.createAcceptedLinearAttestation(IDENTITY_C.party))
                input(issuedAttestation1.ref)
                input(issuedAttestation2.ref)
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                failsWith(AttestationContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On attestation revoking, zero attestation states must be created`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.createAcceptedLinearAttestation(IDENTITY_C.party))
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, issuedAttestation1.state.data)
                command(keysOf(IDENTITY_C), AttestationContract.Revoke)
                failsWith(AttestationContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On attestation revoking, the attestor must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.createAcceptedLinearAttestation(IDENTITY_C.party))
                input(issuedAttestation1.ref)
                command(keysOf(IDENTITY_A), AttestationContract.Revoke)
                failsWith(AttestationContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
