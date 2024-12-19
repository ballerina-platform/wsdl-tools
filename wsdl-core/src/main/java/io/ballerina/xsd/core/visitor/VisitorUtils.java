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

package io.ballerina.xsd.core.visitor;

import io.ballerina.xsd.core.component.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.generateFixedValue;

/**
 * Contains utility functions for working with XSD specifications.
 *
 * @since 0.1.0
 */
public final class VisitorUtils {
    public static final String WHITESPACE = " ";
    public static final String EQUAL = "=";
    public static final String DEFAULT = "default";
    public static final String FIXED = "fixed";
    public static final String QUOTATION_MARK = "\"";
    public static final String OPEN_BRACES = "{";
    public static final String CLOSE_BRACES = "}";
    public static final String OPEN_SQUARE_BRACKET = "[";
    public static final String CLOSE_SQUARE_BRACKET = "]";
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String QUESTION_MARK = "?";
    public static final String MAX_OCCURS = "maxOccurs";
    public static final String MIN_OCCURS = "minOccurs";
    public static final String UNBOUNDED = "unbounded";
    public static final String COMMA = ",";
    public static final String COLON = ":";
    public static final String TIME = "time";
    public static final String DATE_TIME = "dateTime";
    public static final String DATE = "date";
    public static final String LANGUAGE = "language";
    public static final String G_YEAR_MONTH = "gYearMonth";
    public static final String G_YEAR = "gYear";
    public static final String STRING = "string";
    public static final String INTEGER = "integer";
    public static final String LONG = "long";
    public static final String NEGATIVE_INTEGER = "negativeInteger";
    public static final String NON_POSITIVE_INTEGER = "nonPositiveInteger";
    public static final String NON_NEGATIVE_INTEGER = "nonNegativeInteger";
    public static final String POSITIVE_INTEGER = "positiveInteger";
    public static final String SHORT = "short";
    public static final String UNSIGNED_LONG = "unsignedLong";
    public static final String UNSIGNED_INT = "unsignedInt";
    public static final String UNSIGNED_SHORT = "unsignedShort";
    public static final String UNSIGNED_BYTE = "unsignedByte";
    public static final String INT = "int";
    public static final String BASE64_BINARY = "base64Binary";
    public static final String BYTE_ARRAY = "byte[]";
    public static final String BOOLEAN = "boolean";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String DECIMAL = "decimal";
    public static final String ANY_URI = "anyURI";
    public static final String BALLERINA_XML_DATA_MODULE = "ballerina/data.xmldata";
    public static final String XMLDATA_NAMESPACE = "@xmldata:Namespace";
    public static final String PREFIX = "prefix";
    public static final String URI = "uri";
    public static final String DURATION = "duration";

    public static String addNamespace(XSDVisitorImpl xsdVisitor, String namespace) {
        xsdVisitor.addImports(BALLERINA_XML_DATA_MODULE);
        return XMLDATA_NAMESPACE + WHITESPACE + OPEN_BRACES + URI + COLON + QUOTATION_MARK + namespace +
                QUOTATION_MARK + CLOSE_BRACES;
    }

    public static void handleFixedValues(Node node, StringBuilder builder, Node typeNode) {
        Node fixedNode = node.getAttributes().getNamedItem(FIXED);
        if (fixedNode != null) {
            builder.append(generateFixedValue(deriveType(typeNode), fixedNode.getNodeValue())).append(WHITESPACE);
        } else {
            builder.append(deriveType(typeNode)).append(WHITESPACE);
        }
    }

    public static void handleDefaultValues(Node node, StringBuilder builder, Node typeNode) {
        Node defaultNode = node.getAttributes().getNamedItem(DEFAULT);
        if (defaultNode != null) {
            builder.append(generateDefaultValue(deriveType(typeNode), defaultNode.getNodeValue()));
        }
    }

    public static String generateDefaultValue(String type, String value) {
        StringBuilder builder = new StringBuilder().append(WHITESPACE).append(EQUAL).append(WHITESPACE);
        switch (type) {
            case INT -> builder.append(value);
            case STRING -> builder.append(QUOTATION_MARK).append(value).append(QUOTATION_MARK);
        }
        return builder.toString();
    }

