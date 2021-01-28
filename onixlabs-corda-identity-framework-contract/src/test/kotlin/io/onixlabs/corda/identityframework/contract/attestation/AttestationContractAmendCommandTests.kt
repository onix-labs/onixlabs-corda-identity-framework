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

package io.onixlabs.corda.identityframework.contract.attestation

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class AttestationContractAmendCommandTests : ContractTest() {

    @Test
    fun `On attestation amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                fails()
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On attestation amending, only one attestation state must be consumed`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val issuedAttestation2 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                input(issuedAttestation2.ref)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On attestation amending, only one attestation state must be created`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On attestation amending, the attestor, linear ID, pointer class and pointer linear ID must not change (wrong attestor)`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.withInvalidAttestor(IDENTITY_B.party)
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_CHANGES)
            }
        }
    }

    @Test
    fun `On attestation amending, the attestor, linear ID, pointer class and pointer linear ID must not change (wrong pointer)`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.withInvalidPointer()
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_CHANGES)
            }
        }
    }

    @Test
    fun `On attestation amending, the created attestation state must point to the consumed attestation state`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.withInvalidPreviousStateRef()
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_STATE_REF)
            }
        }
    }

    @Test
    fun `On attestation amending, the attestor must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedAttestation1 = issue(issuedClaim1.accept(IDENTITY_C.party))
                val amendedAttestation1 = issuedAttestation1.reject()
                input(issuedAttestation1.ref)
                output(AttestationContract.ID, amendedAttestation1)
                command(keysOf(IDENTITY_B), AttestationContract.Amend)
                failsWith(AttestationContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
