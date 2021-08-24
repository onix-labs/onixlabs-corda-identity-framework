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

/**
 * Adds a claim for issuance to a transaction builder, including the required command.
 *
 * @param state The claim state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addIssuedClaim(state: CordaClaim<*>): Builder = apply {
    addOutputState(state)
    addCommand(CordaClaimContract.Issue, state.issuer.owningKey)
}

/**
 * Adds a claim for amendment to a transaction builder, including the required command.
 *
 * @param oldState The old claim state to be consumed in the transaction.
 * @param newState The new claim state to be created in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addAmendedClaim(oldState: StateAndRef<CordaClaim<*>>, newState: CordaClaim<*>): Builder = apply {
    addInputState(oldState)
    addOutputState(newState)
    addCommand(CordaClaimContract.Amend, newState.issuer.owningKey)
}

/**
 * Adds a claim for revocation to a transaction builder, including the required command.
 *
 * @param state The claim state to be consumed in the transaction.
 * @return Returns the current transaction builder.
 */
fun Builder.addRevokedClaim(state: StateAndRef<CordaClaim<*>>): Builder = apply {
    addInputState(state)
    addCommand(CordaClaimContract.Revoke, state.state.data.issuer.owningKey)
}
