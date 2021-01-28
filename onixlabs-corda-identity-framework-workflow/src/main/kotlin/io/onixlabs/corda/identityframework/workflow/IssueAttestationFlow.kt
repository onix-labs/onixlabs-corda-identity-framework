/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import io.onixlabs.corda.core.workflow.getPreferredNotary
import io.onixlabs.corda.core.workflow.initiateFlows
import io.onixlabs.corda.identityframework.v1.contract.Attestation
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

/**
 * Represents the flow for issuing an attestation.
 *
 * @property attestation The attestation to be issued.
 * @property notary The notary to use for the transaction.
 * @property sessions The sessions required for attestation counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class IssueAttestationFlow(
    private val attestation: Attestation<*>,
    private val notary: Party,
    private val sessions: Set<FlowSession> = emptySet(),
    override val progressTracker: ProgressTracker = tracker()
) : FlowLogic<SignedTransaction>() {

    companion object {
        @JvmStatic
        fun tracker() = ProgressTracker(INITIALIZING, GENERATING, VERIFYING, SIGNING, FINALIZING)

        private const val FLOW_VERSION_1 = 1
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(INITIALIZING)
        checkHasSufficientFlowSessions(sessions, attestation)
        checkHasAttestedStateBeenWitnessed(attestation)
        checkAttestationExists(attestation)

        val transaction = transaction(notary) {
            addIssuedAttestation(attestation)
        }

        val signedTransaction = verifyAndSign(transaction, attestation.attestor.owningKey)
        return finalize(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for issuing an attestation.
     *
     * @property attestation The attestation to be issued.
     * @property notary The notary to use for the transaction.
     * @property observers The additional observers of the attestation.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(FLOW_VERSION_1)
    class Initiator(
        private val attestation: Attestation<*>,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : ProgressTracker.Step("Issuing attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = initiateFlows(observers, attestation)
            return subFlow(
                IssueAttestationFlow(
                    attestation,
                    notary ?: getPreferredNotary(),
                    sessions,
                    ISSUING.childProgressTracker()
                )
            )
        }
    }
}
