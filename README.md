# Ballerina WSDL Tool  

[![Build](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml)  
[![codecov](https://codecov.io/gh/ballerina-platform/wsdl-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/wsdl-tools)  
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/wsdl-tools.svg)](https://github.com/ballerina-platform/wsdl-tools/commits/master)  
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/wsdl-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fwsdl-tools)  

`WSDL` (Web Services Description Language) is an XML-based language for describing the functionalities of a web service that uses SOAP as the underlying protocol for data transfer.  

The WSDL Tool simplifies SOAP web service integration by providing the following functionalities.

1. Generate the Ballerina client skeleton for a given WSDL definition.  
2. Generate Ballerina types necessary for the SOAP integration.  

## Using the WSDL Tool  

The `wsdl` command in Ballerina is used for WSDL-to-Ballerina code generation.  

```bash
bal wsdl <source-file-path>
```

### Command Options  

| Option | Description |
|--------|-------------|
| `--operations`   | Comma separated URIs of the operation action to generate client methods. If not provided, methods for all the operations in the WSDL file will be generated |  
| `-m`, `--module`   | The name of the module in which the Ballerina client and record types are generated |

### Examples

#### 1. Generate client and types for all operations

This command generates the Ballerina client and all the required types for all operations defined in the WSDL file. The generated files will be placed in the current Ballerina project.

```bash
bal wsdl <source-file-path>
```

#### 2. Generate client and types for specific operations

This command generates the Ballerina client and types for specific operations defined by their SOAP action URIs.

```bash
bal wsdl <source-file-path> --operations "https://example.com/soapAction1, https://example.com/soapAction2"
```

#### 3. Generate files into a specific module

This command generates the client and types for all operations and places the generated files in the specified module directory within the current Ballerina project.

```bash
bal wsdl <source-file-path> --module new_module
```

#### 4. Generate specific operations in a Specific Module

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

   >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

2. Export GitHub Personal access token with read package permissions as follows,
   ```
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```

### Building the Source

Execute the commands below to build from the source.

1. To build the library:

        ./gradlew clean build

2. To run the integration tests:

        ./gradlew clean test

3. To build the module without the tests:

        ./gradlew clean build -x test

4. To publish to maven local:

        ./gradlew clean build publishToMavenLocal

5. Publish the generated artifacts to the local Ballerina central repository:

        ./gradlew clean build -PpublishToLocalCentral=true

6. Publish the generated artifacts to the Ballerina central repository:

        ./gradlew clean build -PpublishToCentral=true

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

You can also check for [open issues](https://github.com/ballerina-platform/wsdl-tools/issues) that
interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* View the [Ballerina performance test results](https://github.com/ballerina-platform/ballerina-lang/blob/master/performance/benchmarks/summary.md).
