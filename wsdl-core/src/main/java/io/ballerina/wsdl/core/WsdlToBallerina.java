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

import com.ibm.wsdl.MessageImpl;
import com.ibm.wsdl.PartImpl;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.wsdl.core.diagnostic.DiagnosticMessage;
import io.ballerina.wsdl.core.diagnostic.DiagnosticUtils;
import io.ballerina.wsdl.core.generator.GeneratedSource;
import io.ballerina.wsdl.core.handler.SchemaHandler;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.wsdl.core.handler.model.WsdlService;
import io.ballerina.xsd.core.visitor.XSDVisitorImpl;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.FormattingOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static io.ballerina.xsd.core.Utils.generateModulePartNode;
import static io.ballerina.xsd.core.XSDToRecord.INVALID_XSD_FORMAT_ERROR;
import static io.ballerina.xsd.core.XSDToRecord.SCHEMA;
import static io.ballerina.xsd.core.XSDToRecord.processNodeList;

/**
 * Provides functionality to convert WSDL specifications into Ballerina source code.
 *
 * @since 0.1.0
 */
public class WsdlToBallerina {

    public static final String OPEN_BRACES = "{";
    public static final String CLIENT_ENDPOINT_FIELD = "clientEp";
    private static final String TYPES_FILE_NAME = "types.bal";
    private static final String CLIENT_FILE_NAME = "client.bal";
    private static final String TARGET_NS = "targetNamespace";
    public static final String PUBLIC = "public";
    public static final String WHITESPACE = " ";
    public static final String ISOLATED = "isolated";
    public static final String CLIENT = "client";
    public static final String CLASS = "class";
    public static final String CLIENT_NAME = "Client";
    public static final String FINAL = "final";
    public static final String SOAP = "soap";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";
    public static final String FUNCTION = "function";
    public static final String INIT = "init";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String STRING = "string";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String COMMA = ",";
    public static final String TYPE_INCLUSION = "*";
    public static final String CLIENT_CONFIG = "ClientConfig";
    public static final String CONFIG = "config";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String RETURNS = "returns";
    public static final String ERROR_OR_NIL = "error?";
    public static final String EQUALS = "=";
    public static final String CHECK = "check";
    public static final String NEW = "new";
    public static final String SELF = "self";
    public static final String RETURN = "return";
    public static final String EOF_TOKEN = "";
    public static final String CLOSE_BRACES = "}";
    public static final String DOT = ".";
    public static final String REMOTE = "remote";
    public static final String SOAP_REQUEST = "SoapRequest";
    public static final String ENVELOPE = "envelope";
    public static final String SOAP_RESPONSE = "SoapResponse";
    public static final String VERTICAL_BAR = "|";
    public static final String ERROR = "error";
    public static final String XML = "xml";
    public static final String RESULT = "result";
    public static final String ARROW = "->";
    public static final String SEND_RECEIVE = "sendReceive";
    public static final String XMLDATA_TO_XML = "xmldata:toXml";
    public static final String QUOTATION_MARK = "\"";
    public static final String XMLDATA_PARSE_AS_TYPE = "xmldata:parseAsType";
    private Definition wsdlDefinition;
    private Port soapPort;
    private SoapVersion soapVersion;

