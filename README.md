![ONIX Labs](https://raw.githubusercontent.com/onix-labs/onixlabs-website/main/src/assets/images/logo/full/original/original-md.png)

# ONIXLabs Corda Identity Framework

The ONIXLabs Corda Identity Framework provides a powerful framework for building highly scalable, interoperable, digital and decentralised identities for individuals, organisations and digital assets on Corda.

The design and implementation of this framework is influenced by the W3C specifications for Verifiable Credentials and Decentralised Identifiers and will continue to build upon these concepts in future versions.

[W3C Verifiable Credentials Data Model 1.0](https://www.w3.org/TR/vc-data-model/)

[W3C Decentralised Identifiers (DIDs) 1.0](https://www.w3.org/TR/did-core/)

## Objectives

The ONIXLabs Corda Identity Framework aims to address the following objectives:

-   Allow individuals and organisations to take control of their digital identity.
-   Provide a data layer protocol for application interoperability across Corda applications.
-   Ensure some level of data privacy and trust through cryptographic security.
-   Alleviate the need for centralised and/or federated control.

## Claims

A claim is a statement or assertion about a subject, and are expressed as a subject-property/value relationship. A claim subject represents something about which claims are made; for example an individual, organisation or digital asset. Claims offer several benefits over traditional data models:

-   All digital and completely under the owner's control.
-   Highly scalable and cohesive.
-   Much harder to fake the claim or impersonate the claim owner.
-   Eliminates the need for treasure troves of data.
-   Enables minimum disclosure.
-   Enables zero-knowledge proofs (ZKPs).
-   Data by itself becomes useless to thieves.

Claims in the ONIXLabs Corda Identity Framework are implemented as a comprehensive and open type hierarchy, allowing developers to consume and utilize claims immediately, or extend the framework to build custom claim types on top of the existing infrastructure.

### Corda Claims

Corda claims are part of the open claims type hierarchy. They are contract states that represent a bi-lateral fact between an issuer and a holder, or alternatively may represent a unilateral fact where the issuer and holder identities are the same (known as a self-issued claim). Corda claims _may_ evolve over time; that is, their value is subject to change. Corda claims allow information to be distributed on a need-to-know basis.

### Claim Pointers

Claim pointers are also claims themselves; specifically they are claims whose value points to and resolves a known Corda claim. The intention of claim pointers is to allow claims to be referenced, whilst keeping the underlying claim data private. There are two types of claim pointer:

-   Linear claim pointers which always point to the latest version of a claim.
-   Static claim pointers which always point a specific version of a claim.

### Claim Type Hierarchy

```
AbstractClaim<T>
├─ Claim<T>
│  └─ EmptyClaim
├─ CordaClaim<T>
│  └─ AttachmentClaim
└─ ClaimPointer<T>
   ├─ LinearClaimPointer<T>
   └─ StaticClaimPointer<T>
```

## Attestations

Attestations represent a type of proof that a particular state has been witnessed and that its value has been accepted or rejected. The purpose of attestations is to enable trust of claims, or indeed any Corda state. Verifiers may accept an attestation to a particular claim or Corda state, without needing to see the claim or state itself, which enables privacy through trust.

