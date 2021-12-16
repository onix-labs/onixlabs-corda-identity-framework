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

package io.onixlabs.corda.identityframework.workflow.accounts

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.workflow.*
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.workflow.FLOW_VERSION_1
import io.onixlabs.corda.identityframework.workflow.addRevokedAccount
import io.onixlabs.corda.identityframework.workflow.checkSufficientSessionsWithAccounts
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for revoking an account.
 *
 * @property account The account to be revoked.
 * @property sessions The sessions required for account counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class RevokeAccountFlow(
    private val account: StateAndRef<Account>,
    private val sessions: Set<FlowSession> = emptySet(),
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            BuildTransactionStep,
            VerifyTransactionStep,
            SignTransactionStep,
            SendStatesToRecordStep,
            FinalizeTransactionStep
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        checkSufficientSessionsWithAccounts(sessions, account.state.data)

        val transaction = buildTransaction(account.state.notary) {
            addRevokedAccount(account)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for revoking an account.
     *
     * @property account The account to be revoked.
     * @property observers The additional observers of the account.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val account: StateAndRef<Account>,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object RevokeAccountStep : Step("Revoking account.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(RevokeAccountStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(RevokeAccountStep)
            val sessions = initiateFlows(observers, account.state.data)
            return subFlow(RevokeAccountFlow(account, sessions, RevokeAccountStep.childProgressTracker()))
        }
    }
}
