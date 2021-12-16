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

package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountParty
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.parsePublicKeyBase58

fun CordaRPCOps.ownsLegalIdentity(party: AbstractParty): Boolean {
    val partyToResolve = if (party is AccountParty) party.owner else party
    val wellKnownParty = wellKnownPartyFromAnonymous(partyToResolve)

    return wellKnownParty in nodeInfo().legalIdentities
}

fun CordaRPCOps.resolveParty(value: String, type: Class<out Account> = Account::class.java): AbstractParty {
    return if (AccountParty.DELIMITER in value) resolveAccountParty(value, type) else resolveAbstractParty(value)
}

fun CordaRPCOps.resolveAbstractParty(value: String): AbstractParty {
    return try {
        val parsedCordaX500Name = CordaX500Name.parse(value)
        wellKnownPartyFromX500Name(parsedCordaX500Name)
    } catch (ex: Exception) {
        val parsedPublicKey = parsePublicKeyBase58(value)
        AnonymousParty(parsedPublicKey)
    } ?: throw IllegalArgumentException("Failed to resolve '$value' to party.")
}

fun CordaRPCOps.resolveAccountParty(value: String, type: Class<out Account> = Account::class.java): AccountParty {
    val identityComponent = value.substringAfter(AccountParty.DELIMITER)
    val linearIdComponent = value.substringBefore(AccountParty.DELIMITER)

    val identity = resolveAbstractParty(identityComponent)
    val linearId = UniqueIdentifier.fromString(linearIdComponent)

    return AccountParty(identity, linearId, type)
}
