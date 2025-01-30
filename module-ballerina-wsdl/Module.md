## Module Overview

This package contains the Ballerina WSDL tool, which generates Ballerina client stubs and record types from a given WSDL file. It simplifies the integration with SOAP-based web services by automatically generating necessary types and client functions.

### Features

The WSDL tool provides the following capabilities:

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
