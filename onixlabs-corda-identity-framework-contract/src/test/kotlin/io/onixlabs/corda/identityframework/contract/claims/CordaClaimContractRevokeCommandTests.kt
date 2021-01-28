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

package io.onixlabs.corda.identityframework.contract.claims

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class CordaClaimContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On claim revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                fails()
                command(keysOf(IDENTITY_A), CordaClaimContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On claim revoking, only one claim state must be consumed`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedClaim2 = issue(CLAIM_2)
                input(issuedClaim1.ref)
                input(issuedClaim2.ref)
                command(keysOf(IDENTITY_A), CordaClaimContract.Revoke)
                failsWith(CordaClaimContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On claim revoking, zero claim states must be created`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, CLAIM_1)
                command(keysOf(IDENTITY_A), CordaClaimContract.Revoke)
                failsWith(CordaClaimContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On claim revoking, the issuer must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                command(keysOf(IDENTITY_B), CordaClaimContract.Revoke)
                failsWith(CordaClaimContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
