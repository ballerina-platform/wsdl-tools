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
 * Represents a header within a WSDL operation, encapsulating a WSDL part.
 *
 * @since 0.1.0
 */
public class WsdlHeader {
    private final WsdlPart part;

    private WsdlHeader(Builder builder) {
        this.part = builder.part;
    }

    public WsdlPart getPart() {
        return part;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private WsdlPart part;

        public Builder(WsdlPart part) {
            this.part = part;
        }

        private Builder(WsdlHeader wsdlHeader) {
            this.part = wsdlHeader.part;
        }

        public Builder setPart(WsdlPart part) {
            this.part = part;
            return this;
        }

        public WsdlHeader build() {
            return new WsdlHeader(this);
        }
    }
}
