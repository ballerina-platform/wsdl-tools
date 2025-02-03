# Ballerina WSDL Tool  

[![Build](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/wsdl-tools/actions/workflows/build-timestamped-master.yml) 
[![codecov](https://codecov.io/gh/ballerina-platform/wsdl-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/wsdl-tools)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/wsdl-tools.svg)](https://github.com/ballerina-platform/wsdl-tools/commits/master)
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/wsdl-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fwsdl-tools)

`WSDL` (Web Services Description Language) is an XML-based language for describing the functionalities of a web service that uses SOAP as the underlying protocol for data transfer.  

This package contains the Ballerina WSDL tool, which generates Ballerina client stubs and record types from a given WSDL file. It simplifies the integration with SOAP-based web services by automatically generating necessary types and client functions.

## Installation

Execute the command below to pull the WSDL tool from [Ballerina Central](https://central.ballerina.io/ballerina/wsdl/latest).

```bash
bal tool pull wsdl
```

## Usage

The WSDL tool provides the following capabilities.

1. Generate Ballerina client functions for a given WSDL specification.
2. Generate Ballerina record types for an XML schema provided in the WSDL specification.

The client generated from a WSDL file can be used in your applications to call the SOAP-based web service defined in the WSDL.

The following command will generate Ballerina client stubs and records for a given WSDL file. It is mandatory to run the command within a Ballerina package.

```bash
bal wsdl <wsdl-file-path>
         [--operations <operation-uris>]
         [--module <output-module-name>]
         [--port <port-name>]
```

### Command options

| Option | Description | Mandatory/Optional |
|--------|-------------|--------------------|
| `<wsdl-file-path>` | The path of the WSDL file. | Mandatory |
| `--operations <operation-uris>` | A comma-separated list of operation URIs for which client methods should be generated. If not provided, methods for all operations in the WSDL file will be generated. | Optional |
| `-m, --module <output-module-name>` | The name of the module where the generated client and record types will be placed. If not provided, output files will be saved to the project default package. | Optional |
| `-p, --port <port-name>` | The name of the port that defines the service endpoint. If specified, a client will be generated only for this port. Otherwise, clients for all available ports will be generated. | Optional |

### Generate Ballerina clients and types from a WSDL file

```bash
bal wsdl <wsdl-file-path>
```

This command generates Ballerina clients and record types for all operations in the given WSDL file.

For example,

```bash
bal wsdl calculator.wsdl
```

Upon successful execution, the following files will be created inside the default module in the Ballerina project.

```bash
client.bal (There can be multiple client files depending on the WSDL file)
types.bal
```

### Generate a Ballerina client and types for a specific module

```bash
bal wsdl <wsdl-file-path> --module <output-module-name>
```

This command generates Ballerina clients and record types for the given WSDL file and saves them in the `<output-module-name>` submodule within the Ballerina project.

For example,

```bash
bal wsdl calculator.wsdl --module custom
```

This generates a Ballerina client (`client.bal`) and record types (`types.bal`) for the `calculator.wsdl` WSDL specification.

Upon successful execution, the following files will be created in the `custom` submodule within the Ballerina project.

```bash
modules/
└── custom/
    └── client.bal (There can be multiple client files depending on the WSDL file)
    └── types.bal
```

### Generate a Ballerina client for specific operations

```bash
bal wsdl <wsdl-file-path> --operations <operation-uris>
```

This command generates a client containing methods only for the specified operation actions.

For example,

```bash
bal wsdl sample.wsdl --operations http://sample.org/action1,http://sample.org/action2
```

### Generate a Ballerina client for a specific port

```bash
bal wsdl <wsdl-file-path> --port <port-name>
```

This command generates a client only for the given port in the WSDL file.

For example,

```bash
bal wsdl calculator.wsdl --port SamplePort
```

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
