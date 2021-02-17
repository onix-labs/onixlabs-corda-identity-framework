package io.onixlabs.corda.identityframework.workflow

import io.onixlabs.corda.identityframework.contract.CordaClaim
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TypeReferenceTests {

    @Test
    fun `TypeReference should resolve type of Integer`() {
        val actual = object : TypeReference<Int>() {}.type.typeName
        val expected = "java.lang.Integer"
        assertEquals(expected, actual)
    }

    @Test
    fun `TypeReference should resolve type of CordaClaim`() {
        val actual = object : TypeReference<CordaClaim<*>>() {}.type.typeName
        val expected = "io.onixlabs.corda.identityframework.contract.CordaClaim"
        assertEquals(expected, actual)
    }

    @Test
    fun `TypeReference should resolve type arguments of CordaClaim`() {
        val actual = object : TypeReference<CordaClaim<String>>() {}.arguments[0].typeName
        val expected = "java.lang.String"
        assertEquals(expected, actual)
    }
}