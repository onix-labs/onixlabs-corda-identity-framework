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

package io.onixlabs.corda.identityframework.workflow

import co.paralleluniverse.fibers.Suspendable
import io.onixlabs.corda.core.services.VaultService
import io.onixlabs.corda.core.services.any
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import net.corda.core.flows.FlowException
import net.corda.core.node.services.Vault

@Suspendable
internal fun VaultService<Attestation<*>>.checkAttestationExistsWithIdenticalLinearId(attestation: Attestation<*>) {
    if (any { linearIds(attestation.linearId) }) {
        throw FlowException("An unconsumed attestation with an identical linear ID already exists: ${attestation.linearId}.")
    }
}

@Suspendable
internal fun VaultService<Attestation<*>>.checkAttestationWithIdenticalStatePointerExists(attestation: Attestation<*>) {
    if (any { attestationPointer(attestation.pointer.statePointer) }) {
        throw FlowException("An unconsumed attestation with an identical state pointer already exists: ${attestation.pointer.statePointer}.")
    }
}

@Suspendable
internal fun VaultService<Attestation<*>>.checkAttestationWithIdenticalHashExists(attestation: Attestation<*>) {
    if (any { stateStatus(Vault.StateStatus.ALL); relevancyStatus(Vault.RelevancyStatus.ALL); attestationHash(attestation.hash) }) {
        throw FlowException("An attestation with an identical hash already exists: ${attestation.pointer.hash}.")
    }
}
