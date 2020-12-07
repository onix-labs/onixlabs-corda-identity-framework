/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.integration

import net.corda.core.messaging.CordaRPCOps
import net.corda.testing.driver.NodeHandle

class ClaimsServices(private val rpc: CordaRPCOps) {
    val commandService: ClaimCommandService by lazy { ClaimCommandService(rpc) }
    val queryService: ClaimQueryService by lazy { ClaimQueryService(rpc) }
}

class AttestationServices(private val rpc: CordaRPCOps) {
    val commandService: AttestationCommandService by lazy { AttestationCommandService(rpc) }
    val queryService: AttestationQueryService by lazy { AttestationQueryService(rpc) }
}

val NodeHandle.claims: ClaimsServices get() = ClaimsServices(rpc)
val NodeHandle.attestations: AttestationServices get() = AttestationServices(rpc)
