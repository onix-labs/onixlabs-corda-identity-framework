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

package io.onixlabs.test.cordapp.contract.claims

import io.onixlabs.corda.identityframework.contract.CordaClaimContract
import io.onixlabs.corda.identityframework.contract.amend
import io.onixlabs.test.cordapp.contract.ContractTest
import io.onixlabs.test.cordapp.contract.GREETING_CLAIM
import io.onixlabs.test.cordapp.contract.GreetingClaimContract
import io.onixlabs.test.cordapp.contract.IDENTITY_A
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class GreetingClaimContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On greeting claim revoking, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedClaim = issue(GREETING_CLAIM)
                val amendedClaim = amend(issuedClaim, issuedClaim.amend("Goodbye, World!"))
                input(amendedClaim.ref)
                fails()
                command(keysOf(IDENTITY_A), CordaClaimContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On greeting claim revoking, the input value must be 'Goodbye, World!'`() {
        services.ledger {
            transaction {
                val issuedClaim = issue(GREETING_CLAIM)
                input(issuedClaim.ref)
                command(keysOf(IDENTITY_A), CordaClaimContract.Revoke)
                failsWith(GreetingClaimContract.Revoke.CONTRACT_RULE_INPUT_VALUE)
            }
        }
    }
}
