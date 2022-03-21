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

package io.onixlabs.corda.identityframework.workflow.attestations

import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import io.onixlabs.corda.identityframework.contract.createAcceptedStaticAttestation
import io.onixlabs.corda.identityframework.workflow.FlowTest
import io.onixlabs.corda.identityframework.workflow.Pipeline
import io.onixlabs.corda.identityframework.workflow.claims.IssueClaimFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IssueAttestationFlowCheckTests : FlowTest() {

    private val linearId = UniqueIdentifier()
    private val identifier = "IDENTIFIER"

    private lateinit var claim1: StateAndRef<CordaClaim<String>>
    private lateinit var claim2: StateAndRef<CordaClaim<Int>>

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                IssueClaimFlow.Initiator(CLAIM_1, observers = setOf(partyC))
            }
            .run(nodeB) {
                claim1 = it.tx.outRefsOfType<CordaClaim<String>>().single()
                IssueClaimFlow.Initiator(CLAIM_2, observers = setOf(partyC))
            }
            .run(nodeC) {
                claim2 = it.tx.outRefsOfType<CordaClaim<Int>>().single()
                val attestation = claim1.createAcceptedStaticAttestation(attestor = partyC, linearId = linearId)
                IssueAttestationFlow.Initiator(attestation)
            }
    }

    @Test
    fun `IssueAttestationFlow should fail when an unconsumed attestation with an identical linear ID already exists`() {
        val exception = assertFailsWith<FlowException> {
            Pipeline
                .create(network)
                .run(nodeC) {
                    val attestation = claim1.createAcceptedStaticAttestation(partyC, linearId = linearId)
                    IssueAttestationFlow.Initiator(attestation)
                }
        }

        assertEquals("An unconsumed attestation with an identical linear ID already exists: $linearId.", exception.message)
    }

    @Test
    fun `IssueAttestationFlow should fail when an unconsumed attestation with an identical state pointer already exists`() {
        val exception = assertFailsWith<FlowException> {
            Pipeline
                .create(network)
                .run(nodeC) {
                    val attestation = claim1.createAcceptedStaticAttestation(partyC)
                    IssueAttestationFlow.Initiator(attestation)
                }
        }

        assertEquals("An unconsumed attestation with an identical state pointer already exists: ${claim1.ref}.", exception.message)
    }
}
