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

package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.identityframework.contract.AttestationStatus.ACCEPTED
import io.onixlabs.corda.identityframework.contract.AttestationStatus.REJECTED
import net.corda.core.serialization.CordaSerializable

/**
 * Defines the status of an attestation.
 *
 * @property ACCEPTED The attestation has been accepted.
 * @property REJECTED The attestation has been rejected.
 */
@CordaSerializable
enum class AttestationStatus { ACCEPTED, REJECTED }
