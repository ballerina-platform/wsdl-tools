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

package io.ballerina.wsdl.core.handler.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class providing mappings and functions to convert XML Schema Definition (XSD) data types
 * to Ballerina data types.
 *
 * @since 0.1.0
 */
public class HandlerUtils {
    private static final Map<String, String> xsdToBallerinaMap = new HashMap<>();

    static {
        xsdToBallerinaMap.put("byte", "int");
        xsdToBallerinaMap.put("decimal", "decimal");
        xsdToBallerinaMap.put("int", "int");
        xsdToBallerinaMap.put("integer", "int");
        xsdToBallerinaMap.put("long", "int");
        xsdToBallerinaMap.put("negativeInteger", "int");
        xsdToBallerinaMap.put("nonNegativeInteger", "int");
        xsdToBallerinaMap.put("nonPositiveInteger", "int");
        xsdToBallerinaMap.put("positiveInteger", "int");
        xsdToBallerinaMap.put("short", "int");
        xsdToBallerinaMap.put("unsignedLong", "int");
        xsdToBallerinaMap.put("unsignedInt", "int");
        xsdToBallerinaMap.put("unsignedShort", "int");
        xsdToBallerinaMap.put("unsignedByte", "int");

        xsdToBallerinaMap.put("ENTITIES", "string");
        xsdToBallerinaMap.put("ENTITY", "string");
        xsdToBallerinaMap.put("ID", "string");
        xsdToBallerinaMap.put("IDREF", "string");
        xsdToBallerinaMap.put("IDREFS", "string");
        xsdToBallerinaMap.put("language", "string");
        xsdToBallerinaMap.put("Name", "string");
        xsdToBallerinaMap.put("NCName", "string");
        xsdToBallerinaMap.put("NMTOKEN", "string");
        xsdToBallerinaMap.put("NMTOKENS", "string");
        xsdToBallerinaMap.put("normalizedString", "string");
        xsdToBallerinaMap.put("QName", "string");
        xsdToBallerinaMap.put("string", "string");
        xsdToBallerinaMap.put("token", "string");

        xsdToBallerinaMap.put("date", "string");
        xsdToBallerinaMap.put("dateTime", "string");
        xsdToBallerinaMap.put("duration", "string");
        xsdToBallerinaMap.put("gDay", "string");
        xsdToBallerinaMap.put("gMonth", "string");
        xsdToBallerinaMap.put("gMonthDay", "string");
        xsdToBallerinaMap.put("gYear", "string");
        xsdToBallerinaMap.put("gYearMonth", "string");
        xsdToBallerinaMap.put("time", "string");

        // TODO: Enable commented types later.
        xsdToBallerinaMap.put("anyURI", "string");
        xsdToBallerinaMap.put("base64Binary", "byte[]");
        xsdToBallerinaMap.put("boolean", "boolean");
        xsdToBallerinaMap.put("double", "float");
        xsdToBallerinaMap.put("float", "float");
        xsdToBallerinaMap.put("hexBinary", "byte[]");
        xsdToBallerinaMap.put("NOTATION", "string");
        xsdToBallerinaMap.put("QName", "string");
    }

    public static boolean isXsdDataType(String dataType) {
        return xsdToBallerinaMap.containsKey(dataType);
    }
}
