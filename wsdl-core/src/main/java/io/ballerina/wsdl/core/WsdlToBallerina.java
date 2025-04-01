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
import com.ibm.wsdl.extensions.soap.SOAPHeaderImpl;
import com.ibm.wsdl.extensions.soap12.SOAP12HeaderImpl;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.wsdl.core.diagnostic.DiagnosticMessage;
import io.ballerina.wsdl.core.diagnostic.DiagnosticUtils;
import io.ballerina.wsdl.core.generator.GeneratedSource;
import io.ballerina.wsdl.core.handler.SchemaHandler;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.xsd.core.visitor.XSDVisitor;
import io.ballerina.xsd.core.visitor.XSDVisitorImpl;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.ballerinalang.formatter.core.FormatterException;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.xml.namespace.QName;

import static io.ballerina.wsdl.core.Utils.LINE_BREAK;
import static io.ballerina.xsd.core.Utils.generateModulePartNode;
import static io.ballerina.xsd.core.XSDToRecord.TARGET_NAMESPACE;
import static io.ballerina.xsd.core.XSDToRecord.XMLDATA_NAME_ANNOTATION;
import static io.ballerina.xsd.core.XSDToRecord.generateNodes;
import static io.ballerina.xsd.core.XSDToRecord.generateResidualNodes;
import static io.ballerina.xsd.core.visitor.VisitorUtils.UNDERSCORE;
import static io.ballerina.xsd.core.visitor.VisitorUtils.URI;
import static io.ballerina.xsd.core.visitor.VisitorUtils.XMLDATA_NAMESPACE;
import static io.ballerina.xsd.core.visitor.VisitorUtils.convertToCamelCase;
import static io.ballerina.xsd.core.visitor.VisitorUtils.isSimpleType;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.QUESTION_MARK;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.RECORD;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.TYPE;

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
    public static final String PREFIX = "prefix";
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
    public static final String SOAP11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
    public static final String HEADER = "Header";
    public static final String SLASH = "/";
    public static final String MODULES = "modules";
    public static final String XMLDATA_NAMESPACE_URI = "@xmldata:Namespace {uri: \"%s\"}";
    public static final String MISSING_HEADER_ELEMENT_ERROR = "Header element name cannot be extracted.";
    public static final String MISSING_DATA_IN_HEADER_ERROR = "Header element is not found in the WSDL Definition: ";
    public static final String OPERATION_NOT_FOUND_ERROR = "WSDL operation is not found: ";
    private Definition wsdlDefinition;
    private ArrayList<SoapPort> soapPorts = new ArrayList<>();
    private SoapVersion soapVersion;
    private String soapNamespace;
    private String serviceUrl;

    public Definition getWsdlDefinition() {
        return wsdlDefinition;
    }

    public void setWsdlDefinition(Definition wsdlDefinition) {
        this.wsdlDefinition = wsdlDefinition;
    }

    public SoapVersion getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(SoapVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    public String getSoapNamespace() {
        return soapNamespace;
    }

    public void setSoapNamespace(String soapNamespace) {
        this.soapNamespace = soapNamespace;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void generateFromWSDL(WsdlToBallerinaResponse response, Definition wsdlDefinition,
                                 String outputDirectory, List<DiagnosticMessage> diagnosticMessages,
                                 String[] filteredWSDLOperations, String portName) {
        try {
            setWsdlDefinition(wsdlDefinition);
            boolean hasPortName = generateSoapPorts(wsdlDefinition, portName);
            if (!hasPortName) {
                DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaInputError(null);
                diagnosticMessages.add(message);
                DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
                return;
            }
            for (SoapPort port: soapPorts) {
                setSoapVersion(port.soapVersion());
                setServiceUrl(port.serviceUrl());
                setSoapNamespace(getSoapVersion().equals(SoapVersion.SOAP12) ? SOAP12_NAMESPACE : SOAP11_NAMESPACE);
                initializeSchemas(wsdlDefinition);
                Map<String, WsdlOperation> wsdlOperations = getWSDLOperations(port.soapPort());
                Types types = wsdlDefinition.getTypes();
                Objects.requireNonNull(types, "Could not find <wsdl:types> in the file");
                ArrayList<WsdlOperation> operations = new ArrayList<>();
                if (filteredWSDLOperations.length == 0) {
                    for (Map.Entry<String, WsdlOperation> entry : wsdlOperations.entrySet()) {
                        operations.add(entry.getValue());
                    }
                } else {
                    for (String operationName : filteredWSDLOperations) {
                        WsdlOperation operation = validateAndRetrieveOperation(operationName.strip(), wsdlOperations);
                        operations.add(operation);
                    }
                }
                generateClient(response, outputDirectory, port, operations,
                               soapPorts.size() > 1 ? port.soapPort().getName() + UNDERSCORE : EMPTY_STRING);
            }
            Types types = wsdlDefinition.getTypes();
            generateTypes(response, types, outputDirectory);
        } catch (WSDLException e) {
            DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaError(null);
            diagnosticMessages.add(message);
        } catch (Exception e) {
            DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaGeneralError(e, null);
            diagnosticMessages.add(message);
        }
        DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    public static Header extractHeader(Definition wsdlDefinition, QName headerName, String elementName) {
        Objects.requireNonNull(headerName, MISSING_HEADER_ELEMENT_ERROR);
        Message message = (Message) wsdlDefinition.getMessages().get(headerName);
        Objects.requireNonNull(message, MISSING_DATA_IN_HEADER_ERROR + headerName);
        Part partObj = (Part) message.getParts().get(elementName);
        QName element = partObj.getElementName();
        return new Header(element.getLocalPart(), element.getNamespaceURI());
    }

    private void generateClient(WsdlToBallerinaResponse response, String outputDirectory, SoapPort port,
                                ArrayList<WsdlOperation> operation, String filePrefix) throws FormatterException {
        ModulePartNode clientModule = generateClientModule(operation, port.soapPort());
        String prefix = filePrefix.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        String clientFileName = outputDirectory.equals(EMPTY_STRING)
                ? prefix + CLIENT_FILE_NAME : outputDirectory + SLASH + prefix + CLIENT_FILE_NAME;
        response.addClientSource(new GeneratedSource(clientFileName, Utils.formatModuleParts(clientModule)));
    }

    private void generateTypes(WsdlToBallerinaResponse response, Types types,
                               String outputDirectory) throws Exception {
        XSDVisitor xsdVisitor = new XSDVisitorImpl();
        Map<String, ModuleMemberDeclarationNode> nodes = generateTypeNodes(types, xsdVisitor);
        ModulePartNode typeNodes = generateModulePartNode(nodes, xsdVisitor);
        String typesFileName = outputDirectory.equals(EMPTY_STRING)
                ? TYPES_FILE_NAME : outputDirectory + SLASH + TYPES_FILE_NAME;
        response.setTypesSource(new GeneratedSource(typesFileName, Utils.formatModuleParts(typeNodes)));
    }

    private OperationContext generateEnvelopeTypes(WsdlOperation operation,
                                                   Map<String, ModuleMemberDeclarationNode> nodes, Port port) {
        String requestType = getElementType(operation.getOperationInput(), getWsdlDefinition(), nodes);
        String requestFieldName = isSimpleType(requestType)
                ? getElementName(operation.getOperationInput(), getWsdlDefinition()) : requestType;
        String responseType = getElementType(operation.getOperationOutput(), getWsdlDefinition(), nodes);
        String responseFieldName = isSimpleType(responseType)
                ? getElementName(operation.getOperationOutput(), getWsdlDefinition()) : responseType;
        String suffix = soapPorts.size() > 1 ? convertToPascalCase(port.getName()) : EMPTY_STRING;
        OperationContext operationContext = new OperationContext(operation.getOperationName(), suffix);
        Utils.generateTypeDefinitions(getSoapNamespace(), nodes, requestType, requestFieldName, responseType,
                                      responseFieldName, operationContext);
        ModuleMemberDeclarationNode headerNode = generateHeaderNode(operation, operationContext);
        nodes.put(operation.getOperationName() + HEADER, headerNode);
        return operationContext;
    }

    private ModuleMemberDeclarationNode generateHeaderNode(WsdlOperation operation,
                                                           OperationContext operationContext) {
        Map<String, Header> headers = new HashMap<>();
        Map<String, HeaderPart> elementNames = operation.getHeaderElements();
        String localPart = operation.getInputHeaderName();
        QName headerName = new QName(getWsdlDefinition().getTargetNamespace(), localPart);
        for (String elementName : elementNames.keySet()) {
            Header header = extractHeader(getWsdlDefinition(), headerName, elementName);
            headers.put(elementName, header);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Utils.XMLDATA_NAMESPACE, SOAP, getSoapNamespace())).append(LINE_BREAK);
        stringBuilder.append(PUBLIC).append(WHITESPACE).append(TYPE).append(WHITESPACE)
                .append(operationContext.requestHeaderName()).append(WHITESPACE)
                .append(RECORD).append(OPEN_BRACES);
        for (Map.Entry<String, Header> entry : headers.entrySet()) {
            String key = entry.getKey();
            Header header = entry.getValue();
            String elementName = header.getElementName();
            String namespace = header.getElementNamespace();
            if (!elementName.equals(key)) {
                stringBuilder.append(String.format(XMLDATA_NAME_ANNOTATION, elementName))
                        .append(XMLDATA_NAMESPACE + WHITESPACE + OPEN_BRACES + PREFIX + COLON + QUOTATION_MARK)
                        .append(key).append(QUOTATION_MARK).append(COMMA).append(URI).append(COLON)
                        .append(QUOTATION_MARK).append(namespace).append(QUOTATION_MARK).append(CLOSE_BRACES)
                        .append(key).append(WHITESPACE).append(key).append(QUESTION_MARK).append(SEMICOLON);
            } else {
                String field = String.format(XMLDATA_NAMESPACE_URI, namespace);
                stringBuilder.append(field).append(key).append(WHITESPACE)
                        .append(elementName).append(QUESTION_MARK).append(SEMICOLON);
            }
        }
        stringBuilder.append(CLOSE_BRACES).append(SEMICOLON);
        return NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
    }

    private WsdlOperation validateAndRetrieveOperation(String operationName,
                                                       Map<String, WsdlOperation> wsdlOperations) {
        WsdlOperation operation = wsdlOperations.get(operationName);
        Objects.requireNonNull(operation, OPERATION_NOT_FOUND_ERROR + operationName);
        return operation;
    }

    private ModulePartNode generateClientModule(ArrayList<WsdlOperation> operations, Port port) {
        StringBuilder clientContext = Utils.generateClientContext(getSoapVersion().toString(), getServiceUrl(),
                                                                  port, soapPorts.size() > 1);
        return getClientModulePartNode(clientContext, operations, getSoapVersion().toString(), port);
    }

    private ModulePartNode getClientModulePartNode(StringBuilder stringBuilder, ArrayList<WsdlOperation> operations,
                                                   String soapVersion, Port port) {
        Map<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
        for (WsdlOperation operation: operations) {
            OperationContext operationContext = generateEnvelopeTypes(operation, nodes, port);
            String functionCode = buildRemoteFunctionCode(operationContext, operation.getOperationName(),
                    operation.getOperationAction());
            stringBuilder.append(functionCode);
        }
        stringBuilder.append(CLOSE_BRACES);
        ModuleMemberDeclarationNode functionNode = NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
        nodes.put("generatedFunctionNode", functionNode);
        NodeList<ImportDeclarationNode> imports = generateImportDeclarationNodes(soapVersion);
        List<ModuleMemberDeclarationNode> nodeList = new ArrayList<>(nodes.values());
        Collections.reverse(nodeList);
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(nodeList);
        return NodeFactory.createModulePartNode(imports, moduleMembers,
                                                AbstractNodeFactory.createIdentifierToken(EOF_TOKEN));
    }

    private static NodeList<ImportDeclarationNode> generateImportDeclarationNodes(String soapVersion) {
        String[] imports = new String[]{
                "import ballerina/data.xmldata;",
                "import ballerina/soap;",
                String.format("import ballerina/soap.%s;", soapVersion.toLowerCase(Locale.ROOT))
        };
        return createImportNodes(imports);
    }

    private static String buildRemoteFunctionCode(OperationContext operationContext, String operationName,
                                                  String operationAction) {
        return new StringBuilder()
            .append(REMOTE).append(WHITESPACE).append(ISOLATED).append(WHITESPACE).append(FUNCTION)
            .append(WHITESPACE).append(convertToCamelCase(operationName))
            .append(OPEN_PARENTHESIS).append(operationContext.requestName()).append(WHITESPACE).append(ENVELOPE)
            .append(CLOSE_PARENTHESIS).append(WHITESPACE).append(RETURNS).append(WHITESPACE)
            .append(operationContext.responseName()).append(VERTICAL_BAR).append(ERROR).append(WHITESPACE)
            .append(OPEN_BRACES).append(XML).append(WHITESPACE).append(RESULT).append(WHITESPACE).append(EQUALS)
            .append(WHITESPACE).append(CHECK).append(WHITESPACE).append(SELF).append(DOT).append(CLIENT_ENDPOINT_FIELD)
            .append(ARROW).append(SEND_RECEIVE).append(OPEN_PARENTHESIS).append(CHECK).append(WHITESPACE)
            .append(XMLDATA_TO_XML).append(OPEN_PARENTHESIS).append(ENVELOPE).append(CLOSE_PARENTHESIS)
            .append(COMMA).append(WHITESPACE).append(QUOTATION_MARK).append(operationAction)
            .append(QUOTATION_MARK).append(CLOSE_PARENTHESIS).append(SEMICOLON)
            .append(RETURN).append(WHITESPACE).append(XMLDATA_PARSE_AS_TYPE)
            .append(OPEN_PARENTHESIS).append(RESULT).append(CLOSE_PARENTHESIS).append(SEMICOLON)
            .append(CLOSE_BRACES)
            .toString();
    }

    private static NodeList<ImportDeclarationNode> createImportNodes(String... importStatements) {
        List<ImportDeclarationNode> importNodes = Arrays.stream(importStatements)
                .map(NodeParser::parseImportDeclaration)
                .toList();
        return AbstractNodeFactory.createNodeList(importNodes);
    }

    private Map<String, ModuleMemberDeclarationNode> generateTypeNodes(Types types,
                                                                       XSDVisitor xsdVisitor) throws Exception {
        List<?> extElements = types.getExtensibilityElements();
        Map<String, ModuleMemberDeclarationNode> nodes = new LinkedHashMap<>();
        for (Object extElement : extElements) {
            if (!(extElement instanceof Schema)) {
                continue;
            }
            xsdVisitor.setTargetNamespace(((Schema) extElement).getElement().getAttribute(TARGET_NS));
            generateTypeNode(xsdVisitor, (Schema) extElement, nodes);
        }
        generateResidualNodes(nodes, xsdVisitor);
        return nodes;
    }

    private static void generateTypeNode(XSDVisitor xsdVisitor, Schema schema,
                                         Map<String, ModuleMemberDeclarationNode> nodes) throws Exception {
        Element schemaElement = schema.getElement();
        xsdVisitor.setTargetNamespace(schemaElement.getAttribute(TARGET_NAMESPACE));
        generateNodes(schemaElement, nodes, xsdVisitor);
    }

    private static String getElementType(String messageName, Definition wsdlDefinition,
                                         Map<String, ModuleMemberDeclarationNode> nodes) {
        QName qName = new QName(wsdlDefinition.getTargetNamespace(), messageName);
        MessageImpl message = (MessageImpl) wsdlDefinition.getMessages().get(qName);
        Objects.requireNonNull(message, "Message not found: " + messageName);
        Map<String, PartImpl> parts = message.getParts();
        if (parts.isEmpty()) {
            throw new IllegalStateException("No parts found for message: " + messageName);
        }
        for (Map.Entry<String, PartImpl> entry : parts.entrySet()) {
            PartImpl part = entry.getValue();
            if (part.getTypeName() != null) {
                String requestType = part.getTypeName().getLocalPart();
                if (nodes.containsKey(requestType) || isSimpleType(requestType)) {
                    return requestType;
                }
            }
            return part.getElementName().getLocalPart();
        }
        throw new IllegalStateException("Unexpected state: Unable to determine element " +
                "type for message: " + messageName);
    }

    private static String getElementName(String messageName, Definition wsdlDefinition) {
        QName qName = new QName(wsdlDefinition.getTargetNamespace(), messageName);
        MessageImpl message = (MessageImpl) wsdlDefinition.getMessages().get(qName);
        Objects.requireNonNull(message, "Message not found: " + messageName);
        Map<String, PartImpl> parts = message.getParts();
        if (parts.isEmpty()) {
            throw new IllegalStateException("No parts found for message: " + messageName);
        }
        return parts.keySet().iterator().next();
    }

    private boolean generateSoapPorts(Definition wsdlDefinition, String portName) {
        boolean hasPortName = false;
        Collection<Service> services = wsdlDefinition.getAllServices().values();
        for (Service service : services) {
            for (Port port : (Collection<Port>) service.getPorts().values()) {
                hasPortName = port.getName().equals(portName);
                SoapPort soapPort = extractSoapPort(port);
                if (soapPort != null) {
                    soapPorts.add(soapPort);
                    if (portName.equals(port.getName())) {
                        soapPorts.clear();
                        soapPorts.add(soapPort);
                        return true;
                    }
                }
            }
        }
        return hasPortName || portName.isEmpty();
    }

    private SoapPort extractSoapPort(Port port) {
        for (Object extension : port.getExtensibilityElements()) {
            if (extension instanceof SOAPAddress) {
                return new SoapPort(SoapVersion.SOAP11, port, ((SOAPAddress) extension).getLocationURI());
            } else if (extension instanceof SOAP12Address) {
                return new SoapPort(SoapVersion.SOAP12, port, ((SOAP12Address) extension).getLocationURI());
            }
        }
        return null;
    }

    public static String convertToPascalCase(String pascalCase) {
        if (pascalCase == null || pascalCase.isEmpty()) {
            return pascalCase;
        }
        pascalCase = pascalCase.replaceAll("[._]+", EMPTY_STRING);
        return Character.toUpperCase(pascalCase.charAt(0)) + pascalCase.substring(1);
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

    private Map<String, WsdlOperation> getWSDLOperations(Port soapPort) throws Exception {
        Map<String, WsdlOperation> wsdlOperations = new HashMap<>();
        for (Object op : soapPort.getBinding().getBindingOperations()) {
            BindingOperation bindingOperation = (BindingOperation) op;
            WsdlOperation wsdlOperation = getWsdlOperation(bindingOperation);
            Objects.requireNonNull(bindingOperation.getBindingInput(),
                    "Invalid binding operation: Binding input is null.");
            Objects.requireNonNull(bindingOperation.getBindingOutput(),
                    "Invalid binding operation: Binding output is null.");
            String inputPayload = bindingOperation.getBindingInput().getName();
            String outputPayload = bindingOperation.getBindingOutput().getName();
            Map<String, HeaderPart> headerParts = new HashMap<>();
            String inputHeaderName = generateSOAPInputHeaderParts(bindingOperation, headerParts, getSoapVersion());
            Objects.requireNonNull(bindingOperation.getOperation().getInput().getMessage(),
                    "Message element is missing in the input of the operation: " +
                    bindingOperation.getOperation().getName());
            Objects.requireNonNull(bindingOperation.getOperation().getOutput().getMessage(),
                    "Message element is missing in the output of the operation: " +
                    bindingOperation.getOperation().getName());
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
                    .setInputHeaderName(inputHeaderName)
                    .setHeaderElements(headerParts)
                    .build();
            wsdlOperations.put(wsdlOperation.getOperationAction(), wsdlOperation);
        }
        return wsdlOperations;
    }

    public static String generateSOAPInputHeaderParts(BindingOperation bindingOperation,
                                                      Map<String, HeaderPart> partElements,
                                                      SoapVersion soapVersion) throws Exception {
        if (bindingOperation == null || bindingOperation.getBindingInput() == null) {
            throw new Exception("Invalid binding operation: Binding input is null.");
        }
        List<?> extensibilityElements = bindingOperation.getBindingInput().getExtensibilityElements();
        String headerMessageName = "";
        for (Object element : extensibilityElements) {
            if (element == null) {
                continue;
            }
            if (soapVersion.equals(SoapVersion.SOAP12) && element instanceof SOAP12HeaderImpl soap12Header) {
                String partName = soap12Header.getPart();
                if (partName == null) {
                    continue;
                }
                partElements.put(partName, new HeaderPart(partName, soap12Header.getNamespaceURI()));
                Objects.requireNonNull(soap12Header.getMessage(),
                        "Message element is missing in the <soap:header> for \"" + partName + "\" element");
                headerMessageName = soap12Header.getMessage().getLocalPart();
            } else if (soapVersion.equals(SoapVersion.SOAP11) && element instanceof SOAPHeaderImpl soap11Header) {
                String partName = soap11Header.getPart();
                if (partName == null) {
                    continue;
                }
                partElements.put(partName, new HeaderPart(partName, soap11Header.getNamespaceURI()));
                Objects.requireNonNull(soap11Header.getMessage(),
                        "Message element is missing in the <soap:header> for \"" + partName + "\" element");
                headerMessageName = soap11Header.getMessage().getLocalPart();
            }
        }
        return headerMessageName;
    }

    private WsdlOperation getWsdlOperation(BindingOperation bindingOperation) {
        String operationName = bindingOperation.getName();
        String operationAction = null;
        for (Object element : bindingOperation.getExtensibilityElements()) {
            if (getSoapVersion() == SoapVersion.SOAP11 && element instanceof SOAPOperation soapOperation) {
                operationAction = soapOperation.getSoapActionURI();
            } else if (getSoapVersion() == SoapVersion.SOAP12 && element instanceof SOAP12Operation soapOperation) {
                operationAction = soapOperation.getSoapActionURI();
            }
        }
        return new WsdlOperation.Builder(operationName)
                .setOperationAction(operationAction)
                .build();
    }
}
