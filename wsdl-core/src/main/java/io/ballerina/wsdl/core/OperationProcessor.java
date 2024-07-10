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

package io.ballerina.wsdl.core;

import io.ballerina.wsdl.core.recordgenerator.ballerinair.BasicField;
import io.ballerina.wsdl.core.recordgenerator.ballerinair.ComplexField;
import io.ballerina.wsdl.core.recordgenerator.ballerinair.Field;
import io.ballerina.wsdl.core.recordgenerator.ballerinair.XMLNSAttribute;
import io.ballerina.wsdl.core.wsdlmodel.SOAPVersion;
import io.ballerina.wsdl.core.wsdlmodel.WSDLHeader;
import io.ballerina.wsdl.core.wsdlmodel.WSDLOperation;
import io.ballerina.wsdl.core.wsdlmodel.WSDLPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationProcessor {
    private final WSDLOperation wsdlOperation;
    private final List<WSDLOperation> processedOperations;
    private final SOAPVersion soapVersion;
    private Map<String, String> uriToPrefixMap = new HashMap<>();
    private Map<String, String> prefixToUriMap = new HashMap<>();

    OperationProcessor(WSDLOperation wsdlOperation, List<WSDLOperation> processedOperations, SOAPVersion soapVersion) {
        this.wsdlOperation = wsdlOperation;
        this.processedOperations = processedOperations;
        this.soapVersion = soapVersion;
    }

    List<Field> generateFields() {
        List<Field> fields = new ArrayList<>();
        List<Field> inputFields = generateInputFields();
        List<Field> outputFields = generateOutputFields();
        fields.addAll(inputFields);
        fields.addAll(outputFields);
        return fields;
    }

    private List<Field> generateInputFields() {
        List<String> processedOpInputNames =
                processedOperations.stream().map(op -> op.getOperationInput().getName()).toList();
        if (processedOpInputNames.contains(wsdlOperation.getOperationInput().getName())) {
            return new ArrayList<>();
        }
        List<Field> fields = new ArrayList<>();
        PartProcessor partProcessor = new PartProcessor();

        List<WSDLPart> inputParts = new ArrayList<>();
        List<WSDLPart> inputHeaderParts =
                wsdlOperation.getOperationInput().getHeaders().stream().map(WSDLHeader::getPart).toList();
        List<WSDLPart> inputBodyParts = wsdlOperation.getOperationInput().getMessage().getParts();
        inputParts.addAll(inputHeaderParts);
        inputParts.addAll(inputBodyParts);
        for (WSDLPart inputPart : inputParts) {
            List<Field> inputFields = partProcessor.generateFields(inputPart);
            fields.addAll(inputFields);
        }
        fields.add(processOperationInput(wsdlOperation, fields));
        return fields;
    }

    List<Field> generateOutputFields() {
        List<String> processedOpOutputNames =
                processedOperations.stream().map(op -> op.getOperationOutput().getName()).toList();
        if (processedOpOutputNames.contains(wsdlOperation.getOperationOutput().getName())) {
            return new ArrayList<>();
        }
        List<Field> fields = new ArrayList<>();
        PartProcessor partProcessor = new PartProcessor();

        List<WSDLPart> outputParts = new ArrayList<>();
        List<WSDLPart> outputHeaderParts =
                wsdlOperation.getOperationInput().getHeaders().stream().map(WSDLHeader::getPart).toList();
        List<WSDLPart> outputBodyParts = wsdlOperation.getOperationOutput().getMessage().getParts();
        outputParts.addAll(outputHeaderParts);
        outputParts.addAll(outputBodyParts);
        for (WSDLPart outputPart : outputParts) {
            List<Field> outputFields = partProcessor.generateFields(outputPart);
            fields.addAll(outputFields);
        }
        fields.add(processOperationOutput(wsdlOperation, fields));
        return fields;
    }

    private ComplexField processOperationInput(WSDLOperation operation, List<Field> processedFields) {
        XMLNSAttribute soapAttribute = new XMLNSAttribute(soapVersion == SOAPVersion.SOAP11 ?
                "http://schemas.xmlsoap.org/soap/envelope/" : "http://www.w3.org/2003/05/soap-envelope");
        soapAttribute.setPrefix("soap");
        ComplexField envelopField =
                new ComplexField("Envelope", operation.getOperationInput().getName() + "Envelope");
        envelopField.addXmlnsAttribute(soapAttribute);
        envelopField.setAttributeName("Envelope");
        envelopField.setRootNode(true);
        ComplexField headerField =
                new ComplexField("Header", operation.getOperationInput().getName() + "Header");
        headerField.addXmlnsAttribute(soapAttribute);
        ComplexField bodyField =
                new ComplexField("Body", operation.getOperationInput().getName() + "Body");
        bodyField.addXmlnsAttribute(soapAttribute);

        List<WSDLPart> inputHeaderParts =
                wsdlOperation.getOperationInput().getHeaders().stream().map(WSDLHeader::getPart).toList();
        for (WSDLPart inputPart : inputHeaderParts) {
            XMLNSAttribute headerNSAttr = new XMLNSAttribute(inputPart.getElementNSURI());
            headerNSAttr.setPrefix(generatePrefix(inputPart.getElementNSURI()));
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> inputPart.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field headerMemberField = filteredFields.get(0);
                if (headerMemberField instanceof ComplexField complexHeaderMemberField) {
                    complexHeaderMemberField.setFields(new ArrayList<>());
                    complexHeaderMemberField.setCyclicDep(true);
                    complexHeaderMemberField.addXmlnsAttribute(headerNSAttr);
                    headerField.addField(complexHeaderMemberField);
                } else if (headerMemberField instanceof BasicField basicHeaderMemberField) {
                    basicHeaderMemberField.addXmlnsAttribute(headerNSAttr);
                    headerField.addField(basicHeaderMemberField);
                }
            }
        }

        // TODO: For body also have to check for both Complex and Simple Fields.
        // TODO: Bug - Here we don't check the included types required fields to mark the body field optional
        //  (We have to check that as well)
        List<WSDLPart> inputBodyParts = wsdlOperation.getOperationInput().getMessage().getParts();
        for (WSDLPart inputPart : inputBodyParts) {
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> inputPart.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field bodyMemberField = filteredFields.get(0);
                if (bodyMemberField instanceof ComplexField complexBodyMemberField) {
                    List<Field> optionalFields = complexBodyMemberField.getFields().stream()
                            .filter(field -> (!field.isRequired() && !field.isNullable())).toList();
                    ComplexField bodyPart = new ComplexField(inputPart.getElementName(), inputPart.getElementName());
                    bodyPart.setCyclicDep(true);
                    bodyPart.setRequired(!optionalFields.isEmpty());

                    bodyField.addField(bodyPart);
                }
            }
        }
        envelopField.addField(headerField);
        envelopField.addField(bodyField);
        return envelopField;
    }

    private ComplexField processOperationOutput(WSDLOperation operation, List<Field> processedFields) {
        XMLNSAttribute soapAttribute = new XMLNSAttribute(soapVersion == SOAPVersion.SOAP11 ?
                "http://schemas.xmlsoap.org/soap/envelope/" : "http://www.w3.org/2003/05/soap-envelope");
        soapAttribute.setPrefix("soap");
        ComplexField envelopField =
                new ComplexField("Envelope", operation.getOperationOutput().getName() + "Envelope");
        envelopField.addXmlnsAttribute(soapAttribute);
        envelopField.setAttributeName("Envelope");
        envelopField.setRootNode(true);
        ComplexField headerField =
                new ComplexField("Header", operation.getOperationOutput().getName() + "Header");
        headerField.addXmlnsAttribute(soapAttribute);
        ComplexField bodyField =
                new ComplexField("Body", operation.getOperationOutput().getName() + "Body");
        bodyField.addXmlnsAttribute(soapAttribute);

        List<WSDLPart> outputHeaderParts =
                wsdlOperation.getOperationOutput().getHeaders().stream().map(WSDLHeader::getPart).toList();
        for (WSDLPart outputPart : outputHeaderParts) {
            XMLNSAttribute headerNSAttr = new XMLNSAttribute(outputPart.getElementNSURI());
            headerNSAttr.setPrefix(generatePrefix(outputPart.getElementNSURI()));
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> outputPart.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field headerMemberField = filteredFields.get(0);
                if (headerMemberField instanceof ComplexField complexHeaderMemberField) {
                    complexHeaderMemberField.setFields(new ArrayList<>());
                    complexHeaderMemberField.setCyclicDep(true);
                    complexHeaderMemberField.addXmlnsAttribute(headerNSAttr);
                    headerField.addField(complexHeaderMemberField);
                } else if (headerMemberField instanceof BasicField basicHeaderMemberField) {
                    basicHeaderMemberField.addXmlnsAttribute(headerNSAttr);
                    headerField.addField(basicHeaderMemberField);
                }
            }
        }

        // TODO: For body also have to check for both Complex and Simple Fields.
        // TODO: Bug - Here we don't check the included types required fields to mark the body field optional
        //  (We have to check that as well)
        List<WSDLPart> outputBodyParts = wsdlOperation.getOperationOutput().getMessage().getParts();
        for (WSDLPart outputPart : outputBodyParts) {
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> outputPart.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field bodyMemberField = filteredFields.get(0);
                if (bodyMemberField instanceof ComplexField complexBodyMemberField) {
                    List<Field> optionalFields = complexBodyMemberField.getFields().stream()
                            .filter(field -> (!field.isRequired() && !field.isNullable())).toList();
                    ComplexField bodyPart = new ComplexField(outputPart.getElementName(), outputPart.getElementName());
                    bodyPart.setCyclicDep(true);
                    bodyPart.setRequired(!optionalFields.isEmpty());

                    bodyField.addField(bodyPart);
                }
            }
        }
        envelopField.addField(headerField);
        envelopField.addField(bodyField);
        return envelopField;
    }

    public String generatePrefix(String uri) {
        if (uriToPrefixMap.containsKey(uri)) {
            return uriToPrefixMap.get(uri);
        }

        String normalizedUri = normalizeUri(uri);
        String uniquePrefix = createUniquePrefix(normalizedUri);

        uriToPrefixMap.put(uri, uniquePrefix);
        prefixToUriMap.put(uniquePrefix, uri);

        return uniquePrefix;
    }

    private String normalizeUri(String uri) {
        return uri.replaceFirst("http?://", "").replaceAll("[^a-zA-Z0-9]", "");
    }

    private String createUniquePrefix(String base) {
        for (int len = 3; len <= base.length(); len++) {
            String candidatePrefix = base.substring(0, len);
            if (!prefixToUriMap.containsKey(candidatePrefix)) {
                return candidatePrefix;
            }
        }

        int suffix = 1;
        while (prefixToUriMap.containsKey(base + suffix)) {
            suffix++;
        }
        return base + suffix;
    }
}
