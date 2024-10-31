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
 * Represents a WSDL operation, detailing both its input and output payloads along with operation identifiers.
 *
 * @since 0.1.0
 */
public class WsdlOperation {
    private final String operationName;
    private final String operationAction;
    private final WsdlPayload operationInput;
    private final WsdlPayload operationOutput;

    private WsdlOperation(Builder builder) {
        this.operationName = builder.operationName;
        this.operationAction = builder.operationAction;
        this.operationInput = builder.operationInput;
        this.operationOutput = builder.operationOutput;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getOperationAction() {
        return operationAction;
    }

    public WsdlPayload getOperationInput() {
        return operationInput;
    }

    public WsdlPayload getOperationOutput() {
        return operationOutput;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String operationName;
        private String operationAction;
        private WsdlPayload operationInput;
        private WsdlPayload operationOutput;

        public Builder(String operationName) {
            this.operationName = operationName;
        }

        private Builder(WsdlOperation wsdlOperation) {
            this.operationName = wsdlOperation.operationName;
            this.operationAction = wsdlOperation.operationAction;
            this.operationInput = wsdlOperation.operationInput;
            this.operationOutput = wsdlOperation.operationOutput;
        }

        public Builder setOperationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder setOperationAction(String operationAction) {
            this.operationAction = operationAction;
            return this;
        }

        public Builder setOperationInput(WsdlPayload operationInput) {
            this.operationInput = operationInput;
            return this;
        }

        public Builder setOperationOutput(WsdlPayload operationOutput) {
            this.operationOutput = operationOutput;
            return this;
        }

        public WsdlOperation build() {
            return new WsdlOperation(this);
        }
    }
}
