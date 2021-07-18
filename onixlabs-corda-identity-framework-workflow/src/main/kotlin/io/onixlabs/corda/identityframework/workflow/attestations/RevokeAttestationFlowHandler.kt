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
import io.onixlabs.corda.core.workflow.ReceiveStatesToRecordStep
import io.onixlabs.corda.core.workflow.RecordFinalizedTransactionStep
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.core.workflow.finalizeTransactionHandler
import io.onixlabs.corda.identityframework.workflow.attestations.RevokeAttestationFlow.Initiator
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.node.StatesToRecord
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow handler for revoking attestations.
 *
 * @property session The counter-party session who is initiating the flow.
 * @property expectedTransactionId The expected ID of the transaction.
 * @property statesToRecord Determines which transaction states to record.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class RevokeAttestationFlowHandler(
    private val session: FlowSession,
    private val expectedTransactionId: SecureHash? = null,
    private val statesToRecord: StatesToRecord = StatesToRecord.ALL_VISIBLE,
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(ReceiveStatesToRecordStep, RecordFinalizedTransactionStep)
    }

    @Suspendable
    override fun call(): SignedTransaction {
        return finalizeTransactionHandler(session, expectedTransactionId, statesToRecord)
    }

    /**
     * Represents the initiated flow handler for revoking attestations.
     *
     * @property session The counter-party session who is initiating the flow.
     */
    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object HandleRevokedAttestationStep : Step("Handling revoked attestation.") {
                override fun childProgressTracker(): ProgressTracker = tracker()
            }
        }

        override val progressTracker = ProgressTracker(HandleRevokedAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(HandleRevokedAttestationStep)
            return subFlow(
                RevokeAttestationFlowHandler(
                    session,
                    progressTracker = HandleRevokedAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
