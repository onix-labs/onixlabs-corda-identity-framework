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
import io.onixlabs.corda.identityframework.workflow.*
import io.onixlabs.corda.identityframework.workflow.FLOW_VERSION_1
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents the flow for amending an attestation.
 *
 * @property oldAttestation The attestation to be consumed.
 * @property newAttestation The attestation to be created.
 * @property sessions The sessions required for attestation counter-parties and observers.
 * @property progressTracker The progress tracker which tracks the progress of this flow.
 */
class AmendAttestationFlow(
    private val oldAttestation: StateAndRef<Attestation<*>>,
    private val newAttestation: Attestation<*>,
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
    }

    @Suspendable
    override fun call(): SignedTransaction {
        currentStep(InitializeFlowStep)
        checkSufficientSessionsWithAccounts(sessions, oldAttestation.state.data, newAttestation)
        checkHasAttestedStateBeenWitnessed(newAttestation)
        checkAttestationExists(newAttestation)

        val transaction = buildTransaction(oldAttestation.state.notary) {
            addAmendedAttestation(oldAttestation, newAttestation)
        }

        verifyTransaction(transaction)
        val signedTransaction = signTransaction(transaction)
        return finalizeTransaction(signedTransaction, sessions)
    }

    /**
     * Represents the initiating flow for amending an attestation.
     *
     * @property oldAttestation The attestation to be consumed.
     * @property newAttestation The attestation to be created.
     * @property observers The additional observers of the attestation.
     */
    @StartableByRPC
    @StartableByService
    @InitiatingFlow(version = FLOW_VERSION_1)
    class Initiator(
        private val oldAttestation: StateAndRef<Attestation<*>>,
        private val newAttestation: Attestation<*>,
        private val observers: Set<Party> = emptySet()
    ) : FlowLogic<SignedTransaction>() {

        private companion object {
            object AmendAttestationStep : Step("Amending attestation.") {
                override fun childProgressTracker() = tracker()
            }
        }

        override val progressTracker = ProgressTracker(AmendAttestationStep)

        @Suspendable
        override fun call(): SignedTransaction {
            currentStep(AmendAttestationStep)
            val sessions = initiateFlows(observers, oldAttestation.state.data, newAttestation)
            return subFlow(
                AmendAttestationFlow(
                    oldAttestation,
                    newAttestation,
                    sessions,
                    AmendAttestationStep.childProgressTracker()
                )
            )
        }
    }
}
