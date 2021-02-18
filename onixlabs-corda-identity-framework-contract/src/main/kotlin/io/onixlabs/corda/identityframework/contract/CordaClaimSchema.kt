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

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object CordaClaimSchema {

    object CordaClaimSchemaV1 : MappedSchema(CordaClaimSchema.javaClass, 1, listOf(CordaClaimEntity::class.java)) {
        override val migrationResource = "corda-claim-schema.changelog-master"
    }

    @Entity
    @Table(name = "corda_claim_states")
    class CordaClaimEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "issuer", nullable = false)
        val issuer: AbstractParty = NULL_PARTY,

        @Column(name = "holder", nullable = false)
        val holder: AbstractParty = NULL_PARTY,

        @Column(name = "property", nullable = false)
        val property: String = "",

        @Column(name = "value", nullable = false, columnDefinition = "clob")
        val value: String = "",

        @Column(name = "value_class", nullable = false)
        val valueClass: String = "",

        @Column(name = "previous_state_ref", nullable = true)
        val previousStateRef: String? = null,

        @Column(name = "is_self_issued", nullable = false)
        val isSelfIssued: Boolean = false,

        @Column(name = "hash", nullable = false, unique = true)
        val hash: String = "",

        @Column(name = "claim_class", nullable = false)
        val claimClass: String = ""
    ) : PersistentState()
}
