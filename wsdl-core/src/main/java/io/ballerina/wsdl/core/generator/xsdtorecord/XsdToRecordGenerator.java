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

package io.ballerina.wsdl.core.generator.xsdtorecord;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.wsdl.core.generator.Constants;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.BasicField;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.ComplexField;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.Field;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.FieldAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlAttributeAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlNameAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlNsAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint.EnumConstraint;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint.FieldConstraint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.wsdl.core.generator.xsdtorecord.util.XsdToRecordUtils.escapeIdentifier;
import static io.ballerina.wsdl.core.generator.xsdtorecord.util.XsdToRecordUtils.getBallerinaType;
import static io.ballerina.wsdl.core.generator.xsdtorecord.util.XsdToRecordUtils.getBallerinaTypeToken;
import static io.ballerina.wsdl.core.generator.xsdtorecord.util.XsdToRecordUtils.incrementFieldName;

/**
 * This class generates Ballerina type definitions from XML Schema Definitions (XSD).
 * It handles both basic and complex fields, incorporating field annotations and constraints into the generated types.
 *
 * @since 0.1.0
 */
public class XsdToRecordGenerator {

    private boolean hasXmlDataUsage = false;
    private Map<String, NonTerminalNode> typeToTypeDescNodes;
    private Map<String, List<AnnotationNode>> typeToAnnotationNodes;

    /**
     * Generates Ballerina types for the given list of fields.
     *
     * @param fields List of fields to generate Ballerina types for.
     */
    public void generateBallerinaTypes(List<Field> fields) {
        typeToTypeDescNodes = new LinkedHashMap<>();
        typeToAnnotationNodes = new LinkedHashMap<>();

        fields.forEach(field -> {
            if (field instanceof BasicField basicField) {
                generateTypeDescriptor(basicField, typeToTypeDescNodes);
            } else if (field instanceof ComplexField complexField) {
                generateRecordTypeDescriptors(complexField, typeToTypeDescNodes, typeToAnnotationNodes);
            }
        });
    }

    public boolean hasXmlDataUsage() {
        return hasXmlDataUsage;
    }

    public Map<String, NonTerminalNode> getTypeToTypeDescNodes() {
        return new LinkedHashMap<>(typeToTypeDescNodes);
    }

    public Map<String, List<AnnotationNode>> getTypeToAnnotationNodes() {
        return new LinkedHashMap<>(typeToAnnotationNodes);
    }

    private void generateTypeDescriptor(BasicField basicField, Map<String, NonTerminalNode> typeToTypeDescNodes) {
        String type = basicField.getName();
        EnumConstraint enumConstraint = null;
        for (FieldConstraint constraint : basicField.getConstraints()) {
            if (constraint instanceof EnumConstraint) {
                enumConstraint = (EnumConstraint) constraint;
            }
        }

        if (enumConstraint != null && !enumConstraint.getEnumValues().isEmpty()) {
            TypeDescriptorNode enumTypeDescNode = generateEnumTypeDescriptor(enumConstraint.getEnumValues());
            typeToTypeDescNodes.put(type, enumTypeDescNode);
            return;
        }
        String fieldRawType = getBallerinaType(basicField.getType());
        TypeDescriptorNode fieldType = getBallerinaTypeToken(fieldRawType);
        typeToTypeDescNodes.put(type, fieldType);
    }

    private void generateRecordTypeDescriptors(ComplexField complexField,
                                               Map<String, NonTerminalNode> typeToTypeDescNodes,
                                               Map<String, List<AnnotationNode>> typeToAnnotNodes) {
        if (complexField.isParentField()) {
            return;
        }
        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);

        String type = complexField.getType();