    public WsdlToBallerinaResponse generateFromWSDL(String wsdlDefinitionText, String filteredWSDLOperations) {
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        WsdlToBallerinaResponse response = new WsdlToBallerinaResponse();
        try {
            wsdlDefinition = readWsdlContent(wsdlDefinitionText);
            WsdlService wsdlService = getSoapService(wsdlDefinition);
            if (wsdlService == null) {
                return response;
            }
            soapVersion = wsdlService.getSoapVersion();
            initializeSchemas(wsdlDefinition);
            Map<String, WsdlOperation> wsdlOperations = getWSDLOperations();

            Types types = wsdlDefinition.getTypes();
            if (types == null) {
                throw new Exception("Could not find <xs:schema> in the file");
            }
            WsdlOperation operation = wsdlOperations.get(filteredWSDLOperations);
            String operationName = operation.getOperationName();
            String operationAction = operation.getOperationAction();
            ModulePartNode modulePartNode = generateTypeNodes(operationName, types, operation.getOperationInput(),
                                                              operation.getOperationOutput());
            writeTypesToFile(modulePartNode);
            StringBuilder stringBuilder = Utils.generateClientSkeleton(soapVersion.toString());
            ModulePartNode clientModulePart = getClientModulePartNode(stringBuilder, operationName, operationAction);
            try {
                String content = Utils.formatModuleParts(clientModulePart);
                Files.writeString(Path.of(CLIENT_FILE_NAME), content);
                FormattingOptions formattingOptions = FormattingOptions.builder().build();
                GeneratedSource clientSrc = new GeneratedSource(CLIENT_FILE_NAME,
                        Formatter.format(clientModulePart.syntaxTree(), formattingOptions).toSourceCode());
                response.setClientSource(clientSrc);
            } catch (FormatterException e) {
                DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr101(null);
                diagnosticMessages.add(message);
            }
        } catch (WSDLException e) {
            DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr100(null);
            diagnosticMessages.add(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    private static ModulePartNode getClientModulePartNode(StringBuilder stringBuilder,
                                                          String operationName, String operationAction) {
        stringBuilder.append(REMOTE).append(WHITESPACE).append(ISOLATED).append(WHITESPACE).append(FUNCTION)
                .append(WHITESPACE).append(operationName).append(OPEN_PARENTHESIS).append(SOAP_REQUEST)
                .append(WHITESPACE).append(ENVELOPE).append(CLOSE_PARENTHESIS).append(WHITESPACE)
                .append(RETURNS).append(WHITESPACE).append(SOAP_RESPONSE).append(VERTICAL_BAR).append(ERROR)
                .append(WHITESPACE).append(OPEN_BRACES).append(XML).append(WHITESPACE).append(RESULT)
                .append(WHITESPACE).append(EQUALS).append(WHITESPACE).append(CHECK).append(WHITESPACE)
                .append(SELF).append(DOT).append(CLIENT_ENDPOINT_FIELD).append(ARROW).append(SEND_RECEIVE)
                .append(OPEN_PARENTHESIS).append(CHECK).append(WHITESPACE).append(XMLDATA_TO_XML)
                .append(OPEN_PARENTHESIS).append(ENVELOPE).append(CLOSE_PARENTHESIS).append(COMMA)
                .append(WHITESPACE).append(QUOTATION_MARK).append(operationAction).append(QUOTATION_MARK)
                .append(CLOSE_PARENTHESIS).append(SEMICOLON).append(RETURN).append(WHITESPACE)
                .append(XMLDATA_PARSE_AS_TYPE).append(OPEN_PARENTHESIS).append(RESULT).append(CLOSE_PARENTHESIS)
                .append(SEMICOLON).append(CLOSE_BRACES);
        stringBuilder.append(CLOSE_BRACES);
        ModuleMemberDeclarationNode moduleNode = NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
        String[] importStatements = {"import ballerina/data.xmldata;", "import ballerina/soap;",
                "import ballerina/soap.soap12;"};
        NodeList<ImportDeclarationNode> imports = Utils.createImportNodes(importStatements);

        List<ModuleMemberDeclarationNode> moduleMemberDeclarations = new ArrayList<>();
        moduleMemberDeclarations.add(moduleNode);
        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(moduleMemberDeclarations);
        Token eofToken = AbstractNodeFactory.createIdentifierToken(EOF_TOKEN);
        return NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);
    }

    private static void writeTypesToFile(ModulePartNode modulePartNode) throws FormatterException, IOException {
        String records = Utils.formatModuleParts(modulePartNode);
        Path path = Paths.get(TYPES_FILE_NAME);
        Path destinationFile = Files.exists(path) ? Utils.handleFileOverwrite(path) : path;
        Files.writeString(destinationFile, records);
    }

    private ModulePartNode generateTypeNodes(String wsdlAction, Types types,
                                             String operationInput, String operationOutput) throws Exception {
        List<?> extElements = types.getExtensibilityElements();
        HashMap<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
        XSDVisitorImpl xsdVisitor = new XSDVisitorImpl();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document unifiedDocument = builder.newDocument();
        Element unifiedRoot = unifiedDocument.createElementNS("http://www.w3.org/2001/XMLSchema", SCHEMA);
        unifiedDocument.appendChild(unifiedRoot);
        for (Object extElement : extElements) {
            if (extElement instanceof Schema schema) {
                Element schemaElement = schema.getElement();
                org.w3c.dom.NodeList childNodes = schemaElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node importedNode = unifiedDocument.importNode(childNodes.item(i), true);
                    unifiedRoot.appendChild(importedNode);
                }
            }
        }
        Element rootElement = unifiedDocument.getDocumentElement();
        if (!Objects.equals(rootElement.getLocalName(), SCHEMA)) {
            throw new Exception(INVALID_XSD_FORMAT_ERROR);
        }
        processNodeList(rootElement, nodes, xsdVisitor);
        String requestType = getElementLocalPart(operationInput, wsdlDefinition);
        String responseType = getElementLocalPart(operationOutput, wsdlDefinition);
        Utils.generateTypeDefinitions(wsdlAction, nodes, requestType, responseType);
        return generateModulePartNode(nodes, xsdVisitor);
    }

    private String getElementLocalPart(String messageName, Definition wsdlDefinition) {
        QName qName = new QName(wsdlDefinition.getTargetNamespace(), messageName);
        MessageImpl message = (MessageImpl) wsdlDefinition.getMessages().get(qName);
        if (message == null) {
            throw new IllegalArgumentException("Message not found: " + messageName);
        }
        Map<String, PartImpl> parts = message.getParts();
        if (parts.isEmpty()) {
            throw new IllegalStateException("No parts found for message: " + messageName);
        }
        String firstPartKey = parts.keySet().iterator().next();
        return parts.get(firstPartKey).getElementName().getLocalPart();
    }

    private Definition readWsdlContent(String wsdlDefinitionText) throws WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setFeature("javax.wsdl.importDocuments", true);
        InputStream wsdlStream = new ByteArrayInputStream(wsdlDefinitionText.getBytes(Charset.defaultCharset()));
        return reader.readWSDL(null, new InputSource(wsdlStream));
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

    private Map<String, WsdlOperation> getWSDLOperations() {
        Map<String, WsdlOperation> wsdlOperations = new HashMap<>();
        for (Object op : soapPort.getBinding().getBindingOperations()) {
            BindingOperation bindingOperation = (BindingOperation) op;
            WsdlOperation wsdlOperation = getWsdlOperation(bindingOperation);
            String inputPayload = bindingOperation.getBindingInput().getName();
            String outputPayload = bindingOperation.getBindingOutput().getName();

            inputPayload = (inputPayload == null)
                    ? bindingOperation.getOperation().getInput().getMessage().getQName().getLocalPart()
                    : inputPayload;

            outputPayload = (outputPayload == null)
                    ? bindingOperation.getOperation().getOutput().getMessage().getQName().getLocalPart()
                    : outputPayload;

            wsdlOperation = wsdlOperation.toBuilder()
                    .setOperationName(bindingOperation.getOperation().getName())
                    .setOperationInput(inputPayload)
                    .setOperationOutput(outputPayload)
                    .build();

            wsdlOperations.put(wsdlOperation.getOperationAction(), wsdlOperation);
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
}
