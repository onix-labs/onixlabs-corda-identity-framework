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

package io.onixlabs.corda.identityframework.contract.accounts

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class AccountContractIssueCommandTests : ContractTest() {

    @Test
    fun `On account issuing, the transaction must include the Issue command`() {
        services.ledger {
            transaction {
                output(AccountContract.ID, ACCOUNT_A)
                fails()
                command(keysOf(IDENTITY_A), AccountContract.Issue)
                verifies()
            }
        }
    }

    @Test
    fun `On account issuing, zero account states must be consumed`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_A), AccountContract.Issue)
                failsWith(AccountContract.Issue.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On account issuing, at least one account state must be created`() {
        services.ledger {
            transaction {
                output(AccountContract.ID, NotAnAccount)
                command(keysOf(IDENTITY_A), AccountContract.Issue)
                failsWith(AccountContract.Issue.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On account issuing, the owner of each created account must be a participant`() {
        services.ledger {
            transaction {
                output(AccountContract.ID, AccountOwnerIsNotParticipant)
                command(keysOf(IDENTITY_A), AccountContract.Issue)
                failsWith(AccountContract.Issue.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On account issuing, the owner of each account must sign the transaction`() {
        services.ledger {
            transaction {
                output(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_B), AccountContract.Issue)
                failsWith(AccountContract.Issue.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
