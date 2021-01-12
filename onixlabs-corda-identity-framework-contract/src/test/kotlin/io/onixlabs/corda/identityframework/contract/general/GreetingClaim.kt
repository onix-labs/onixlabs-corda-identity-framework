package io.onixlabs.corda.identityframework.contract.general

import io.onixlabs.corda.identityframework.contract.Claim

class GreetingClaim(val greeter: String) : Claim<String>("Greeting", "Hello, World!")