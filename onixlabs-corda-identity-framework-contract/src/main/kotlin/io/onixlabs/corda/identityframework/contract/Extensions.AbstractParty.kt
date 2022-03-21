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

package io.onixlabs.corda.identityframework.contract

import io.onixlabs.corda.identityframework.contract.accounts.Account
import io.onixlabs.corda.identityframework.contract.accounts.AccountParty
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

/**
 * Gets the account linear ID from an [AccountParty], or null of this [AbstractParty] is not an [AccountParty].
 */
val AbstractParty.accountLinearId: UniqueIdentifier?
    get() = if (this is AccountParty) accountLinearId else null

/**
 * Gets the account type from an [AccountParty], or null of this [AbstractParty] is not an [AccountParty].
 */
val AbstractParty.accountType: Class<out Account>?
    get() = if (this is AccountParty) accountType else null
