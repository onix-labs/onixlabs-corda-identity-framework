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

package io.onixlabs.corda.identityframework.workflow.accounts

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import java.sql.ResultSet

@StartableByRPC
@StartableByService
class GetAccountStateRefsByClaimFlow(
    private val property: String? = null,
    private val value: Any? = null,
    private val hash: SecureHash? = null
) : FlowLogic<List<StateRef>>() {

    private companion object {
        const val TRANSACTION_ID = "transaction_id"
        const val OUTPUT_INDEX = "output_index"
    }

    @Suspendable
    override fun call(): List<StateRef> {
        return if (property == null && value == null && hash == null) emptyList() else executeQuery(buildString {
            val criteria = mutableListOf<String>()

            property?.let { criteria.add("property = '$it'") }
            value?.let { criteria.add("value = '$it'") }
            hash?.let { criteria.add("hash = '$it'") }

            appendln("select $TRANSACTION_ID, $OUTPUT_INDEX")
            appendln("from onixlabs_account_claims")
            appendln("where ${criteria.joinToString("\nand ")}")
        })
    }

    @Suspendable
    private fun executeQuery(query: String): List<StateRef> {
        return with(serviceHub.jdbcSession()) {
            val nativeQuery = nativeSQL(query)
            prepareStatement(nativeQuery).use {
                it.executeQuery().use(::getStateRefsFromResults)
            }
        }
    }

    @Suspendable
    private fun getStateRefsFromResults(results: ResultSet): List<StateRef> {
        val stateRefs = mutableListOf<StateRef>()

        while (results.next()) {
            val txHash = SecureHash.parse(results.getString(TRANSACTION_ID))
            val index = results.getInt(OUTPUT_INDEX)
            stateRefs.add(StateRef(txHash, index))
        }

        return stateRefs
    }
}
