package io.onixlabs.corda.identityframework.v1.contract.general

import io.onixlabs.corda.identityframework.v1.contract.Claim

class GreetingClaim(val greeter: String) : Claim<String>("Greeting", "Hello, World!")