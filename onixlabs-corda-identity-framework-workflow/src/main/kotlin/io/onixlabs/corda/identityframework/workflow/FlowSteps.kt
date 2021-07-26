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

import net.corda.core.utilities.ProgressTracker.Step

/**
 * Represents a progress tracker step indicating that a claim transaction is being sent.
 */
object SendClaimTransactionStep : Step("Sending claim transaction.")

/**
 * Represents a progress tracker step indicating that a claim transaction is being received.
 */
object ReceiveClaimTransactionStep : Step("Receiving claim transaction.")

/**
 * Represents a progress tracker step indicating that an attestation transaction is being sent.
 */
object SendAttestationTransactionStep : Step("Sending claim transaction.")

/**
 * Represents a progress tracker step indicating that an attestation transaction is being received.
 */
object ReceiveAttestationTransactionStep : Step("Receiving claim transaction.")

/**
 * Represents a progress tracker step indicating that an account transaction is being sent.
 */
object SendAccountTransactionStep : Step("Sending account transaction.")

/**
 * Represents a progress tracker step indicating that an account transaction is being received.
 */
object ReceiveAccountTransactionStep : Step("Receiving account transaction.")
