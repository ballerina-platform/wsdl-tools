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
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlNsAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint.FieldConstraint;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Ballerina Complex Filed (ie: Record type),
 * <p>
 * This class supports below specs.
 * XSD
 *     - annotations?
 *     - (simpleContent|complexContent|((group|all|choice|sequence)?,((attribute|attributeGroup)*,anyAttribute?)))
 * XSD
 *
 * @since 0.1.0
 */
public class ComplexField implements Field {
    private final String name;
    private final String type;
    private final boolean isRequired;
    private final boolean isNullable;
    private final boolean isArray;
    private final List<Field> fields;
    private final List<String> includedTypes;
    private final List<FieldConstraint> constraints;
    private final List<FieldAnnotation> annotations;
    private final boolean isPartOfCycle;
    private final boolean isParentField;

    private ComplexField(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.isRequired = builder.isRequired;
        this.isNullable = builder.isNullable;
        this.isArray = builder.isArray;
        this.fields = new ArrayList<>(builder.fields);
        this.includedTypes = new ArrayList<>(builder.includedTypes);
        this.constraints = new ArrayList<>(builder.constraints);
        this.annotations = new ArrayList<>(builder.annotations);
        this.isPartOfCycle = builder.isPartOfCycle;
        this.isParentField = builder.isParentField;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }

    public boolean isArray() {
        return isArray;
    }

    public List<Field> getFields() {
        return new ArrayList<>(fields);
    }

    public List<String> getIncludedType() {
        return new ArrayList<>(includedTypes);
    }

    public List<FieldConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public List<FieldAnnotation> getAnnotations() {
        return new ArrayList<>(annotations);
    }

    public boolean isPartOfCycle() {
        return isPartOfCycle;
    }

    public boolean isParentField() {
        return isParentField;
    }

    public ComplexField.Builder toBuilder() {
        return new ComplexField.Builder(this);
    }

    public static class Builder {
        private String name;
        private String type;
        private boolean isRequired;
        private boolean isNullable;
        private boolean isArray;
        private List<Field> fields;
        private List<String> includedTypes;
        private List<FieldConstraint> constraints;
        private List<FieldAnnotation> annotations;
        private boolean isPartOfCycle;
        private boolean isParentField;

        public Builder(String name) {
            this.name = name;
            this.isRequired = true;
            this.isNullable = false;
            this.isArray = false;
            this.fields = new ArrayList<>();
            this.includedTypes = new ArrayList<>();
            this.constraints = new ArrayList<>();
            this.annotations = new ArrayList<>();
            this.isPartOfCycle = false;
            this.isParentField = false;
        }

        private Builder(ComplexField complexField) {
            this.name = complexField.name;
            this.type = complexField.type;
            this.isRequired = complexField.isRequired;
            this.isNullable = complexField.isNullable;
            this.isArray = complexField.isArray;
            this.fields = new ArrayList<>(complexField.fields);
            this.includedTypes = new ArrayList<>(complexField.includedTypes);
            this.constraints = new ArrayList<>(complexField.constraints);
            this.annotations = new ArrayList<>(complexField.annotations);
            this.isPartOfCycle = complexField.isPartOfCycle;
            this.isParentField = complexField.isParentField;
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

        public Builder setArray(boolean isArray) {
            this.isArray = isArray;
            return this;
        }

        public Builder setFields(List<Field> fields) {
            this.fields = new ArrayList<>(fields);
            return this;
        }

        public Builder addField(Field field) {
            this.fields.add(field);
            return this;
        }

        public Builder setIncludedTypes(List<String> includedTypes) {
            this.includedTypes = new ArrayList<>(includedTypes);
            return this;
        }

        public Builder addIncludedType(String includedType) {
            this.includedTypes.add(includedType);
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

        public Builder setPartOfCycle(boolean isPartOfCycle) {
            this.isPartOfCycle = isPartOfCycle;
            return this;
        }

        public Builder setParentField(boolean isParentField) {
            this.isParentField = isParentField;
            return this;
        }

        public ComplexField build() {
            return new ComplexField(this);
        }
    }
}
