To test please do

mvn clean verify cargo:run


then

curl -X 'GET' 'http://127.0.0.1:8080/glassfish-car-booking/api/car-booking/chat?question=I%20want%20to%20book%20a%20car%20how%20can%20you%20help%20me%3F' -H 'accept: text/plain'