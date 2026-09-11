# Changelog

## [v1.3.1] - Unreleased

- Support for one-way operations ([#40](https://github.com/ballerina-platform/wsdl-tools/pull/40))

## [v1.3.0] - 2026-03-24

- Updated XSD core dependency version ([#38](https://github.com/ballerina-platform/wsdl-tools/pull/38))

## [v1.2.0] - 2025-10-24

- Added namespace support for header and body fields in generated types ([#35](https://github.com/ballerina-platform/wsdl-tools/pull/35))
- Bumped the XSD core version to 1.1.2 ([#36](https://github.com/ballerina-platform/wsdl-tools/pull/36))

## [v1.1.1] - 2025-08-05

- Updated XSD core version to 1.1.1 for bug fixes ([#33](https://github.com/ballerina-platform/wsdl-tools/pull/33))

## [v1.1.0] - 2025-07-24

- Fixed resolved record names for WSDL types not being updated in header records ([#31](https://github.com/ballerina-platform/wsdl-tools/pull/31))

## [v1.0.4] - 2025-07-14

- Handle missing parts in header gracefully ([#29](https://github.com/ballerina-platform/wsdl-tools/pull/29))

## [v1.0.3] - 2025-04-03

- Improved handling inputs in the CLI tool ([#27](https://github.com/ballerina-platform/wsdl-tools/pull/27))

## [v1.0.2] - 2025-04-01

- Refined native XSD core APIs ([#25](https://github.com/ballerina-platform/wsdl-tools/pull/25))

## [v1.0.1] - 2025-02-18

- Added missing XSD core dependency to the Ballerina runtime ([#23](https://github.com/ballerina-platform/wsdl-tools/pull/23))

## [v1.0.0] - 2025-02-17

- Initial release of the WSDL to Ballerina tool
- WSDL to Ballerina client generation from WSDL files
- Support for SOAP 1.1 and SOAP 1.2 bindings
- Support for generating multiple operations
- SOAP header and body field generation
- XSD schema type generation
- CLI tool with module name flag and help text
- Diagnostic error reporting for invalid WSDL definitions
