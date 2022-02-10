# Rejected Approaches

To integration **Reslience4J** to the backend.

## Integration Strategies

### Resilience4J + RestTemplate

**Using a RestTemplateInterceptor**

| | Advantage  | Disadvantage |
|-|------------- | ------------- |
|1.| All calls in the application automatically have resiliency built in | Client-By-Client configuration is not possible (which is usually what you need) |
|2.| | `Interceptors` will need to throw Exceptions to indicate failure (for example, when a CircuitBreaker is open). This would mean that this exception needs to be handled in different places in the code, and might make comprehension harder|

**Creating a Reslience4J implementation of RestOperations**

| | Advantage  | Disadvantage |
|-|------------- | ------------- |
|1.| An app wide change, just like the last approach - Spring DI can be used to inject this decorated RestOperations to every API Client that needs a RestTemplate| The `RestOperations` interface is pretty big.However there are a lot of methods in the single RestOperations interface, and it would mean a lot of maintainence of boilerplate. |

