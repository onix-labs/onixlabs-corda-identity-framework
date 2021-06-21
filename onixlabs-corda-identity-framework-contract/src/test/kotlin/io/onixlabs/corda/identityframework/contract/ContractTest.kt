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

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.*
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
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
        private val cordapps = listOf("io.onixlabs.corda.identityframework.contract")
        private val contracts = listOf(CordaClaimContract.ID, AttestationContract.ID)
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

    fun <T : ContractState> createStateAndRef(state: T, contract: ContractClassName = ""): StateAndRef<T> {
        return StateAndRef(TransactionState(state, contract, NOTARY.party), EMPTY_REF)
    }

    fun <T : Any> StateAndRef<CordaClaim<T>>.withInvalidStateRef(value: T): CordaClaim<T> {
        return with(state.data) {
            CordaClaim(issuer, holder, property, value, linearId, EMPTY_REF)
        }
    }

    fun <T : ContractState> StateAndRef<Attestation<T>>.withInvalidAttestor(attestor: AbstractParty): Attestation<T> {
        return with(state.data) {
            Attestation(attestor, attestees, pointer, status, metadata, linearId, null)
        }
    }

    fun <T : LinearState> StateAndRef<Attestation<T>>.withInvalidPointer(): Attestation<T> {
        return with(state.data) {
            val invalidPointer = LinearAttestationPointer(pointer.stateType, UniqueIdentifier())
            Attestation(attestor, attestees, invalidPointer, status, metadata, linearId, null)
        }
    }

    fun <T : ContractState> StateAndRef<Attestation<T>>.withInvalidPreviousStateRef(): Attestation<T> {
        return with(state.data) {
            Attestation(attestor, attestees, pointer, status, metadata, linearId, EMPTY_REF)
        }
    }

    fun <T : Attestation<*>> LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        attestation: T,
        contract: ContractClassName = AttestationContract.ID
    ): StateAndRef<T> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(contract, label, attestation)
            command(listOf(attestation.attestor.owningKey), AttestationContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(attestation.javaClass, label)
    }

    fun <T : CordaClaim<*>> LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.issue(
        claim: T,
        contract: ContractClassName = CordaClaimContract.ID
    ): StateAndRef<T> {
        val label = SecureHash.randomSHA256().toString()

        transaction {
            output(contract, label, claim)
            command(listOf(claim.issuer.owningKey), CordaClaimContract.Issue)
            verifies()
        }

        return retrieveOutputStateAndRef(claim.javaClass, label)
    }

    fun <T : CordaClaim<*>> LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.amend(
        oldClaim: StateAndRef<T>,
        newClaim: T
    ): StateAndRef<T> {
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
