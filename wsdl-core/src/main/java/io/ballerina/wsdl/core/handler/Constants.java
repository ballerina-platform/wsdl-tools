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

package io.ballerina.wsdl.core.handler;

/**
 * Contains constant values used throughout the classes in handler class path.
 *
 * @since 0.1.0
 */
public class Constants {
    public static final String REGEX_ANY_CHAR_NOT_ALPHA_NUMERIC = "[^a-zA-Z0-9]";
    public static final String REGEX_URL_SCHEME_MATCH = "http?://";
    public static final String SOAP11_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_NS_URI = "http://www.w3.org/2003/05/soap-envelope";
    public static final String SOAP_PREFIX = "soap";
    public static final String SOAP_ENVELOPE = "Envelope";
    public static final String SOAP_HEADER = "Header";
    public static final String SOAP_BODY = "Body";
    public static final String CONTENT = "#content";
    public static final String STRING = "string";
    public static final String USE_REQUIRED = "required";
}
