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

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.Attachment
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.AttachmentId
import net.corda.core.transactions.LedgerTransaction
import java.io.InputStream

/**
 * Represents a claim that resolves to a Corda attachment.
 *
 * @property issuer The issuer of the attachment claim.
 * @property holder The holder of the attachment claim.
 * @property property The property of the attachment claim.
 * @property value The value of the attachment claim, which is the attachment ID.
 * @property linearId The unique identifier of the attachment claim.
 * @property previousStateRef The state reference of the claim that precedes this one.
 * @property isSelfIssued Determines whether the attachment claim has been self-issued.
 * @property hash The unique hash which represents this attachment claim.
 * @property participants The participants of this attachment claim; namely the issuer and the holder.
 */
@BelongsToContract(CordaClaimContract::class)
class AttachmentClaim(
    issuer: AbstractParty,
    holder: AbstractParty,
    property: String,
    value: AttachmentId,
    linearId: UniqueIdentifier = UniqueIdentifier(),
    previousStateRef: StateRef? = null
) : CordaClaim<AttachmentId>(issuer, holder, property, value, linearId, previousStateRef) {

    /**
     * Amends the claim value.
     *
     * @param previousStateRef The state reference of the claim that precedes this one.
     * @param value The amended claim value.
     * @return Returns an amended claim.
     */
    override fun amend(previousStateRef: StateRef, value: AttachmentId): CordaClaim<AttachmentId> {
        return AttachmentClaim(issuer, holder, property, value, linearId, previousStateRef)
    }

    /**
     * Resolves this claim to an attachment.
     *
     * @param cordaRPCOps The [CordaRPCOps] instance to use to resolve the attachment.
     * @return Returns the resolved attachment [InputStream].
     */
    fun resolve(cordaRPCOps: CordaRPCOps): InputStream {
        return cordaRPCOps.openAttachment(value)
    }

    /**
     * Resolves this claim to an attachment.
     *
     * @param serviceHub The [ServiceHub] instance to use to resolve the attachment.
     * @return Returns the resolved [Attachment], or null if no matching attachment is found.
     */
    fun resolve(serviceHub: ServiceHub): Attachment? {
        return serviceHub.attachments.openAttachment(value)
    }

    /**
     * Resolves this claim to an attachment.
     *
     * @param transaction The [LedgerTransaction] instance to use to resolve the attachment.
     * @return Returns the resolved [Attachment], or null if no matching attachment is found.
     */
    fun resolve(transaction: LedgerTransaction): Attachment? {
        return transaction.attachments.singleOrNull { it.id == value }
    }
}
