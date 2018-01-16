# Sauce Labs Espresso Runner

A tool to upload and run Espresso/Robotium tests on the Sauce Labs real device platform

## Usage

Arguments can be provided with environment variables:
```
APP=path/to/app.apk TEST=/path/to/test.apk USER_NAME=user PASSWORD=pass PROJECT=project SUITE=1 TUNNEL_IDENTIFIER=tunnel java -jar espresso-runner.jar
```

Or from command line parameters (which take precedence):

```
java -jar espresso-runner.jar \
    --app path/to/app.apk \
    --test path/to/test.apk \
    --username testObjectUsername \
    --password testObjectPassword \
    --project projectName \
    --suite 1 \
    --tunnelIdentifier tunnel
```

Or from a file:
```
java -jar espresso-runner.jar @/parameters.txt
```
## Additional options

To see the following output, run: `java -jar espressorunner.jar --help`

```
Usage: espressorunner.jar [options]
  Options:
  * --app
      Path to APK of app under test
  * --test
      Path to test APK
  * --username
      Your TestObject username
  * --password
      Your TestObject password
  * --project
      Your TestObject project
  * --suite
      ID of your Espresso suite within your project
    --testsToRun
      Individual tests to run
    --classesToRun
      Individual classes to run
    --annotationsToRun
      Individual annotations to run
    --sizesToRun
      Test sizes to run
    --help, -h, -?
      Displays details on usage
    --checkFrequency
      Interval in seconds to check test results
      Default: 0
    --failOnError
      Abort if a test failure occurs
      Default: false
    --failOnUnknown
      Abort if an unknown error occurs
      Default: false
    --runAsPackage
      Runs the test APK without any configuration
      Default: false
    --team
      Your TestObject team
    --timeout
      Test timeout in minutes
      Default: 0
    --url
      URL of TestObject API endpoint
    --verbosity
      Logging level.
    --xmlFolder
      Folder where XML test results will be located
    --tunnelIdentifier
      Sauce Connect tunnel identifier
```

## Building

Run `mvn package` to create a `target/espressorunner.jar` file.
