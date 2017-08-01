# Raffia

Raffia is a Java library which provides a persistent JSON-like data structure, called a *Basket*, together with an API for creating, validating, querying, mapping and transforming instances of that structure.

The standard way of handling a JSON-structured document in Java is to map it into an [anaemic domain model](https://martinfowler.com/bliki/AnemicDomainModel.html), which provides type-safe access to the values held in the document. Raffia offers an alternative, which operates on the document itself as a first-class object.

Raffia is especially suited to those cases where we are only concerned with querying or modifying a small part of the document, and do not need to reflect its entire structure within the Java type system. For query-only access, the best thing to use for this approach is [https://github.com/json-path/JsonPath]. Raffia