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

import io.ballerina.wsdl.core.WsdlToBallerinaResponse;

import java.util.List;

/**
 * Utility class for diagnostic operations related to the WSDL to Ballerina conversion process.
 * This class provides methods to integrate diagnostic messages into response structures.
 *
 * @since 0.1.0
 */
public class DiagnosticUtils {

    private DiagnosticUtils() {}

    public static WsdlToBallerinaResponse getDiagnosticResponse(List<DiagnosticMessage> diagnosticMessages,
                                                                WsdlToBallerinaResponse response) {
        List<WsdlToBallerinaDiagnostic> diagnostics = response.getDiagnostics();
        for (DiagnosticMessage message : diagnosticMessages) {
            WsdlToBallerinaDiagnostic diagnostic = new WsdlToBallerinaDiagnostic(
                    message.getCode(), message.getDescription(), message.getSeverity(), null, message.getArgs());
            diagnostics.add(diagnostic);
        }
        return response;
    }
}
