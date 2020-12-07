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
import io.onixlabs.corda.identityframework.contract.CordaClaim
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

/**
 * Represents the flow for issuing a claim.
 *
 * @property claim The claim to be issued.
 * @property notary The notary to use for the transaction.
 * @property sessions The sessions required for attestation counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class IssueClaimFlow(
    private val claim: CordaClaim<*>,
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
        checkHasSufficientFlowSessions(sessions, claim)
        checkClaimExists(claim)

        val transaction = transaction(notary) {
            addIssuedClaim(claim)
        }

        val signedTransaction = verifyAndSign(transaction, claim.issuer.owningKey)
        return finalize(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for issuing a claim.
     *
     * @property claim The claim to be issued.
     * @property notary The notary to use for the transaction.
     * @property observers The additional observers of the attestation.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(FLOW_VERSION_1)
    class Initiator(
        private val claim: CordaClaim<*>,
        private val notary: Party? = null,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object ISSUING : ProgressTracker.Step("Issuing claim.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(ISSUING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(ISSUING)
            val sessions = initiateFlows(observers, claim)
            return subFlow(IssueClaimFlow(claim, notary ?: preferredNotary, sessions, ISSUING.childProgressTracker()))
        }
    }

    /**
     * Represents the handler of the [Initiator] flow.
     *
     * @property session The counter-party session who is initiating the flow.
     */
    @InitiatedBy(Initiator::class)
    private class Handler(private val session: FlowSession) : FlowLogic<SignedTransaction>() {

        private companion object {
            object OBSERVING : ProgressTracker.Step("Observing issued claim.") {
                override fun childProgressTracker(): ProgressTracker = IssueClaimFlowHandler.tracker()
            }
        }

        override val progressTracker = ProgressTracker(OBSERVING)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(OBSERVING)
            return subFlow(IssueClaimFlowHandler(session, progressTracker = OBSERVING.childProgressTracker()))
        }
    }
}
