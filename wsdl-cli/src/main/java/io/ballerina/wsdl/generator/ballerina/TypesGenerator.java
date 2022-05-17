/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.wsdl.generator.ballerina;

import com.predic8.schema.Attribute;
import com.predic8.schema.ComplexType;
import com.predic8.schema.Element;
import com.predic8.schema.Schema;
import com.predic8.schema.Sequence;
import com.predic8.schema.SimpleType;
import com.predic8.schema.TypeDefinition;
import com.predic8.schema.restriction.BaseRestriction;
import com.predic8.schema.restriction.facet.EnumerationFacet;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.wsdl.exception.TypesGenerationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createEnumMemberNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ENUM_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.REPRESENTS;
import static io.ballerina.wsdl.generator.CodeGeneratorUtils.escapeIdentifier;

/**
 * This class is used to generate ballerina types file according to given SDL and query files.
 */
public class TypesGenerator {
    private static final Log log = LogFactory.getLog(TypesGenerator.class);
    private static TypesGenerator typesGenerator = null;

    public static TypesGenerator getInstance() {
        if (typesGenerator == null) {
            typesGenerator = new TypesGenerator();
        }
        return typesGenerator;
    }

    /**
     * Generates the types file content.
     *
     * @param wsdlPath                      URL or file path of WSDL file
     * @return                              the types file content
     * @throws TypesGenerationException     when an error occurs during type generation
     */
    public String generateSrc(String wsdlPath) throws TypesGenerationException {
        try {
            String generatedSyntaxTree = Formatter.format(generateSyntaxTree(wsdlPath)).toString();
            return Formatter.format(generatedSyntaxTree);
        } catch (FormatterException | IOException e) {
            throw new TypesGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the types syntax tree.
     *
     * @param wsdlPath         URL of file path of WSDL file
     * @return                  Syntax tree for the types.bal
     * @throws IOException      If an I/O error occurs
     */
    private SyntaxTree generateSyntaxTree(String wsdlPath) throws IOException {
        List<ModuleMemberDeclarationNode> typeDefinitionNodeList = new LinkedList<>();
        List<EnumDeclarationNode> enumDeclarationNodes = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        Definitions wsdl = new WSDLParser().parse(wsdlPath);
        List<Schema> schemas = wsdl.getSchemas();
        for (Schema schema: schemas) {
            handleComplexTypes(schema, typeDefinitionNodeList);
            handleElements(schema, typeDefinitionNodeList);
            handleSimpleTypes(schema, enumDeclarationNodes);
        }

        typeDefinitionNodeList.addAll(enumDeclarationNodes);
        NodeList<ModuleMemberDeclarationNode> members =  createNodeList(typeDefinitionNodeList.toArray(
                new ModuleMemberDeclarationNode[typeDefinitionNodeList.size()]));

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private static void handleComplexTypes(Schema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodeList) {

        List<ComplexType> complexTypeList = schema.getComplexTypes();
        for (ComplexType complexType: complexTypeList) {
            List<Node> recordFieldList = new ArrayList<>();

            String complexTypeName = complexType.getName();

            if (complexType.getSequence() != null) {
                Sequence sequence = complexType.getSequence();
                List<Element> sequenceElements = sequence.getElements();
                for (Element seqElement: sequenceElements) {
                    String occurrence = "";
                    if (!seqElement.getMaxOccurs().contentEquals("1")) {
                        occurrence = "[]";
                    }
                    if (seqElement.getMinOccurs().contentEquals("0")) {
                        occurrence += "?";
                    }

                    if (seqElement.getRef() != null) {
                        String typeName = seqElement.getRef().getLocalPart();
                        typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);

                        String fullTypeName = typeName + occurrence;
                        String fieldName = escapeIdentifier(seqElement.getRef().getLocalPart());

                        RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(fullTypeName),
                                createIdentifierToken(" " + fieldName), null,
                                createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                        );
                        recordFieldList.add(recordFieldNode);

                    } else if (seqElement.getType() != null) {
                        String ballerinaTypeName = getBallerinaTypeName(seqElement.getType().getLocalPart());
                        String fullTypeName = ballerinaTypeName + occurrence;
                        String fieldName = escapeIdentifier(seqElement.getName());

                        RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(fullTypeName),
                                createIdentifierToken(" " + fieldName), null,
                                createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                        );
                        recordFieldList.add(recordFieldNode);
                    }
                }
            }

            if (complexType.getAllAttributes() != null) {
                List<Attribute> attributes = complexType.getAllAttributes();
                for (Attribute attribute: attributes) {
                    if (attribute.getType() != null) {
                        String ballerinaType = getBallerinaTypeName(attribute.getType().getLocalPart());

                        String fieldName = escapeIdentifier(attribute.getName());

                        RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                createIdentifierToken(ballerinaType),
                                createIdentifierToken(" " + fieldName), null,
                                //createToken(SEMICOLON_TOKEN)
                                createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                        );
                        recordFieldList.add(recordFieldNode);
                    }

                }
            }

            NodeList<Node> fieldNodes = createNodeList(recordFieldList);

            RecordTypeDescriptorNode typeDescriptorNode = createRecordTypeDescriptorNode(
                    //createToken(RECORD_KEYWORD),
                    createIdentifierToken(" " + createToken(RECORD_KEYWORD)),
                    createToken(OPEN_BRACE_TOKEN),
                    fieldNodes,
                    null,
                    createToken(CLOSE_BRACE_TOKEN));

            MetadataNode metadataNode = createMetadataNode(createIdentifierToken(REPRESENTS + complexTypeName + "\n"),
                    createEmptyNodeList());
            TypeDefinitionNode typeDefNode = createTypeDefinitionNode(metadataNode,
                    //createToken(PUBLIC_KEYWORD),
                    createIdentifierToken(createToken(PUBLIC_KEYWORD).text() + " "),
                    //createToken(TYPE_KEYWORD),
                    createIdentifierToken(createToken(TYPE_KEYWORD).text() + " "),
                    createIdentifierToken(complexTypeName),
                    typeDescriptorNode,
                    //createToken(SEMICOLON_TOKEN)
                    createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n\n")
            );
            typeDefinitionNodeList.add(typeDefNode);
        }
    }

    private static void handleElements(Schema schema, List<ModuleMemberDeclarationNode> typeDefinitionNodeList) {

        List<Element> elements = schema.getElements();
        for (Element element: elements) {
            if (element.getRef() != null) {
                // Ignoring since a record will be generated for the ref
            } else {
                TypeDefinition typeDef;
                if (element.getType() == null) {
                    typeDef = element.getEmbeddedType();
                } else {
                    if (element.getSchema().getType(element.getType()) != null) {
                        typeDef = element.getSchema().getType(element.getType());
                    } else {
                        typeDef = element.getEmbeddedType();
                    }
                }

                if (typeDef instanceof ComplexType && element.getType() == null) {
                    List<Node> recordFieldList = new ArrayList<>();
                    ComplexType complexType = (ComplexType) typeDef;
                    String complexTypeName = "";

                    if (complexType.getQname() != null) {
                        complexTypeName = complexType.getQname().getLocalPart();
                    } else {
                        complexTypeName = element.getName();
                    }


                    if (complexType.getSequence() != null) {
                        Sequence sequence = complexType.getSequence();
                        List<Element> sequenceElements = sequence.getElements();
                        for (Element seqElement: sequenceElements) {
                            String occurrence = "";
                            if (!seqElement.getMaxOccurs().contentEquals("1")) {
                                occurrence = "[]";
                            }
                            if (seqElement.getMinOccurs().contentEquals("0")) {
                                occurrence += "?";
                            }

                            if (seqElement.getRef() != null) {
                                String typeName = seqElement.getRef().getLocalPart();
                                typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);

                                String fullTypeName = typeName + occurrence;
                                String fieldName = escapeIdentifier(seqElement.getRef().getLocalPart());

                                RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                        createIdentifierToken(fullTypeName),
                                        createIdentifierToken(" " + fieldName), null,
                                        createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                                );
                                recordFieldList.add(recordFieldNode);

                            } else if (seqElement.getType() != null) {
                                String ballerinaTypeName = getBallerinaTypeName(seqElement.getType().getLocalPart());
                                String fullTypeName = ballerinaTypeName + occurrence;
                                String fieldName = escapeIdentifier(seqElement.getName());

                                RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                        createIdentifierToken(fullTypeName),
                                        createIdentifierToken(" " + fieldName), null,
                                        createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                                );
                                recordFieldList.add(recordFieldNode);
                            }
                        }
                    }
                    if (complexType.getAllAttributes() != null) {
                        List<Attribute> attributes = complexType.getAllAttributes();
                        for (Attribute attribute: attributes) {
                            String ballerinaType = getBallerinaTypeName(attribute.getType().getLocalPart());
                            String fieldName = escapeIdentifier(attribute.getName());

                            RecordFieldNode recordFieldNode = createRecordFieldNode(null, null,
                                    createIdentifierToken(ballerinaType),
                                    createIdentifierToken(" " + fieldName), null,
                                    //createToken(SEMICOLON_TOKEN)
                                    createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n")
                            );
                            recordFieldList.add(recordFieldNode);
                        }
                    }

                    NodeList<Node> fieldNodes = createNodeList(recordFieldList);

                    RecordTypeDescriptorNode typeDescriptorNode = createRecordTypeDescriptorNode(
                            //createToken(RECORD_KEYWORD),
                            createIdentifierToken(" " + createToken(RECORD_KEYWORD)),
                            createToken(OPEN_BRACE_TOKEN),
                            fieldNodes,
                            null,
                            createToken(CLOSE_BRACE_TOKEN));

                    MetadataNode metadataNode = createMetadataNode(createIdentifierToken(REPRESENTS
                                    + complexTypeName + "\n"), createEmptyNodeList());
                    TypeDefinitionNode typeDefNode = createTypeDefinitionNode(metadataNode,
                            //createToken(PUBLIC_KEYWORD),
                            createIdentifierToken(createToken(PUBLIC_KEYWORD).text() + " "),
                            //createToken(TYPE_KEYWORD),
                            createIdentifierToken(createToken(TYPE_KEYWORD).text() + " "),
                            createIdentifierToken(complexTypeName),
                            typeDescriptorNode,
                            //createToken(SEMICOLON_TOKEN)
                            createIdentifierToken(createToken(SEMICOLON_TOKEN) + "\n\n")
                    );
                    typeDefinitionNodeList.add(typeDefNode);
                }

            }
        }
    }

    private static void handleSimpleTypes(Schema schema, List<EnumDeclarationNode> enumDeclarationNodes) {
        List<SimpleType> simpleTypes = schema.getSimpleTypes();
        for (SimpleType simpleType: simpleTypes) {
            String simpleTypeName = simpleType.getName();
            BaseRestriction baseRestriction = simpleType.getRestriction();
            //LengthFacet dfd = baseRestriction.getLengthFacet();

            List<EnumerationFacet> enumerationFacets = baseRestriction.getEnumerationFacets();

            List<Node> enumMemberNodeNodeList =  new ArrayList<>();

            for (EnumerationFacet enumerationFacet: enumerationFacets) {
                String enumValue = enumerationFacet.getValue();
                enumMemberNodeNodeList.add(
                        createEnumMemberNode(
                                null,
                                createIdentifierToken(enumValue.toUpperCase() + " "),
                                createToken(EQUAL_TOKEN),
                                createRequiredExpressionNode(createIdentifierToken(" \"" + enumValue + "\""))
                        )
                );
                enumMemberNodeNodeList.add(createToken(COMMA_TOKEN));
            }
            if (!enumerationFacets.isEmpty()) {
                enumMemberNodeNodeList.remove(enumMemberNodeNodeList.size() - 1);
            }

            MetadataNode metadataNode = createMetadataNode(createIdentifierToken(REPRESENTS + simpleTypeName + "\n"),
                    createEmptyNodeList());
            EnumDeclarationNode enumDeclarationNode = createEnumDeclarationNode(metadataNode,
                    //createToken(PUBLIC_KEYWORD),
                    createIdentifierToken(createToken(PUBLIC_KEYWORD).text() + " "),
                    //createToken(ENUM_KEYWORD),
                    createIdentifierToken(createToken(ENUM_KEYWORD).text() + " "),
                    createIdentifierToken(simpleTypeName),
                    createToken(OPEN_BRACE_TOKEN),
                    createSeparatedNodeList(enumMemberNodeNodeList),
                    createToken(CLOSE_BRACE_TOKEN));
            enumDeclarationNodes.add(enumDeclarationNode);

        }

    }

    public static String getBallerinaTypeName(String type) {
        String ballerinaTypeName;
        switch (type) {
            case "long":
                ballerinaTypeName = "int";
                break;
            case "double":
                ballerinaTypeName = "float";
                break;
            case "integer":
                ballerinaTypeName = "int";
                break;
            case "date":
                ballerinaTypeName = "string";
                break;
            case "time":
                ballerinaTypeName = "string";
                break;
            case "dateTime":
                ballerinaTypeName = "string";
                break;
            case "duration":
                ballerinaTypeName = "string";
                break;
            default:
                ballerinaTypeName = type;
        }
        return ballerinaTypeName;
    }
}
