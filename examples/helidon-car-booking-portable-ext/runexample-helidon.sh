export AZURE_OPENAI_ENDPOINT=$(mvn help:evaluate -Dexpression=azure.openai.endpoint -q -DforceStdout)
export AZURE_OPENAI_API_KEY=$(mvn help:evaluate -Dexpression=azure.openai.api.key -q -DforceStdout)
export AZURE_OPENAI_DEPLOYMENT_NAME=$(mvn help:evaluate -Dexpression=azure.openai.deployment.name -q -DforceStdout)

java -Dsun.misc.URLClassPath.disableJarChecking=true --add-opens jdk.naming.rmi/com.sun.jndi.rmi.registry=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/sun.net=ALL-UNNAMED -jar target/helidon-*.jar
