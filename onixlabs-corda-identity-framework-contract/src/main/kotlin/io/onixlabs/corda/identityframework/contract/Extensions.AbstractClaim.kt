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

/**
 * Determines whether a collection of [AbstractClaim] contains duplicate properties.
 *
 * @param property The property to search for duplicates.
 * @param ignoreCase Determines whether to ignore case when searching for duplicates.
 * @return Returns true if the collection contains duplicates; otherwise, false.
 */
fun Iterable<AbstractClaim<*>>.containsDuplicateProperties(
    property: String? = null,
    ignoreCase: Boolean = false
): Boolean {
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
fun Iterable<AbstractClaim<*>>.checkForDuplicateProperties(
    property: String? = null,
    ignoreCase: Boolean = false,
    message: String = "The claim collection contains duplicate keys."
) = check(!containsDuplicateProperties(property, ignoreCase)) { message }
