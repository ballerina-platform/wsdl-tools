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
import static io.ballerina.wsdl.core.Utils.SOAP_RESPONSE;
import static io.ballerina.wsdl.core.WsdlToBallerina.HEADER;

/**
 * Represents the context of a SOAP operation, including names for requests, responses, headers, and body elements.
 *
 * @param requestName The name of the request type
 * @param responseName The name of the response type
 * @param requestHeaderName The name of the request header
 * @param requestBodyName The name of the request body
 * @param responseBodyName The name of the response body
 *
 * @since 0.1.0
 */
public record OperationContext(String requestName, String responseName, String requestHeaderName,
                               String requestBodyName, String responseBodyName) {
    public OperationContext(String operationName, String suffix) {
        this(
                operationName + suffix + SOAP_REQUEST,
                operationName + suffix + SOAP_RESPONSE,
                operationName + suffix + HEADER,
                operationName + suffix + REQUEST_BODY,
                operationName + suffix + RESPONSE_BODY
        );
    }
}
