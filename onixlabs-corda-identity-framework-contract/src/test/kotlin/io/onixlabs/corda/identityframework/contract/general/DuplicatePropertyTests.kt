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

package io.onixlabs.corda.identityframework.contract.general

import io.onixlabs.corda.identityframework.contract.checkForDuplicateProperties
import io.onixlabs.corda.identityframework.contract.claims.Claim
import io.onixlabs.corda.identityframework.contract.containsDuplicateProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DuplicatePropertyTests {

    @Test
    fun `containsDuplicateProperties should return false when a collection does not contain duplicates`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("PROPERTY1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val result = claims.containsDuplicateProperties()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `containsDuplicateProperties should return true when a collection does contain duplicates (ignore case)`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("PROPERTY1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val result = claims.containsDuplicateProperties(ignoreCase = true)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `containsDuplicateProperties should return false when a collection does not contain duplicates (property specified)`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("PROPERTY1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val result = claims.containsDuplicateProperties("Property1")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `containsDuplicateProperties should return true when a collection does not contain duplicates (property specified, ignore case)`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("PROPERTY1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val result = claims.containsDuplicateProperties("Property1", ignoreCase = true)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `containsDuplicateProperties should return false when a collection does not contain duplicates (distinct property specified)`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("PROPERTY1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val result = claims.containsDuplicateProperties("Property3")

        // Assert
        assertFalse(result)
    }

    @Test
    fun `checkForDuplicateProperties should throw an exception when duplicate properties are detected`() {

        // Arrange
        val claims = listOf(
            Claim("Property1", Unit),
            Claim("Property1", Unit),
            Claim("Property2", Unit),
            Claim("Property3", Unit)
        )

        // Act
        val exception = assertThrows<IllegalStateException> {
            claims.checkForDuplicateProperties()
        }

        // Assert
        assertEquals("The claim collection contains duplicate keys.", exception.message)
    }
}
