/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.xsd.core;

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.xsd.core.component.XSDComponent;
import io.ballerina.xsd.core.visitor.VisitorUtils;
import io.ballerina.xsd.core.visitor.XSDVisitor;
import io.ballerina.xsd.core.visitor.XSDVisitorImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.xsd.core.visitor.VisitorUtils.CLOSE_BRACES;
import static io.ballerina.xsd.core.visitor.VisitorUtils.COMMA;
import static io.ballerina.xsd.core.visitor.VisitorUtils.OPEN_BRACES;
import static io.ballerina.xsd.core.visitor.VisitorUtils.QUOTATION_MARK;
import static io.ballerina.xsd.core.visitor.VisitorUtils.WHITESPACE;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.CONTENT_FIELD;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.ENUM;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.NAME;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.RECORD_WITH_OPEN_BRACE;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.SEMICOLON;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.VERTICAL_BAR;

/**
 * This class is used for transforming an XSD into a corresponding record format.
 *
 * @since 0.1.0
 */
public final class XSDToRecord {
    public static final String SCHEMA = "schema";
    public static final String EOF_TOKEN = "";
    public static final String INVALID_IMPORTS_ERROR = "Invalid imports have been found.";
    public static final String INVALID_XSD_FORMAT_ERROR = "The provided XML document is not a valid XSD schema. " +
            "The root element must be a <schema>.";
    public static final String XMLDATA_NAME_ANNOTATION = "@xmldata:Name {value: \"%s\"}";
    public static final String XMLDATA_NAME = "@xmldata:Name";
    public static final String EQUAL = "=";
    public static final String TARGET_NAMESPACE = "targetNamespace";

    public static String convert(Document document) throws Exception {
        Element rootElement = document.getDocumentElement();
        if (!Objects.equals(rootElement.getLocalName(), SCHEMA)) {
            throw new Exception(INVALID_XSD_FORMAT_ERROR);
        }
        XSDVisitor xsdVisitor = new XSDVisitorImpl();
        xsdVisitor.setTargetNamespace(rootElement.getAttribute(TARGET_NAMESPACE));
        Map<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
        processNodeList(rootElement, nodes, xsdVisitor);
        ModulePartNode modulePartNode = Utils.generateModulePartNode(nodes, xsdVisitor);
        return Utils.formatModuleParts(modulePartNode);
    }

    public static void processNodeList(Element rootElement, Map<String, ModuleMemberDeclarationNode> nodes,
                                       XSDVisitor xsdVisitor) throws Exception {
        generateNodes(rootElement, nodes, xsdVisitor);
        processRootElements(nodes, xsdVisitor.getRootElements());
        processNestedElements(nodes, xsdVisitor.getNestedElements());
        processNameResolvers(nodes, xsdVisitor.getNameResolvers());
        processExtensions(nodes, xsdVisitor);
        processEnumerations(nodes, xsdVisitor.getEnumerationElements());
    }

    public static void generateNodes(Element rootElement, Map<String, ModuleMemberDeclarationNode> nodes,
                                     XSDVisitor xsdVisitor) throws Exception {
        for (Node childNode : VisitorUtils.asIterable(rootElement.getChildNodes())) {
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            StringBuilder stringBuilder = new StringBuilder();
            Optional<XSDComponent> component = XSDFactory.generateComponents(childNode);
            if (component.isEmpty()) {
                continue;
            }
            stringBuilder.append(component.get().accept(xsdVisitor));
            ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
            String name = Utils.extractTypeName(moduleNode.toString().split(WHITESPACE));
            if (name == null) {
                name = childNode.getAttributes().getNamedItem(NAME).getNodeValue();
            }
            if (nodes.containsKey(name)) {
                if (Objects.equals(extractTypeName(nodes.get(name).toString()),
                                   extractTypeName(moduleNode.toString()))) {
                    String newNode = String.format(XMLDATA_NAME_ANNOTATION, name)
                            + replaceTypeName(moduleNode.toString(), Utils.resolveNameConflicts(name, nodes));
                    name = Utils.resolveNameConflicts(name, nodes);
                    ModuleMemberDeclarationNode resolvedNode = NodeParser.parseModuleMemberDeclaration(newNode);
                    nodes.put(name, resolvedNode);
                } else {
                    nodes.put(Utils.resolveNameConflicts(name, nodes), moduleNode);
                }
            } else {
                nodes.put(name, moduleNode);
            }

        }
    }

