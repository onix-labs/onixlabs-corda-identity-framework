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

package io.onixlabs.corda.identityframework.workflow.claims

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.workflow.*
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.workflow.SendClaimTransactionStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for publishing a claim.
 *
 * @param claim The claim to publish to the specified counter-parties.
 * @param sessions The flow sessions for the counter-parties who will receive the attestation.
 * @param progressTracker The progress tracker which tracks the progress of this flow.
 */
class PublishClaimFlow(
    private val claim: StateAndRef<CordaClaim<*>>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(InitializeFlowStep, SendClaimTransactionStep)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        val transaction = findTransaction(claim)
        return publishTransaction(transaction, sessions, SendClaimTransactionStep)
    }

    /**
     * Represents the initiating flow for publishing a claim.
     *
     * @param claim The claim to publish to the specified counter-parties.
     * @param observers The counter-parties who will observer the attestation.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(FLOW_VERSION_1)
    class Initiator(
        private val claim: StateAndRef<CordaClaim<*>>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PublishClaimStep : Step("Publishing claim.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PublishClaimStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PublishClaimStep)
            return subFlow(
                PublishClaimFlow(
                    claim,
                    initiateFlows(observers),
                    PublishClaimStep.childProgressTracker()
                )
            )
        }
    }
}
