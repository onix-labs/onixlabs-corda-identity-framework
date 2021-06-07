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
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.services.vaultServiceFor
import io.onixlabs.corda.core.workflow.currentStep
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.AttestationSchema
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey

/**
 * Checks whether the state for the specified attestation has been witnessed by this node.
 *
 * @param attestation The attestation to check.
 * @throws FlowException if the state for the specified attestation has not been witnessed.
 */
@Suspendable
fun FlowLogic<*>.checkHasAttestedStateBeenWitnessed(attestation: Attestation<*>) {
    if (attestation.pointer.resolve(serviceHub) == null) {
        val message = "A state with the specified state reference has not been witnessed:"
        throw FlowException("$message ${attestation.pointer.stateRef}.")
    }
}

/**
 * Checks whether the specified claim already exists.
 *
 * @param claim The claim to check for existence.
 * @throws FlowException if the claim already exists.
 */
@Suspendable
fun FlowLogic<*>.checkClaimExists(claim: CordaClaim<*>) {
    val claimExists = serviceHub.vaultServiceFor(claim.javaClass).any {
        where(CordaClaimSchema.CordaClaimEntity::hash equalTo claim.hash.toString())
    }

    if (claimExists) {
        throw FlowException("A claim with the specified hash already exists: ${claim.hash}.")
    }
}

/**
 * Checks whether the specified attestation already exists.
 *
 * @param attestation The attestation to check for existence.
 * @throws FlowException if the claim already exists.
 */
@Suspendable
fun FlowLogic<*>.checkAttestationExists(attestation: Attestation<*>) {
    val attestationExists = serviceHub.vaultServiceFor(attestation.javaClass).any {
        where(AttestationSchema.AttestationEntity::hash equalTo attestation.hash.toString())
    }

    if (attestationExists) {
        throw FlowException("An attestation with the specified hash already exists: ${attestation.hash}.")
    }
}

/**
 * Generates an unsigned transaction.
 *
 * @param notary The notary to assign to the transaction.
 * @param action The context in which the [TransactionBuilder] will build the transaction.
 * @return Returns an unsigned transaction.
 */
@Suspendable
internal fun FlowLogic<*>.transaction(
    notary: Party,
    action: TransactionBuilder.() -> TransactionBuilder
): TransactionBuilder {
    currentStep(GENERATING)
    return with(TransactionBuilder(notary)) { action(this) }
}

/**
 * Verifies and signs an unsigned transaction.
 *
 * @param builder The unsigned transaction to verify and sign.
 * @param signingKey The initial signing ket for the transaction.
 * @return Returns a verified and signed transaction.
 */
@Suspendable
internal fun FlowLogic<*>.verifyAndSign(
    builder: TransactionBuilder,
    signingKey: PublicKey
): SignedTransaction {
    currentStep(VERIFYING)
    builder.verify(serviceHub)

    currentStep(SIGNING)
    return serviceHub.signInitialTransaction(builder, signingKey)
}

/**
 * Gathers counter-party signatures for a partially signed transaction.
 *
 * @param transaction The signed transaction for which to obtain additional signatures.
 * @param sessions The flow sessions for the required signing counter-parties.
 * @return Returns a signed transaction.
 */
@Suspendable
internal fun FlowLogic<*>.countersign(
    transaction: SignedTransaction,
    sessions: Set<FlowSession>
): SignedTransaction {
    currentStep(COUNTERSIGNING)
    return subFlow(CollectSignaturesFlow(transaction, sessions, COUNTERSIGNING.childProgressTracker()))
}

/**
 * Finalizes and records a signed transaction to the vault.
 *
 * @param transaction The transaction to finalize and record.
 * @param sessions The flow sessions for counter-parties who are expected to finalize and record the transaction.
 * @return Returns a finalized and recorded transaction.
 */
@Suspendable
internal fun FlowLogic<*>.finalize(
    transaction: SignedTransaction,
    sessions: Set<FlowSession> = emptySet()
): SignedTransaction {
    currentStep(FINALIZING)
    return subFlow(FinalityFlow(transaction, sessions, FINALIZING.childProgressTracker()))
}