    public static String replaceTypeName(String input, String newTypeName) {
        String regex = "\\b(type\\s+)(\\w+)";
        return input.replaceAll(regex, "$1" + newTypeName);
    }

    public static String extractTypeName(String input) {
        String regex = "\\btype\\s+(\\w+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void processRootElements(Map<String, ModuleMemberDeclarationNode> nodes,
                                           Map<String, String> rootElements) {
        for (String element: rootElements.keySet()) {
            String type = rootElements.get(element);
            String[] tokens = nodes.get(type).toString().split(WHITESPACE);
            if (!nodes.get(type).toString().contains(RECORD_WITH_OPEN_BRACE)) {
                Utils.processSingleTypeElements(nodes, element, type, tokens);
            } else {
                Utils.processRecordTypeElements(nodes, element, type);
            }
        }
    }

    public static void processNestedElements(Map<String, ModuleMemberDeclarationNode> nodes,
                                             Map<String, String> nestedElements) {
        for (String element: nestedElements.keySet()) {
            String nestedElement = nestedElements.get(element);
            ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(nestedElement);
            nodes.put(element, moduleNode);
        }
    }

    public static void processNameResolvers(Map<String, ModuleMemberDeclarationNode> nodes,
                                            Map<String, String> nameResolvers) {
        for (String element: nameResolvers.keySet()) {
            String node = nodes.get(element).toString();
            if (node.contains(XMLDATA_NAME)) {
                continue;
            }
            String newNode = String.format(XMLDATA_NAME_ANNOTATION, nameResolvers.get(element)) + node;
            ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(newNode);
            nodes.put(element, moduleNode);
        }
    }

    public static void processExtensions(Map<String, ModuleMemberDeclarationNode> nodes, XSDVisitor xsdVisitor) {
        Map<String, String> extensions = xsdVisitor.getExtensions();
        for (String key: extensions.keySet()) {
            if (!nodes.containsKey(key)) {
                continue;
            }
            String baseValue = extensions.get(key);
            if (VisitorUtils.isSimpleType(baseValue)) {
                String fields = RECORD_WITH_OPEN_BRACE + baseValue + WHITESPACE + CONTENT_FIELD + SEMICOLON;
                ModuleMemberDeclarationNode parentNode = nodes.get(key);
                String extendedValue = parentNode.toString().replace(RECORD_WITH_OPEN_BRACE, fields);
                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(extendedValue);
                nodes.replace(key, moduleNode);
            } else {
                ModuleMemberDeclarationNode baseNode = nodes.get(baseValue);
                ModuleMemberDeclarationNode parentNode = nodes.get(key);
                String fields = Utils.extractSubstring(baseNode.toString(), RECORD_WITH_OPEN_BRACE,
                        VERTICAL_BAR + CLOSE_BRACES + SEMICOLON);
                fields = RECORD_WITH_OPEN_BRACE + fields;
                String extendedValue = parentNode.toString().replace(RECORD_WITH_OPEN_BRACE, fields);
                ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(extendedValue);
                nodes.replace(key, moduleNode);
            }
        }
    }

    public static void processEnumerations(Map<String, ModuleMemberDeclarationNode> nodes,
                                           Map<String, ArrayList<String>> enumerations) {
        for (String key: enumerations.keySet()) {
            ArrayList<String> enums = enumerations.get(key);
            StringBuilder enumBuilder = new StringBuilder();
            for (String enumValue: enums) {
                if (nodes.containsKey(enumValue)) {
                    enumValue = enumValue.toLowerCase(Locale.ROOT) + WHITESPACE + EQUAL +
                            QUOTATION_MARK + enumValue + QUOTATION_MARK;
                }
                enumBuilder.append(enumValue).append(COMMA);
            }
            String enumeration = nodes.get(key).toString();
            String replacingString = ENUM + WHITESPACE + key + WHITESPACE + OPEN_BRACES;
            enumeration = enumeration.replace(replacingString, replacingString + enumBuilder.substring(0,
                    enumBuilder.length() - 1));
            ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(enumeration);
            nodes.put(key, moduleNode);
        }
    }
}
