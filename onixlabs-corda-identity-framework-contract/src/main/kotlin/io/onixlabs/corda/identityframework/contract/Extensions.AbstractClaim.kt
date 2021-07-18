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

import io.onixlabs.corda.core.toTypedClass
import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import io.onixlabs.corda.identityframework.contract.claims.ClaimTypeInfo

private typealias Claims = Iterable<AbstractClaim<*>>

/**
 * Determines whether a collection of [AbstractClaim] contains duplicate properties.
 *
 * @param property The property to search for duplicates.
 * @param ignoreCase Determines whether to ignore case when searching for duplicates.
 * @return Returns true if the collection contains duplicates; otherwise, false.
 */
fun Claims.containsDuplicateProperties(property: String? = null, ignoreCase: Boolean = false): Boolean {
    val properties = map { if (ignoreCase) it.property.toLowerCase() else it.property }
    val filteredProperties = properties.filter { property?.equals(it, ignoreCase) ?: true }
    return filteredProperties.size != filteredProperties.distinct().size
}

/**
 * Checks a collection of [AbstractClaim] instances for duplicate properties.
 *
 * @param property The property to search for duplicates.
 * @param ignoreCase Determines whether to ignore case when searching for duplicates.
 * @param message The exception message to throw if the collection contains duplicate keys.
 * @throws IllegalStateException if the collection contains duplicate keys.
 */
fun Claims.checkForDuplicateProperties(
    property: String? = null,
    ignoreCase: Boolean = false,
    message: String = "The claim collection contains duplicate keys."
) = check(!containsDuplicateProperties(property, ignoreCase)) { message }

/**
 * Casts an [AbstractClaim] to the specified type.
 *
 * @param T The underlying [AbstractClaim] type to cast to.
 * @param type The [AbstractClaim] type to cast to.
 * @return Returns an instance of [T] cast from this [AbstractClaim].
 * @throws ClassCastException if this instance cannot be cast to the specified type [T].
 */
fun <T : AbstractClaim<*>> AbstractClaim<*>.cast(type: Class<T>): T {
    return type.cast(this)
}

/**
 * Casts an [AbstractClaim] to the specified type.
 *
 * @param T The underlying [AbstractClaim] type to cast to.
 * @return Returns an instance of [T] cast from this [AbstractClaim].
 * @throws ClassCastException if this instance cannot be cast to the specified type [T].
 */
inline fun <reified T : AbstractClaim<*>> AbstractClaim<*>.cast(): T {
    return cast(T::class.java)
}

/**
 * Casts an [Iterable] of [AbstractClaim] to the specified type.
 *
 * @param T The underlying [AbstractClaim] type to cast to.
 * @param type The [AbstractClaim] type to cast to.
 * @return Returns an [Iterable] of [T] cast from this [Iterable] of [AbstractClaim].
 * @throws ClassCastException if any instance cannot be cast to the specified type [T].
 */
fun <T : AbstractClaim<*>> Claims.cast(type: Class<T>): List<T> {
    return map { it.cast(type) }
}

/**
 * Casts an [Iterable] of [AbstractClaim] to the specified type.
 *
 * @param T The underlying [AbstractClaim] type to cast to.
 * @return Returns an [Iterable] of [T] cast from this [Iterable] of [AbstractClaim].
 * @throws ClassCastException if any instance cannot be cast to the specified type [T].
 */
inline fun <reified T : AbstractClaim<*>> Claims.cast(): List<T> {
    return cast(T::class.java)
}

/**
 * Filters an [Iterable] of [AbstractClaim] by the specified claim type, and optionally by the value type.
 *
 * @param T The underlying claim value type.
 * @param U The underlying claim type.
 * @param claimType The claim type to filter by.
 * @param valueType The claim value type to filter by.
 * @return Returns a [List] of [U] specified by the claim type, and optionally by the value type.
 */
fun <T, U : AbstractClaim<in T>> Claims.filterByType(claimType: Class<U>, valueType: Class<in T>? = null): List<U> {
    return filter { it.javaClass == claimType }
        .filterIsInstance(claimType)
        .filter { claim -> valueType?.let { claim.value?.javaClass == it } ?: true }
}

/**
 * Filters an [Iterable] of [AbstractClaim] by the specified claim type.
 *
 * @param T The underlying claim type.
 * @return Returns a [List] of [T] specified by the claim type.
 */
inline fun <reified T : AbstractClaim<*>> Claims.filterByType(): List<T> {
    val claimTypeInfo = object : ClaimTypeInfo<T>() {}
    return filterByType(claimTypeInfo.claimType.toTypedClass(), claimTypeInfo.claimValueType)
}

/**
 * Filters an [Iterable] of [AbstractClaim] by the specified property.
 *
 * @param T The underlying claim type.
 * @param property The property to filter by.
 * @param ignoreCase Determines whether to ignore the property case when filtering.
 * @return Returns an [Iterable] of [AbstractClaim] by the specified property.
 */
fun <T : AbstractClaim<*>> Iterable<T>.filterByProperty(property: String, ignoreCase: Boolean = false): List<T> {
    return filter { it.property.equals(property, ignoreCase) }
}
