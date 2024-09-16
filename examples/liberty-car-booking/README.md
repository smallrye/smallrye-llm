# Car Booking (Open Liberty)

These are the steps to run this service.

 1. Go to the root `smallrye-llm` directory, then build it using the command:
 	> `mvn clean install -e`
 2. Once the project is built, move to directory `cd examples/liberty-car-booking` and run the following command to install the bundle as a user liberty feature:
 	> `mvn clean liberty:create liberty:prepare-feature liberty:install-feature liberty:dev -e`
 	
Application requirements:
 - JDK 17 and higher
 - Maven 3.9.9 and higher
 	
Then you can access the application through the browser of your choice.
