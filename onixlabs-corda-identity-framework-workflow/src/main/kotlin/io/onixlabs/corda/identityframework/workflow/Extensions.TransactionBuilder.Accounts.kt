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

package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountContract
import net.corda.core.contracts.StateAndRef

/**
 * Adds an account for issuance to a transaction builder, including the required command.
 *
 * @param state The account state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addIssuedAccount(state: Account): Builder = apply {
    addOutputState(state)
    addCommand(AccountContract.Issue, state.owner.owningKey)
}

/**
 * Adds an account for amendment to a transaction builder, including the required command.
 *
 * @param oldState The old account state to be consumed in the transaction.
 * @param newState The new account state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addAmendedAccount(oldState: StateAndRef<Account>, newState: Account): Builder = apply {
    addInputState(oldState)
    addOutputState(newState)
    addCommand(AccountContract.Amend, newState.owner.owningKey)
}

/**
 * Adds an account for revocation to a transaction builder, including the required command.
 *
 * @param state The account state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addRevokedAccount(state: StateAndRef<Account>): Builder = apply {
    addInputState(state)
    addCommand(AccountContract.Revoke, state.state.data.owner.owningKey)
}
