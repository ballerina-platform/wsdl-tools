/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.xsd.core.component;

import io.ballerina.xsd.core.visitor.XSDVisitor;
import org.w3c.dom.Node;

/**
 * Represents a simple type in the XSD schema.
 *
 * @since 0.1.0
 */
public class SimpleType implements XSDComponent {
    private final Node node;
    private boolean isSubType = false;
    private boolean isOptional = false;
    private boolean isNestedElement = false;

    public SimpleType(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String accept(XSDVisitor xsdVisitor) throws Exception {
        return xsdVisitor.visit(this);
    }

    @Override
    public void setSubType(boolean subType) {
        this.isSubType = subType;
    }

    @Override
    public boolean isSubType() {
        return isSubType;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public boolean isNestedElement() {
        return isNestedElement;
    }

    @Override
    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    @Override
    public void setNestedElement(boolean isNestedElement) {
        this.isNestedElement = isNestedElement;
    }
}
