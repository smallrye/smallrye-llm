export AZURE_OPENAI_ENDPOINT=$(mvn help:evaluate -Dexpression=azure.openai.endpoint -q -DforceStdout)
export AZURE_OPENAI_API_KEY=$(mvn help:evaluate -Dexpression=azure.openai.api.key -q -DforceStdout)
export AZURE_OPENAI_DEPLOYMENT_NAME=$(mvn help:evaluate -Dexpression=azure.openai.deployment.name -q -DforceStdout)

mvn quarkus:dev
