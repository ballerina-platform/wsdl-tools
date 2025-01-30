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

import io.ballerina.wsdl.core.HeaderPart;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a WSDL operation, detailing both its input and output payloads along with operation identifiers.
 *
 * @since 0.1.0
 */
public class WsdlOperation {
    private final String operationName;
    private final String operationAction;
    private final String operationInput;
    private final String operationOutput;
    private final String operationUri;
    private final String inputHeaderName;
    private final Map<String, HeaderPart> headerElements;

    private WsdlOperation(Builder builder) {
        this.operationAction = builder.operationAction;
        this.operationInput = builder.operationInput;
        this.operationOutput = builder.operationOutput;
        this.operationUri = builder.operationUri;
        this.operationName = builder.operationName;
        this.headerElements = builder.headerElements;
        this.inputHeaderName = builder.inputHeaderName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getOperationAction() {
        return operationAction;
    }

    public String getOperationInput() {
        return operationInput;
    }

    public String getOperationOutput() {
        return operationOutput;
    }

    public String getOperationUri() {
        return operationUri;
    }

    public Map<String, HeaderPart> getHeaderElements() {
        return headerElements;
    }

    public String getInputHeaderName() {
        return inputHeaderName;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String operationName;
        private String operationAction;
        private String operationInput;
        private String operationOutput;
        private String operationUri;
        private String inputHeaderName;
        private Map<String, HeaderPart> headerElements = new HashMap<>();

        public Builder(String operationName) {
            this.operationName = operationName;
        }

        private Builder(WsdlOperation wsdlOperation) {
            this.operationName = wsdlOperation.operationName;
            this.operationAction = wsdlOperation.operationAction;
            this.operationInput = wsdlOperation.operationInput;
            this.operationOutput = wsdlOperation.operationOutput;
            this.operationUri = wsdlOperation.operationUri;
            this.headerElements = wsdlOperation.headerElements;
            this.inputHeaderName = wsdlOperation.inputHeaderName;
        }

        public Builder setOperationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public void setOperationUri(String operationUri) {
            this.operationUri = operationUri;
        }

        public Builder setOperationAction(String operationAction) {
            this.operationAction = operationAction;
            return this;
        }

        public Builder setOperationInput(String operationInput) {
            this.operationInput = operationInput;
            return this;
        }

        public Builder setOperationOutput(String operationOutput) {
            this.operationOutput = operationOutput;
            return this;
        }

        public Map<String, HeaderPart> getHeaderElements() {
            return headerElements;
        }

        public Builder setHeaderElements(Map<String, HeaderPart> headerElements) {
            this.headerElements = headerElements;
            return this;
        }

        public String getInputHeaderName() {
            return inputHeaderName;
        }

        public Builder setInputHeaderName(String inputHeaderName) {
            this.inputHeaderName = inputHeaderName;
            return this;
        }

        public WsdlOperation build() {
            return new WsdlOperation(this);
        }
    }
}
