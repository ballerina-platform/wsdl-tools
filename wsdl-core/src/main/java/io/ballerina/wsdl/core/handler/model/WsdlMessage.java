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

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a WSDL message, which is a collection of logical parts of a web service operation.
 *
 * @since 0.1.0
 */
public class WsdlMessage {
    private final String name;
    private final List<WsdlPart> parts;

    private WsdlMessage(Builder builder) {
        this.name = builder.name;
        this.parts = new ArrayList<>(builder.parts);
    }

    public String getName() {
        return name;
    }

    public List<WsdlPart> getParts() {
        return new ArrayList<>(parts);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String name;
        private List<WsdlPart> parts;

        public Builder(String name) {
            this.name = name;
            this.parts = new ArrayList<>();
        }

        private Builder(WsdlMessage wsdlMessage) {
            this.name = wsdlMessage.name;
            this.parts = new ArrayList<>(wsdlMessage.parts);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setParts(List<WsdlPart> parts) {
            this.parts = new ArrayList<>(parts);
            return this;
        }

        public Builder addPart(WsdlPart part) {
            this.parts.add(part);
            return this;
        }

        public WsdlMessage build() {
            return new WsdlMessage(this);
        }
    }
}
