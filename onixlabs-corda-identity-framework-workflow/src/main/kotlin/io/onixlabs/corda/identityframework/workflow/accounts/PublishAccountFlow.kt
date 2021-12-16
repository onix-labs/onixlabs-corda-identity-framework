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
import io.onixlabs.corda.identityframework.workflow.SendAccountTransactionStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for publishing an account.
 *
 * @param account The account to publish to the specified counter-parties.
 * @param sessions The flow sessions for the counter-parties who will receive the account.
 * @param progressTracker The progress tracker which tracks the progress of this flow.
 */
class PublishAccountFlow(
    private val account: StateAndRef<Account>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(
            InitializeFlowStep,
            SendAccountTransactionStep
        )
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        val transaction = findTransaction(account)
        return publishTransaction(transaction, sessions, SendAccountTransactionStep)
    }

    /**
     * Represents the initiating flow for publishing an account.
     *
     * @param account The account to publish to the specified counter-parties.
     * @param observers The counter-parties who will observer the account.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val account: StateAndRef<Account>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PublishAccountStep : Step("Publishing account.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PublishAccountStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PublishAccountStep)
            return subFlow(
                PublishAccountFlow(
                    account,
                    initiateFlows(observers),
                    PublishAccountStep.childProgressTracker()
                )
            )
        }
    }
}
