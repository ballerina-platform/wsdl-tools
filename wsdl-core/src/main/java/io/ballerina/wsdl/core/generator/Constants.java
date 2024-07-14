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

package io.ballerina.wsdl.core.generator;

import io.ballerina.compiler.syntax.tree.SyntaxInfo;

import java.util.List;

/**
 * Contains constant values used throughout the classes in generator class path.
 *
 * @since 0.1.0
 */
public class Constants {

    // Constants related to escaping Identifier Token
    public static final List<String> BAL_KEYWORDS = SyntaxInfo.keywords();
    public static final String REGEX_ONLY_NUMBERS_OR_NUMBERS_WITH_SPECIAL_CHARACTERS = "\\b[0-9([\\[\\]\\\\?!<>@#&~'`" +
            "*\\-=^+();:\\/{}\\s|.$])]*\\b";
    public static final String REGEX_WORDS_STARTING_WITH_NUMBERS = "^[0-9].*";
    public static final String ESCAPE_PATTERN = "([\\[\\]\\\\?!<>@#&~'`*\\-=^+();:\\/{}\\s|.$])";
    public static final String REGEX_WITHOUT_SPECIAL_CHARACTERS = "\\b[_a-zA-Z][_a-zA-Z0-9]*\\b";

    // Constants related to XsdToRecordGenerator
    public static final String XML_DATA = "xmldata";
    public static final String XML_NAMESPACE = "Namespace";
    public static final String XML_PREFIX = "prefix";
    public static final String XML_URI = "uri";
    public static final String XML_NAME = "Name";
    public static final String XML_VALUE = "value";
    public static final String XML_ATTRIBUTE = "Attribute";

    // Constants related to ClientGenerator
    public static final String BALLERINA = "ballerina";
    public static final String SOAP_SOAP11 = "soap.soap11";
    public static final String SOAP_SOAP12 = "soap.soap12";

    public static final String INPUT = "input";

    public static final String CLIENT = "Client";
    public static final String SOAP_CLIENT = "soapClient";
    public static final String SOAP11 = "soap11";
    public static final String SOAP12 = "soap12";
    public static final String INIT = "init";
    public static final String SELF = "self";
    public static final String SERVICE_URL = "serviceUrl";

    public static final String DATA = "data";
}
