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
import io.onixlabs.corda.core.workflow.*
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.workflow.addIssuedAttestation
import io.onixlabs.corda.identityframework.workflow.checkAttestationExists
import io.onixlabs.corda.identityframework.workflow.checkHasAttestedStateBeenWitnessed
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

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
        checkSufficientSessions(sessions, attestation)
        checkHasAttestedStateBeenWitnessed(attestation)
        checkAttestationExists(attestation)

        val transaction = buildTransaction(notary) {
            addIssuedAttestation(attestation)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
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
            object IssueAttestationStep : Step("Issuing attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(IssueAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(IssueAttestationStep)
            val sessions = initiateFlows(observers, attestation)
            return subFlow(
                IssueAttestationFlow(
                    attestation,
                    notary ?: getPreferredNotary(),
                    sessions,
                    IssueAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
