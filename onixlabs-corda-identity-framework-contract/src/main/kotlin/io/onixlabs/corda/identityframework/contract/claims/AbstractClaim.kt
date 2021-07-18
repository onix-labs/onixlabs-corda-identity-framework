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

package io.onixlabs.corda.identityframework.contract.claims

import io.onixlabs.corda.identityframework.contract.toDataClassString
import net.corda.core.serialization.CordaSerializable
import java.util.*

/**
 * Represents the base implementation for a claim.
 *
 * @param T The underlying type of the claim value.
 * @property property The property of the claim.
 * @property value The value of the claim.
 */
@CordaSerializable
abstract class AbstractClaim<T : Any> {
    abstract val property: String
    abstract val value: T

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        return this === other || (other is AbstractClaim<*>
                && other.javaClass == javaClass
                && other.property == property
                && other.value == value)
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int {
        return Objects.hash(property, value)
    }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String {
        return toDataClassString()
    }
}
