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

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder

/**
 * Adds an account for issuance to a transaction builder, including the required command.
 *
 * @param account The account state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
@Suspendable
fun TransactionBuilder.addIssuedAccount(
    account: Account
): TransactionBuilder = apply {
    addOutputState(account)
    addCommand(AccountContract.Issue, account.owner.owningKey)
}

/**
 * Adds an account for amendment to a transaction builder, including the required command.
 *
 * @param oldAccount The old account state to be consumed in the transaction.
 * @param newAccount The new account state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
@Suspendable
fun TransactionBuilder.addAmendedAccount(
    oldAccount: StateAndRef<Account>,
    newAccount: Account
): TransactionBuilder = apply {
    addInputState(oldAccount)
    addOutputState(newAccount)
    addCommand(AccountContract.Amend, newAccount.owner.owningKey)
}

/**
 * Adds an account for revocation to a transaction builder, including the required command.
 *
 * @param account The account state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
@Suspendable
fun TransactionBuilder.addRevokedAccount(
    account: StateAndRef<Account>
): TransactionBuilder = apply {
    addInputState(account)
    addCommand(AccountContract.Revoke, account.state.data.owner.owningKey)
}
