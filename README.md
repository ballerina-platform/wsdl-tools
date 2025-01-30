# Ballerina WSDL Tool  

[![Build](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml)  
[![codecov](https://codecov.io/gh/ballerina-platform/wsdl-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/wsdl-tools)  
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/wsdl-tools.svg)](https://github.com/ballerina-platform/wsdl-tools/commits/master)  
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/wsdl-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fwsdl-tools)  

`WSDL` (Web Services Description Language) is an XML-based language for describing the functionalities of a web service that uses SOAP as the underlying protocol for data transfer.  


This package contains the Ballerina WSDL tool, which generates Ballerina client stubs and record types from a given WSDL file. It simplifies the integration with SOAP-based web services by automatically generating necessary types and client functions.

### Features

The WSDL tool provides the following capabilities.

1. Generate a Ballerina client for a given WSDL specification.
2. Generate Ballerina record types for the XSDs defined in the WSDL specification.

### WSDL to Ballerina Code Generation

The `bal wsdl` command in Ballerina is used to generate Ballerina client stubs and record types from a given WSDL file.

#### Command Syntax

```bash
bal wsdl <wsdl-file-path> [--operations <operation-uris>] [--module <output-module-name>] [--port <port-name>]
```

**Command Options:**

| Option | Description |
|--------|-------------|
| `<wsdl-file-path>` | (Required) The path to the WSDL file.methods for all the operations in the WSDL file will be generated |
| `--operations <operation-uris>` | (Optional) A comma-separated list of operation URIs for which client methods should be generated. If not provided, methods for all operations in the WSDL file will be generated. |
| `-m, --module <output-module-name>` | (Optional) The name of the module where the generated client and record types will be placed. If not provided, output files will be saved to the current Ballerina project. |
| `-p, --port <port-name>` | (Optional) The name of the port that defines the service endpoint. If specified, a client will be generated only for this port. Otherwise, clients for all available ports will be generated. |

### Examples

#### Generate a Ballerina client and types from a WSDL file

```bash
bal wsdl calculator.wsdl
```
This command generates a Ballerina client and record types for all operations in `calculator.wsdl` and saves them in the current Ballerina project.

#### Generate a Ballerina client and types for a specific module

```bash
bal wsdl calculator.wsdl --module temp
```

This command saves the generated client and record types in the `temp` module within the Ballerina project.

#### Generate a Ballerina client for specific operations

```bash
bal wsdl calculator.wsdl --operations http://example-operation-action-uri/path -m temp
```

This command generates a client only for the specified operation and saves it in the `temp` module.

#### Generate a Ballerina client for a specific port

```bash
bal wsdl calculator.wsdl --port SamplePortName
```

This command generates a client only for the `SamplePortName` port in the WSDL file.

### Output Files

Upon successful execution, the generated files include,

```bash
-- client.bal (There can be multiple client files depends on the WSDL file)
-- types.bal
```

These files contain the necessary client and type definitions to interact with the service defined in the WSDL file.

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
