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

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.util.Locale;
import java.util.Map;

import javax.wsdl.Port;

import static io.ballerina.wsdl.core.WsdlToBallerina.CHECK;
import static io.ballerina.wsdl.core.WsdlToBallerina.CLASS;
import static io.ballerina.wsdl.core.WsdlToBallerina.CLIENT_CONFIG;
import static io.ballerina.wsdl.core.WsdlToBallerina.CLIENT_ENDPOINT_FIELD;
import static io.ballerina.wsdl.core.WsdlToBallerina.CLOSE_BRACES;
import static io.ballerina.wsdl.core.WsdlToBallerina.CLOSE_PARENTHESIS;
import static io.ballerina.wsdl.core.WsdlToBallerina.COMMA;
import static io.ballerina.wsdl.core.WsdlToBallerina.CONFIG;
import static io.ballerina.wsdl.core.WsdlToBallerina.DOT;
import static io.ballerina.wsdl.core.WsdlToBallerina.EQUALS;
import static io.ballerina.wsdl.core.WsdlToBallerina.ERROR_OR_NIL;
import static io.ballerina.wsdl.core.WsdlToBallerina.HEADER;
import static io.ballerina.wsdl.core.WsdlToBallerina.NEW;
import static io.ballerina.wsdl.core.WsdlToBallerina.OPEN_BRACES;
import static io.ballerina.wsdl.core.WsdlToBallerina.OPEN_PARENTHESIS;
import static io.ballerina.wsdl.core.WsdlToBallerina.RETURNS;
import static io.ballerina.wsdl.core.WsdlToBallerina.SELF;
import static io.ballerina.wsdl.core.WsdlToBallerina.SEMICOLON;
import static io.ballerina.wsdl.core.WsdlToBallerina.SERVICE_URL;
import static io.ballerina.wsdl.core.WsdlToBallerina.SOAP;
import static io.ballerina.wsdl.core.WsdlToBallerina.TYPE_INCLUSION;
import static io.ballerina.wsdl.core.WsdlToBallerina.WHITESPACE;
import static io.ballerina.xsd.core.visitor.VisitorUtils.convertToPascalCase;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.QUESTION_MARK;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.RECORD;

public final class Utils {
    private static final String XMLDATA_NAME = "@xmldata:Name {value: \"Envelope\"}";
    private static final String PUBLIC_TYPE = "public type ";
    private static final String BODY_FIELD = "Body;";
    public static final String XMLDATA_NAMESPACE = "@xmldata:Namespace {prefix: \"%s\", uri: \"%s\"}";
    public static final String SOAP_REQUEST = "SoapRequest";
    public static final String REQUEST_BODY = "RequestBody";
    public static final String SOAP_RESPONSE = "SoapResponse";
    public static final String RESPONSE_BODY = "ResponseBody";
    public static final String LINE_BREAK = "\n";
    public static final String QUOTATION = "\"";

    private Utils() {}

    public static String formatModuleParts(ModulePartNode modulePartNode) throws FormatterException {
        ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                .setForceFormatRecordFields(true).build();
        FormattingOptions formattingOptions = FormattingOptions.builder()
                .setForceFormattingOptions(forceFormattingOptions).build();
        return Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode();
    }

    public static void generateTypeDefinitions(String namespace, Map<String, ModuleMemberDeclarationNode> nodes,
                                               String requestType, String requestFieldName, String responseType,
                                               String responseFieldName, OperationContext operation) {
        String requestBody = operation.requestBodyName() + WHITESPACE + BODY_FIELD;
        String requestHeader = operation.requestHeaderName() + WHITESPACE + HEADER + QUESTION_MARK + SEMICOLON;
        generateTypeDefinition(namespace, nodes, operation.requestName(),
                   requestHeader + requestBody, true);
        generateTypeDefinition(namespace, nodes, operation.requestBodyName(), requestType +
                               WHITESPACE + requestFieldName + QUESTION_MARK + SEMICOLON, false);
        generateTypeDefinition(EMPTY_STRING, nodes, operation.responseName(),
                   operation.responseBodyName() + WHITESPACE + BODY_FIELD, true);
        generateTypeDefinition(EMPTY_STRING, nodes, operation.responseBodyName(), responseType +
                               WHITESPACE + responseFieldName + QUESTION_MARK + SEMICOLON, false);
    }

