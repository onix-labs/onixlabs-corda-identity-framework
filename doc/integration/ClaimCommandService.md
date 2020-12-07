# <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/class.svg" style="zoom:50%;" />ClaimCommandService

Represents the claim command service.

---

## <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/constructor.svg" style="zoom:50%;" />Primary Constructor

Creates a new instance of the claim command service.

### <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameters.svg" style="zoom:33%;" />Parameters

| Parameter                                                    | Type        | Description                                            |
| ------------------------------------------------------------ | ----------- | ------------------------------------------------------ |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> rpc | CordaRPCOps | The Corda RPC instance that that service will bind to. |

### Example

The following example demonstrates how to create a new claim command service instance.

```kotlin
val service = ClaimCommandService(rpc)
```

---

## <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/method.svg" style="zoom:50%;" />issueClaim Method

Issues a new Corda claim and distributes it between the issuer, holder and optional observers.

### <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameters.svg" style="zoom:33%;" />Parameters

| Parameter                                                    | Type             | Description                              |
| ------------------------------------------------------------ | ---------------- | ---------------------------------------- |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/typeparameter.svg" style="zoom:25%;" /> T | Any              | The underlying claim value type.         |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> property | String           | The property of the claim.               |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> value | T                | The property of the claim.               |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> issuer | AbstractParty    | The issuer of the claim.                 |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> holder | AbstractParty    | The holder of the claim.                 |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> linearId | UniqueIdentifier | The unique identifier of the claim.      |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> notary | Party?           | The notary to use for the transaction.   |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> observers | Set&lt;Party&gt; | Additional observers of the transaction. |

### <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/returnvalue.svg" style="zoom:33%;" />Return Value

Returns `FlowProgressHandle<SignedTransaction>` which represents a flow progress handle to the claim issuance flow, and subsequent transaction result.

---

## <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/method.svg" style="zoom:50%;" />amendClaim Method

Amends an existing Corda claim and distributes it between the issuer, holder and optional observers.

### <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameters.svg" style="zoom:33%;" />Parameters

| Parameter                                                    | Type                                   | Description                              |
| ------------------------------------------------------------ | -------------------------------------- | ---------------------------------------- |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/typeparameter.svg" style="zoom:25%;" /> T | Any                                    | The underlying claim value type.         |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> claim | StateAndRef&lt;CordaClaim&lt;T&gt;&gt; | The claim to be consumed.                |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> value | T                                      | The amended claim value.                 |
| <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/parameter.svg" style="zoom:25%;" /> observers | Set&lt;Party&gt;                       | Additional observers of the transaction. |

### <img src="https://raw.githubusercontent.com/onix-labs/onix-labs.github.io/master/content/icons/code_docs/returnvalue.svg" style="zoom:33%;" />Return Value

Returns `FlowProgressHandle<SignedTransaction>` which represents a flow progress handle to the claim amendment flow, and subsequent transaction result.

