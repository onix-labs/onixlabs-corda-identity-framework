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

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountParty
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.ServiceHub
import net.corda.core.utilities.parsePublicKeyBase58

/**
 * Determines whether the current Corda node owns the specified party identity.
 *
 * @param party The identity to determine is owned by the current Corda node.
 * @return Returns true if the current Corda node owns the specified party identity; otherwise, false.
 */
fun ServiceHub.ownsLegalIdentity(party: AbstractParty): Boolean {
    val partyToResolve = if (party is AccountParty) party.owner else party
    val wellKnownParty = identityService.wellKnownPartyFromAnonymous(partyToResolve)

    return wellKnownParty in myInfo.legalIdentities
}

/**
 * Resolves the specified value to an [AbstractParty].
 *
 * @param value The value to resolve to an [AbstractParty].
 * @param type The type of account to resolve, if the value represents an [AccountParty].
 * @return Returns an [AbstractParty] resolved from the specified value.
 * @throws IllegalArgumentException if the specified value cannot be resolved to an [AbstractParty].
 */
fun ServiceHub.resolveParty(value: String, type: Class<out Account> = Account::class.java): AbstractParty {
    return if (AccountParty.DELIMITER in value) resolveAccountParty(value, type) else resolveAbstractParty(value)
}

/**
 * Resolves the specified value to an [AbstractParty].
 *
 * @param value The value to resolve to an [AbstractParty].
 * @return Returns an [AbstractParty] resolved from the specified value.
 * @throws IllegalArgumentException if the specified value cannot be resolved to an [AbstractParty].
 */
fun ServiceHub.resolveAbstractParty(value: String): AbstractParty {
    return try {
        val parsedCordaX500Name = CordaX500Name.parse(value)
        identityService.wellKnownPartyFromX500Name(parsedCordaX500Name)
    } catch (ex: Exception) {
        val parsedPublicKey = parsePublicKeyBase58(value)
        AnonymousParty(parsedPublicKey)
    } ?: throw IllegalArgumentException("Failed to resolve '$value' to party.")
}

/**
 * Resolves the specified value to an [AccountParty].
 *
 * @param value The value to resolve to an [AccountParty].
 * @param type The type of account to resolve.
 * @return Returns an [AccountParty] resolved from the specified value.
 * @throws IllegalArgumentException if the specified value cannot be resolved to an [AccountParty].
 */
fun ServiceHub.resolveAccountParty(value: String, type: Class<out Account> = Account::class.java): AccountParty {
    val identityComponent = value.substringAfter(AccountParty.DELIMITER)
    val linearIdComponent = value.substringBefore(AccountParty.DELIMITER)

    val identity = resolveAbstractParty(identityComponent)
    val linearId = UniqueIdentifier.fromString(linearIdComponent)

    return AccountParty(identity, linearId, type)
}
