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

package io.onixlabs.test.cordapp.contract

import io.onixlabs.corda.identityframework.v1.contract.AttestationContract
import io.onixlabs.corda.identityframework.v1.contract.CordaClaimContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.node.NotaryInfo
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.LedgerDSL
import net.corda.testing.dsl.TestLedgerDSLInterpreter
import net.corda.testing.dsl.TestTransactionDSLInterpreter
import net.corda.testing.node.MockServices
import org.junit.jupiter.api.BeforeEach

abstract class ContractTest {

    protected companion object {
        private val cordapps = listOf("io.onixlabs.test.cordapp.contract")
        private val contracts = listOf(GreetingClaimContract.ID, GreetingAttestationContract.ID)

        fun keysOf(vararg identities: TestIdentity) = identities.map { it.publicKey }
    }

    private lateinit var _services: MockServices
    protected val services: MockServices get() = _services

    @BeforeEach
    private fun setup() {
        val networkParameters = testNetworkParameters(
            minimumPlatformVersion = 8,
            notaries = listOf(NotaryInfo(NOTARY.party, true))
        )
        _services = MockServices(cordapps, IDENTITY_A, networkParameters, IDENTITY_B, IDENTITY_C)
        contracts.forEach { _services.addMockCordapp(it) }
    }

    fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(claim: GreetingClaim): StateAndRef<GreetingClaim> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(GreetingClaimContract.ID, label, claim)
            command(listOf(claim.issuer.owningKey), CordaClaimContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(claim.javaClass, label)
    }

    fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        attestation: GreetingAttestation,
        claim: StateAndRef<GreetingClaim>
    ): StateAndRef<GreetingAttestation> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(GreetingAttestationContract.ID, label, attestation)
            reference(claim.ref)
            command(listOf(attestation.attestor.owningKey), AttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(attestation.javaClass, label)
    }

    fun LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.amend(
        oldClaim: StateAndRef<GreetingClaim>,
        newClaim: GreetingClaim
    ): StateAndRef<GreetingClaim> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            input(oldClaim.ref)
            output(oldClaim.state.contract, label, newClaim)
            command(listOf(newClaim.issuer.owningKey), CordaClaimContract.Amend)
            verifies()
        }

        return retrieveOutputStateAndRef(newClaim.javaClass, label)
    }
}
