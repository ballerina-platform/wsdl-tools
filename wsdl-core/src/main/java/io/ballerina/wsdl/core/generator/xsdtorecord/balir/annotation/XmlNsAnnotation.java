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
 * Represents an XML Namespace annotation in a Ballerina field.
 *
 * @since 0.1.0
 */
public class XmlNsAnnotation implements FieldAnnotation {
    private final String prefix;
    private final String uri;

    private XmlNsAnnotation(Builder builder) {
        this.prefix = builder.prefix;
        this.uri = builder.uri;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String prefix;
        private String uri;

        public Builder(String uri) {
            this.uri = uri;
        }

        private Builder(XmlNsAnnotation annotation) {
            this.prefix = annotation.prefix;
            this.uri = annotation.uri;
        }

        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public XmlNsAnnotation build() {
            return new XmlNsAnnotation(this);
        }
    }
}
