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
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlNameAnnotation;
import io.ballerina.wsdl.core.generator.xsdtorecord.balir.annotation.XmlNsAnnotation;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlHeader;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.wsdl.core.handler.model.WsdlPart;
import io.ballerina.wsdl.core.handler.model.WsdlPayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.wsdl.core.handler.Constants.REGEX_ANY_CHAR_NOT_ALPHA_NUMERIC;
import static io.ballerina.wsdl.core.handler.Constants.REGEX_URL_SCHEME_MATCH;
import static io.ballerina.wsdl.core.handler.Constants.SOAP11_NS_URI;
import static io.ballerina.wsdl.core.handler.Constants.SOAP12_NS_URI;
import static io.ballerina.wsdl.core.handler.Constants.SOAP_BODY;
import static io.ballerina.wsdl.core.handler.Constants.SOAP_ENVELOPE;
import static io.ballerina.wsdl.core.handler.Constants.SOAP_HEADER;
import static io.ballerina.wsdl.core.handler.Constants.SOAP_PREFIX;

/**
 * Manages the processing of WSDL operations, handling the generation of Ballerina fields
 * from WSDL operations.
 *
 * @since 0.1.0
 */
public class OperationHandler {

    private final WsdlOperation wsdlOperation;
    private final List<WsdlOperation> processedOperations;
    private final SoapVersion soapVersion;
    private Map<String, String> uriToPrefixMap = new HashMap<>();
    private Map<String, String> prefixToUriMap = new HashMap<>();

    public OperationHandler(WsdlOperation wsdlOperation, List<WsdlOperation> processedOperations, SoapVersion soapVersion) {
        this.wsdlOperation = wsdlOperation;
        this.processedOperations = processedOperations;
        this.soapVersion = soapVersion;
    }

    public List<Field> generateFields() {
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
        PartHandler partHandler = new PartHandler();

        List<WsdlPart> inputParts = new ArrayList<>();
        List<WsdlPart> inputHeaderParts =
                wsdlOperation.getOperationInput().getHeaders().stream().map(WsdlHeader::getPart).toList();
        List<WsdlPart> inputBodyParts = wsdlOperation.getOperationInput().getMessage().getParts();
        inputParts.addAll(inputHeaderParts);
        inputParts.addAll(inputBodyParts);
        for (WsdlPart inputPart : inputParts) {
            List<Field> inputFields = partHandler.generateFields(inputPart);
            fields.addAll(inputFields);
        }
        fields.add(processOperationPayload(wsdlOperation.getOperationInput(), fields));
        return fields;
    }

    List<Field> generateOutputFields() {
        List<String> processedOpOutputNames =
                processedOperations.stream().map(op -> op.getOperationOutput().getName()).toList();
        if (processedOpOutputNames.contains(wsdlOperation.getOperationOutput().getName())) {
            return new ArrayList<>();
        }
        List<Field> fields = new ArrayList<>();
        PartHandler partHandler = new PartHandler();

        List<WsdlPart> outputParts = new ArrayList<>();
        List<WsdlPart> outputHeaderParts =
                wsdlOperation.getOperationInput().getHeaders().stream().map(WsdlHeader::getPart).toList();
        List<WsdlPart> outputBodyParts = wsdlOperation.getOperationOutput().getMessage().getParts();
        outputParts.addAll(outputHeaderParts);
        outputParts.addAll(outputBodyParts);
        for (WsdlPart outputPart : outputParts) {
            List<Field> outputFields = partHandler.generateFields(outputPart);
            fields.addAll(outputFields);
        }
        fields.add(processOperationPayload(wsdlOperation.getOperationOutput(), fields));
        return fields;
    }

    private ComplexField processOperationPayload(WsdlPayload operationPayload, List<Field> processedFields) {
        XmlNsAnnotation xmlNsAnnotation =
                new XmlNsAnnotation.Builder(soapVersion == SoapVersion.SOAP11 ? SOAP11_NS_URI : SOAP12_NS_URI)
                        .setPrefix(SOAP_PREFIX)
                        .build();
        ComplexField.Builder envelopFieldBuilder = new ComplexField.Builder(SOAP_ENVELOPE)
                .setType(operationPayload.getName() + SOAP_ENVELOPE)
                .addAnnotation(xmlNsAnnotation)
                .addAnnotation(new XmlNameAnnotation.Builder(SOAP_ENVELOPE).build())
                .setParentField(true);
        ComplexField.Builder headerFieldBuilder = new ComplexField.Builder(SOAP_HEADER)
                .setType(operationPayload.getName() + SOAP_HEADER)
                .addAnnotation(xmlNsAnnotation);
        ComplexField.Builder bodyFieldBuilder = new ComplexField.Builder(SOAP_BODY)
                .setType(operationPayload.getName() + SOAP_BODY)
                .addAnnotation(xmlNsAnnotation);

        List<WsdlPart> payloadHeaderParts =
                operationPayload.getHeaders().stream().map(WsdlHeader::getPart).toList();
        for (WsdlPart part : payloadHeaderParts) {
            XmlNsAnnotation headerNsAnnotation = new XmlNsAnnotation.Builder(part.getElementNsUri())
                    .setPrefix(generatePrefix(part.getElementNsUri()))
                    .build();
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> part.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field headerMemberField = filteredFields.get(0);
                if (headerMemberField instanceof ComplexField complexHeaderMemberField) {
                    complexHeaderMemberField = complexHeaderMemberField.toBuilder()
                            .setFields(new ArrayList<>())
                            .setPartOfCycle(true)
                            .addAnnotation(headerNsAnnotation)
                            .build();
                    headerFieldBuilder.addField(complexHeaderMemberField);
                } else if (headerMemberField instanceof BasicField basicHeaderMemberField) {
                    basicHeaderMemberField = basicHeaderMemberField.toBuilder()
                            .addAnnotation(headerNsAnnotation).build();
                    headerFieldBuilder.addField(basicHeaderMemberField);
                }
            }
        }

        // TODO: For body also have to check for both Complex and Simple Fields.
        // TODO: Bug - Here we don't check the included types required fields to mark the body field optional
        //  (We have to check that as well)
        List<WsdlPart> payloadBodyParts = operationPayload.getMessage().getParts();
        for (WsdlPart part : payloadBodyParts) {
            List<Field> filteredFields = processedFields.stream()
                    .filter(field -> part.getElementName().equals(field.getName()))
                    .toList();
            if (!filteredFields.isEmpty()) {
                Field bodyMemberField = filteredFields.get(0);
                if (bodyMemberField instanceof ComplexField complexBodyMemberField) {
                    List<Field> optionalFields = complexBodyMemberField.getFields().stream()
                            .filter(field -> (!field.isRequired() && !field.isNullable())).toList();
                    ComplexField bodyPart = new ComplexField.Builder(part.getElementName())
                            .setType(part.getElementName())
                            .setPartOfCycle(true)
                            .setRequired(!optionalFields.isEmpty())
                            .build();
                    bodyFieldBuilder.addField(bodyPart);
                }
            }
        }
        envelopFieldBuilder.addField(headerFieldBuilder.build());
        envelopFieldBuilder.addField(bodyFieldBuilder.build());
        return envelopFieldBuilder.build();
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
        return uri.replaceFirst(REGEX_URL_SCHEME_MATCH, "").replaceAll(REGEX_ANY_CHAR_NOT_ALPHA_NUMERIC, "");
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
