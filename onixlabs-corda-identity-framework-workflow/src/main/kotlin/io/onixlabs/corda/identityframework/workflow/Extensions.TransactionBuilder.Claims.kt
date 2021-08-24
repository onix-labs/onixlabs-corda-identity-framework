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

import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.claims.CordaClaimContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.transactions.TransactionBuilder

/**
 * Adds a claim for issuance to a transaction builder, including the required command.
 *
 * @param claim The claim state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addIssuedClaim(
    claim: CordaClaim<*>
): TransactionBuilder = apply {
    addOutputState(claim)
    addCommand(CordaClaimContract.Issue, claim.issuer.owningKey)
}

/**
 * Adds a claim for amendment to a transaction builder, including the required command.
 *
 * @param oldClaim The old claim state to be consumed in the transaction.
 * @param newClaim The new claim state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addAmendedClaim(
    oldClaim: StateAndRef<CordaClaim<*>>,
    newClaim: CordaClaim<*>
): TransactionBuilder = apply {
    addInputState(oldClaim)
    addOutputState(newClaim)
    addCommand(CordaClaimContract.Amend, newClaim.issuer.owningKey)
}

/**
 * Adds a claim for revocation to a transaction builder, including the required command.
 *
 * @param claim The claim state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
fun TransactionBuilder.addRevokedClaim(
    claim: StateAndRef<CordaClaim<*>>
): TransactionBuilder = apply {
    addInputState(claim)
    addCommand(CordaClaimContract.Revoke, claim.state.data.issuer.owningKey)
}
