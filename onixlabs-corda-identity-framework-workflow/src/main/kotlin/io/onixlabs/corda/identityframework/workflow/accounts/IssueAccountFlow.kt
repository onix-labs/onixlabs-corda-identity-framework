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
import io.onixlabs.corda.identityframework.workflow.addIssuedAccount
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for issuing an account.
 *
 * @property account The account to be issued.
 * @property notary The notary to use for the transaction.
 * @property sessions The sessions required for account counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class IssueAccountFlow(
    private val account: Account,
    private val notary: Party,
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

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        checkSufficientSessions(sessions, account)

        val transaction = buildTransaction(notary) {
            addIssuedAccount(account)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for issuing an account.
     *
     * @property account The account to be issued.
     * @property notary The notary to use for the transaction.
     * @property observers The additional observers of the account.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val account: Account,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object IssueAccountStep : Step("Issuing account.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(IssueAccountStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(IssueAccountStep)
            val sessions = initiateFlows(observers, account)
            return subFlow(
                IssueAccountFlow(
                    account,
                    notary ?: getPreferredNotary(),
                    sessions,
                    IssueAccountStep.childProgressTracker()
                )
            )
        }
    }
}
