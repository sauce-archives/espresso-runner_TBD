# Sauce Labs Espresso Runner

A tool to upload and run Espresso/Robotium tests on the Sauce Labs real device platform

## Usage

Arguments can be provided with environment variables:
```
APP=path/to/app.apk TEST=/path/to/test.apk USER_NAME=user PASSWORD=pass PROJECT=project SUITE=1 java -jar espresso-runner.jar
```

Or from command line parameters (which take precedence):

```
java -jar espresso-runner.jar \
    --app path/to/app.apk \
    --test path/to/test.apk \
    --username testObjectUsername \
    --password testObjectPassword \
    --project projectName \
    --suite 1
```

Or from a file:
```
java -jar espresso-runner.jar @/parameters.txt
```

For additional options, try `java -jar espressorunner.jar --help`.

## Building

Run `mvn package` to create a `target/espressorunner.jar` file.
