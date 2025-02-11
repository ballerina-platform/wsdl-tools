/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.wsdl.core.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.Objects;

/**
 * Encapsulates information about a diagnostic message, its code, description, severity, and additional arguments.
 * This class is used to represent errors and warnings generated during WSDL to Ballerina conversions.
 *
 * @since 0.1.0
 */
public class DiagnosticMessage {
    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;
    private final Object[] args;

    private DiagnosticMessage(String code, String description, DiagnosticSeverity severity, Object[] args) {
        this.code = code;
        this.description = description;
        this.severity = severity;
        this.args = args;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public DiagnosticSeverity getSeverity() {
        return this.severity;
    }

    public Object[] getArgs() {
        return Objects.requireNonNullElse(this.args, new Object[0]).clone();
    }

    public static DiagnosticMessage wsdlToBallerinaError(Object[] args) {
        return new DiagnosticMessage("Error",
                "Invalid WSDL. Provided WSDL is invalid.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage wsdlToBallerinaInputError(Object[] args) {
        return new DiagnosticMessage("Error",
                          "Provided port name is invalid", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage wsdlToBallerinaIOError(Exception e, Object[] args) {
        return new DiagnosticMessage("IO_Error",
                "Failed to read the source file.", DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage wsdlToBallerinaParserError(Exception e, Object[] args) {
        return new DiagnosticMessage("PARSER_ERROR",
                "Failed to parse the WSDL content. " + e.getMessage(),
                DiagnosticSeverity.ERROR, args);
    }

    public static DiagnosticMessage wsdlToBallerinaGeneralError(Exception e, Object[] args) {
        return new DiagnosticMessage("Error",
                "Failed to generate files from the source. " + e.getMessage(),
                DiagnosticSeverity.ERROR, args);
    }
}
