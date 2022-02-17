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

package io.onixlabs.corda.identityframework.workflow.accounts

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import java.sql.ResultSet

/**
 * Gets a [List] of [StateRef] for accounts by querying for account claims.
 *
 * @property property The property of a claim linked to an account.
 * @property value The value of a claim linked to an account.
 * @property hash The hash of a claim linked to an account.
 */
@StartableByRPC
@StartableByService
class GetAccountStateRefsByClaimFlow(
    private val property: String? = null,
    private val value: Any? = null,
    private val hash: SecureHash? = null
) : FlowLogic<List<StateRef>?>() {

    private companion object {

        /**
         * The name of the transaction ID column.
         */
        const val TRANSACTION_ID = "transaction_id"

        /**
         * The name of the transaction output index column.
         */
        const val OUTPUT_INDEX = "output_index"
    }

    @Suspendable
    override fun call(): List<StateRef>? {
        return if (property == null && value == null && hash == null) null else executeQuery()
    }

    /**
     * Builds the SQL query.
     *
     * @return Returns a [String] representing the SQL query to be executed.
     */
    private fun buildQuery(): String {
        return buildString {
            val criteria = mutableListOf<String>()

            property?.let { criteria.add("property = '$it'") }
            value?.let { criteria.add("value = '$it'") }
            hash?.let { criteria.add("hash = '$it'") }

            appendln("select $TRANSACTION_ID, $OUTPUT_INDEX")
            appendln("from onixlabs_account_claims")
            appendln("where ${criteria.joinToString("\nand ")}")
        }
    }

    /**
     * Executes the SQL query.
     *
     * @return Returns a [List] of [StateRef] representing the recorded accounts.
     */
    @Suspendable
    private fun executeQuery(): List<StateRef> {
        return with(serviceHub.jdbcSession()) {
            prepareStatement(nativeSQL(buildQuery())).use {
                it.executeQuery().use(::getStateRefsFromResults)
            }
        }
    }

    /**
     * Gets a [List] of [StateRef] representing the recorded accounts.
     *
     * @param results The [ResultSet] from the executed SQL query containing the table query data.
     * @return Returns a [List] of [StateRef] representing the recorded accounts.
     */
    @Suspendable
    private fun getStateRefsFromResults(results: ResultSet): List<StateRef> {
        return mutableListOf<StateRef>().apply {
            while (results.next()) {
                val txHash = SecureHash.parse(results.getString(TRANSACTION_ID))
                val index = results.getInt(OUTPUT_INDEX)
                add(StateRef(txHash, index))
            }
        }
    }
}
