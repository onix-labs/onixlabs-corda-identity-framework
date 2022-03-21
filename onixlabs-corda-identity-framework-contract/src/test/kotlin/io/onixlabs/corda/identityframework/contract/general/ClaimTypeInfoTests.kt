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
import io.onixlabs.corda.identityframework.contract.claims.AbstractClaim
import io.onixlabs.corda.identityframework.contract.claims.Claim
import io.onixlabs.corda.identityframework.contract.claims.ClaimTypeInfo
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClaimTypeInfoTests {

    @Test
    fun `ClaimTypeInfo of type ExampleNumberClaim should produce the expected result`() {
        val info = object : ClaimTypeInfo<ExampleNumberClaim>() {}

        assertEquals(ExampleNumberClaim::class.java, info.claimType)
        assertEquals(Integer::class.java, info.claimValueType)
    }

    @Test
    fun `ClaimTypeInfo of type Claim of BigDecimal should produce the expected result`() {
        val info = object : ClaimTypeInfo<Claim<BigDecimal>>() {}

        assertEquals(Claim::class.java, info.claimType)
        assertEquals(BigDecimal::class.java, info.claimValueType)
    }

    @Test
    fun `ClaimTypeInfo of type Claim of unknown type should produce the expected result`() {
        val info = object : ClaimTypeInfo<Claim<*>>() {}

        assertEquals(Claim::class.java, info.claimType)
        assertNull(info.claimValueType)
    }

    @Test
    fun `ClaimTypeInfo of type AbstractClaim of BigDecimal should produce the expected result`() {
        val info = object : ClaimTypeInfo<AbstractClaim<BigDecimal>>() {}

        assertEquals(AbstractClaim::class.java, info.claimType)
        assertEquals(BigDecimal::class.java, info.claimValueType)
    }

    @Test
    fun `ClaimTypeInfo of type AbstractClaim of unknown type should produce the expected result`() {
        val info = object : ClaimTypeInfo<AbstractClaim<*>>() {}

        assertEquals(AbstractClaim::class.java, info.claimType)
        assertNull(info.claimValueType)
    }
}
