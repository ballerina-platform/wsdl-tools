/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

import static io.ballerina.wsdl.core.Utils.REQUEST_BODY;
import static io.ballerina.wsdl.core.Utils.RESPONSE_BODY;
import static io.ballerina.wsdl.core.Utils.SOAP_REQUEST;
import static io.ballerina.wsdl.core.WsdlToBallerina.HEADER;
import static io.ballerina.wsdl.core.WsdlToBallerina.SOAP_RESPONSE;

public class OperationContext {
    private String requestName;
    private String responseName;
    private String requestHeaderName;
    private String requestBodyName;
    private String responseBodyName;

    public OperationContext(String requestName, String responseName, String requestHeaderName, String requestBodyName) {
        this.requestName = requestName;
        this.responseName = responseName;
        this.requestHeaderName = requestHeaderName;
        this.requestBodyName = requestBodyName;
    }

    public OperationContext(String operationName) {
        this.requestName = operationName + SOAP_REQUEST;
        this.responseName = operationName + SOAP_RESPONSE;
        this.requestHeaderName = operationName + HEADER;
        this.requestBodyName = operationName + REQUEST_BODY;
        this.responseBodyName = operationName + RESPONSE_BODY;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }

    public String getRequestHeaderName() {
        return requestHeaderName;
    }

    public void setRequestHeaderName(String requestHeaderName) {
        this.requestHeaderName = requestHeaderName;
    }

    public String getRequestBodyName() {
        return requestBodyName;
    }

    public void setRequestBodyName(String requestBodyName) {
        this.requestBodyName = requestBodyName;
    }

    public String getResponseBodyName() {
        return responseBodyName;
    }

    public void setResponseBodyName(String responseBodyName) {
        this.responseBodyName = responseBodyName;
    }
}
