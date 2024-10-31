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
 * Represents a WSDL service, detailing the SOAP version, service URL, and associated operations.
 *
 * @since 0.1.0
 */
public class WsdlService {
    private final SoapVersion soapVersion;
    private final String soapServiceUrl;
    private final List<WsdlOperation> wsdlOperations;

    private WsdlService(Builder builder) {
        this.soapVersion = builder.soapVersion;
        this.soapServiceUrl = builder.soapServiceUrl;
        this.wsdlOperations = new ArrayList<>(builder.wsdlOperations);
    }

    public SoapVersion getSoapVersion() {
        return soapVersion;
    }

    public String getSoapServiceUrl() {
        return soapServiceUrl;
    }

    public List<WsdlOperation> getWSDLOperations() {
        return new ArrayList<>(wsdlOperations);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private SoapVersion soapVersion;
        private String soapServiceUrl;
        private List<WsdlOperation> wsdlOperations = new ArrayList<>();

        public Builder() {
            this.wsdlOperations = new ArrayList<>();
        }

        private Builder(WsdlService wsdlService) {
            this.soapVersion = wsdlService.soapVersion;
            this.soapServiceUrl = wsdlService.soapServiceUrl;
            this.wsdlOperations = new ArrayList<>(wsdlService.wsdlOperations);
        }

        public Builder setSoapVersion(SoapVersion soapVersion) {
            this.soapVersion = soapVersion;
            return this;
        }

        public Builder setSoapServiceUrl(String soapServiceUrl) {
            this.soapServiceUrl = soapServiceUrl;
            return this;
        }

        public Builder setWsdlOperations(List<WsdlOperation> wsdlOperations) {
            this.wsdlOperations = new ArrayList<>(wsdlOperations);
            return this;
        }

        public Builder addWsdlOperation(WsdlOperation wsdlOperation) {
            this.wsdlOperations.add(wsdlOperation);
            return this;
        }

        public WsdlService build() {
            return new WsdlService(this);
        }
    }
}
