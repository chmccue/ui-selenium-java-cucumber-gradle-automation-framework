#!/usr/bin/env bash

if [[ -f ".env" ]]; then
  echo "Sourcing .env file..."
  source .env
fi

if [[ -z "${SELENIUM_BROWSER_TYPE}" ]]; then
  export SELENIUM_BROWSER_TYPE="chrome"
fi

if [[ -z "${SELENIUM_DRIVER_TYPE}" ]]; then
  export SELENIUM_DRIVER_TYPE="remote"
fi

if [[ -z "${SELENIUM_REMOTE_URL}" ]]; then
  export SELENIUM_REMOTE_URL="http://selenium-hub:4444/wd/hub"
fi

if [[ -z "${USER_PROVISIONING_ENVIRONMENT}" ]]; then
  export USER_PROVISIONING_ENVIRONMENT="uat-dev"
fi

if [[ -z "${USER_PROVISIONING_RETRY_LOCK}" ]]; then
  export USER_PROVISIONING_RETRY_LOCK="true"
fi

if [[ -z "${GRADLEW_RUN_PARAMS}" ]]; then
  if [[ "${SELENIUM_BROWSER_TYPE}" == *"firefox"* ]]; then
    export GRADLEW_RUN_PARAMS="runFirefoxTest"
  else
    export GRADLEW_RUN_PARAMS="runChromeTest"
  fi

  if [[ -z "${GRADLE_LOG_LEVEL}" ]]; then
    export GRADLE_LOG_LEVEL="--info"
  fi

  export GRADLEW_RUN_PARAMS="${GRADLEW_RUN_PARAMS} ${GRADLE_LOG_LEVEL}"

  if [[ -n "${NODE_COUNT}" ]]; then
    export GRADLEW_RUN_PARAMS="${GRADLEW_RUN_PARAMS} -Dcourgette.threads=${NODE_COUNT} -Dcourgette.runLevel=SCENARIO"
  fi
fi

# Below logic ensures gradle task name is used based on entered browser (chrome, firefox).
if [[ "${SELENIUM_BROWSER_TYPE}" == *"firefox"* ]]; then
  export GRADLEW_RUN_PARAMS=${GRADLEW_RUN_PARAMS/Chrome/Firefox}
else
  export GRADLEW_RUN_PARAMS=${GRADLEW_RUN_PARAMS/Firefox/Chrome}
fi

echo "Stopping gradlew daemons first..."
./gradlew --stop

if [[ -n "${CUCUMBER_TAGS}" ]]; then
  echo "Running ./gradlew ${GRADLEW_RUN_PARAMS} with tags ${CUCUMBER_TAGS}"
  ./gradlew ${GRADLEW_RUN_PARAMS} -Dcucumber.tags="${CUCUMBER_TAGS}"
else
  echo "Running ./gradlew ${GRADLEW_RUN_PARAMS}"
  ./gradlew ${GRADLEW_RUN_PARAMS}
fi
