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

package io.ballerina.wsdl.core.handler;

import io.ballerina.wsdl.core.generator.xsdtorecord.balir.BasicField;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.ComplexField;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.Field;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlAttributeAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.constraint.EnumConstraint;
import io.ballerina.wsdl.core.handler.util.HandlerUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parses XML schema elements to generate basic fields and complex types representing Ballerina record fields.
 *
 * @since 0.1.0
 */
public class SchemaElementParser {
    private final XmlSchemaElement rootElement;
    private final List<Field> fields;
    private final List<XmlSchemaType> visitedElementTypes;

    protected SchemaElementParser(XmlSchemaElement rootElement) {
        this.rootElement = rootElement;
        this.fields = new ArrayList<>();
        this.visitedElementTypes = new ArrayList<>();
    }

    protected List<Field> parseRootElement() {
        XmlSchemaType elementType = rootElement.getSchemaType();
        if (elementType instanceof XmlSchemaSimpleType simpleElementType) {
            fields.add(processSimpleField(rootElement, simpleElementType));
        } else if (elementType instanceof XmlSchemaComplexType complexElementType) {
            fields.add(processComplexField(rootElement, complexElementType));
        }
        return fields;
    }

    private BasicField processSimpleField(XmlSchemaElement element, XmlSchemaSimpleType elementType) {
        String name = element.getName();
        XmlSchemaSimpleTypeContent typeContent = elementType.getContent();
        if (typeContent == null) {
            String type = element.getSchemaTypeName().getLocalPart();
            return new BasicField.Builder(name)
                    .setType(type)
                    .setNullable(element.isNillable())
                    .setRequired(element.getMinOccurs() > 0)
                    .setDefaultValue(element.getDefaultValue())
                    .setArray(element.getMaxOccurs() > 1)
                    .build();
        }
        if (typeContent instanceof XmlSchemaSimpleTypeList) {
            // TODO: Implement this later
        } else if (typeContent instanceof XmlSchemaSimpleTypeRestriction restriction) {
            String type = restriction.getBaseTypeName().getLocalPart();
            BasicField.Builder elementBasicFieldBuilder = new BasicField.Builder(name)
                    .setType(type)
                    .setNullable(element.isNillable())
                    .setRequired(element.getMinOccurs() > 0)
                    .setDefaultValue(element.getDefaultValue())
                    .setArray(element.getMaxOccurs() > 1);
            processRestriction(restriction, elementBasicFieldBuilder);
            return elementBasicFieldBuilder.build();
        } else if (typeContent instanceof XmlSchemaSimpleTypeUnion) {
            // TODO: Implement this later
        }
        return null;
    }

    private ComplexField processComplexField(XmlSchemaElement element, XmlSchemaComplexType elementType) {
        String name = element.getName();
        ComplexField.Builder complexFieldBuilder = new ComplexField.Builder(name)
                .setType(elementType.getName());
        ComplexField.Builder processedComplexFieldBuilder =
                processComplexField(complexFieldBuilder, elementType);
        processedComplexFieldBuilder
                .setNullable(element.isNillable())
                .setRequired(element.getMinOccurs() > 0)
                .setArray(element.getMaxOccurs() > 1);
        return processedComplexFieldBuilder.build();
    }

    private ComplexField.Builder processComplexField(XmlSchemaComplexType elementType) {
        ComplexField.Builder complexFieldBuilder = new ComplexField.Builder(null)
                .setType(elementType.getName());
        return processComplexField(complexFieldBuilder, elementType);
    }

