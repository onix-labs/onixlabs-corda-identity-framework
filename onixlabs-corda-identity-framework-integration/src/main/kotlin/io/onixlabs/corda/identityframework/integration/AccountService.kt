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

package io.onixlabs.corda.identityframework.integration

import io.onixlabs.corda.core.integration.RPCService
import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.workflow.accounts.AmendAccountFlow
import io.onixlabs.corda.identityframework.workflow.accounts.IssueAccountFlow
import io.onixlabs.corda.identityframework.workflow.accounts.PublishAccountFlow
import io.onixlabs.corda.identityframework.workflow.accounts.RevokeAccountFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.*
import net.corda.core.transactions.SignedTransaction
import java.util.*

class AccountService(rpc: CordaRPCOps) : RPCService(rpc) {

    /**
     * Issues an account.
     *
     * @param account The account to issue.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun issueAccount(
        account: Account,
        notary: Party? = null,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            IssueAccountFlow::Initiator,
            account,
            notary,
            observers
        )
    }

    /**
     * Issues an account.
     *
     * @param account The account to issue.
     * @param notary The notary to use for the transaction.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun issueAccount(
        account: Account,
        notary: Party? = null,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            IssueAccountFlow::Initiator,
            account,
            notary,
            observers
        )
    }

    /**
     * Amends an account.
     *
     * @param oldAccount The old account to be consumed.
     * @param newAccount The new account to be created.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun amendAccount(
        oldAccount: StateAndRef<Account>,
        newAccount: Account,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            AmendAccountFlow::Initiator,
            oldAccount,
            newAccount,
            observers
        )
    }

    /**
     * Amends an account.
     *
     * @param oldAccount The old account to be consumed.
     * @param newAccount The new account to be created.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun amendAccount(
        oldAccount: StateAndRef<Account>,
        newAccount: Account,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            AmendAccountFlow::Initiator,
            oldAccount,
            newAccount,
            observers
        )
    }

    /**
     * Revokes an account.
     *
     * @param account The old account to be consumed.
     * @param observers Additional observers of the transaction.
     * @return Returns a flow process handle.
     */
    fun revokeAccount(
        account: StateAndRef<Account>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            RevokeAccountFlow::Initiator,
            account,
            observers
        )
    }

    /**
     * Revokes an account.
     *
     * @param account The old account to be consumed.
     * @param observers Additional observers of the transaction.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun revokeAccount(
        account: StateAndRef<Account>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            RevokeAccountFlow::Initiator,
            account,
            observers
        )
    }

    /**
     * Publishes an account.
     *
     * @param account The account to be published.
     * @param observers Observers of the attestation.
     * @return Returns a flow process handle.
     */
    fun publishAccount(
        account: StateAndRef<Account>,
        observers: Set<Party> = emptySet()
    ): FlowProgressHandle<SignedTransaction> {
        return rpc.startTrackedFlow(
            PublishAccountFlow::Initiator,
            account,
            observers
        )
    }

    /**
     * Publishes an account.
     *
     * @param account The account to be published.
     * @param observers Observers of the attestation.
     * @param clientId The client ID of the started flow.
     * @return Returns a flow process handle.
     */
    fun publishAccount(
        account: StateAndRef<Account>,
        observers: Set<Party> = emptySet(),
        clientId: String = UUID.randomUUID().toString()
    ): FlowHandleWithClientId<SignedTransaction> {
        return rpc.startFlowWithClientId(
            clientId,
            PublishAccountFlow::Initiator,
            account,
            observers
        )
    }
}
