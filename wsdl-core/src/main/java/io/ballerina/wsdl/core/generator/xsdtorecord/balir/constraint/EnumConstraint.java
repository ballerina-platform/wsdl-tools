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

package io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents constraints that specify allowed enumeration values for a Ballerina field.
 *
 * @since 0.1.0
 */
public class EnumConstraint implements FieldConstraint {
    private final List<String> enumValues;

    private EnumConstraint(Builder builder) {
        this.enumValues = new ArrayList<>(builder.enumValues);
    }

    public List<String> getEnumValues() {
        return new ArrayList<>(enumValues);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private List<String> enumValues;

        public Builder() {
            enumValues = new ArrayList<>();
        }

        private Builder(EnumConstraint constraint) {
            this.enumValues = new ArrayList<>(constraint.enumValues);
        }

        public Builder setEnumValues(List<String> enumValues) {
            this.enumValues = new ArrayList<>(enumValues);
            return this;
        }

        public Builder addEnumValue(String enumValue) {
            this.enumValues.add(enumValue);
            return this;
        }

        public EnumConstraint build() {
            return new EnumConstraint(this);
        }
    }
}
