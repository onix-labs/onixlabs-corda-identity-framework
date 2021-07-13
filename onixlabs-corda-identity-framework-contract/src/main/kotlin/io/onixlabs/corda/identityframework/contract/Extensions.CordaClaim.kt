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
 * @throws IllegalStateException if the amend function of the specified state type cannot be cast to [U].
 *
 * Note that this function tends to fail if you don't override the amend function of custom claim types.
 */
inline fun <T : Any, reified U : CordaClaim<T>> StateAndRef<U>.amend(value: T): U {
    val amendedClaim = state.data.amend(ref, value)

    return try {
        U::class.java.cast(amendedClaim)
    } catch (classCastException: ClassCastException) {
        val baseTypeName = CordaClaim::class.java.canonicalName
        val derivedTypeName = U::class.java.canonicalName
        val returnTypeName = amendedClaim.javaClass.canonicalName

        throw IllegalStateException(buildString {
            append("${classCastException.message}. ")
            append("This typically occurs if a derived type of '$baseTypeName' does not override 'amend'. ")
            append("The 'amend' function of '$derivedTypeName' appears to return '$returnTypeName' instead of '$derivedTypeName'.")
        }, classCastException)
    }
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
