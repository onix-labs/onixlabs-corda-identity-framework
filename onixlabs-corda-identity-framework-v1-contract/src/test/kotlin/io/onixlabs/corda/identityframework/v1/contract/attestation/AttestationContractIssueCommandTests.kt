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

package io.onixlabs.corda.identityframework.v1.contract.attestation

import io.onixlabs.corda.identityframework.v1.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class AttestationContractIssueCommandTests : ContractTest() {

    @Test
    fun `On attestation issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val attestation1 = issuedClaim1.accept(IDENTITY_C.party)
                output(AttestationContract.ID, attestation1)
                fails()
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On attestation issuing, zero attestation states must be consumed`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val attestation1 = issuedClaim1.accept(IDENTITY_C.party)
                input(AttestationContract.ID, attestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                failsWith(AttestationContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On attestation issuing, only one attestation state must be created`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val attestation1 = issuedClaim1.accept(IDENTITY_C.party)
                output(AttestationContract.ID, attestation1)
                output(AttestationContract.ID, attestation1)
                command(keysOf(IDENTITY_C), AttestationContract.Issue)
                failsWith(AttestationContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On attestation issuing, the attestor must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val attestation1 = issuedClaim1.accept(IDENTITY_C.party)
                output(AttestationContract.ID, attestation1)
                command(keysOf(IDENTITY_A), AttestationContract.Issue)
                failsWith(AttestationContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
