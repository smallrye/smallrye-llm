This example is based on a simplified car booking application inspired from the [Java meets AI](https://www.youtube.com/watch?v=BD1MSLbs9KE) talk from [Lize Raes](https://www.linkedin.com/in/lize-raes-a8a34110/) at Devoxx Belgium 2023 with additional work from [Jean-François James](http://jefrajames.fr/). The original demo is from [Dmytro Liubarskyi](https://www.linkedin.com/in/dmytro-liubarskyi/).

# Car Booking (Open Liberty)

These are the steps to run this service.

1. Go to the root `langchain4j-microprofile-jakarta` directory, then build it using the command:
 	> `mvn clean install -e`
2. Once the project is built, move to directory `cd langchain4j-microprofile-examples/liberty-car-booking` and run the following command to install the bundle as a user liberty feature:
 	> `mvn liberty:dev -e`
 	
## Application requirements:
- JDK 17 and higher
- Maven 3.9.9 and higher
- LangChain4j 0.33.0 or higher.
- Testing against GPT 3.5 and 4.0 on a dedicated Azure instance (to be customized in your context). 

Then you can access the application through the browser of your choice.

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

All configuration is centralized in `microprofile-config.properties` (found is `resources\META-INF` folder) and can be redefined using environment variables.

## Running the application

To run in dev mode with OpenLiberty: Run the following command in the CLI: `mvn liberty:dev`.

To run in JVM mode (after packaging): `mvn liberty:start`. To stop the running server: `mvn liberty:stop`.


## Playing with the application

The application exposes a REST API documented with OpenAPI. 

To interact with the application go to: [http://localhost:9080/openapi/ui](http://localhost:8080/openapi/ui).


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
