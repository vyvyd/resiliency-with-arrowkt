# ⚡ resiliency-with-arrowkt

## Background 

Consider that you choose the following tools in your backend service 
- 🛠 **Kotlin** language to target the JVM 
- 🛠 **SpringBoot** as the web-framework
- 🛠 **RestTemplate** to make external API calls
- 🛠 **ArrowKT** to avoid exception-driven logical flows

## Considerations

**RestTemplate** 
 - is a deprecated tool. _We must expect that it is replaced by future maintainers of the code-base._
 - makes use of exceptions for logic flow

**WireMock** 
 - will be used to test the API client

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






