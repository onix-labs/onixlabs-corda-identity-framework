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

import io.onixlabs.corda.core.getArgumentType
import io.onixlabs.corda.core.services.QueryDsl
import io.onixlabs.corda.core.services.equalTo
import io.onixlabs.corda.core.toClass
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.contract.CordaClaimSchema

/**
 * Specifies the claim type to be applied to the query criteria.
 *
 * @param T The underlying [CordaClaim] type.
 * @param claimType The [CordaClaim] type to apply to the query criteria.
 * @param valueType The [CordaClaim] value type to apply to the query criteria.
 */
fun <T : CordaClaim<*>> QueryDsl<T>.claimType(claimType: Class<T>, valueType: Class<*>) {
    contractStateTypes(claimType)
    where(CordaClaimSchema.CordaClaimEntity::claimType equalTo claimType.canonicalName)
    where(CordaClaimSchema.CordaClaimEntity::valueType equalTo valueType.canonicalName)
}

/**
 * Specifies the claim type to be applied to the query criteria.
 *
 * @param T The underlying [CordaClaim] type.
 */
inline fun <reified T : CordaClaim<*>> QueryDsl<T>.claimType() {
    val claimType = T::class.java
    val valueType = claimType.getArgumentType(0).toClass()
    claimType(claimType, valueType)
}
