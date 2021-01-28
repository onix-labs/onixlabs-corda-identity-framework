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

package io.onixlabs.corda.identityframework.v1.contract.claims

import io.onixlabs.corda.identityframework.v1.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class CordaClaimContractAmendCommandTests : ContractTest() {

    @Test
    fun `On claim amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, issuedClaim1.amend("Amended Value"))
                fails()
                command(keysOf(IDENTITY_A), CordaClaimContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On claim amending, only one claim state must be consumed`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val issuedClaim2 = issue(CLAIM_2)
                input(issuedClaim1.ref)
                input(issuedClaim2.ref)
                command(keysOf(IDENTITY_A), CordaClaimContract.Amend)
                failsWith(CordaClaimContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On claim amending, only one claim state must be created`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, issuedClaim1.amend("Amended Value"))
                output(CordaClaimContract.ID, issuedClaim1.amend("Amended Value"))
                command(keysOf(IDENTITY_A), CordaClaimContract.Amend)
                failsWith(CordaClaimContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On claim amending, the created claim state must point to the consumed claim state`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, issuedClaim1.withInvalidStateRef("Amended Value"))
                command(keysOf(IDENTITY_A), CordaClaimContract.Amend)
                failsWith(CordaClaimContract.Amend.CONTRACT_RULE_STATE_REF)
            }
        }
    }

    @Test
    fun `On claim amending, the issuer, holder, property, and linear ID must not change`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                val amendedClaim1 = CLAIM_2.amend(issuedClaim1.ref, 123)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, amendedClaim1)
                command(keysOf(IDENTITY_A), CordaClaimContract.Amend)
                failsWith(CordaClaimContract.Amend.CONTRACT_RULE_CHANGES)
            }
        }
    }

    @Test
    fun `On claim amending, the issuer must sign the transaction`() {
        services.ledger {
            transaction {
                val issuedClaim1 = issue(CLAIM_1)
                input(issuedClaim1.ref)
                output(CordaClaimContract.ID, issuedClaim1.amend("Amended Value"))
                command(keysOf(IDENTITY_B), CordaClaimContract.Amend)
                failsWith(CordaClaimContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
