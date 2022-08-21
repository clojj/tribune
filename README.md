Tribune
=========================

_parse don't validate_ for Kotlin.

Inspired by [this blog post by Alexis King](https://lexi-lambda.github.io/blog/2019/11/05/parse-don-t-validate/), this
library
provides a toolset for creating simple parsers from raw _input_ types, to properly validated _parsed_ types.

![master](https://github.com/sksamuel/tribune/workflows/master/badge.svg)
[<img src="https://img.shields.io/maven-central/v/com.sksamuel.tribune/tribune-core.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Ctribune)
[<img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sksamuel.tribune/tribune-core.svg?label=latest%20snapshot&style=plastic"/>](https://oss.sonatype.org/content/repositories/snapshots/com/sksamuel/tribune)

### Rationale

Usually, when we have a system that accepts input, we validate that input. That is, we run some checks on the inputs,
throwing an error if they don't meet our requirements. For example that a string has a max length or is not null, and so
on. Then we continue with the original request safe in the knowledge that we've done our due diligence.

Here is an extremely simplified example.

```kotlin
fun validate(email: String) = name.contains("@")
fun persist(email: String) { ... write to db ... }
fun process(email: String) {
  if (!validate(email)) error("Not a real email")
  persist(email)
}
```

But the ultimate receiver of the input has to take it on faith that the input was validated.
There's no compiler checks this is the case, since we are still using the original types. And since we use a compiled
language to help us catch errors, why aren't we using it to help us catch validation errors.

In larger systems, we sometimes find that validation is done in multiple places. As we go deeper into the stack, and our
code grows more complex, we sometimes perform multiple validations 'just to be sure'. We don't trust that the callers of
our code are giving us properly validated types, so we check again, just in case.

If we could indicate through types that our input had already and categorically been validated, then we could trust that
input. One way to do this is to have a type that represents the "checked and validated" result of the original input.

This is what we mean when we say _parsing not validating_.

### Getting Started

Start by creating a parser from your raw type. This could be a string or another type marshalled from JSON for example.
A possibly nullable string is a very common starting point, so let's use that, and ~~validate~~ parse to a full name.

```kotlin
val parser: Parser<String?, Nothing, Nothing> = Parser.fromNullableString()
```

### Examples

```kotlin
data class Address(
   val city: City,
   val zip: Zipcode,
   val country: CountryCode,
)

data class City(val value: String)
data class Zipcode(val value: String)
data class CountryCode(val value: String)

val cityParser = Parser
   .nonBlankString { "City must be provided" }
   .map { City(it) }

val zipcodeParser = Parser
   .nonBlankString { "Zipcode must be provided" }
   .length(5) { "Zipcode should be 5 digits" }
   .map { Zipcode(it) }

val countryCodeParser = Parser
   .nonBlankString { "CountryCode must be provided" }
   .length(2) { "CountryCode should be 2 digits" }
   .map { CountryCode(it) }

val addressParser = Parser.compose(
   cityParser,
   zipcodeParser,
   countryCodeParser,
   ::Address
)
```

### Ktor Integration

Tribune provides Ktor integration through the additional `tribune-ktor` module. Add this to your build
and `withParsedInput`
becomes available inside your Ktor routes. This method retrieves the request object as an instance of the parser input
type,
and then passes it to the parser. If any errors are returned, a handler is invoked to return a 400, otherwise the given
function is invoked with the parse result.

In the following example, we parse input using the `isbnParser` which requires 10 or 13 digit ISBN codes, and 13 digit
codes must start with a 9.

This parser is then used inside a POST endpoint and if valid, we respond with a 201.

```kotlin
val isbnParser =
   Parser.fromNullableString()
      .notNullOrBlank { "ISBN must be provided" }
      .length({ it == 10 || it == 13 }) { "Valid ISBNs have length 10 or 13" }
      .filter({ it.length == 10 || it.startsWith("9") }, { "13 Digit ISBNs must start with 9" })
      .map { Isbn(it) }
```

```kotlin
routing {
   post("myendpoint") {
      withParsedInput(isbnParser) { parsed ->
         println("Parsed input $parsed")
         call.respond(HttpStatusCode.Created)
      }
   }
}
```

### Changelog

### 1.2.4 (Pending)

* Added `fromNullableString` to Parser.

#### 1.2.3

* Updated to Ktor2

#### 1.2.2

* Renamed to Tribune

#### 1.2.1

* Added set parser
* Renamed .repeated to .asList
* Removed internal custom valid/invalid extensions

#### 1.2.0

* Package renames

#### 1.1.1

* Added ktor output handler

#### 1.1.0

* Renamed project to Optio
* Added datetime module
* Added ktor module
* Added zip and compose

### Using tribune in your project

```groovy
compile 'com.sksamuel.tribune:tribune-core:x.x.x'
```
