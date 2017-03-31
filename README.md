# Sauce Labs Espresso Runner

A tool to upload and run Espresso/Robotium tests on the Sauce Labs real device platform

## Usage

Arguments can be provided with environment variables, or command line parameters (which take precedence):

```
java -jar espresso-runner.jar \
    --app path/to/app.apk \
    --test path/to/test.apk \
    --username testObjectUsername \
    --password testObjectPassword \
    --suites 1
```

For additional options, try `java -jar espressorunner.jar --help`.

## Building

Run `mvn package` to create a `target/espressorunner.jar` file.
