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

class AccountContractRevokeCommandTests : ContractTest() {

    @Test
    fun `On account revoking, the transaction must include the Revoke command`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                fails()
                command(keysOf(IDENTITY_A), AccountContract.Revoke)
                verifies()
            }
        }
    }

    @Test
    fun `On account revoking, at least one account state must be consumed`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, NotAnAccount)
                command(keysOf(IDENTITY_A), AccountContract.Revoke)
                failsWith(AccountContract.Revoke.CONTRACT_RULE_INPUTS)
            }
        }
    }

    @Test
    fun `On account revoking, zero account states must be created`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                output(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_A), AccountContract.Revoke)
                failsWith(AccountContract.Revoke.CONTRACT_RULE_OUTPUTS)
            }
        }
    }

    @Test
    fun `On account revoking, the owner of each account must sign the transaction`() {
        services.ledger {
            transaction {
                input(AccountContract.ID, ACCOUNT_A)
                command(keysOf(IDENTITY_B), AccountContract.Revoke)
                failsWith(AccountContract.Revoke.CONTRACT_RULE_SIGNERS)
            }
        }
    }
}
