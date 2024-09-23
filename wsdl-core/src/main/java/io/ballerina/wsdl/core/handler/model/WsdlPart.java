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

package io.ballerina.wsdl.core.handler.model;

/**
 * Represents a part of a WSDL operation, identifying a specific element by its name and namespace URI.
 *
 * @since 0.1.0
 */
public class WsdlPart {
    private final String elementName;
    private final String elementNsUri;

    private WsdlPart(Builder builder) {
        this.elementName = builder.elementName;
        this.elementNsUri = builder.elementNsUri;
    }

    public String getElementName() {
        return elementName;
    }

    public String getElementNsUri() {
        return elementNsUri;
    }

    public WsdlPart.Builder toBuilder() {
        return new WsdlPart.Builder(this);
    }

    public static class Builder {
        private String elementName;
        private String elementNsUri;

        public Builder(String elementName, String elementNsUri) {
            this.elementName = elementName;
            this.elementNsUri = elementNsUri;
        }

        private Builder(WsdlPart wsdlPart) {
            this.elementName = wsdlPart.elementName;
            this.elementNsUri = wsdlPart.elementNsUri;
        }

        public Builder setElementName(String elementName) {
            this.elementName = elementName;
            return this;
        }

        public Builder setElementNSURI(String elementNSURI) {
            this.elementNsUri = elementNsUri;
            return this;
        }

        public WsdlPart build() {
            return new WsdlPart(this);
        }
    }
}
