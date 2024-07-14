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

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * Represents a diagnostic message specific to WSDL to Ballerina conversion processes,
 * extending generic diagnostic capabilities.
 * This class encapsulates all relevant details such as the diagnostic info, location, message,
 * and severity specific to a diagnostic event.
 *
 * @since 0.1.0
 */
public class WsdlToBallerinaDiagnostic extends Diagnostic {
    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final List<DiagnosticProperty<?>> properties;
    private final String message;
    private final String severity;

    public WsdlToBallerinaDiagnostic(String code, String message, DiagnosticSeverity severity,
                                     Location location, Object[] args) {
        this.diagnosticInfo = new DiagnosticInfo(code, message, severity);
        this.location = location;
        this.properties = Collections.emptyList();
        this.message = MessageFormat.format(message, args);
        this.severity = severity.name();
    }

    @Override
    public Location location() {
        return this.location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return this.diagnosticInfo;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return this.properties;
    }

    public String getSeverity() {
        return this.severity;
    }

    @Override
    public  String toString() {
        String severity = this.diagnosticInfo().severity().toString();
        return "[" + severity + "] " + this.message();
    }
}
