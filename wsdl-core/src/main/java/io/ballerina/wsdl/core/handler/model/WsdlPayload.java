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

/**
 * Represents the payload of a WSDL operation, consisting of headers and a message.
 *
 * @since 0.1.0
 */
public class WsdlPayload {
    private final String name;
    private final List<WsdlHeader> headers;
    private final WsdlMessage message;

    private WsdlPayload(Builder builder) {
        this.name = builder.name;
        this.headers = new ArrayList<>(builder.headers);
        this.message = builder.message;
    }

    public String getName() {
        return name;
    }

    public List<WsdlHeader> getHeaders() {
        return new ArrayList<>(headers);
    }

    public WsdlMessage getMessage() {
        return message;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String name;
        private List<WsdlHeader> headers;
        private WsdlMessage message;

        public Builder() {
            this.headers = new ArrayList<>();
        }

        private Builder(WsdlPayload wsdlPayload) {
            this.name = wsdlPayload.name;
            this.headers = new ArrayList<>(wsdlPayload.headers);
            this.message = wsdlPayload.message;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHeaders(List<WsdlHeader> headers) {
            this.headers = new ArrayList<>(headers);
            return this;
        }

        public Builder addHeader(WsdlHeader header) {
            this.headers.add(header);
            return this;
        }

        public Builder setMessage(WsdlMessage message) {
            this.message = message;
            return this;
        }

        public WsdlPayload build() {
            return new WsdlPayload(this);
        }
    }
}
