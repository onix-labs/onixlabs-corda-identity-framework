/**
 * Copyright 2020 Matthew Layton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.corda.identityframework.contract.general

import io.onixlabs.corda.identityframework.contract.Claim
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DataClassStringTests {

    @Test
    fun `toDataClassString called on Claim should produce the expected result`() {
        val claim = Claim("Test Claim", 123)

        val expected = "Claim(property = Test Claim, value = 123)"
        val actual = claim.toString()

        assertEquals(expected, actual)
    }
}