    private ComplexField.Builder processComplexField(ComplexField.Builder complexFieldBuilder,
                                                     XmlSchemaComplexType elementType) {
        visitedElementTypes.add(elementType);

        // Process Content Models
        XmlSchemaContentModel contentModel = elementType.getContentModel();
        if (contentModel != null) {
            // In this case `particle` should be null;
            if (contentModel instanceof XmlSchemaSimpleContent) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaSimpleContentExtension simpleContent) {
                    String baseType = simpleContent.getBaseTypeName().getLocalPart();
                    String baseTypeNsUri = simpleContent.getBaseTypeName().getNamespaceURI();
                    BasicField contentField = new BasicField.Builder(Constants.CONTENT)
                            .setType(Constants.STRING)
                            .build();
                    complexFieldBuilder.addField(contentField);
                    if (!HandlerUtils.isXsdDataType(baseType)) {
                        complexFieldBuilder.addIncludedType(baseType);
                        processIncludedType(baseType, baseTypeNsUri);
                    }
                    XmlSchemaObjectCollection attributes = ((XmlSchemaSimpleContentExtension) content).getAttributes();
                    if (attributes != null) {
                        for (Iterator it = attributes.getIterator(); it.hasNext(); ) {
                            Object attributeMember = it.next();
                            if (attributeMember instanceof XmlSchemaAttribute attribute) {
                                BasicField attributeBasicField = processAttributeType(attribute);
                                complexFieldBuilder.addField(attributeBasicField);
                            }
                        }
                    }
                } else if (content instanceof XmlSchemaSimpleContentRestriction) {
                    // TODO: Implement this later
                }
            } else if (contentModel instanceof XmlSchemaComplexContent) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaComplexContentExtension complexContent) {
                    String baseType = complexContent.getBaseTypeName().getLocalPart();
                    String baseTypeNsUri = complexContent.getBaseTypeName().getNamespaceURI();
                    if (!HandlerUtils.isXsdDataType(baseType)) {
                        complexFieldBuilder.addIncludedType(baseType);
                        processIncludedType(baseType, baseTypeNsUri);
                    }
                    XmlSchemaParticle particle = complexContent.getParticle();
                    if (particle != null) {
                        processParticle(particle, complexFieldBuilder);
                    }
                    XmlSchemaObjectCollection attributes = complexContent.getAttributes();
                    if (attributes != null) {
                        processAttributes(attributes, complexFieldBuilder);
                    }
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    // TODO: Implement this later
                }
            }
        }
        // Process Particles
        XmlSchemaParticle particle = elementType.getParticle();
        if (particle != null) {
            // In this case `contentModel` should be null;
            processParticle(particle, complexFieldBuilder);
        }
        // Process Attributes
        XmlSchemaObjectCollection attributes = elementType.getAttributes();
        if (attributes != null) {
            processAttributes(attributes, complexFieldBuilder);
        }
        XmlSchemaAnyAttribute anyAttribute = elementType.getAnyAttribute();
        if (anyAttribute != null) {
            // TODO: Implement this later
        }
        return complexFieldBuilder;
    }

    private void processParticle(XmlSchemaParticle particle, ComplexField.Builder complexFieldBuilder) {
        if (particle instanceof XmlSchemaAll) {
            // TODO: Implement this later
        } else if (particle instanceof XmlSchemaChoice) {
            // TODO: Implement this later
        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaObjectCollection childElements = ((XmlSchemaSequence) particle).getItems();
            for (Iterator it = childElements.getIterator(); it.hasNext(); ) {
                Object childElementMember = it.next();
                if (childElementMember instanceof XmlSchemaElement childElement) {
                    XmlSchemaType childElementType = childElement.getSchemaType();
                    if (visitedElementTypes.contains(childElementType)) {
                        ComplexField nestedField = new ComplexField.Builder(childElement.getName())
                                .setType(childElement.getSchemaType().getName())
                                .setNullable(childElement.isNillable())
                                .setRequired(childElement.getMinOccurs() > 0)
                                .setArray(childElement.getMaxOccurs() > 1)
                                .setPartOfCycle(true)
                                .build();
                        complexFieldBuilder.addField(nestedField);
                        continue;
                    }
                    if (childElementType instanceof XmlSchemaSimpleType) {
                        BasicField childElementBasicField =
                                processSimpleField(childElement, (XmlSchemaSimpleType) childElementType);
                        complexFieldBuilder.addField(childElementBasicField);
                    } else if (childElementType instanceof XmlSchemaComplexType) {
                        ComplexField childElementComplexField =
                                processComplexField(childElement, (XmlSchemaComplexType) childElementType);
                        complexFieldBuilder.addField(childElementComplexField);
                    }
                }
            }
        }
    }

    private void processAttributes(XmlSchemaObjectCollection attributes, ComplexField.Builder complexFieldBuilder) {
        for (Iterator it = attributes.getIterator(); it.hasNext(); ) {
            Object attributeMember = it.next();
            if (attributeMember instanceof XmlSchemaAttribute attribute) {
                BasicField attributeBasicField = processAttributeType(attribute);
                complexFieldBuilder.addField(attributeBasicField);
            }
        }
    }

    private void processRestriction(XmlSchemaSimpleTypeRestriction restriction, BasicField.Builder basicFieldBuilder) {
        XmlSchemaObjectCollection facets = restriction.getFacets();
        EnumConstraint.Builder enumConstraintBuilder = new EnumConstraint.Builder();
        for (Iterator it = facets.getIterator(); it.hasNext(); ) {
            XmlSchemaFacet facet = (XmlSchemaFacet) it.next();
            if (facet instanceof XmlSchemaEnumerationFacet) {
                XmlSchemaEnumerationFacet enumFacet = (XmlSchemaEnumerationFacet) facet;
                enumConstraintBuilder.addEnumValue(enumFacet.getValue().toString());
            } else {
                // TODO: There are so many facets have to handle.
            }
        }
        basicFieldBuilder.addConstraint(enumConstraintBuilder.build());
    }

    private BasicField processAttributeType(XmlSchemaAttribute attribute) {
        String attributeName = attribute.getName();
        String attributeType = attribute.getSchemaTypeName().getLocalPart();
        XmlSchema schema = SchemaHandler.getInstance().getSchema(attribute.getSchemaTypeName().getNamespaceURI());
        if (schema != null) {
            XmlSchemaType schemaType = schema.getTypeByName(attribute.getSchemaTypeName());
            if (schemaType instanceof XmlSchemaSimpleType) {
                BasicField dependentBasicField =
                        processDependentBasicFieldForAttribute(attribute, (XmlSchemaSimpleType) schemaType);
                fields.add(dependentBasicField);
            }
        }
        return new BasicField.Builder(attributeName)
                .setType(attributeType)
                .setNullable(false)
                .setRequired(attribute.getUse().getValue().equals(Constants.USE_REQUIRED))
                .setArray(false)
                .addAnnotation(new XmlAttributeAnnotation())
                .build();
    }

    private BasicField processDependentBasicFieldForAttribute(XmlSchemaAttribute attribute,
                                                              XmlSchemaSimpleType elementType) {
        String name = elementType.getName();
        XmlSchemaSimpleTypeContent typeContent = elementType.getContent();
        if (typeContent == null) {
            String type = attribute.getSchemaTypeName().getLocalPart();
            return new BasicField.Builder(name)
                    .setType(type)
                    .setNullable(false)
                    .setRequired(attribute.getUse().getValue().equals(Constants.USE_REQUIRED))
                    .setDefaultValue(attribute.getDefaultValue())
                    .setArray(false)
                    .build();
        }
        if (typeContent instanceof XmlSchemaSimpleTypeList) {
            // TODO: Implement this later
        } else if (typeContent instanceof XmlSchemaSimpleTypeRestriction restriction) {
            String type = restriction.getBaseTypeName().getLocalPart();
            BasicField.Builder basicFieldBuilder = new BasicField.Builder(name)
                    .setType(type)
                    .setNullable(false)
                    .setRequired(attribute.getUse().getValue().equals(Constants.USE_REQUIRED))
                    .setDefaultValue(attribute.getDefaultValue())
                    .setArray(false);
            processRestriction(restriction, basicFieldBuilder);
            return basicFieldBuilder.build();
        } else if (typeContent instanceof XmlSchemaSimpleTypeUnion) {
            // TODO: Implement this later
        }
        return null;
    }

    private void processIncludedType(String includedType, String baseTypeNSURI) {
        XmlSchema schema = SchemaHandler.getInstance().getSchema(baseTypeNSURI);
        if (schema != null) {
            XmlSchemaType schemaType = schema.getTypeByName(includedType);
            if (schemaType instanceof XmlSchemaComplexType) {
                if (!visitedElementTypes.contains(schemaType)) {
                    ComplexField includedTypeField =
                            processComplexField((XmlSchemaComplexType) schemaType).build();
                    fields.add(includedTypeField);
                }
            }
        }
    }
}
