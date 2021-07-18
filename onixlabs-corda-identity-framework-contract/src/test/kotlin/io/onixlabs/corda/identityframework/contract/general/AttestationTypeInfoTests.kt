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

import io.onixlabs.corda.identityframework.contract.CustomAttestation
import io.onixlabs.corda.identityframework.contract.CustomCordaClaim
import io.onixlabs.corda.identityframework.contract.attestations.Attestation
import io.onixlabs.corda.identityframework.contract.attestations.AttestationTypeInfo
import io.onixlabs.corda.identityframework.contract.claims.CordaClaim
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AttestationTypeInfoTests {

    @Test
    fun `AttestationTypeInfo of type CustomAttestation should produce the expected result`() {
        val info = object : AttestationTypeInfo<CustomAttestation>() {}

        assertEquals(CustomAttestation::class.java, info.attestationType)
        assertEquals(CustomCordaClaim::class.java, info.attestationStateType)
    }

    @Test
    fun `AttestationTypeInfo of type Attestation of CordaClaim should produce the expected result`() {
        val info = object : AttestationTypeInfo<Attestation<CordaClaim<*>>>() {}

        assertEquals(Attestation::class.java, info.attestationType)
        assertEquals(CordaClaim::class.java, info.attestationStateType)
    }

    @Test
    fun `AttestationTypeInfo of type Attestation of unknown type should produce the expected result`() {
        val info = object : AttestationTypeInfo<Attestation<*>>() {}

        assertEquals(Attestation::class.java, info.attestationType)
        assertNull(info.attestationStateType)
    }
}
