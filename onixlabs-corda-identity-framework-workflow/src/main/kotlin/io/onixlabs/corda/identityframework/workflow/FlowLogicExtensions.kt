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
import io.onixlabs.corda.identityframework.contract.Attestation
import io.onixlabs.corda.identityframework.contract.CordaClaim
import net.corda.core.contracts.ContractState
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey

/**
 * Checks whether there are sufficient sessions for counter-parties of the specified contract states.
 *
 * @param sessions The list of available flow sessions.
 * @param states The specified contract states to check.
 * @throws FlowException if a flow session does not exist for a required counter-party.
 */
fun FlowLogic<*>.checkHasSufficientFlowSessions(sessions: Iterable<FlowSession>, vararg states: ContractState) {
    val sessionParties = sessions.map { it.counterparty }.toSet()
    states.flatMap { it.participants }.toSet().filter { it !in serviceHub.myInfo.legalIdentities }.forEach {
        if (it !in sessionParties) throw FlowException("A flow session is required for the specified counterparty: $it")
    }
}

/**
 * Checks whether the state for the specified attestation has been witnessed by this node.
 *
 * @param attestation The attestation to check.
 * @throws FlowException if the state for the specified attestation has not been witnessed.
 */
fun FlowLogic<*>.checkHasAttestedStateBeenWitnessed(attestation: Attestation<*>) {
    if (attestation.pointer.resolve(serviceHub) == null) {
        throw FlowException("The state that the attestation is pointing to has not been witnessed by this node.")
    }
}

/**
 * Checks whether the specified claim already exists.
 *
 * @param claim The claim to check for existence.
 * @throws FlowException if the claim already exists.
 */
fun FlowLogic<*>.checkClaimExists(claim: CordaClaim<*>) {
    if (subFlow(FindClaimsFlow<CordaClaim<*>>(hash = claim.hash)).isNotEmpty()) {
        throw FlowException("A claim with the specified hash already exists: ${claim.hash}.")
    }
}

/**
 * Checks whether the specified attestation already exists.
 *
 * @param attestation The attestation to check for existence.
 * @throws FlowException if the claim already exists.
 */
fun FlowLogic<*>.checkAttestationExists(attestation: Attestation<*>) {
    if (subFlow(FindAttestationsFlow<Attestation<*>>(hash = attestation.hash)).isNotEmpty()) {
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
fun FlowLogic<*>.transaction(notary: Party, action: TransactionBuilder.() -> TransactionBuilder): TransactionBuilder {
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
fun FlowLogic<*>.verifyAndSign(builder: TransactionBuilder, signingKey: PublicKey): SignedTransaction {
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
fun FlowLogic<*>.countersign(transaction: SignedTransaction, sessions: Set<FlowSession>): SignedTransaction {
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
fun FlowLogic<*>.finalize(transaction: SignedTransaction, sessions: Set<FlowSession> = emptySet()): SignedTransaction {
    currentStep(FINALIZING)
    return subFlow(FinalityFlow(transaction, sessions, FINALIZING.childProgressTracker()))
}
