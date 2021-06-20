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

package io.onixlabs.corda.identityframework.contract

import net.corda.core.contracts.StateAndRef

/**
 * Amends a Corda claim.
 *
 * @param T The underlying value type of the claim.
 * @param U The underlying [CordaClaim] type.
 * @param value The amended claim value.
 * @return Returns an amended claim.
 */
inline fun <T : Any, reified U : CordaClaim<T>> StateAndRef<U>.amend(value: T): U = try {
    U::class.java.cast(state.data.amend(ref, value))
} catch (ex: ClassCastException) {
    val message = "${ex.message}. Did you forget to override ${U::class.java.simpleName}.amend?"
    throw IllegalStateException(message, ex)
}

/**
 * Creates a static claim pointer from a Corda claim.
 * A static claim pointer always points to a specific version of a claim.
 *
 * @param T The underlying [CordaClaim] type.
 * @return Returns a static claim pointer from a Corda claim.
 */
fun <T : CordaClaim<*>> StateAndRef<T>.toStaticPointer(): StaticClaimPointer<T> {
    return StaticClaimPointer(this)
}

/**
 * Creates a linear claim pointer from a Corda claim.
 * A linear claim pointer always points to the latest version of a claim.
 *
 * @param T The underlying [CordaClaim] type.
 * @return Returns a linear claim pointer from a Corda claim.
 */
fun <T : CordaClaim<*>> T.toLinearPointer(): LinearClaimPointer<T> {
    return LinearClaimPointer(this)
}

/**
 * Creates a linear claim pointer from a Corda claim.
 * A linear claim pointer always points to the latest version of a claim.
 *
 * @param T The underlying [CordaClaim] type.
 * @return Returns a linear claim pointer from a Corda claim.
 */
fun <T : CordaClaim<*>> StateAndRef<T>.toLinearPointer(): LinearClaimPointer<T> {
    return LinearClaimPointer(this.state.data)
}
