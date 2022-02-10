# ⚡ resiliency-with-arrowkt

## Background 

Consider you backend service makes calls to external service(s) and you would like to introduce [resliency](https://hackernoon.com/lets-talk-about-resilience-97051e14761f) to your API. 

The tools that you have in your toolkit are: 

- 🛠 **Kotlin** language to target the JVM 
- 🛠 **SpringBoot** as the web-framework
- 🛠 **RestTemplate** to make external API calls
- 🛠 **ArrowKT** to avoid exceptions

## Target 

![Diagram](https://www.planttext.com/api/plantuml/svg/LO_12i9034Jl-OgXdliBOgqzU1F4a_ImT6DTt6ObZQ0K_zrjyTBSvWsPJ7QZERNN42dopM096lgxmGVbpa8IgZfBYil8IZDoqaT6iNVTWyKNm0HQN9HbkSMrJcAQOcHjQQMmsDNfY3e65clfIv9ypchUnYekulmPZOS2cyCoc0YhZV-9cJJYdodCBCViSfFymWy0)

```puml
@startuml
left to right direction
Actor User
rectangle "Backend"  { 
 User ---> [Controller]
 [Controller] --> [API Client]
}
node "External System" #DDDDDD {
  [API Client] ---> [API Endpoint] 
}
@enduml
```

We will implement the [Circuit-Breaker](https://martinfowler.com/bliki/CircuitBreaker.html) pattern in this Target picture

## Error Handling Strategy 

An errors arising during making a call to the External System are to be converted into ArrowKT [Either](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/) objects - which are then processed by the API controller. 

| | Condition  | Status Code | Retryable 
|-|------------- | ------------- |-------------|
|1.|External System responds with 5xx error  | Backend responds with 502  | Yes 
|2.|External System responds with 4xx error  | Backend responds with 502  | No
|3.|External System responds with 4xx error (**due to `User` input**)  | Backend responds with 400  | No

## Resiliency Strategy

We will implement two resiliency patterns: 

### 1. Circuit-Breaker
If there is a progressive degradation of the External System, calls to the system will be blocked. 

If the Circuit-Breaker trips (due to say, the External System, being down) - the API Endpoint in our backend system would respond with a `503 Service Unavailable` 

### 2. Retry
If there is an occassional failure during communication with the External System, the failed call will be retried automatically.

## Testing

1. All logic is driven through an end-to-end [test](https://github.com/vyvyd/resiliency-with-arrowkt/blob/main/src/test/kotlin/com/kotlin/resiliency/ResiliencyApplicationTests.kt)

2. All calls to the External System is mocked through [WireMock](http://wiremock.org/) in the end-to-end test. 

# Resiliency Solution 

A Resilience4J based implementation of API Client (modelled as a [Decorator](https://refactoring.guru/design-patterns/decorator)): 

```kotlin
class ResilientAPIClient(
	private val apiClient: Client,
	private val circuitBreaker: CircuitBreaker = newCircuitBreaker(name="backend"),
	private val retry: Retry = newRetry(name="backend")
): Client {

	override fun getCustomers() =
        circuitBreaker.executeEitherKT {
            retry.executeEitherKT {
                apiClient.getCustomers()
            }
        }
}
```

Do consider that most of the Reslience4J logic is hidden behind the two custom extension methods `circuitBreaker.executeEitherKT` and `retry.executeEitherKT`.

## Implementation Details 

### 1. Using Reslience4J with ArrowKT

This is done through the two extension functions [here](https://github.com/vyvyd/resiliency-with-arrowkt/blob/main/src/main/kotlin/com/kotlin/resiliency/external/resilience4j/Resilience4JExtensions.kt).

The methods evaluate a provided supplier returning an `Either` object. If the `Either` resolves to an `Either.Left`, an exception is thrown which kicks in the Resilience4J behavior. 

---

#### Alternate approach: Copy `executeSupplier` implementation  
This is the  existing `executeSupplier` [implementation](https://github.com/resilience4j/resilience4j/blob/master/resilience4j-circuitbreaker/src/main/java/io/github/resilience4j/circuitbreaker/CircuitBreaker.java#L189) method. 

We can duplicate this method, and write an implementation that is specifically to be used for ArrowKT `Either` objects. This [library](https://mvnrepository.com/artifact/com.duytsev/resilience4j-arrowkt) seems to be following the same approach.

**Disadvantage**  
I did not prefer this approach because this code we write will have to evolve lock-step with the implementation of the actual `executeSupplier` method. This can be risky with a future update of the Resilience4J library version which might change the definition of the `executeSupplier` method.

---

### 2. Prefer using Decorators 

The Reslience4J logic is also encapsulated in an specific `APIClient` instance. This enable selective injection of this interface during application-load based on a feature-toggle. 

With Kotlin's [Delegation](https://kotlinlang.org/docs/delegation.html#overriding-a-member-of-an-interface-implemented-by-delegation) feature, it becomes easily to selectively introduce Resiliency to 'some' API calls where as we can ignore others, 
 
## Extra Reading

https://www.dynatrace.com/news/blog/the-cost-of-an-exception/


