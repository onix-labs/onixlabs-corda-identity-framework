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

package io.onixlabs.corda.identityframework.contract.accounts

import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.*

object AccountSchema {

    object AccountSchemaV1 :
        MappedSchema(AccountSchema.javaClass, 1, listOf(AccountEntity::class.java, AccountClaimEntity::class.java)) {
        override val migrationResource = "account-schema.changelog-master"
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

        @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
        @JoinColumns(
            JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
            JoinColumn(name = "output_index", referencedColumnName = "output_index")
        )
        @OrderColumn
        val claims: MutableSet<AccountClaimEntity> = mutableSetOf()
    ) : PersistentState() {

        companion object {
            fun fromAccount(account: Account): AccountEntity {
                val entity = AccountEntity(
                    account.linearId.id,
                    account.linearId.externalId,
                    account.owner
                )

                account.claims.forEach {
                    val claimEntity = AccountClaimEntity(
                        null,
                        it.property,
                        it.value.toString(),
                        it.computeHash().toString(),
                        entity
                    )

                    entity.claims.add(claimEntity)
                }

                return entity
            }
        }
    }

    @Entity
    @Table(name = "onixlabs_account_claims")
    class AccountClaimEntity(
        @Id
        @GeneratedValue
        @Column(name = "id", unique = true, nullable = true)
        val id: UUID? = null,

        @Column(name = "property", nullable = false)
        val property: String = "",

        @Column(name = "value", nullable = false, columnDefinition = "clob")
        val value: String = "",

        @Column(name = "hash", nullable = false)
        val hash: String = "",

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns(
            JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
            JoinColumn(name = "output_index", referencedColumnName = "output_index")
        )
        val account: AccountEntity? = null
    )
}
