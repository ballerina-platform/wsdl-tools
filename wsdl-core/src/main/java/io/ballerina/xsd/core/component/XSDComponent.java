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

package io.ballerina.xsd.core.component;

import io.ballerina.xsd.core.visitor.XSDVisitor;

/**
 * Interface representing a component of an XSD schema.
 *
 * @since 0.1.0
 */
public interface XSDComponent {
    String accept(XSDVisitor xsdVisitor) throws Exception;
    void setSubType(boolean subType);
    boolean isSubType();
    boolean isOptional();
    boolean isNestedElement();
    void setOptional(boolean isOptional);
    void setNestedElement(boolean isOptional);
}
