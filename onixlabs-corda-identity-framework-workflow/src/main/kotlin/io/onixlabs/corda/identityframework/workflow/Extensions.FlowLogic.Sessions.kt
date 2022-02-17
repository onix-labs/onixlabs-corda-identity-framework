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

package io.onixlabs.corda.identityframework.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.workflow.checkSufficientSessionsForContractStates
import io.onixlabs.corda.core.workflow.checkSufficientSessionsForTransactionBuilder
import io.onixlabs.corda.identityframework.contract.accounts.AccountParty
import net.corda.core.contracts.ContractState
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.AbstractParty
import net.corda.core.transactions.TransactionBuilder

/**
 * Checks that sufficient flow sessions have been provided for the account identities of the specified states.
 *
 * @param sessions The flow sessions that have been provided to the flow.
 * @param states The states that will be used as input or output states in a transaction.
 * @throws FlowException if a required counter-party session is missing for a state participant.
 */
@Suspendable
fun FlowLogic<*>.checkSufficientSessionsForAccounts(
    sessions: Iterable<FlowSession>,
    states: Iterable<ContractState>
) = checkSufficientSessionsForContractStates(sessions, states) { it.getAccountOwningPartyOrThis() }

/**
 * Checks that sufficient flow sessions have been provided for the account identities of the specified states.
 *
 * @param sessions The flow sessions that have been provided to the flow.
 * @param states The states that will be used as input or output states in a transaction.
 * @throws FlowException if a required counter-party session is missing for a state participant.
 */
@Suspendable
fun FlowLogic<*>.checkSufficientSessionsForAccounts(
    sessions: Iterable<FlowSession>,
    vararg states: ContractState
) = checkSufficientSessionsForContractStates(sessions, *states) { it.getAccountOwningPartyOrThis() }

/**
 * Checks that sufficient flow sessions have been provided for the specified transaction.
 *
 * @param sessions The flow sessions that have been provided to the flow.
 * @param transaction The transaction for which to check that sufficient flow sessions exist.
 * @throws FlowException if a required counter-party session is missing for a state participant.
 */
@Suspendable
fun FlowLogic<*>.checkSufficientSessionsForTransactionBuilder(
    sessions: Iterable<FlowSession>,
    transaction: TransactionBuilder
) = checkSufficientSessionsForTransactionBuilder(sessions, transaction) { it.getAccountOwningPartyOrThis() }

/**
 * Gets the underlying account owner identity, or returns the current identity if it is not an [AccountParty].
 */
private fun AbstractParty.getAccountOwningPartyOrThis(): AbstractParty {
    return if (this is AccountParty) owner else this
}
