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

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountContract
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationPointer
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.claims.Claim
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.core.TestIdentity

val IDENTITY_A = TestIdentity(CordaX500Name("PartyA", "London", "GB"))
val IDENTITY_B = TestIdentity(CordaX500Name("PartyB", "New York", "US"))
val IDENTITY_C = TestIdentity(CordaX500Name("PartyC", "Paris", "FR"))
val NOTARY = TestIdentity(DUMMY_NOTARY_NAME)

val CLAIM_1 = CordaClaim(IDENTITY_A.party, IDENTITY_B.party, "example", "Hello, World!")
val CLAIM_2 = CordaClaim(IDENTITY_B.party, IDENTITY_B.party, "example", 123)

val ACCOUNT_A = Account(IDENTITY_A.party)

val EMPTY_REF = StateRef(SecureHash.zeroHash, 0)

class ExampleStringClaim(property: String, value: String) : Claim<String>(property, value)
class ExampleNumberClaim(property: String, value: Int) : Claim<Int>(property, value)

@BelongsToContract(CordaClaimContract::class)
class CustomCordaClaim(
    value: String = "Hello, World!",
    previousStateRef: StateRef? = null,
    override val participants: List<AbstractParty> = emptyList()
) : CordaClaim<String>(IDENTITY_A.party, IDENTITY_B.party, "example", value, UniqueIdentifier(), previousStateRef) {

    override fun amend(previousStateRef: StateRef, value: String): CordaClaim<String> {
        return CustomCordaClaim(value, previousStateRef)
    }

    fun withIssuerAndHolder() = CustomCordaClaim(participants = listOf(issuer, holder))
    fun withoutIssuer() = CustomCordaClaim(participants = listOf(holder))
    fun withoutHolder() = CustomCordaClaim(participants = listOf(issuer))
}

class CustomAttestation(
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: AttestationPointer<CustomCordaClaim>,
    status: AttestationStatus,
    metadata: Map<String, String>,
    linearId: UniqueIdentifier,
    previousStateRef: StateRef?
) : Attestation<CustomCordaClaim>(
    attestor,
    attestees,
    pointer,
    status,
    metadata,
    linearId,
    previousStateRef
)

@BelongsToContract(AccountContract::class)
object NotAnAccount : ContractState {
    override val participants: List<AbstractParty> get() = listOf(IDENTITY_A.party)
}

@BelongsToContract(AccountContract::class)
object AccountOwnerIsNotParticipant : Account(IDENTITY_A.party) {
    override val participants: List<AbstractParty> get() = listOf(IDENTITY_B.party)
}
