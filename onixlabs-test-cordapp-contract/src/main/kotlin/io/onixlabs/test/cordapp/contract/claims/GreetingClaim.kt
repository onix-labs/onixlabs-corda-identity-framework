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

package io.onixlabs.test.cordapp.contract.claims

import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

@BelongsToContract(GreetingClaimContract::class)
class GreetingClaim(
    issuer: AbstractParty,
    holder: AbstractParty,
    property: String = "greeting",
    value: String = "Hello, World!",
    linearId: UniqueIdentifier = UniqueIdentifier(),
    previousStateRef: StateRef? = null
) : CordaClaim<String>(issuer, holder, property, value, linearId, previousStateRef) {

    override fun amend(previousStateRef: StateRef, value: String): CordaClaim<String> {
        return GreetingClaim(issuer, holder, property, value, linearId, previousStateRef)
    }
}
