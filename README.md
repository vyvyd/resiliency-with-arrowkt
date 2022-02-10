# ⚡ resiliency-with-arrowkt

## Background 

Consider you backend service makes calls to external service(s) and you would like to introduce [resliency](https://hackernoon.com/lets-talk-about-resilience-97051e14761f) to your API. 

The tools that you have in your toolkit are: 

- 🛠 **Kotlin** language to target the JVM 
- 🛠 **SpringBoot** as the web-framework
- 🛠 **RestTemplate** to make external API calls
- 🛠 **ArrowKT** to avoid exception-driven logical flows

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

1. The single API endpoint that is exposed by our service will respond with the follow non-200 OK HTTP status codes 


| | Condition  | Status Code | Retryable 
|-|------------- | ------------- |-------------|
|1.|External System responds with 5xx error  | Backend responds with 502  | Yes 
|2.|External System responds with 4xx error  | Backend responds with 502  | No
|3.|External System responds with 4xx error (**due to `User` input**)  | Backend responds with 400  | No

In this example project, we will not handle case 3.

## Background Details

### Error Handling using ArrowKT

1. The API Client class will not throw exceptions.
2. Operations will respond with an [Either](https://arrow-kt.io/docs/apidocs/arrow-core/arrow.core/-either/). 
3. Based on the response from the External System,the `Either` will contain a `ExternalAPIResponse` or `ExternalAPIError` object.
4. [Sealed classes](https://kotlinlang.org/docs/sealed-classes.html) are used to model both these types of objects
5. Results are then handled throught an exhaustive `when` [block](https://kotlinlang.org/docs/control-flow.html#when-expression). 

### Testing and Demonstration

1. Everything is driven through an end-to-end `SpringBootTest` which is named `ResiliencyApplicationTests`
2. The External System is mocked through [WireMock](http://wiremock.org/) 

## Solution Details 

For every solution, we must consider that Resilience4J primarily works with Exceptions. Since we don't use Exceptions in our own code (for example, in the APIClients, we will face a few small challenges. 

### Rejected Approaches 

**1. Using a RestTemplateInterceptor**
| | Advantage  | Disadvantage |
|-|------------- | ------------- |
|1.| All calls in the application automatically have resiliency built in | Client-By-Client configuration is not possible (which is usually what you need) |
|2.| | `Interceptors` will need to throw Exceptions to indicate failure (for example, when a CircuitBreaker is open). This would mean that this exception needs to be handled in different places in the code, and might make comprehension harder|



However, we will have throw an exception when the CircuitBreaker becomes open, and hence this approach was rejected since we were looking to avoid Exceptions in our logic flows.

**2. Decorating a `RestOperations` interface**
| | Advantage  | Disadvantage |
|-|------------- | ------------- |
|1.| An app wide change, just like the last approach - Spring DI can be used to inject this decorated RestOperations to every API Client that needs a RestTemplate| The `RestOperations` interface is pretty big.However there are a lot of methods in the single RestOperations interface, and it would mean a lot of maintainence of boilerplate. |










