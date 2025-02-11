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

import io.ballerina.wsdl.core.handler.model.SoapVersion;

import javax.wsdl.Port;

/**
 * Represents the context of a SOAP port.
 *
 * @param soapVersion The version of the SOAP port
 * @param soapPort The additional information of the SOAP port
 * @param serviceUrl The address of the SOAP port
 *
 * @since 0.1.0
 */
public record SoapPort(SoapVersion soapVersion, Port soapPort, String serviceUrl) {
}
