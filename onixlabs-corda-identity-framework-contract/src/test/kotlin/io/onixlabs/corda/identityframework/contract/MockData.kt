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

import net.corda.core.contracts.BelongsToContract
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

val EMPTY_REF = StateRef(SecureHash.zeroHash, 0)

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
