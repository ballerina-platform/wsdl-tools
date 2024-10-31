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

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.wsdl.core.diagnostic.DiagnosticMessage;
import io.ballerina.wsdl.core.diagnostic.DiagnosticUtils;
import io.ballerina.wsdl.core.generator.GeneratedSource;
import io.ballerina.wsdl.core.generator.type.WsdlTypeGenerator;
import io.ballerina.wsdl.core.handler.SchemaHandler;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlHeader;
import io.ballerina.wsdl.core.handler.model.WsdlMessage;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.wsdl.core.handler.model.WsdlPart;
import io.ballerina.wsdl.core.handler.model.WsdlPayload;
import io.ballerina.wsdl.core.handler.model.WsdlService;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.FormattingOptions;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLElement;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

/**
 * Provides functionality to convert WSDL specifications into Ballerina source code.
 *
 * @since 0.1.0
 */
public class WsdlToBallerina {

    private static final String TYPES_FILE_NAME = "types.bal";
    private static final String TARGET_NS = "targetNamespace";
    private Definition wsdlDefinition;
    private Port soapPort;
    private SoapVersion soapVersion;

    public WsdlToBallerinaResponse generateFromWSDL(String wsdlDefinitionText, List<String> filteredWSDLOperations) {
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        WsdlToBallerinaResponse response = new WsdlToBallerinaResponse();
        try {
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);
            InputStream wsdlStream = new ByteArrayInputStream(wsdlDefinitionText.getBytes(Charset.defaultCharset()));
            wsdlDefinition = reader.readWSDL(null, new InputSource(wsdlStream));

            WsdlService wsdlService = getSoapService(wsdlDefinition);
            if (wsdlService == null) {
                return response;
            }

            soapVersion = wsdlService.getSoapVersion();
            initializeSchemas(wsdlDefinition);

            List<WsdlOperation> wsdlOperations = getWSDLOperations();
            WsdlService wsdlServiceWithOperations = wsdlService.toBuilder()
                    .setWsdlOperations(wsdlOperations)
                    .build();

            WsdlTypeGenerator wsdlTypeGenerator = new WsdlTypeGenerator(wsdlServiceWithOperations, soapVersion);
            ModulePartNode typeModulePart = wsdlTypeGenerator.getWsdlTypeModulePart(filteredWSDLOperations);

            try {
                FormattingOptions formattingOptions = FormattingOptions.builder().build();
                GeneratedSource typesSrc = new GeneratedSource(TYPES_FILE_NAME,
                        Formatter.format(typeModulePart.syntaxTree(), formattingOptions).toSourceCode());
                response.setTypesSource(typesSrc);
            } catch (FormatterException e) {
                DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr101(null);
                diagnosticMessages.add(message);
            }
        } catch (WSDLException e) {
            DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr100(null);
            diagnosticMessages.add(message);
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    private void initializeSchemas(Definition wsdlDefinition) {
        Map<String, XmlSchema> targetNSToSchema = new HashMap<>();
        Types types = wsdlDefinition.getTypes();
        List<?> extensions = new ArrayList<>();
        if (types != null) {
            extensions = wsdlDefinition.getTypes().getExtensibilityElements();
        }
        for (Object extension : extensions) {
            if (extension instanceof Schema) {
                Element schElement = ((Schema) extension).getElement();
                String targetNamespace = schElement.getAttribute(TARGET_NS);
                if (!targetNamespace.isEmpty()) {
                    targetNSToSchema.put(targetNamespace, new XmlSchemaCollection().read(schElement));
                }
            }
        }
        SchemaHandler.getInstance().initializeSchemas(targetNSToSchema);
    }

    private WsdlService getSoapService(Definition wsdlDefinition) {
        @SuppressWarnings("unchecked")
        Collection<Service> services = wsdlDefinition.getAllServices().values();
        for (Service service : services) {
            @SuppressWarnings("unchecked")
            Collection<Port> ports = service.getPorts().values();
            for (Port port : ports) {
                List<?> extensions = port.getExtensibilityElements();
                for (Object extension : extensions) {
                    if (extension instanceof SOAPAddress || extension instanceof SOAP12Address) {
                        this.soapPort = port;
                        SoapVersion version = extension instanceof SOAPAddress ?
                                SoapVersion.SOAP11 : SoapVersion.SOAP12;
                        String serviceUrl = extension instanceof SOAPAddress ?
                                ((SOAPAddress) extension).getLocationURI() :
                                ((SOAP12Address) extension).getLocationURI();
                        return new WsdlService.Builder()
                                .setSoapVersion(version)
                                .setSoapServiceUrl(serviceUrl)
                                .build();
                    }
                }
            }
        }
        return null;
    }

    private List<WsdlOperation> getWSDLOperations() {
        List<WsdlOperation> wsdlOperations = new ArrayList<>();
        for (Object op : soapPort.getBinding().getBindingOperations()) {
            BindingOperation bindingOperation = (BindingOperation) op;
            WsdlOperation wsdlOperation = getWsdlOperation(bindingOperation);
            WsdlPayload inputPayload = getWSDLPayload(bindingOperation, bindingOperation.getOperation().getInput());
            WsdlPayload outputPayload = getWSDLPayload(bindingOperation, bindingOperation.getOperation().getOutput());

            wsdlOperation = wsdlOperation.toBuilder()
                    .setOperationInput(inputPayload)
                    .setOperationOutput(outputPayload)
                            .build();

            wsdlOperations.add(wsdlOperation);
        }
        return wsdlOperations;
    }

    private WsdlOperation getWsdlOperation(BindingOperation bindingOperation) {

        String operationName = bindingOperation.getName();
        String operationAction = null;
        for (Object element : bindingOperation.getExtensibilityElements()) {
            if (soapVersion == SoapVersion.SOAP11 && element instanceof SOAPOperation soapOperation) {
                operationAction = soapOperation.getSoapActionURI();
            } else if (soapVersion == SoapVersion.SOAP12 && element instanceof SOAP12Operation soapOperation) {
                operationAction = soapOperation.getSoapActionURI();
            }
        }

        return new WsdlOperation.Builder(operationName)
                .setOperationAction(operationAction)
                .build();
    }

    private WsdlPayload getWSDLPayload(BindingOperation bindingOperation, WSDLElement operationPayload) {
        WsdlPayload.Builder wsdlPayloadBuilder = new WsdlPayload.Builder();
        List<?> extensions = new ArrayList<>();
        Message message = null;
        if (operationPayload instanceof Input) {
            wsdlPayloadBuilder.setName(bindingOperation.getOperation().getInput().getName());
            extensions.addAll(bindingOperation.getBindingInput().getExtensibilityElements());
            message = bindingOperation.getOperation().getInput().getMessage();
        } else if (operationPayload instanceof Output) {
            wsdlPayloadBuilder.setName(bindingOperation.getOperation().getOutput().getName());
            extensions.addAll(bindingOperation.getBindingOutput().getExtensibilityElements());
            message = bindingOperation.getOperation().getOutput().getMessage();
        }
        for (Object element : extensions) {
            Message headerMessage = null;
            Part headerPart = null;
            if (soapVersion == SoapVersion.SOAP11 && element instanceof SOAPHeader soapHeader) {
                headerMessage = wsdlDefinition.getMessage(soapHeader.getMessage());
                if (headerMessage != null) {
                    headerPart = headerMessage.getPart(soapHeader.getPart());
                }
            } else if (soapVersion == SoapVersion.SOAP12 && element instanceof SOAP12Header soap12Header) {
                headerMessage = wsdlDefinition.getMessage(soap12Header.getMessage());
                if (headerMessage != null) {
                    headerPart = headerMessage.getPart(soap12Header.getPart());
                }
            }
            if (headerMessage != null && headerPart != null) {
                String localPart = headerPart.getElementName().getLocalPart();
                String namespaceURI = headerPart.getElementName().getNamespaceURI();
                WsdlPart wsdlPart = new WsdlPart.Builder(localPart, namespaceURI).build();

                WsdlHeader wsdlHeader = new WsdlHeader.Builder(wsdlPart).build();
                wsdlPayloadBuilder.addHeader(wsdlHeader);
            }
        }
        if (message != null) {
            // TODO: Handle when the messages doesn't have tns:Types and rather have parts with basic types.
            WsdlMessage.Builder wsdlMessageBuilder = new WsdlMessage.Builder(message.getQName().getLocalPart());
            for (Iterator it = message.getParts().values().iterator(); it.hasNext(); ) {
                Object partObject = it.next();
                if (partObject instanceof Part part) {
                    QName partElementName = part.getElementName();
                    WsdlPart wsdlPart =
                            new WsdlPart.Builder(partElementName.getLocalPart(), partElementName.getNamespaceURI())
                                    .build();
                    wsdlMessageBuilder.addPart(wsdlPart);

                }
            }
            wsdlPayloadBuilder.setMessage(wsdlMessageBuilder.build());
        }
        return wsdlPayloadBuilder.build();
    }
}
