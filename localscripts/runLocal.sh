#!/bin/sh

########################################################################
# Bash script for executing Chrome and Firefox gradle tasks locally.
# To customize tags to run and other configs, edit test runner files in 'src/test/java/suites'.
# Passing in env vars CUCUMBER_TAGS and GRADLEW_RUN_PARAMS not currently supported with this.
# Note that setting any env vars below will be overwritten by .env file vars, if they are present.
########################################################################

export SELENIUM_DRIVER_TYPE=local

cd ..
./gradlew --stop
./gradlew runChromeTest --info &
sleep 1 &
./gradlew runFirefoxTest --info &
wait