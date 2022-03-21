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

package io.onixlabs.corda.identityframework.contract.accounts

import io.onixlabs.corda.identityframework.contract.*
import net.corda.testing.node.ledger
import org.junit.jupiter.api.Test

class AccountContractAmendCommandTests : ContractTest() {

    @Test
    fun `On account amending, the transaction must include the Amend command`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                output(AccountContract.ID, ACCOUNT_A)
                fails()
                command(keysOf(IDENTITY_A), AccountContract.Amend)
                verifies()
            }
        }
    }

    @Test
    fun `On account amending, at least one account state must be consumed`() {
        services.ledger {
            transaction {
                output(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_A), AccountContract.Amend)
                failsWith(AccountContract.Amend.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On account amending, at least one account state must be created`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_A), AccountContract.Amend)
                failsWith(AccountContract.Amend.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On account amending, the owner of each created account must be a participant`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                output(AccountContract.ID, AccountOwnerIsNotParticipant)
                command(keysOf(IDENTITY_A), AccountContract.Amend)
                failsWith(AccountContract.Amend.CONTRACT_RULE_PARTICIPANTS)
            }
        }
    }

    @Test
    fun `On account amending, the owner of each account must sign the transaction`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                output(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_B), AccountContract.Amend)
                failsWith(AccountContract.Amend.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
