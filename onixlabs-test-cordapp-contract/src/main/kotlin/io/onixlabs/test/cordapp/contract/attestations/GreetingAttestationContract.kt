/*
 * Copyright 2020-2022 ONIXLabs
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

package io.onixlabs.test.cordapp.contract.attestations

import io.onixlabs.corda.core.contract.ContractID
import io.onixlabs.corda.identityframework.contract.attestations.AttestationContract
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.security.PublicKey

class GreetingAttestationContract : AttestationContract(), Contract {

    companion object : ContractID

    internal object Issue {
        const val CONTRACT_RULE_REFERENCES =
            "On greeting attestation issuing, only one greeting state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On greeting attestation issuing, the attestation pointer must point to the referenced greeting state."
    }

    internal object Amend {
        const val CONTRACT_RULE_REFERENCES =
            "On greeting attestation amending, only one greeting state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On greeting attestation amending, the attestation pointer must point to the referenced greeting state."

        const val CONTRACT_RULE_VALUE =
            "On greeting attestation amending, the attestation status must be REJECTED if the referenced greeting value is 'Hello, World!'"
    }

    internal object Revoke {
        const val CONTRACT_RULE_REFERENCES =
            "On greeting attestation revoking, only one greeting state must be referenced."

        const val CONTRACT_RULE_POINTER =
            "On greeting attestation revoking, the attestation pointer must point to the referenced greeting state."

        const val CONTRACT_RULE_STATUS =
            "On greeting attestation revoking, the attestation status must be ACCEPTED."
    }

    override fun onVerifyIssue(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestation = transaction.outputsOfType<GreetingAttestation>().single()
        val references = transaction.referenceInputRefsOfType<GreetingClaim>()

        Issue.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val reference = references.single()

        Issue.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(reference))
    }

    override fun onVerifyAmend(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestation = transaction.outputsOfType<GreetingAttestation>().single()
        val references = transaction.referenceInputRefsOfType<GreetingClaim>()

        Amend.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val reference = references.single()

        Amend.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(reference))

        if (reference.state.data.value == "Hello, World!") {
            Amend.CONTRACT_RULE_VALUE using (attestation.status == AttestationStatus.REJECTED)
        }
    }

    override fun onVerifyRevoke(transaction: LedgerTransaction, signers: Set<PublicKey>) = requireThat {
        val attestation = transaction.inputsOfType<GreetingAttestation>().single()
        val references = transaction.referenceInputRefsOfType<GreetingClaim>()

        Revoke.CONTRACT_RULE_REFERENCES using (references.size == 1)

        val reference = references.single()

        Revoke.CONTRACT_RULE_POINTER using (attestation.pointer.isPointingTo(reference))
        Revoke.CONTRACT_RULE_STATUS using (attestation.status == AttestationStatus.ACCEPTED)
    }
}
