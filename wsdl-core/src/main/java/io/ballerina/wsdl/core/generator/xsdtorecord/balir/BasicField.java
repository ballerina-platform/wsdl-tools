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

package io.ballerina.wsdl.core.generator.xsdtorecord.balir;

import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.FieldAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint.FieldConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a basic field within a Ballerina record, encapsulating various properties..
 *
 * @since 0.1.0
 */
public class BasicField implements Field {
    private final String name;
    private final String type;
    private final boolean isRequired;
    private final boolean isNullable;
    private final String defaultValue;
    private final boolean isArray;
    private final List<FieldConstraint> constraints;
    private final List<FieldAnnotation> annotations;

    private BasicField(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.isRequired = builder.isRequired;
        this.isNullable = builder.isNullable;
        this.defaultValue = builder.defaultValue;
        this.isArray = builder.isArray;
        this.constraints = new ArrayList<>(builder.constraints);
        this.annotations = new ArrayList<>(builder.annotations);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isArray() {
        return isArray;
    }

    public List<FieldConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public List<FieldAnnotation> getAnnotations() {
        return new ArrayList<>(annotations);
    }

    public BasicField.Builder toBuilder() {
        return new BasicField.Builder(this);
    }

    public static class Builder {
        private String name;
        private String type;
        private boolean isRequired;
        private boolean isNullable;
        private String defaultValue;
        private boolean isArray;
        private List<FieldConstraint> constraints;
        private List<FieldAnnotation> annotations;

        public Builder(String name) {
            this.name = name;
            this.isRequired = true;
            this.isNullable = false;
            this.isArray = false;
            this.constraints = new ArrayList<>();
            this.annotations = new ArrayList<>();
        }

        private Builder(BasicField basicField) {
            this.name = basicField.name;
            this.type = basicField.type;
            this.isRequired = basicField.isRequired;
            this.isNullable = basicField.isNullable;
            this.defaultValue = basicField.defaultValue;
            this.isArray = basicField.isArray;
            this.constraints = new ArrayList<>(basicField.constraints);
            this.annotations = new ArrayList<>(basicField.annotations);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public Builder setNullable(boolean isNullable) {
            this.isNullable = isNullable;
            return this;
        }

        public Builder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setArray(boolean isArray) {
            this.isArray = isArray;
            return this;
        }

        public Builder setConstraints(List<FieldConstraint> constraints) {
            this.constraints = new ArrayList<>(constraints);
            return this;
        }

        public Builder addConstraint(FieldConstraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public Builder setAnnotations(List<FieldAnnotation> annotations) {
            this.annotations = new ArrayList<>(annotations);
            return this;
        }

        public Builder addAnnotation(FieldAnnotation annotation) {
            this.annotations.add(annotation);
            return this;
        }

        public BasicField build() {
            return new BasicField(this);
        }
    }
}
