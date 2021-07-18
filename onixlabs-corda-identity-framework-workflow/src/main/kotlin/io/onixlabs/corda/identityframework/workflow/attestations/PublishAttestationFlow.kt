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

package io.onixlabs.corda.identityframework.workflow.attestations

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.workflow.InitializeFlowStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.findTransaction
import io.onixlabs.corda.core.workflow.initiateFlows
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.workflow.SendAttestationTransactionStep
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for publishing an attestation.
 *
 * @param attestation The attestation to publish to the specified counter-parties.
 * @param sessions The flow sessions for the counter-parties who will receive the attestation.
 * @param progressTracker The progress tracker which tracks the progress of this flow.
 */
class PublishAttestationFlow(
    private val attestation: StateAndRef<Attestation<*>>,
    private val sessions: Set<FlowSession>,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(InitializeFlowStep, SendAttestationTransactionStep)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        val transaction = findTransaction(attestation)

        currentStep(SendAttestationTransactionStep)
        sessions.forEach { subFlow(SendTransactionFlow(it, transaction)) }

        return transaction
    }

    /**
     * Represents the initiating flow for publishing an attestation.
     *
     * @param attestation The attestation to publish to the specified counter-parties.
     * @param observers The counter-parties who will observer the attestation.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(FLOW_VERSION_1)
    class Initiator(
        private val attestation: StateAndRef<Attestation<*>>,
        private val observers: Set<Party>
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object PublishAttestationTransactionStep : Step("Publishing attestation transaction.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(PublishAttestationTransactionStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(PublishAttestationTransactionStep)
            return subFlow(
                PublishAttestationFlow(
                    attestation,
                    initiateFlows(observers),
                    PublishAttestationTransactionStep.childProgressTracker()
                )
            )
        }
    }
}
