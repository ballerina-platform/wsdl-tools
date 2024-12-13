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

public class WsdlOperation {
    private String operationInput;
    private String operationOutput;
    private String wsdlAction;
    private String wsdlOperationUri;

    public WsdlOperation(String operationInput, String operationOutput, String wsdlAction, String wsdlOperationUri) {
        this.operationInput = operationInput;
        this.operationOutput = operationOutput;
        this.wsdlAction = wsdlAction;
        this.wsdlOperationUri = wsdlOperationUri;
    }

    public String getOperationInput() {
        return operationInput;
    }

    public String getOperationOutput() {
        return operationOutput;
    }

    public String getWsdlAction() {
        return wsdlAction;
    }

    public String getWsdlOperationUri() {
        return wsdlOperationUri;
    }
}