        List<Node> recordFields = getRecordFields(complexField, typeToTypeDescNodes, typeToAnnotNodes);
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                        fieldNodes, null, bodyEndDelimiter);

        typeToTypeDescNodes.put(type, recordTypeDescriptorNode);
        if (complexField.isParentField() && !complexField.getAnnotations().isEmpty()) {
            for (FieldAnnotation annotation : complexField.getAnnotations()) {
                if (annotation instanceof XmlNsAnnotation xmlNsAnnotation) {
                    addAnnotationNode(typeToAnnotNodes, type,
                            getXMLNamespaceNode(xmlNsAnnotation.getPrefix(), xmlNsAnnotation.getUri()));
                } else if (annotation instanceof XmlNameAnnotation xmlNameAnnotation) {
                    addAnnotationNode(typeToAnnotNodes, type, getXMLNameNode(xmlNameAnnotation.getName()));
                }
            }
        }
    }

    private List<Node> getRecordFields(ComplexField complexField, Map<String, NonTerminalNode> typeToTypeDescNodes,
                                       Map<String, List<AnnotationNode>> typeToAnnotNodes) {
        List<Field> fields = complexField.getFields();
        List<Node> recordFields = new ArrayList<>();

        // TODO: Have to handle when conflicting names are present properly, this is just a small fix,
        //  which will only handle if attribute field comes later,
        for (Field field : fields) {
            if (field instanceof BasicField basicFieldInstance) {
                List<String> collectedRecordFieldNames = recordFields.stream()
                        .map(node -> (RecordFieldNode) node)
                        .map(recNode -> recNode.fieldName().text())
                        .toList();
                String alternateName = null;
                if (collectedRecordFieldNames.contains(basicFieldInstance.getName())) {
                    alternateName = incrementFieldName(basicFieldInstance.getName());
                }
                RecordFieldNode recordFieldNode = getRecordFieldForBasicField(basicFieldInstance, alternateName);
                recordFields.add(recordFieldNode);
            } else if (field instanceof ComplexField complexFieldInstance) {
                RecordFieldNode recordFieldNode =
                        getRecordFieldForComplexField(complexFieldInstance, typeToTypeDescNodes, typeToAnnotNodes);
                recordFields.add(recordFieldNode);
            }
        }

        List<String> includedTypes = complexField.getIncludedType();
        for (String includedType : includedTypes) {
            Token asteriskToken = AbstractNodeFactory.createToken(SyntaxKind.ASTERISK_TOKEN);
            Token typeName = AbstractNodeFactory.createIdentifierToken(includedType);
            Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
            TypeReferenceNode typeReferenceNode =
                    NodeFactory.createTypeReferenceNode(asteriskToken, typeName, semicolonToken);
            recordFields.add(typeReferenceNode);
        }
        return recordFields;
    }

    private RecordFieldNode getRecordFieldForBasicField(BasicField field, String alternateName) {
        List<AnnotationNode> annotations = new ArrayList<>();
        for (FieldAnnotation annotation : field.getAnnotations()) {
            if (annotation instanceof XmlNsAnnotation xmlNsAnnotation) {
                annotations.add(
                        getXMLNamespaceNode(xmlNsAnnotation.getPrefix(), xmlNsAnnotation.getUri()));
            } else if (annotation instanceof XmlAttributeAnnotation) {
                annotations.add(getXMLAttributeNode());
            }
        }
        NodeList<AnnotationNode> annotationNodes = AbstractNodeFactory.createNodeList(annotations);
        MetadataNode metadataNode = null;
        if (!annotations.isEmpty()) {
            metadataNode = NodeFactory.createMetadataNode(null, annotationNodes);
            this.hasXmlDataUsage = true;
        }

        String fieldRawType = getBallerinaType(field.getType());
        TypeDescriptorNode fieldType = getBallerinaTypeToken(fieldRawType);
        if (field.isArray()) {
            fieldType = getArrayTypeDescNodeFor(fieldType);
        }
        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        TypeDescriptorNode optionalFieldType =
                NodeFactory.createOptionalTypeDescriptorNode(fieldType, questionMarkToken);
        IdentifierToken fieldName = AbstractNodeFactory
                .createIdentifierToken(escapeIdentifier(alternateName == null ? field.getName() : alternateName));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);

        return NodeFactory.createRecordFieldNode(metadataNode, null,
                field.isNullable() ? optionalFieldType : fieldType, fieldName,
                field.isRequired() ? null : questionMarkToken, semicolonToken);
    }

    private RecordFieldNode getRecordFieldForComplexField(ComplexField field,
                                                          Map<String, NonTerminalNode> typeToTypeDescNodes,
                                                          Map<String, List<AnnotationNode>> typeToAnnotNodes) {
        List<AnnotationNode> annotations = new ArrayList<>();
        for (FieldAnnotation annotation : field.getAnnotations()) {
            if (annotation instanceof XmlNsAnnotation xmlNsAnnotation) {
                annotations.add(
                        getXMLNamespaceNode(xmlNsAnnotation.getPrefix(), xmlNsAnnotation.getUri()));
            }
        }
        NodeList<AnnotationNode> annotationNodes = AbstractNodeFactory.createNodeList(annotations);
        MetadataNode metadataNode = null;
        if (!annotations.isEmpty()) {
            metadataNode = NodeFactory.createMetadataNode(null, annotationNodes);
            this.hasXmlDataUsage = true;
        }

        Token questionMarkToken = AbstractNodeFactory.createToken(SyntaxKind.QUESTION_MARK_TOKEN);
        String fieldRawType = field.getType();
        Token fieldTypeToken = AbstractNodeFactory.createIdentifierToken(fieldRawType);
        BasicLiteralNode fieldTypeLiteralNode =
                NodeFactory.createBasicLiteralNode(SyntaxKind.IDENTIFIER_TOKEN, fieldTypeToken);
        TypeDescriptorNode fieldType = NodeFactory.createSingletonTypeDescriptorNode(fieldTypeLiteralNode);
        if (field.isArray()) {
            fieldType = getArrayTypeDescNodeFor(fieldType);
        }
        TypeDescriptorNode optionalFieldType =
                NodeFactory.createOptionalTypeDescriptorNode(fieldType, questionMarkToken);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(field.getName()));
        Token semicolonToken = AbstractNodeFactory.createToken(SyntaxKind.SEMICOLON_TOKEN);
        generateRecordTypeDescriptors(field, typeToTypeDescNodes, typeToAnnotNodes);
        return NodeFactory.createRecordFieldNode(metadataNode, null,
                field.isNullable() ? optionalFieldType : fieldType, fieldName,
                field.isRequired() ? null : questionMarkToken, semicolonToken);
    }

    private TypeDescriptorNode generateEnumTypeDescriptor(List<String> enumValues) {
        if (enumValues.size() == 1) {
            return getEnumValueTypeDescNode(enumValues.get(0));
        }

        Token pipeToken = NodeFactory.createToken(SyntaxKind.PIPE_TOKEN);
        TypeDescriptorNode leftTypeDescNode = getEnumValueTypeDescNode(enumValues.get(0));
        for (int i = 1; i < enumValues.size(); i++) {
            TypeDescriptorNode rightTypeDescNode = getEnumValueTypeDescNode(enumValues.get(i));
            leftTypeDescNode =
                    NodeFactory.createUnionTypeDescriptorNode(leftTypeDescNode, pipeToken, rightTypeDescNode);
        }
        return leftTypeDescNode;
    }

    private TypeDescriptorNode getEnumValueTypeDescNode(String enumValue) {
        Token valueToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, "\"" + enumValue + "\"",
                        AbstractNodeFactory.createEmptyMinutiaeList(), AbstractNodeFactory.createEmptyMinutiaeList());
        BasicLiteralNode valueNode = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, valueToken);
        return NodeFactory.createSingletonTypeDescriptorNode(valueNode);
    }

    private ArrayTypeDescriptorNode getArrayTypeDescNodeFor(TypeDescriptorNode typeDescNode) {
        Token openSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACKET_TOKEN);
        Token closeSBracketToken = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACKET_TOKEN);
        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(openSBracketToken, null,
                closeSBracketToken);
        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createNodeList(arrayDimension);

        return NodeFactory.createArrayTypeDescriptorNode(typeDescNode, arrayDimensions);
    }

    private static AnnotationNode getXMLNamespaceNode(String prefix, String uri) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(Constants.XML_DATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken(Constants.XML_NAMESPACE);
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        List<Node> mappingFields = new ArrayList<>();
        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();

        if (prefix != null) {
            IdentifierToken prefixFieldName = AbstractNodeFactory.createIdentifierToken(Constants.XML_PREFIX);
            LiteralValueToken prefixLiteralToken =
                    NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                            String.format("\"%s\"", prefix), emptyMinutiaeList, emptyMinutiaeList);
            BasicLiteralNode prefixValueExpr =
                    NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, prefixLiteralToken);
            MappingFieldNode prefixMappingField =
                    NodeFactory.createSpecificFieldNode(null, prefixFieldName, colon, prefixValueExpr);
            mappingFields.add(prefixMappingField);
            mappingFields.add(NodeFactory.createToken(SyntaxKind.COMMA_TOKEN));
        }

        IdentifierToken uriFieldName = AbstractNodeFactory.createIdentifierToken(Constants.XML_URI);
        LiteralValueToken uriLiteralToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, String.format("\"%s\"", uri),
                        emptyMinutiaeList, emptyMinutiaeList);
        BasicLiteralNode uriValueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, uriLiteralToken);
        MappingFieldNode uriMappingField =
                NodeFactory.createSpecificFieldNode(null, uriFieldName, colon, uriValueExpr);
        mappingFields.add(uriMappingField);

        SeparatedNodeList<MappingFieldNode> mappingFieldNodes =
                AbstractNodeFactory.createSeparatedNodeList(mappingFields);
        MappingConstructorExpressionNode annotValue =
                NodeFactory.createMappingConstructorExpressionNode(openBrace, mappingFieldNodes, closeBrace);

        return NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    private AnnotationNode getXMLNameNode(String value) {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(Constants.XML_DATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken(Constants.XML_NAME);
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        Token openBrace = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
        Token closeBrace = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(Constants.XML_VALUE);
        MinutiaeList emptyMinutiaeList = AbstractNodeFactory.createEmptyMinutiaeList();
        LiteralValueToken literalToken =
                NodeFactory.createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, String.format("\"%s\"", value),
                        emptyMinutiaeList, emptyMinutiaeList);
        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL, literalToken);
        MappingFieldNode mappingField =
                NodeFactory.createSpecificFieldNode(null, fieldName, colon, valueExpr);
        SeparatedNodeList<MappingFieldNode> mappingFields = AbstractNodeFactory.createSeparatedNodeList(mappingField);
        MappingConstructorExpressionNode annotValue =
                NodeFactory.createMappingConstructorExpressionNode(openBrace, mappingFields, closeBrace);

        return NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    private AnnotationNode getXMLAttributeNode() {
        Token atToken = AbstractNodeFactory.createToken(SyntaxKind.AT_TOKEN);

        IdentifierToken modulePrefix = AbstractNodeFactory.createIdentifierToken(Constants.XML_DATA);
        Token colon = AbstractNodeFactory.createToken(SyntaxKind.COLON_TOKEN);
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken(Constants.XML_ATTRIBUTE);
        Node annotReference = NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);

        return NodeFactory.createAnnotationNode(atToken, annotReference, null);
    }

    private void addAnnotationNode(Map<String, List<AnnotationNode>> map, String key, AnnotationNode node) {
        List<AnnotationNode> nodes = map.computeIfAbsent(key, k -> new ArrayList<>());
        nodes.add(node);
    }
}
