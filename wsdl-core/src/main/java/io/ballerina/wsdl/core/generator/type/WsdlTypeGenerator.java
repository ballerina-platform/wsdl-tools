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

package io.ballerina.wsdl.core.generator.type;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.wsdl.core.generator.Constants;
import io.ballerina.wsdl.core.generator.util.GeneratorUtils;
import io.ballerina.wsdl.core.generator.xsdtorecord.XsdToRecordGenerator;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.Field;
import io.ballerina.wsdl.core.handler.OperationHandler;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.wsdl.core.handler.model.WsdlService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates type definitions from a WSDL service for operations.
 *
 * @since 0.1.0
 */
public class WsdlTypeGenerator {
    private final WsdlService wsdlService;
    private final SoapVersion soapVersion;

    public WsdlTypeGenerator(WsdlService wsdlService, SoapVersion soapVersion) {
        this.wsdlService = wsdlService;
        this.soapVersion = soapVersion;
    }

    public ModulePartNode getWsdlTypeModulePart(List<String> operations) {
        XsdToRecordGenerator recordGenerator = new XsdToRecordGenerator();
        Map<String, NonTerminalNode> allTypeToTypeDescNodes = new LinkedHashMap<>();
        Map<String, List<AnnotationNode>> allTypeToAnnotationNodes = new LinkedHashMap<>();
        List<WsdlOperation> wsdlOperations = wsdlService.getWSDLOperations();
        List<WsdlOperation> processedOperations = new ArrayList<>();
        for (WsdlOperation wsdlOperation : wsdlOperations) {
            if (operations.isEmpty() || operations.contains(wsdlOperation.getOperationName())) {
                OperationHandler operationHandler =
                        new OperationHandler(wsdlOperation, processedOperations, soapVersion);
                List<Field> fields = operationHandler.generateFields();
                recordGenerator.generateBallerinaTypes(fields);
                Map<String, NonTerminalNode> typeToTypeDescNodes = recordGenerator.getTypeToTypeDescNodes();
                Map<String, List<AnnotationNode>> typeToAnnotationNodes = recordGenerator.getTypeToAnnotationNodes();
                allTypeToTypeDescNodes.putAll(typeToTypeDescNodes);
                allTypeToAnnotationNodes.putAll(typeToAnnotationNodes);
                processedOperations.add(wsdlOperation);
            }
        }

        boolean usesXmlData = recordGenerator.hasXmlDataUsage();
        NodeList<ImportDeclarationNode> imports;
        if (usesXmlData) {
            imports = AbstractNodeFactory.createNodeList(getImportDeclarations());
        } else {
            imports = AbstractNodeFactory.createEmptyNodeList();
        }

        List<Map.Entry<String, NonTerminalNode>> typeToTypeDescEntries =
                new ArrayList<>(allTypeToTypeDescNodes.entrySet());
        List<TypeDefinitionNode> typeDefNodes = typeToTypeDescEntries.stream()
                .map(entry -> {
                    List<AnnotationNode> annotations = new ArrayList<>();
                    String recordName = entry.getKey();
                    Token typeKeyWord = AbstractNodeFactory.createToken(SyntaxKind.TYPE_KEYWORD);
                    if (allTypeToAnnotationNodes.containsKey(recordName)) {
                        annotations.addAll(allTypeToAnnotationNodes.get(recordName));
                    }
                    NodeList<AnnotationNode> annotationNodes = NodeFactory.createNodeList(annotations);
                    MetadataNode metadata = NodeFactory.createMetadataNode(null, annotationNodes);
                    IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken(recordName);
                    Token semicolon = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
                    return NodeFactory.createTypeDefinitionNode(metadata, null, typeKeyWord, typeName,
                            entry.getValue(), semicolon);
                }).toList();

        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(new ArrayList<>(typeDefNodes));

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        return NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
    }

    private List<ImportDeclarationNode> getImportDeclarations() {
        ImportDeclarationNode importForXmlData = GeneratorUtils.getImportDeclarationNode(
                Constants.BALLERINA, Constants.DATA + "." + Constants.XML_DATA);
        return new ArrayList<>(List.of(importForXmlData));
    }
}
