package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

/**
 * Represents the smart contract for accounts.
 * This contract is open to allow developers to implement their own account contracts.
 */
open class AccountContract : Contract {

    companion object {

        /**
         * The ID of this contract.
         */
        @JvmStatic
        val ID: ContractClassName = this::class.java.enclosingClass.canonicalName
    }

    /**
     * Verifies a ledger transaction using a command from this contract.
     *
     * @param tx The ledger transaction to verify.
     */
    final override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<AccountContractCommand>()
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
    interface AccountContractCommand : CommandData

    /**
     * Represents the command to issue accounts.
     */
    object Issue : AccountContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On account issuing, zero account states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On account issuing, at least one account state must be created."

        internal const val CONTRACT_RULE_PARTICIPANTS =
            "On account issuing, the owner of each created account must be a participant."

        internal const val CONTRACT_RULE_SIGNERS =
            "On account issuing, the owner of each account must sign the transaction."
    }

    /**
     * Represents the command to amend accounts.
     */
    object Amend : AccountContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On account amending, at least one account state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On account amending, at least one account state must be created."

        internal const val CONTRACT_RULE_PARTICIPANTS =
            "On account amending, the owner of each created account must be a participant."

        internal const val CONTRACT_RULE_SIGNERS =
            "On account amending, the owner of each account must sign the transaction."
    }

    /**
     * Represents the command to revoke accounts.
     */
    object Revoke : AccountContractCommand {
        internal const val CONTRACT_RULE_INPUTS =
            "On account revoking, at least one account state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On account revoking, zero account states must be created."

        internal const val CONTRACT_RULE_SIGNERS =
            "On account revoking, the owner of each account must sign the transaction."
    }

    /**
     * Provides the ability to extend the rules for issuing accounts.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for amending accounts.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    protected open fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat { }

    /**
     * Provides the ability to extend the rules for revoking accounts.
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
        val accountInputs = transaction.inputsOfType<Account>()
        val accountOutputs = transaction.outputsOfType<Account>()

        Issue.CONTRACT_RULE_INPUTS using (accountInputs.isEmpty())
        Issue.CONTRACT_RULE_OUTPUTS using (accountOutputs.isNotEmpty())
        Issue.CONTRACT_RULE_PARTICIPANTS using (accountOutputs.all { it.owner in it.participants })
        Issue.CONTRACT_RULE_SIGNERS using (accountOutputs.all { it.owner.owningKey in signers })

        onVerifyIssue(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Amend command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val accountInputs = transaction.inputsOfType<Account>()
        val accountOutputs = transaction.outputsOfType<Account>()

        Amend.CONTRACT_RULE_INPUTS using (accountInputs.isNotEmpty())
        Amend.CONTRACT_RULE_OUTPUTS using (accountOutputs.isNotEmpty())
        Amend.CONTRACT_RULE_PARTICIPANTS using (accountOutputs.all { it.owner in it.participants })
        Amend.CONTRACT_RULE_SIGNERS using (accountOutputs.all { it.owner.owningKey in signers })

        onVerifyAmend(transaction, signers)
    }

    /**
     * Verifies a ledger transaction using the Revoke command.
     *
     * @param transaction The ledger transaction to verify.
     * @param signers The signers of the transaction.
     */
    private fun verifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val accountInputs = transaction.inputsOfType<Account>()
        val accountOutputs = transaction.outputsOfType<Account>()

        Revoke.CONTRACT_RULE_INPUTS using (accountInputs.isNotEmpty())
        Revoke.CONTRACT_RULE_OUTPUTS using (accountOutputs.isEmpty())
        Revoke.CONTRACT_RULE_SIGNERS using (accountInputs.all { it.owner.owningKey in signers })

        onVerifyRevoke(transaction, signers)
    }
}
