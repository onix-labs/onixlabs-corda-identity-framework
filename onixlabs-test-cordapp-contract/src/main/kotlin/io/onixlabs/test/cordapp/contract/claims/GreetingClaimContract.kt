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

package io.onixlabs.test.cordapp.contract.claims

import io.onixlabs.corda.core.contract.ContractID
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimContract
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class GreetingClaimContract : CordaClaimContract(), Contract {

    companion object : ContractID

    internal object Issue {
        const val CONTRACT_RULE_OUTPUT_PROPERTY =
            "On greeting claim issuing, the property must be 'greeting'."

        const val CONTRACT_RULE_OUTPUT_VALUE =
            "On greeting claim issuing, the value must be 'Hello, World!'."
    }

    internal object Amend {
        const val CONTRACT_RULE_OUTPUT_VALUE =
            "On greeting claim amending, the output value must be 'Goodbye, World!'."
    }

    internal object Revoke {
        const val CONTRACT_RULE_INPUT_VALUE =
            "On greeting claim revoking, the input value must be 'Goodbye, World!'."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val output = transaction.outputsOfType<GreetingClaim>().single()

        Issue.CONTRACT_RULE_OUTPUT_PROPERTY using (output.property == "greeting")
        Issue.CONTRACT_RULE_OUTPUT_VALUE using (output.value == "Hello, World!")
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val output = transaction.outputsOfType<GreetingClaim>().single()

        Amend.CONTRACT_RULE_OUTPUT_VALUE using (output.value == "Goodbye, World!")
    }

    override fun onVerifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val input = transaction.inputsOfType<GreetingClaim>().single()

        Revoke.CONTRACT_RULE_INPUT_VALUE using (input.value == "Goodbye, World!")
    }
}
