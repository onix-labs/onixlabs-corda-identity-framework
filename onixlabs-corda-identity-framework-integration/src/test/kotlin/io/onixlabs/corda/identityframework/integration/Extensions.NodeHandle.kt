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

package io.onixlabs.corda.identityframework.integration

import net.corda.core.crypto.SecureHash
import net.corda.testing.driver.NodeHandle

val NodeHandle.claimService: ClaimService get() = ClaimService(rpc)
val NodeHandle.attestationService: AttestationService get() = AttestationService(rpc)

/**
 * Sometimes, integration tests cause race conditions, whereby a vault query begins executing before the database
 * has persisted new states, thus causing the tests to fail. This function, albeit hacky ensures that a transaction
 * has been recorded before performing vault queries, thus reducing the likeliness of a test failure due to a race
 * condition.
 */
fun NodeHandle.waitForTransaction(id: SecureHash, millisecondsToSleep: Long = 100) {
    while (id !in rpc.stateMachineRecordedTransactionMappingSnapshot().map { it.transactionId }) {
        Thread.sleep(millisecondsToSleep)
    }
}