## Introduction

This example demonstrates [LangChain4J](https://docs.langchain4j.dev/) with [Quarkus](https://quarkus.io/). It aims at studying how to leverage LLMs (impressive) capabilities in Java applications. In particular, it illustrates how to use RAG and Function Calling.

It is derived from my [Quarkus-LangChain4j](https://github.com/jefrajames/car-booking) example used to illustrate my talk at [JChateau 2024](https://www.jchateau.org).

It is based on a simplified car booking application inspired from the [Java meets AI](https://www.youtube.com/watch?v=BD1MSLbs9KE) talk from [Lize Raes](https://www.linkedin.com/in/lize-raes-a8a34110/) at Devoxx Belgium 2023 with additional work from [Jean-François James](http://jefrajames.fr/). The original demo is from [Dmytro Liubarskyi](https://www.linkedin.com/in/dmytro-liubarskyi/). The car booking company is called "Miles of Smiles" and the application exposes two AI services:

 1. a chat service to freely discuss with a customer assistant
 2. a fraud service to determine if a customer is a frauder.

For the sake of simplicity, there is no database interaction, the application is standalone and can be used "as is". Of course thanks to Quarkus, it can  easily be extended according to your needs.

Warning: you must first configure the application to connect to an LLM that supports Function Calling (see Environment Variables below).

## Technical context

The project has been developed and tested with:

* Java 22 (Temurin OpenJDK distro)
* Helidon 4.0.7
* Helidon CLI 3.0.4
* LangChain4j 0.30.0
* Maven 3.9.5
* Testing against GPT 3.5 and 4.0 on a dedicated Azure instance (to be customized in your context). 

During my tests, GPT 3.5 has proved to be faster but less precise en consistent than GPT 4. In particular, GPT 4 has provided much better result with Fraud Detection.

## Differences with Quarkus-LangChain4j

Quarkus provides a deep integration with LangChain4j thanks to a specific [extension](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html).

In particular, it provides a powerful `@RegisterAiService` annotation and network interactions with LLMs are managed with its own RestClient.

This example is based on a standard usage of LangChain4j with Helidon. There is no such deep integration. 

I've added 3 technical classes to manage "the glue" (more or less the equivalent of `@RegisterAiService`):

* ModelFactory: generates an OpenAI Chat model
* ChatAiServiceFactory: generates a Chat assistant
* FraudAiServiceFactory: generates a Fraud assistant.

I've been obliged to turn FraudResponse in a POJO. It seems that Google GSON, used to deserialize OpenAI responses does not support Java Record.

In contrast with Quarkus, network interactions with LLMs are based on standard LangChain4j. For instance, the Azure SDK is used with Azure OpenAI.

## Packaging the application

To package the application in JVM mode run: `mvn package`.

## Configuration

All configuration is centralized in `microprofile-config.properties` and can be redefined using environment variables.

## Running the application

To run in dev mode with Helidon CLI: _helidon dev_

To run in JVM mode (after packaging): _java -jar target/carbooking-helidon.jar_

Note: native mode not yet tested.

## Playing with the application

The application exposes a REST API documented with OpenAPI. 

To interact with the application go to: [http://localhost:8080/openapi/ui](http://localhost:8080/openapi/ui).


Typical questions you can ask in the Chat:

* Hello, how can you help me?
* What is your list of cars?
* What is your cancellation policy?
* What is your fleet size? Be short please.
* How many electric cars do you have?
* My name is James Bond, please list my bookings
* Is my booking 123-456 cancelable?
* Is my booking 234-567 cancelable?
* Can you check the duration please?
* I'm James Bond, can I cancel all my booking 345-678?
* Can you provide the details of all my bookings?

You can ask fraud for:

* James Bond
* Emilio Largo

For more information, please see my [Quarkus-LangChain4j](https://github.com/jefrajames/car-booking) example.
