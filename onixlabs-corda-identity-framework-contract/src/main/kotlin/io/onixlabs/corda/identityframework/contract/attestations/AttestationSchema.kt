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

package io.onixlabs.corda.identityframework.contract.attestations

import io.onixlabs.corda.identityframework.contract.accountLinearId
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object AttestationSchema {

    object AttestationSchemaV1 : MappedSchema(AttestationSchema.javaClass, 1, listOf(AttestationEntity::class.java)) {
        override val migrationResource = "attestation-schema.changelog-master"
    }

    @Entity
    @Table(name = "onixlabs_attestation_states")
    class AttestationEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "attestor", nullable = false)
        val attestor: AbstractParty = NULL_PARTY,

        @Column(name = "attestor_account_linear_id", nullable = true)
        val attestorAccountLinearId: UUID? = null,

        @Column(name = "attestor_account_external_id", nullable = true)
        val attestorAccountExternalId: String? = null,

        @Column(name = "pointer_state_pointer", nullable = false)
        val pointer: String = "",

        @Column(name = "pointer_state_type", nullable = false)
        val pointerStateType: String = "",

        @Column(name = "pointer_hash", nullable = false)
        val pointerHash: String = "",

        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        val status: AttestationStatus = AttestationStatus.REJECTED,

        @Column(name = "previous_state_ref", nullable = true)
        val previousStateRef: String? = null,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = "",

        @Column(name = "attestation_type", nullable = false)
        val attestationType: String = ""
    ) : PersistentState() {
        internal constructor(attestation: Attestation<*>) : this(
            linearId = attestation.linearId.id,
            externalId = attestation.linearId.externalId,
            attestor = attestation.attestor,
            attestorAccountLinearId = attestation.attestor.accountLinearId?.id,
            attestorAccountExternalId = attestation.attestor.accountLinearId?.externalId,
            pointer = attestation.pointer.statePointer.toString(),
            pointerStateType = attestation.pointer.stateType.canonicalName,
            pointerHash = attestation.pointer.hash.toString(),
            status = attestation.status,
            previousStateRef = attestation.previousStateRef?.toString(),
            hash = attestation.hash.toString(),
            attestationType = attestation.javaClass.canonicalName
        )
    }
}