    public static void handleMinOccurrences(Element element, StringBuilder builder) {
        if (element.isOptional()) {
            builder.append(QUESTION_MARK);
            return;
        }
        Node node = element.getNode();
        Node minOccurrence = node.getAttributes().getNamedItem(MIN_OCCURS);
        if (minOccurrence != null) {
            if (minOccurrence.getNodeValue().equals(ZERO)) {
                builder.append(QUESTION_MARK);
            }
        }
    }

    public static void handleMaxOccurrences(Node node, StringBuilder builder) {
        Node maxOccurrence = node.getAttributes().getNamedItem(MAX_OCCURS);
        if (maxOccurrence != null) {
            String maxValue = maxOccurrence.getNodeValue();
            builder.append(maxValue.equals(UNBOUNDED)
                    ? OPEN_SQUARE_BRACKET + CLOSE_SQUARE_BRACKET + WHITESPACE : !maxValue.equals(ONE)
                    ? OPEN_SQUARE_BRACKET + EMPTY_STRING + CLOSE_SQUARE_BRACKET + WHITESPACE : EMPTY_STRING
            );
        }
    }

    public static void handleFixedValues(StringBuilder builder, Node typeNode, Node fixedNode) {
        builder.append(fixedNode != null
                ? generateFixedValue(deriveType(typeNode), fixedNode.getNodeValue())
                : deriveType(typeNode)).append(WHITESPACE);
    }

    public static String deriveType(Node node) {
        String derivedType = node.getNodeValue().contains(COLON) ?
                node.getNodeValue().substring(node.getNodeValue().indexOf(COLON) + 1) : node.getNodeValue();
        return typeGenerator(derivedType);
    }

    public static String typeGenerator(String typeName) {
        switch (typeName) {
            case TIME, DATE_TIME, DATE, G_YEAR_MONTH, G_YEAR, STRING, LANGUAGE, DURATION, ANY_URI -> {
                return STRING;
            }
            case INTEGER, LONG, NEGATIVE_INTEGER, NON_POSITIVE_INTEGER, POSITIVE_INTEGER, SHORT,
                    UNSIGNED_LONG, UNSIGNED_INT, UNSIGNED_SHORT, UNSIGNED_BYTE, INT, NON_NEGATIVE_INTEGER -> {
                return INT;
            }
            case BASE64_BINARY -> {
                return BYTE_ARRAY;
            }
            default -> {
                return typeName;
            }
        }
    }

    public static boolean isSimpleType(String type) {
        String typeName = type.contains(COLON) ? type.substring(type.indexOf(COLON) + 1) : type;
        String[] simpleTypes = {
                TIME, DATE_TIME, DATE, G_YEAR_MONTH, G_YEAR, STRING, LANGUAGE, DURATION,
                INTEGER, LONG, NEGATIVE_INTEGER, NON_POSITIVE_INTEGER, POSITIVE_INTEGER, SHORT,
                UNSIGNED_LONG, UNSIGNED_INT, UNSIGNED_SHORT,
                UNSIGNED_BYTE, INT, BASE64_BINARY, BOOLEAN, FLOAT, DOUBLE, DECIMAL, ANY_URI, NON_NEGATIVE_INTEGER
        };
        return Arrays.stream(simpleTypes).toList().contains(typeName);
    }

    public static Iterable<Node> asIterable(NodeList nodeList) {
        return () -> new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return nodeList.item(index++);
            }
        };
    }

    public static String convertToCamelCase(String pascalCase) {
        if (pascalCase == null || pascalCase.isEmpty()) {
            return pascalCase;
        }
        return Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);
    }

    public static String sanitizeString(String input) {
        if (!input.matches(".*[!@$%^&*()_\\-|/\\\\\\s\\d].*")
                || (input.matches(".*\\d.*") && !input.matches("^\\d.*"))) {
            return input;
        }
        if (input.matches("^\\d.*")) {
            input = "_" + input;
        }
        String keyPart = input.toLowerCase(Locale.ROOT);
        for (String s : Arrays.asList("[/\\\\]", "\\s", "[!@$%^&*()_\\-|]")) {
            keyPart = keyPart.replaceAll(s, "_");
        }
        return keyPart + " = \"" + input + "\"";
    }
}
