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
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.workflow.IssueAttestationFlow.Initiator
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

/**
 * Represents the flow handler for issuing attestations.
 *
 * @property session The counter-party session who is initiating the flow.
 * @property expectedTransactionId The expected ID of the transaction.
 * @property statesToRecord Determines which transaction states to record.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class IssueAttestationFlowHandler(
    private val session: FlowSession,
    private val expectedTransactionId: SecureHash? = null,
    private val statesToRecord: StatesToRecord = StatesToRecord.ALL_VISIBLE,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(RECORDING)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(RECORDING)
        return subFlow(ReceiveFinalityFlow(session, expectedTransactionId, statesToRecord))
    }

    /**
     * Represents the initiated flow handler for issuing attestations.
     *
     * @property session The counter-party session who is initiating the flow.
     */
    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : ProgressTracker.Step("Observing issued attestation.") {
                override fun childProgressTracker(): ProgressTracker = tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(IssueAttestationFlowHandler(session, progressTracker = OBSERVING.childProgressTracker()))
        }
    }
}
