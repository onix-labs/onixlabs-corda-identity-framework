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

package io.onixlabs.corda.identityframework.contract.attestations

import io.onixlabs.corda.core.contract.ContractID
import io.onixlabs.corda.core.contract.isPointingTo
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

/**
 * Represents the smart contract for attestations.
 */
open class AttestationContract : Contract {

    companion object : ContractID

    /**
     * Verifies a ledger transaction using a command from this contract.
     *
     * @param tx The ledger transaction to verify.
     */
    final override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<AttestationContractCommand>()
        when (command.value) {
            is Issue -> verifyIssue(tx, command.signers.toSet())
            is Amend -> verifyAmend(tx, command.signers.toSet())
            is Revoke -> verifyRevoke(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}.")
        }
    }

    /**
     * Defines the interface for attestation contract commands.
     */
    interface AttestationContractCommand : CommandData

    /**
     * Represents the command to issue attestations.
     */
    object Issue : AttestationContractCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On attestation issuing, zero attestation states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On attestation issuing, only one attestation state must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On attestation issuing, the attestor must sign the transaction."
    }

    /**
     * Represents the command to amend attestations.
     */
    object Amend : AttestationContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On attestation amending, only one attestation state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On attestation amending, only one attestation state must be created."

        internal const val CONTRACT_RULE_CHANGES =
            "On attestation amending, the attestor, linear ID, pointer class and pointer linear ID must not change."

        internal const val CONTRACT_RULE_STATE_REF =
            "On attestation amending, the created attestation state must point to the consumed attestation state."

        internal const val CONTRACT_RULE_SIGNERS =
            "On attestation amending, the attestor must sign the transaction."
    }

    /**
     * Represents the command to revoke attestations.
     */
    object Revoke : AttestationContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On attestation revoking, only one attestation state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On attestation revoking, zero attestation states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On attestation revoking, the attestor must sign the transaction."
    }

    /**
     * Provides the ability to extend the rules for issuing attestations.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for amending attestations.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for revoking attestations.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Verifies a ledger transaction using the Issue command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestationInputs = transaction.inputsOfType<Attestation<*>>()
        val attestationOutputs = transaction.outputsOfType<Attestation<*>>()

        Issue.CONTRACT_RULE_INPUTS using (attestationInputs.isEmpty())
        Issue.CONTRACT_RULE_OUTPUTS using (attestationOutputs.size == 1)

        val attestationOutput = attestationOutputs.single()

        Issue.CONTRACT_RULE_SIGNERS using (attestationOutput.attestor.owningKey in signers)

        onVerifyIssue(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Amend command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestationInputs = transaction.inRefsOfType<Attestation<*>>()
        val attestationOutputs = transaction.outputsOfType<Attestation<*>>()

        Amend.CONTRACT_RULE_INPUTS using (attestationInputs.size == 1)
        Amend.CONTRACT_RULE_OUTPUTS using (attestationOutputs.size == 1)

        val attestationInput = attestationInputs.single()
        val attestationOutput = attestationOutputs.single()

        Amend.CONTRACT_RULE_CHANGES using (attestationInput.state.data.internalImmutableEquals(attestationOutput))
        Amend.CONTRACT_RULE_STATE_REF using (attestationOutput.isPointingTo(attestationInput))
        Amend.CONTRACT_RULE_SIGNERS using (attestationOutput.attestor.owningKey in signers)

        onVerifyAmend(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Revoke command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestationInputs = transaction.inputsOfType<Attestation<*>>()
        val attestationOutputs = transaction.outputsOfType<Attestation<*>>()

        Revoke.CONTRACT_RULE_INPUTS using (attestationInputs.size == 1)
        Revoke.CONTRACT_RULE_OUTPUTS using (attestationOutputs.isEmpty())

        val attestationInput = attestationInputs.single()

        Revoke.CONTRACT_RULE_SIGNERS using (attestationInput.attestor.owningKey in signers)

        onVerifyRevoke(transaction, signers)
    }
}
