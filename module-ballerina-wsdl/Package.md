## Package Overview

This package provides the Ballerina WSDL tooling, which will make it easy to start the development of a client skeletons and types from a WSDL specification.

The WSDL tool provide the following capabilities.

    1. Generate the Ballerina client code for a given WSDL specification. 
    2. Generate the Ballerina record types for a XSD provided in the WSDL specification. 

The `wsdl` command in Ballerina is used for WSDL to Ballerina code generations. Code generation from WSDL to Ballerina can produce Ballerina client stubs.

### WSDL to Ballerina

#### Generate Client Stubs from an WSDL Specifications

```bash
bal wsdl -i <FILE_NAME> [--operations <COMMA_SEPARATED_OPERATION_NAMES>]
```

Generates Ballerina client stubs for a given WSDL file.

`-i, --input <FILE_NAME>`: The input WSDL file from which Ballerina types are to be generated is specified by this required parameter.

`--operations <COMMA_SEPARATED_OPERATION_NAMES>`: This optional parameter allows the user to specify a list of operations for which Ballerina types are to be generated. If not provided, types for all operations in the WSDL will be generated.

### Samples for WSDL Commands

#### Generate Client Stub and Records from WSDL

```bash
    bal wsdl -i calculator.wsdl --operations add, subtract, divide, multiply
```

This will generate a Ballerina client stub and records for the `calculator.wsdl` file. The above command can be run from within anywhere on the execution path.
It is not mandatory to run it from inside the Ballerina project.

Output:

```bash
The service generation process is complete. The following files were created.
-- client.bal
-- types.bal
```