    private static void generateTypeDefinition(String namespace, Map<String, ModuleMemberDeclarationNode> nodes,
                                               String typeName, String bodyContent, boolean includeXmlData) {
        StringBuilder builder = new StringBuilder();
        if (includeXmlData) {
            builder.append(XMLDATA_NAME).append(LINE_BREAK);
        }
        if (!namespace.equals(EMPTY_STRING)) {
            builder.append(String.format(XMLDATA_NAMESPACE, SOAP, namespace)).append(LINE_BREAK);
        }
        builder.append(PUBLIC_TYPE).append(typeName).append(WHITESPACE).append(RECORD)
                .append(OPEN_BRACES).append(LINE_BREAK).append(bodyContent).append(LINE_BREAK)
                .append(CLOSE_BRACES).append(SEMICOLON);
        nodes.put(typeName, NodeParser.parseModuleMemberDeclaration(builder.toString()));
    }

    public static StringBuilder generateClientContext(String soapVersion, String serviceUrl, Port port,
                                                      boolean hasMultiplePorts) {
        StringBuilder stringBuilder = new StringBuilder();
        String version = soapVersion.toLowerCase(Locale.ROOT);
        String clientName = hasMultiplePorts
                ? convertToPascalCase(port.getName()) + WsdlToBallerina.CLIENT_NAME : WsdlToBallerina.CLIENT_NAME;
        stringBuilder.append(WsdlToBallerina.PUBLIC).append(WHITESPACE).append(WsdlToBallerina.ISOLATED)
                .append(WHITESPACE).append(WsdlToBallerina.CLIENT).append(WHITESPACE).append(CLASS)
                .append(WHITESPACE).append(clientName).append(WHITESPACE).append(OPEN_BRACES);

        stringBuilder.append(WsdlToBallerina.FINAL).append(WHITESPACE).append(version).append(WsdlToBallerina.COLON)
                .append(WsdlToBallerina.CLIENT_NAME).append(WHITESPACE)
                .append(CLIENT_ENDPOINT_FIELD).append(SEMICOLON);

        stringBuilder.append(WsdlToBallerina.PUBLIC).append(WHITESPACE).append(WsdlToBallerina.ISOLATED)
                .append(WHITESPACE).append(WsdlToBallerina.FUNCTION).append(WHITESPACE).append(WsdlToBallerina.INIT)
                .append(OPEN_PARENTHESIS).append(WsdlToBallerina.STRING).append(WHITESPACE)
                .append(SERVICE_URL).append(WHITESPACE).append(EQUALS).append(WHITESPACE).append(QUOTATION)
                .append(serviceUrl).append(QUOTATION).append(COMMA).append(TYPE_INCLUSION)
                .append(SOAP).append(WsdlToBallerina.COLON).append(CLIENT_CONFIG).append(WHITESPACE)
                .append(CONFIG).append(CLOSE_PARENTHESIS).append(WHITESPACE).append(RETURNS).append(WHITESPACE)
                .append(ERROR_OR_NIL).append(WHITESPACE).append(OPEN_BRACES).append(WHITESPACE);

        stringBuilder.append(SELF).append(DOT).append(CLIENT_ENDPOINT_FIELD).append(WHITESPACE).append(EQUALS)
                .append(WHITESPACE).append(CHECK).append(WHITESPACE).append(NEW).append(WHITESPACE)
                .append(OPEN_PARENTHESIS).append(SERVICE_URL).append(COMMA).append(WHITESPACE)
                .append(CONFIG).append(CLOSE_PARENTHESIS).append(SEMICOLON).append(CLOSE_BRACES);
        return stringBuilder;
    }
}
