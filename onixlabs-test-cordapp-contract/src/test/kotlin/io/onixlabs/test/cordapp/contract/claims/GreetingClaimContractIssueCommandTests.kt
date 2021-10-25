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

import io.onixlabs.corda.identityframework.contract.claims.CordaClaimContract
import io.onixlabs.test.cordapp.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class GreetingClaimContractIssueCommandTests : ContractTest() {

    @Test
    fun `On greeting claim issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(GreetingClaimContract.ID, GREETING_CLAIM)
                fails()
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On greeting claim issuing, the property must be 'greeting'`() {
        services.ledger {
            transaction {
                output(GreetingClaimContract.ID, GREETING_CLAIM_INVALID_PROPERTY)
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(GreetingClaimContract.Issue.CONTRACT_RULE_OUTPUT_PROPERTY)
            }
        }
    }

    @Test
    fun `On greeting claim issuing, the value must be 'Hello, World!'`() {
        services.ledger {
            transaction {
                output(GreetingClaimContract.ID, GREETING_CLAIM_INVALID_VALUE)
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(GreetingClaimContract.Issue.CONTRACT_RULE_OUTPUT_VALUE)
            }
        }
    }
}
