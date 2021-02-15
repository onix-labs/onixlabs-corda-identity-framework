package io.onixlabs.test.cordapp.workflow.claims

import io.onixlabs.corda.core.contract.cast
import io.onixlabs.corda.identityframework.contract.CordaClaim
import io.onixlabs.corda.identityframework.workflow.FindClaimsFlow
import io.onixlabs.corda.identityframework.workflow.IssueClaimFlow
import io.onixlabs.test.cordapp.contract.GreetingClaim
import io.onixlabs.test.cordapp.workflow.FlowTest
import io.onixlabs.test.cordapp.workflow.Pipeline
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FindClaimsFlowValueTypeTests : FlowTest() {

    override fun initialize() {
        Pipeline
            .create(network)
            .run(nodeA) {
                val claim = CordaClaim(partyA, partyB, "greeting", "abc")
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = GREETING_CLAIM
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = CordaClaim(partyA, "greeting", 12345)
                IssueClaimFlow.Initiator(claim)
            }
            .run(nodeA) {
                val claim = CordaClaim(partyA, "greeting", false)
                IssueClaimFlow.Initiator(claim)
            }
    }

    @Test
    fun `FindClaimsFlow should only return all claims when the value type isn't specified`() {
        Pipeline
            .create(network)
            .run(nodeA) {
                FindClaimsFlow(
                    claimClass = CordaClaim::class.java,
                    property = "greeting"
                )
            }
            .finally {
                val claims = it.map { it.cast<CordaClaim<Any>>() }
                assertEquals(3, claims.size)
            }
    }

    @Test
    fun `FindClaimsFlow should only return a single claim when the value type is specified`() {
        Pipeline
            .create(network)
            .run(nodeA) {
                FindClaimsFlow(
                    claimClass = CordaClaim::class.java,
                    valueClass = String::class.java,
                    property = "greeting"
                )
            }
            .finally {
                val claims = it.map { it.cast<CordaClaim<String>>() }
                assertEquals(1, claims.size)
                assertEquals("Hello, World!", claims.single().state.data.value)
            }
    }
}
