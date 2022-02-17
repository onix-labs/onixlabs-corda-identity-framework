/*
 * Copyright 2020-2022 ONIXLabs
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

package io.onixlabs.corda.identityframework.contract.claims

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class CordaClaimContractIssueCommandTests : ContractTest() {

    @Test
    fun `On claim issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(CordaClaimContract.ID, CLAIM_1)
                fails()
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On claim issuing, zero claim states must be consumed`() {
        services.ledger {
            transaction {
                input(CordaClaimContract.ID, CLAIM_1)
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(CordaClaimContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On claim issuing, only one claim state must be created`() {
        services.ledger {
            transaction {
                output(CordaClaimContract.ID, CLAIM_1)
                output(CordaClaimContract.ID, CLAIM_1)
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(CordaClaimContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On claim issuing, the issuer of the created claim state must be a participant`() {
        services.ledger {
            transaction {
                output(CordaClaimContract.ID, CustomCordaClaim().withoutIssuer())
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(CordaClaimContract.Issue.CONTRACT_RULE_ISSUER_PARTICIPANT)
            }
        }
    }

    @Test
    fun `On claim issuing, the holder of the created claim state must be a participant`() {
        services.ledger {
            transaction {
                output(CordaClaimContract.ID, CustomCordaClaim().withoutHolder())
                command(keysOf(IDENTITY_A), CordaClaimContract.Issue)
                failsWith(CordaClaimContract.Issue.CONTRACT_RULE_HOLDER_PARTICIPANT)
            }
        }
    }

    @Test
    fun `On claim issuing, the issuer must sign the transaction`() {
        services.ledger {
            transaction {
                output(CordaClaimContract.ID, CLAIM_1)
                command(keysOf(IDENTITY_B), CordaClaimContract.Issue)
                failsWith(CordaClaimContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
