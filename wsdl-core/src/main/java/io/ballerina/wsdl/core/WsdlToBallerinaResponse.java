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

package io.ballerina.wsdl.core;

import io.ballerina.wsdl.core.diagnostic.WsdlToBallerinaDiagnostic;
import io.ballerina.wsdl.core.generator.GeneratedSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the response from converting a WSDL description into Ballerina client and type definitions.
 *
 * @since 0.1.0
 */
public class WsdlToBallerinaResponse {
    private GeneratedSource clientSource;
    private GeneratedSource typesSource;
    private List<WsdlToBallerinaDiagnostic> diagnostics = new ArrayList<>();

    public GeneratedSource getClientSource() {
        return clientSource;
    }

    public void setClientSource(GeneratedSource clientSource) {
        this.clientSource = clientSource;
    }

    public GeneratedSource getTypesSource() {
        return typesSource;
    }

    public void setTypesSource(GeneratedSource typesSource) {
        this.typesSource = typesSource;
    }

    public List<WsdlToBallerinaDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<WsdlToBallerinaDiagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }
}
