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

import io.onixlabs.corda.core.contract.AbstractSingularResolvable
import io.onixlabs.corda.core.contract.SingularResolvable
import io.onixlabs.corda.core.services.vaultQuery
import net.corda.core.contracts.PartyAndReference
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.OpaqueBytes
import java.util.*

/**
 * Represents a party that resolves back to an account.
 *
 * @property owner The underlying party that owns the account.
 * @property accountLinearId The linear ID of the account associated with this party.
 * @property accountType The type of the account associated with this party.
 */
class AccountParty(
    val owner: AbstractParty,
    val accountLinearId: UniqueIdentifier,
    val accountType: Class<out Account>
) : AbstractParty(owner.owningKey) {

    /**
     * @property DELIMITER The delimiter that will be used to format the account party.
     */
    companion object {
        const val DELIMITER = '@'
    }

    /**
     * Determines whether this [AccountParty] owns the specified [Account].
     *
     * @param account The account for which to determine ownership.
     * @return Returns true if this [AccountParty] owns the specified [Account]; otherwise, false.
     */
    fun owns(account: Account): Boolean {
        return this == account.toAccountParty()
    }

    /**
     * Gets an account resolver to resolve this [AccountParty] back to the associated [Account].
     *
     * @param T The underlying account type of the associated account.
     * @param type The account type of the associated account.
     * @return Returns an account resolver to resolve this [AccountParty] back to the associated [Account].
     * @throws IllegalStateException if the specified type does not match the [accountType].
     */
    fun <T : Account> getAccountResolver(type: Class<T>): SingularResolvable<T> {
        check(type == accountType) { "Account type mismatch. Expected account type:${accountType.canonicalName}." }
        return AccountResolver(accountLinearId, type)
    }

    /**
     * Gets an account resolver to resolve this [AccountParty] back to the associated [Account].
     *
     * @param T The underlying account type of the associated account.
     * @return Returns an account resolver to resolve this [AccountParty] back to the associated [Account].
     * @throws IllegalStateException if the specified type does not match the [accountType].
     */
    inline fun <reified T : Account> getAccountResolver(): SingularResolvable<T> {
        return getAccountResolver(T::class.java)
    }

    /**
     * Gets the name of the account owner; or null if the account owner is unknown.
     *
     * @return Returns the name of the account owner; or null if the account owner is unknown.
     */
    override fun nameOrNull(): CordaX500Name? {
        return owner.nameOrNull()
    }

    /**
     * Builds a reference to an object being stored or issued by a party.
     *
     * @param bytes The bytes of the object being referenced.
     * @return Returns a party and reference of this object instance.
     */
    override fun ref(bytes: OpaqueBytes): PartyAndReference {
        return owner.ref(bytes)
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is AccountParty
                && other.owner == owner
                && other.accountLinearId == accountLinearId
                && other.accountType == accountType)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(owner, accountLinearId, accountType)
    }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String {
        return "$accountLinearId$DELIMITER$owner"
    }

    /**
     * Represents an account resolver which resolves accounts by linear ID and type.
     *
     * @param T The underlying account type of the account to resolve.
     * @property accountLinearId The linear ID of the account to resolve.
     * @property accountType The account type of the account to resolve.
     */
    private class AccountResolver<T : Account>(
        private val accountLinearId: UniqueIdentifier,
        private val accountType: Class<T>
    ) : AbstractSingularResolvable<T>() {

        override val contractStateType: Class<T> get() = accountType

        override val criteria get() = vaultQuery(accountType) {
            contractStateTypes(accountType)
            linearIds(accountLinearId)
        }

        /**
         * Determines whether this [SingularResolvable] is pointing to the specified [StateAndRef] instance.
         *
         * @param stateAndRef The [StateAndRef] to determine being pointed to.
         * @return Returns true if this [SingularResolvable] is pointing to the specified [StateAndRef]; otherwise, false.
         */
        override fun isPointingTo(stateAndRef: StateAndRef<T>): Boolean {
            return accountLinearId == stateAndRef.state.data.linearId
        }
    }
}
