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
import net.corda.core.contracts.TypeOnlyCommandData
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.NullKeys.NULL_PARTY
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.contracts.DummyContract
import net.corda.testing.core.singleIdentity
import net.corda.testing.internal.vault.DummyLinearContract
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.internal.cordappsForPackages
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class FlowTest {

    protected val LINEAR_ID_1 = UniqueIdentifier("External ID 1")
    protected val LINEAR_ID_2 = UniqueIdentifier("External ID 2")
    protected val LINEAR_ID_3 = UniqueIdentifier("External ID 3")

    protected val CLAIM_1 by lazy { CordaClaim(partyA, partyB, "Greeting", "Hello, World!", LINEAR_ID_1) }
    protected val CLAIM_2 by lazy { CordaClaim(partyB, "Number", 123, LINEAR_ID_2) }
    protected val CLAIM_3 by lazy { CordaClaim(partyC, "Time", Instant.now(), LINEAR_ID_3) }

    protected class CustomCordaClaim : CordaClaim<BigDecimal>(NULL_PARTY, "Custom", BigDecimal.ZERO)

    protected val CONTRACT_STATE by lazy { DummyContract.SingleOwnerState(owner = partyA) }
    protected val LINEAR_STATE by lazy { DummyLinearContract.State(participants = listOf(partyA)) }

    protected object COMMAND : TypeOnlyCommandData()

    private lateinit var _network: MockNetwork
    protected val network: MockNetwork get() = _network

    private lateinit var _notaryNode: StartedMockNode
    protected val notaryNode: StartedMockNode get() = _notaryNode
    private lateinit var _notaryParty: Party
    protected val notaryParty: Party get() = _notaryParty

    private lateinit var _nodeA: StartedMockNode
    protected val nodeA: StartedMockNode get() = _nodeA
    private lateinit var _partyA: Party
    protected val partyA: Party get() = _partyA

    private lateinit var _nodeB: StartedMockNode
    protected val nodeB: StartedMockNode get() = _nodeB
    private lateinit var _partyB: Party
    protected val partyB: Party get() = _partyB

    private lateinit var _nodeC: StartedMockNode
    protected val nodeC: StartedMockNode get() = _nodeC
    private lateinit var _partyC: Party
    protected val partyC: Party get() = _partyC

    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    @BeforeAll
    private fun setup() {
        _network = MockNetwork(
            MockNetworkParameters(
                networkParameters = testNetworkParameters(minimumPlatformVersion = 10),
                cordappsForAllNodes = cordappsForPackages(
                    "io.onixlabs.corda.core.workflow",
                    "io.onixlabs.corda.identityframework.contract",
                    "io.onixlabs.corda.identityframework.workflow",
                    "net.corda.testing.internal.vault",
                    "net.corda.testing.contracts"
                )
            )
        )

        _notaryNode = network.defaultNotaryNode
        _nodeA = network.createPartyNode(CordaX500Name("PartyA", "London", "GB"))
        _nodeB = network.createPartyNode(CordaX500Name("PartyB", "New York", "US"))
        _nodeC = network.createPartyNode(CordaX500Name("PartyC", "Paris", "FR"))

        _notaryParty = notaryNode.info.singleIdentity()
        _partyA = nodeA.info.singleIdentity()
        _partyB = nodeB.info.singleIdentity()
        _partyC = nodeC.info.singleIdentity()

        initialize()
    }

    @AfterAll
    private fun tearDown() {
        network.stopNodes()
        finalize()
    }
}
