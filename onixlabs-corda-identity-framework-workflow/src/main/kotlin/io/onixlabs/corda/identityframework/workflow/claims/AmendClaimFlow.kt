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
import io.onixlabs.corda.identityframework.workflow.addAmendedClaim
import io.onixlabs.corda.identityframework.workflow.checkClaimExists
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for amending a claim.
 *
 * @property oldClaim The claim to be consumed.
 * @property newClaim The claim to be created.
 * @property sessions The sessions required for claim counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class AmendClaimFlow(
    private val oldClaim: StateAndRef<CordaClaim<*>>,
    private val newClaim: CordaClaim<*>,
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
        checkSufficientSessions(sessions, oldClaim.state.data, newClaim)
        checkClaimExists(newClaim)

        val transaction = buildTransaction(oldClaim.state.notary) {
            addAmendedClaim(oldClaim, newClaim)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for amending a claim.
     *
     * @property oldClaim The claim to be consumed.
     * @property newClaim The claim to be created.
     * @property observers The additional observers of the claim.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(FLOW_VERSION_1)
    class Initiator(
        private val oldClaim: StateAndRef<CordaClaim<*>>,
        private val newClaim: CordaClaim<*>,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AmendClaimStep : Step("Amending claim.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AmendClaimStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AmendClaimStep)
            val sessions = initiateFlows(observers, oldClaim.state.data, newClaim)
            return subFlow(AmendClaimFlow(oldClaim, newClaim, sessions, AmendClaimStep.childProgressTracker()))
        }
    }
}
