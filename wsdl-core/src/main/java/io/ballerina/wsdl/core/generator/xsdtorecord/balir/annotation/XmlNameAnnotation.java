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

package io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation;

/**
 * Represents an XML Name annotation in a Ballerina field.
 *
 * @since 0.1.0
 */
public class XmlNameAnnotation implements FieldAnnotation {
    private final String name;

    private XmlNameAnnotation(Builder builder) {
        this.name = builder.name;
    }

    public String getName() {
        return name;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String name;

        public Builder(String name) {
            this.name = name;
        }

        private Builder(XmlNameAnnotation annotation) {
            this.name = annotation.name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public XmlNameAnnotation build() {
            return new XmlNameAnnotation(this);
        }
    }
}
