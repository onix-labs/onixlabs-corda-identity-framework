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

import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.PortAllocation
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import net.corda.testing.node.internal.cordappsForPackages

abstract class IntegrationTest : AutoCloseable {

    private companion object {
        val RPC_USERS = listOf(User("guest", "letmein", permissions = setOf("ALL")))
        val log = loggerFor<IntegrationTest>()
    }

    private lateinit var _nodeA: NodeHandle
    private lateinit var _nodeB: NodeHandle
    private lateinit var _nodeC: NodeHandle

    protected val nodeA: NodeHandle get() = _nodeA
    protected val nodeB: NodeHandle get() = _nodeB
    protected val nodeC: NodeHandle get() = _nodeC

    protected val partyA: Party get() = nodeA.nodeInfo.legalIdentities.first()
    protected val partyB: Party get() = nodeB.nodeInfo.legalIdentities.first()
    protected val partyC: Party get() = nodeC.nodeInfo.legalIdentities.first()

    fun start(action: () -> Unit) {
        val parameters = DriverParameters(
            isDebug = true,
            startNodesInProcess = true,
            waitForAllNodesToFinish = false,
            cordappsForAllNodes = cordappsForPackages(
                "io.onixlabs.corda.identityframework.contract",
                "io.onixlabs.corda.identityframework.workflow"
            ),
            portAllocation = object : PortAllocation() {
                private var start = 10000
                override fun nextPort(): Int = start++
            },
            networkParameters = testNetworkParameters(
                minimumPlatformVersion = 7
            )
        )

        driver(parameters) {
            _nodeA = startNode(providedName = IDENTITY_A.name, rpcUsers = RPC_USERS).getOrThrow()
            _nodeB = startNode(providedName = IDENTITY_B.name, rpcUsers = RPC_USERS).getOrThrow()
            _nodeC = startNode(providedName = IDENTITY_C.name, rpcUsers = RPC_USERS).getOrThrow()

            listOf(_nodeA, _nodeB, _nodeC).forEach {
                val identity = it.nodeInfo.legalIdentities.first()
                val rpcAddress = it.rpcAddress
                log.info("Node registered with RPC address '$rpcAddress' for node '$identity'.")
            }

            initialize()
            action()
            finalize()
            close()
        }
    }

    protected open fun initialize() = Unit
    protected open fun finalize() = Unit

    override fun close() = listOf(nodeA, nodeB, nodeC).forEach {
        it.stop()
        it.close()
    }
}
