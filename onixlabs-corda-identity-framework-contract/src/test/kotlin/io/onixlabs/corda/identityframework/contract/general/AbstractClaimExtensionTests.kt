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

package io.onixlabs.corda.identityframework.contract.general

import io.onixlabs.corda.identityframework.contract.ExampleNumberClaim
import io.onixlabs.corda.identityframework.contract.ExampleStringClaim
import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import io.onixlabs.corda.identityframework.contract.claims.Claim
import io.onixlabs.corda.identityframework.contract.filterByType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AbstractClaimExtensionTests {

    private val claims: Iterable<AbstractClaim<*>> = listOf(
        Claim("a", 123),
        Claim("b", 456),
        Claim("c", "Hello, World!"),
        Claim("d", true),
        Claim("e", false),
        ExampleStringClaim("f", "abc"),
        ExampleStringClaim("g", "xyz"),
        ExampleNumberClaim("h", 123),
        ExampleNumberClaim("i", 456)
    )

    @Test
    fun `filterByType should filter all claims of type Claim where the value type is unknown`() {
        val result = claims.filterByType(Claim::class.java)
        assertEquals(5, result.size)
    }

    @Test
    fun `filterByType should filter all claims of type Claim where the value type is Integer`() {
        val result = claims.filterByType(Claim::class.java, Integer::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all claims of type Claim where the value type is String`() {
        val result = claims.filterByType(Claim::class.java, String::class.java)
        assertEquals(1, result.size)
    }

    @Test
    fun `filterByType should filter all claims of type Claim where the value type is Boolean`() {
        val result = claims.filterByType(Claim::class.java, java.lang.Boolean::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all claims of type ExampleStringClaim`() {
        val result = claims.filterByType(ExampleStringClaim::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterByType should filter all claims of type ExampleNumberClaim`() {
        val result = claims.filterByType(ExampleNumberClaim::class.java)
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type Claim where the value type is unknown`() {
        val result = claims.filterByType<Claim<*>>()
        assertEquals(5, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type Claim where the value type is Integer`() {
        val result = claims.filterByType<Claim<Int>>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type Claim where the value type is String`() {
        val result = claims.filterByType<Claim<String>>()
        assertEquals(1, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type Claim where the value type is Boolean`() {
        val result = claims.filterByType<Claim<Boolean>>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type ExampleStringClaim`() {
        val result = claims.filterByType<ExampleStringClaim>()
        assertEquals(2, result.size)
    }

    @Test
    fun `inline filterByType should filter all claims of type ExampleNumberClaim`() {
        val result = claims.filterByType<ExampleNumberClaim>()
        assertEquals(2, result.size)
    }
}
