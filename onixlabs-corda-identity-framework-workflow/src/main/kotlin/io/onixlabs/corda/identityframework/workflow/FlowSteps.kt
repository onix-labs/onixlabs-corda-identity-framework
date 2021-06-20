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

import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker.Step

/**
 * The progress tracker step that occurs when a flow is initializing.
 */
object INITIALIZING : Step("Initializing flow.")

/**
 * The progress tracker step that occurs when a flow is generating a transaction.
 */
object GENERATING : Step("Generating initial transaction.")

/**
 * The progress tracker step that occurs when a flow is verifying an initial transaction.
 */
object VERIFYING : Step("Verifying initial transaction.")

/**
 * The progress tracker step that occurs when a flow is signing an initial transaction.
 */
object SIGNING : Step("Signing initial transaction.")

/**
 * The progress tracker step that occurs when a flow is gathering counter-party signatures (counter-signing).
 */
object COUNTERSIGNING : Step("Gathering counter-party signatures.") {
    override fun childProgressTracker() = CollectSignaturesFlow.tracker()
}

/**
 * The progress tracker step that occurs when a flow is finalizing a transaction.
 */
object FINALIZING : Step("Finalizing signed transaction.") {
    override fun childProgressTracker() = FinalityFlow.tracker()
}

/**
 * The progress tracker step that occurs when a flow is recording a transaction.
 */
object RECORDING : Step("Recording finalized transaction.")

/**
 * The progress tracker step that occurs when a flow is sending a transaction.
 */
object SENDING : Step("Sending transaction.")

/**
 * The progress tracker step that occurs when a flow is receiving a transaction.
 */
object RECEIVING : Step("Receiving transaction.")
