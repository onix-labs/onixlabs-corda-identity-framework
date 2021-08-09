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

package io.onixlabs.corda.identityframework.contract.accounts

import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.*
import java.io.Serializable
import java.util.*
import javax.persistence.*
import kotlin.jvm.Transient

object AccountSchema {

    object AccountSchemaV1 :
        MappedSchema(AccountSchema.javaClass, 1, listOf(AccountEntity::class.java, AccountClaim::class.java)) {
        override val migrationResource = super.migrationResource
    }

    @Entity
    @Table(name = "onixlabs_account_states")
    class AccountEntity(
        @Column(name = "linear_id", nullable = false)
        val linearId: UUID = UUID.randomUUID(),

        @Column(name = "external_id", nullable = true)
        val externalId: String? = null,

        @Column(name = "owner", nullable = false)
        val owner: AbstractParty = NULL_PARTY,

        @OneToMany(cascade = [CascadeType.PERSIST])
        @JoinColumns(
            JoinColumn(name = "output_index", referencedColumnName = "output_index"),
            JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
        )
        val claims: List<AccountClaim> = emptyList()
    ) : PersistentState() {
        constructor(account: Account) : this(
            account.linearId.id,
            account.linearId.externalId,
            account.owner,
            account.claims.map { AccountClaim(it) })
    }

    @Entity
    @Table(name = "onixlabs_account_claims")
    class AccountClaim(
        @Column(name = "property", nullable = false)
        val property: String = "",

        @Column(name = "value", nullable = false, columnDefinition = "clob")
        val value: String = "",

        @Column(name = "hash", nullable = false)
        val hash: String = "",

        @EmbeddedId
        override val compositeKey: Key
        ) : IndirectStatePersistable<Key> {
        constructor(claim: AbstractClaim<*>) : this(
            //UUID.randomUUID(),
            claim.property,
            claim.value.toString(),
            claim.computeHash().toString(),
            Key()
        )
    }

    class Key(override val stateRef: PersistentStateRef? = null) : DirectStatePersistable, Serializable {
        override fun equals(other: Any?): Boolean {
            return this === other || (other is Key && other.stateRef == stateRef)
        }

        override fun hashCode(): Int {
            return Objects.hash(stateRef)
        }
    }
}
