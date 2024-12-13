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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.ForceFormattingOptions;
import org.ballerinalang.formatter.core.options.FormattingOptions;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static io.ballerina.wsdl.core.WsdlToBallerina.SEMICOLON;
import static io.ballerina.wsdl.core.WsdlToBallerina.WHITESPACE;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;

public class Utils {
    private static final String XMLDATA_NAME = "@xmldata:Name {value: \"Envelope\"}\n";
    private static final String XMLDATA_NAMESPACE = "@xmldata:Namespace {prefix: \"soap\", uri: \"http://schemas.xmlsoap.org/soap/envelope/\"}\n";
    private static final String PUBLIC_TYPE = "public type ";
    private static final String RECORD_START = " record {|";
    private static final String RECORD_END = "|};\n";
    private static final String BODY_FIELD = "    Body;";

    public static String formatModuleParts(ModulePartNode modulePartNode) throws FormatterException {
        ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder()
                .setForceFormatRecordFields(true).build();
        FormattingOptions formattingOptions = FormattingOptions.builder()
                .setForceFormattingOptions(forceFormattingOptions).build();
        return Formatter.format(modulePartNode.syntaxTree(), formattingOptions).toSourceCode();
    }

    public static void generateTypeDefinitions(String operationName, HashMap<String, ModuleMemberDeclarationNode> nodes,
                                               String requestType, String responseType) {
        StringBuilder sb = new StringBuilder();
        sb.append(XMLDATA_NAME)
                .append(XMLDATA_NAMESPACE)
                .append(PUBLIC_TYPE).append("SoapRequest").append(RECORD_START).append("\n")
                .append("RequestBody").append(WHITESPACE).append(BODY_FIELD).append("\n")
                .append(RECORD_END);

        nodes.put("SoapRequest", NodeParser.parseModuleMemberDeclaration(sb.toString()));

        sb = new StringBuilder();
        sb.append(XMLDATA_NAME)
                .append(XMLDATA_NAMESPACE)
                .append(PUBLIC_TYPE).append("SoapResponse").append(RECORD_START).append("\n")
                .append("ResponseBody").append(WHITESPACE).append(BODY_FIELD).append("\n")
                .append(RECORD_END);

        nodes.put("SoapResponse", NodeParser.parseModuleMemberDeclaration(sb.toString()));

        sb = new StringBuilder();
        sb.append(XMLDATA_NAMESPACE)
                .append(PUBLIC_TYPE).append("ResponseBody").append(RECORD_START).append("\n")
                .append(WHITESPACE + responseType + WHITESPACE + responseType + SEMICOLON).append("\n")
                .append(RECORD_END);

        nodes.put("ResponseBody", NodeParser.parseModuleMemberDeclaration(sb.toString()));

        sb = new StringBuilder();
        sb.append(XMLDATA_NAMESPACE)
                .append(PUBLIC_TYPE).append("RequestBody").append(RECORD_START).append("\n")
                .append(WHITESPACE + requestType + WHITESPACE + requestType + SEMICOLON).append("\n")
                .append(RECORD_END);

        nodes.put("RequestBody", NodeParser.parseModuleMemberDeclaration(sb.toString()));
    }

    public static NodeList<ImportDeclarationNode> createImportNodes(String... importStatements) {
        ArrayList<ImportDeclarationNode> nodeList = new ArrayList<>();
        for (String importStatement : importStatements) {
            ImportDeclarationNode node = NodeParser.parseImportDeclaration(importStatement);
            nodeList.add(node);
        }
        return AbstractNodeFactory.createNodeList(nodeList);
    }

    public static Path handleFileOverwrite(Path destinationFile) {
        if (!Files.exists(destinationFile)) {
            return destinationFile;
        }
        String filePath = destinationFile.toString();
        int counter = 1;
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
        String extension = dotIndex == -1 ? EMPTY_STRING : fileName.substring(dotIndex);
        String parentPath = new File(filePath).getParent() != null ? new File(filePath).getParent() : EMPTY_STRING;
        while (Files.exists(destinationFile)) {
            String newFileName = baseName + "." + counter + extension;
            destinationFile = Path.of(parentPath, newFileName);
            counter++;
        }
        return destinationFile;
    }

    public static StringBuilder generateClientSkeleton(String soapVersion) {
        StringBuilder stringBuilder = new StringBuilder();
        String version = soapVersion.toLowerCase(Locale.ROOT);
        stringBuilder.append(WsdlToBallerina.PUBLIC).append(WHITESPACE).append(WsdlToBallerina.ISOLATED).append(WHITESPACE).append(WsdlToBallerina.CLIENT)
                .append(WHITESPACE).append(WsdlToBallerina.CLASS).append(WHITESPACE).append(WsdlToBallerina.CLIENT_NAME).append(WHITESPACE)
                .append(WsdlToBallerina.OPEN_BRACES);
        stringBuilder.append(WsdlToBallerina.FINAL).append(WHITESPACE).append(version).append(WsdlToBallerina.COLON)
                .append(WsdlToBallerina.CLIENT_NAME).append(WHITESPACE)
                .append(WsdlToBallerina.CLIENT_ENDPOINT_FIELD).append(SEMICOLON);

        stringBuilder.append(WsdlToBallerina.PUBLIC).append(WHITESPACE).append(WsdlToBallerina.ISOLATED).append(WHITESPACE).append(WsdlToBallerina.FUNCTION)
                .append(WHITESPACE).append(WsdlToBallerina.INIT).append(WsdlToBallerina.OPEN_PARENTHESIS).append(WsdlToBallerina.STRING).append(WHITESPACE)
                .append(WsdlToBallerina.SERVICE_URL).append(WsdlToBallerina.COMMA).append(WsdlToBallerina.TYPE_INCLUSION).append(WsdlToBallerina.SOAP).append(WsdlToBallerina.COLON)
                .append(WsdlToBallerina.CLIENT_CONFIG).append(WHITESPACE).append(WsdlToBallerina.CONFIG).append(WsdlToBallerina.CLOSE_PARENTHESIS)
                .append(WHITESPACE).append(WsdlToBallerina.RETURNS).append(WHITESPACE).append(WsdlToBallerina.ERROR_OR_NIL).append(WHITESPACE)
                .append(WsdlToBallerina.OPEN_BRACES).append(WHITESPACE);

        stringBuilder.append(version).append(WsdlToBallerina.COLON).append(WsdlToBallerina.CLIENT_NAME).append(WHITESPACE).append(WsdlToBallerina.SOAP)
                .append(WHITESPACE).append(WsdlToBallerina.EQUALS).append(WHITESPACE).append(WsdlToBallerina.CHECK).append(WHITESPACE).append(WsdlToBallerina.NEW)
                .append(WHITESPACE).append(WsdlToBallerina.OPEN_PARENTHESIS).append(WsdlToBallerina.SERVICE_URL).append(WsdlToBallerina.COMMA)
                .append(WHITESPACE).append(WsdlToBallerina.CONFIG).append(WsdlToBallerina.CLOSE_PARENTHESIS).append(SEMICOLON);

        stringBuilder.append(WsdlToBallerina.SELF).append(WsdlToBallerina.DOT).append(WsdlToBallerina.CLIENT_ENDPOINT_FIELD).append(WHITESPACE).append(WsdlToBallerina.EQUALS)
                .append(WHITESPACE).append(WsdlToBallerina.SOAP).append(SEMICOLON).append(WsdlToBallerina.RETURN)
                .append(SEMICOLON).append(WsdlToBallerina.CLOSE_BRACES);
        return stringBuilder;
    }
}
