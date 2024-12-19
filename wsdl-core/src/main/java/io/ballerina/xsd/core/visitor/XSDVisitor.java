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

import io.ballerina.xsd.core.component.ComplexType;
import io.ballerina.xsd.core.component.Element;
import io.ballerina.xsd.core.component.SimpleType;

import java.util.ArrayList;
import java.util.Map;

/**
 * Defines methods for visiting various components of an XSD schema.
 *
 * @since 0.1.0
 */
public interface XSDVisitor {
    String visit(Element element) throws Exception;
    String visit(Element element, boolean isSubType) throws Exception;
    String visit(ComplexType element) throws Exception;
    String visit(ComplexType element, boolean isSubType) throws Exception;
    String visit(SimpleType element);
    void setTargetNamespace(String targetNamespace);
    String getTargetNamespace();
    ArrayList<String> getImports();
    Map<String, String> getRootElements();
    Map<String, String> getExtensions();
    Map<String, String> getNestedElements();
    Map<String, String> getNameResolvers();
    Map<String, ArrayList<String>> getEnumerationElements();
}
