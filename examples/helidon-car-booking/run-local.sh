#bin/bash

ENGINE="${CONTAINER_ENGINE:-podman}"

$ENGINE stop ollama
$ENGINE run -d --name ollama --replace --pull=always --restart=always -p 11434:11434 -v ollama:/root/.ollama --stop-signal=SIGKILL docker.io/ollama/ollama
$ENGINE  exec -it ollama ollama run llama3.1 << EOF
/bye
EOF

java -Dsun.misc.URLClassPath.disableJarChecking=true --add-opens jdk.naming.rmi/com.sun.jndi.rmi.registry=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED -jar target/helidon-car-booking.jar