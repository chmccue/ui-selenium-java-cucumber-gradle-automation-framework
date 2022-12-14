# Test Automation Framework (V1.0)

> UI Test Automation built in Java with SeleniumWebDriver, Junit, Cucumber using Gradle.

**Supports:** MacOSX, Linux, Windows.

## Installation

Install the following requirements with their respective versions:

| [Docker](https://www.docker.com/community-edition) | [Java](https://jdk.java.net/) | [Gradle](https://gradle.org/releases/) |
| --- | --- | --- |
| 19.03 | 11.0.7 LTS | 6.5 |

All other dependencies are defined in `build.gradle` and are automatically loaded.

## Configuration

Create `.env` file from provided `.env_template` and edit with your desired configuration / credentials:

```bash
cp .env-template .env
vim .env
```

Also, additional and more advanced settings can be configured in the `gradle.properties` file.

### Explanation of `.env` variables

* `SITE_URL`: HTTPS URL of website.
* `GRADLEW_RUN_PARAMS`: Parameters passed to gradle to run test task. Example: `runChromeTest --info`. You can also pass in more complex
commands to alter the default test runner parameters, such as `runChromeTest --info -Dcourgette.runLevel=SCENARIO`
* `SELENIUM_BROWSER_TYPE`: Browser that should be used to run the tests. Possible values: 
`chrome`, `chromeHeadless`, `firefox`, `firefoxHeadless`, `safari`, `MicrosoftEdge`, `iexplore`.
Note that only chrome and firefox are currently stable and maintained.
* `SELENIUM_BROWSER_VERSION`: Browser version that goes with browser type. If running with gradle test task,
this is set up by default for chrome and firefox, so can be left blank in gitlab-ci and environment vars.
If running in junit through the feature file runner, this must be set manually.
* `SELENIUM_DRIVER_TYPE`: Defines if tests should be run with `remote` web driver (i.e.: running with docker, with or without grid/hub)
or with `local` executable browser driver (i.e.: `chromedriver`, `geckodriver`). Values: `remote` or `local`.
* `SELENIUM_REMOTE_URL`: When `SELENIUM_DRIVER_TYPE` is `remote`, Docker automatically defines the following value: `http://selenium-hub:4444/wd/hub`.
If `SELENIUM_DRIVER_TYPE` is `local`, then this variable can be empty.
* `USER_PROVISIONING_SERVER_URL`: HTTPS User Provisioner server address that connects to User Provisioning System.
* `USER_PROVISIONING_ENVIRONMENT`: Environment name used as base path in User Provisioner KV System to store/retrieve user data.
* `USER_PROVISIONING_RETRY_LOCK`: If `true` and User Provisioning System fails to find an available user, it will wait 30 seconds and retry again.
If it fails again, a RuntimeException is raised. When `false`, the exception will be raised the first time the provisioner fails to find an available user.
* `SELENIUM_MAX_WAIT_SECONDS_FOR_ELEMENT`: Optional argument: defines maximum number of seconds selenium web driver waits for a DOM element to meet
certain conditions, like: be clickable, visible, etc. If variable is not defined, then tests use wait values programmed within the framework.
* `CUCUMBER_TAGS`: Optional argument: Ability to add custom tag expressions to overwrite default test runner tags.
Currently only works when running in Docker. Example value: `(@navigation or @login) and not @ignore`
For more details on tag expressions, see: https://github.com/cucumber/cucumber/tree/master/tag-expressions

View `docker/start.sh` file to see the default values that get set when running in docker.

## Usage

### Running locally with a browser driver through Gradle

First modify the `.env` file:
```bash
SITE_URL=<HTTPS URL of the QA website>
SELENIUM_DRIVER_TYPE=local
USER_PROVISIONING_SERVER_URL=<enter correct value>
USER_PROVISIONING_ENVIRONMENT=uat-dev
```

Then for Chrome or Firefox, run one of the following:
```bash
./gradlew runChromeTest --info

./gradlew runFirefoxTest --info
```

Alternatively, if using an IDE such as intellij, you can run outside terminal: open build.gradle file, select "run" option along left side of test task.

If you want to run only specific cucumber tags tests:
* If running in docker, all you need to do is add a custom tag expression to CUCUMBER_TAGS variable in your .env file:
```bash
CUCUMBER_TAGS=(@navigation or @login) and not @ignore
```
* If running outside docker: edit tags section under feature suite you're running (`src/test/java/suites/`). Don't forget to change tags back to what they were!

If there was an error releasing users from the User Provisioning lock, you can run the command to release the users:
```bash
./gradlew releasePendingConsulUsers
```

You can see all the currently locked users in the active locks section (url information not provided for security reasons).
 You need to click on the `Lock` sessions tab and then you will be able to see them in the form `{environment name} | {username}`.
 
The entire DB of users is stored in JSON format.

### Uploading Users to Provisioning Server

First, define the environment variables `USER_PROVISIONING_SERVER_URL`, `USER_PROVISIONING_ENVIRONMENT`, `UPLOAD_USERS_FILEPATH`,
and then run:
```bash
./gradlew uploadUsersToConsul
```

This command will look for the CSV (`,` comma-separated values) file located at `UPLOAD_USERS_FILEPATH` and after parsing
it, those users will be uploaded to the Consul server at the `user-provisioning/USER_PROVISIONING_ENVIRONMENT.json` path.

The CSV file must contain in the first line the column names, which could be one or more of these constants:

- Values of the user instance:
    - username
    - password
    - tfa-method
    - tfa-code
- Properties to filter users by:
    - country
    - tier
    - tfa
    - funds
    - doc-id
    - doc-res
    - doc-funds
    - other

One corner case occurs when the value of a given column of the CSV is the string 'null', in which
case it will be stored as ac actual null object. This allow developers to have some users within
Provisioner with unknown values.

### Running with [docker-compose](https://docs.docker.com/compose/) and Selenium Standalone

#### For Chrome
Run:
```bash
docker-compose up --detach selenium-chrome
docker-compose up --build company-uat
```

#### For Firefox
First modify the `.env` file:
```bash
SELENIUM_BROWSER_TYPE=firefox
```

Then run:
```bash
docker-compose up --detach selenium-firefox
docker-compose up --build company-uat
```

### Running with [docker-compose](https://docs.docker.com/compose/) and Selenium Grid

#### For Chrome
Run:
```bash
Running a single node: docker-compose up --detach selenium-hub node-chrome
Scale to more than 1 node: docker-compose up --detach --scale node-chrome=<scale number> --no-recreate node-chrome
docker-compose up --build company-uat
```

#### For Firefox
First modify the `.env` file:
```bash
SELENIUM_BROWSER_TYPE=firefox
```

Then run:
```bash
Running a single node: docker-compose up --detach selenium-hub node-firefox
Scale to more than 1 node: docker-compose up --detach --scale node-firefox=<scale number> --no-recreate node-firefox
docker-compose up --build company-uat
```

#### @Before and @After setups/teardowns (last updated 2/27/2019)

- Setups (`@Before`) execute basic set up steps such as reporting and browser initialization. Most
  scenario set up details are handled by feature file `Background` steps, as they are better
  reported in the log reports.

- Teardowns (`@After`) are more complex, as there is no `Background` equivalent for teardowns and
  you don't need to include teardown details in the feature files, as it's executing reset steps
  after the tests have completed. 2 general teardown types we use:
  - globally shared teardowns, such as reporting/screen shot details and browser reset.
  - teardowns used by specifically tagged scenarios.
    - These run between global teardowns of reporting and browser reset, so the reporting screen
      shot is configured properly in the report before the tests navigate away from the point of
      failure but before the browser resets for the next scenario.
      - Use case example: Global Settings Lock turns on, then scenario fails before turning off.
        - Teardown for reporting to place failed screen shot into report runs first.
        - Teardown to check for and reset Global Settings Lock runs.
        - Teardown of browser reset to be ready for next scenario runs last.

- Order arguments for `@Before` and `@After`:
  - For `@Before`, `order = 0` runs first, and orders with bigger numbers run last.
  - For `@After`,  `order = 0` runs last, and orders with bigger numbers run first.
  - details here: https://docs.cucumber.io/cucumber/api/#before

### Reports

Test Report generated locally:
* `build/chrome/`
* `build/firefox/`

Test Log files: `build/logs`

Docker Logs: `docker logs company-uat`

