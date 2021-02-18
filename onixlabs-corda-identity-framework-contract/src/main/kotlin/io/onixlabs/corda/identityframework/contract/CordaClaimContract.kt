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

package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.core.contract.ContractID
import io.onixlabs.corda.core.contract.isPointingTo
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

/**
 * Represents the smart contract for corda claims.
 * This contract is open to allow developers to implement their own corda claim contracts.
 */
open class CordaClaimContract : Contract {

    companion object : ContractID

    /**
     * Verifies a ledger transaction using a command from this contract.
     *
     * @param tx The ledger transaction to verify.
     */
    final override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<CordaClaimContractCommand>()
        when (command.value) {
            is Issue -> verifyIssue(tx, command.signers.toSet())
            is Amend -> verifyAmend(tx, command.signers.toSet())
            is Revoke -> verifyRevoke(tx, command.signers.toSet())
            else -> throw IllegalArgumentException("Unrecognised command: ${command.value}.")
        }
    }

    /**
     * Defines the interface for corda claim contract commands.
     */
    interface CordaClaimContractCommand : CommandData

    /**
     * Represents the command to issue corda claims.
     */
    object Issue : CordaClaimContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On claim issuing, zero claim states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On claim issuing, only one claim state must be created."

        internal const val CONTRACT_RULE_ISSUER_PARTICIPANT =
            "On claim issuing, the issuer of the created claim state must be a participant."

        internal const val CONTRACT_RULE_HOLDER_PARTICIPANT =
            "On claim issuing, the holder of the created claim state must be a participant."

        internal const val CONTRACT_RULE_SIGNERS =
            "On claim issuing, the issuer must sign the transaction."
    }

    /**
     * Represents the command to amend evolvable claims.
     */
    object Amend : CordaClaimContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On claim amending, only one claim state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On claim amending, only one claim state must be created."

        internal const val CONTRACT_RULE_ISSUER_PARTICIPANT =
            "On claim amending, the issuer of the created claim state must be a participant."

        internal const val CONTRACT_RULE_HOLDER_PARTICIPANT =
            "On claim amending, the holder of the created claim state must be a participant."

        internal const val CONTRACT_RULE_STATE_REF =
            "On claim amending, the created claim state must point to the consumed claim state."

        internal const val CONTRACT_RULE_CHANGES =
            "On claim amending, the issuer, holder, property, and linear ID must not change."

        internal const val CONTRACT_RULE_SIGNERS =
            "On claim amending, the issuer must sign the transaction."
    }

    /**
     * Represents the command to revoke evolvable claims.
     */
    object Revoke : CordaClaimContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On claim revoking, only one claim state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On claim revoking, zero claim states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On claim revoking, the issuer must sign the transaction."
    }

    /**
     * Provides the ability to extend the rules for issuing evolvable claims.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for amending evolvable claims.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for revoking evolvable claims.
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
        val claimInputs = transaction.inputsOfType<CordaClaim<*>>()
        val claimOutputs = transaction.outputsOfType<CordaClaim<*>>()

        Issue.CONTRACT_RULE_INPUTS using (claimInputs.isEmpty())
        Issue.CONTRACT_RULE_OUTPUTS using (claimOutputs.size == 1)

        val claimOutput = claimOutputs.single()

        Issue.CONTRACT_RULE_ISSUER_PARTICIPANT using (claimOutput.issuer in claimOutput.participants)
        Issue.CONTRACT_RULE_HOLDER_PARTICIPANT using (claimOutput.holder in claimOutput.participants)
        Issue.CONTRACT_RULE_SIGNERS using (claimOutput.issuer.owningKey in signers)

        onVerifyIssue(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Amend command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val claimInputs = transaction.inRefsOfType<CordaClaim<*>>()
        val claimOutputs = transaction.outputsOfType<CordaClaim<*>>()

        Amend.CONTRACT_RULE_INPUTS using (claimInputs.size == 1)
        Amend.CONTRACT_RULE_OUTPUTS using (claimOutputs.size == 1)

        val claimInput = claimInputs.single()
        val claimOutput = claimOutputs.single()

        Amend.CONTRACT_RULE_ISSUER_PARTICIPANT using (claimOutput.issuer in claimOutput.participants)
        Amend.CONTRACT_RULE_HOLDER_PARTICIPANT using (claimOutput.holder in claimOutput.participants)
        Amend.CONTRACT_RULE_STATE_REF using (claimOutput.isPointingTo(claimInput))
        Amend.CONTRACT_RULE_CHANGES using (claimOutput.internalImmutableEquals(claimInput.state.data))
        Amend.CONTRACT_RULE_SIGNERS using (claimOutput.issuer.owningKey in signers)

        onVerifyAmend(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Revoke command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val claimInputs = transaction.inputsOfType<CordaClaim<*>>()
        val claimOutputs = transaction.outputsOfType<CordaClaim<*>>()

        Revoke.CONTRACT_RULE_INPUTS using (claimInputs.size == 1)
        Revoke.CONTRACT_RULE_OUTPUTS using (claimOutputs.isEmpty())

        val claimInput = claimInputs.single()

        Revoke.CONTRACT_RULE_SIGNERS using (claimInput.issuer.owningKey in signers)

        onVerifyRevoke(transaction, signers)
    }
}
