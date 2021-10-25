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

package io.onixlabs.test.cordapp.contract.attestations

import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationPointer
import io.onixlabs.corda.identityframework.contract.attestations.AttestationStatus
import io.onixlabs.corda.identityframework.contract.attestations.StaticAttestationPointer
import io.onixlabs.test.cordapp.contract.claims.GreetingClaim
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

@BelongsToContract(GreetingAttestationContract::class)
class GreetingAttestation private constructor(
    attestor: AbstractParty,
    attestees: Set<AbstractParty>,
    pointer: AttestationPointer<GreetingClaim>,
    status: AttestationStatus = AttestationStatus.REJECTED,
    metadata: Map<String, String> = emptyMap(),
    linearId: UniqueIdentifier = UniqueIdentifier(),
    previousStateRef: StateRef? = null
) : Attestation<GreetingClaim>(attestor, attestees, pointer, status, metadata, linearId, previousStateRef) {

    constructor(
        attestor: AbstractParty,
        greeting: StateAndRef<GreetingClaim>,
        status: AttestationStatus = AttestationStatus.REJECTED,
        metadata: Map<String, String> = emptyMap(),
        linearId: UniqueIdentifier = UniqueIdentifier()
    ) : this(
        attestor,
        greeting.state.data.participants.toSet(),
        StaticAttestationPointer(greeting),
        status,
        metadata,
        linearId,
        null
    )

    override fun amend(
        previousStateRef: StateRef,
        status: AttestationStatus,
        pointer: AttestationPointer<GreetingClaim>,
        metadata: Map<String, String>
    ) = GreetingAttestation(attestor, attestees, pointer, status, metadata, linearId, previousStateRef)
}
