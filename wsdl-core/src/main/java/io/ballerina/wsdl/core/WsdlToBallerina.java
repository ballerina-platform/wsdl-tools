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
import io.ballerina.wsdl.core.handler.model.WsdlService;
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

import static io.ballerina.xsd.core.Utils.generateModulePartNode;
import static io.ballerina.xsd.core.XSDToRecord.TARGET_NAMESPACE;
import static io.ballerina.xsd.core.XSDToRecord.generateNodes;
import static io.ballerina.xsd.core.XSDToRecord.processEnumerations;
import static io.ballerina.xsd.core.XSDToRecord.processExtensions;
import static io.ballerina.xsd.core.XSDToRecord.processNameResolvers;
import static io.ballerina.xsd.core.XSDToRecord.processNestedElements;
import static io.ballerina.xsd.core.XSDToRecord.processRootElements;
import static io.ballerina.xsd.core.visitor.VisitorUtils.convertToCamelCase;
import static io.ballerina.xsd.core.visitor.VisitorUtils.isSimpleType;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;
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
    public static final String XMLDATA_NAMESPACE_URI = "@xmldata:Namespace {uri: \"%s\"}\n%s %s?;";
    public static final String MISSING_HEADER_ELEMENT_ERROR = "Header element name cannot be extracted.";
    public static final String MISSING_DATA_IN_HEADER_ELEMENT_ERROR = "Header element is not found in the WSDL Definition: ";
    public static final String OPERATION_NOT_FOUND_ERROR = "WSDL operation is not found: ";
    private Definition wsdlDefinition;
    private Port soapPort;
    private SoapVersion soapVersion;
    private String soapNamespace;
    private String serviceUrl;

    public void generateFromWSDL(WsdlToBallerinaResponse response, Definition wsdlDefinition,
                                 String outputDirectory, List<DiagnosticMessage> diagnosticMessages,
                                 String[] filteredWSDLOperations) {
        try {
            this.wsdlDefinition = wsdlDefinition;
            WsdlService wsdlService = getSoapService(wsdlDefinition);
            if (wsdlService == null) {
                DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaError(null);
                diagnosticMessages.add(message);
                DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
                return;
            }
            soapVersion = wsdlService.getSoapVersion();
            serviceUrl = wsdlService.getSoapServiceUrl();
            soapNamespace = soapVersion.equals(SoapVersion.SOAP12) ? SOAP12_NAMESPACE : SOAP11_NAMESPACE;
            initializeSchemas(wsdlDefinition);
            Map<String, WsdlOperation> wsdlOperations = getWSDLOperations();
            Types types = wsdlDefinition.getTypes();
            if (types == null) {
                throw new Exception("Could not find <wsdl:types> in the file");
            }
            generateFiles(filteredWSDLOperations, response, wsdlOperations, types, outputDirectory);
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
        if (headerName == null) {
            throw new IllegalArgumentException(MISSING_HEADER_ELEMENT_ERROR);
        }
        Message message = (Message) wsdlDefinition.getMessages().get(headerName);
        if (message == null) {
            throw new IllegalArgumentException(MISSING_DATA_IN_HEADER_ELEMENT_ERROR + headerName);
        }
        Part partObj = (Part) message.getParts().get(elementName);
        QName element = partObj.getElementName();
        return new Header(element.getLocalPart(), element.getNamespaceURI());
    }

    private void generateFiles(String[] filteredWSDLOperations, WsdlToBallerinaResponse response,
                               Map<String, WsdlOperation> wsdlOperations, Types types,
                               String outputDirectory) throws Exception {
        ArrayList<WsdlOperation> operations = new ArrayList<>();
        if (filteredWSDLOperations.length == 0) {
            for (String wsdlOperation: wsdlOperations.keySet()) {
                operations.add(wsdlOperations.get(wsdlOperation));
            }
        } else {
            for (String operationName : filteredWSDLOperations) {
                WsdlOperation operation = validateAndRetrieveOperation(operationName.strip(), wsdlOperations);
                operations.add(operation);
            }
        }
        generateTypes(response, types, outputDirectory, operations);
        generateClient(response, outputDirectory, operations);
    }

    private void generateClient(WsdlToBallerinaResponse response, String outputDirectory,
                                ArrayList<WsdlOperation> operation) throws FormatterException {
        ModulePartNode clientModule = generateClientModule(operation);
        String clientFileName = outputDirectory.equals(EMPTY_STRING)
                ? CLIENT_FILE_NAME : outputDirectory + SLASH + CLIENT_FILE_NAME;
        response.setClientSource(new GeneratedSource(clientFileName, Utils.formatModuleParts(clientModule)));
    }

    private void generateTypes(WsdlToBallerinaResponse response, Types types,
                               String outputDirectory, ArrayList<WsdlOperation> operations) throws Exception {
        XSDVisitor xsdVisitor = new XSDVisitorImpl();
        Map<String, ModuleMemberDeclarationNode> nodes = generateTypeNodes(types, xsdVisitor);
        for (WsdlOperation wsdlOperation : operations) {
            generateEnvelopeTypes(wsdlOperation, nodes);
        }
        ModulePartNode typeNodes = generateModulePartNode(nodes, xsdVisitor);
        String typesFileName = outputDirectory.equals(EMPTY_STRING)
                ? TYPES_FILE_NAME : outputDirectory + SLASH + TYPES_FILE_NAME;
        response.setTypesSource(new GeneratedSource(typesFileName, Utils.formatModuleParts(typeNodes)));
    }

    private void generateEnvelopeTypes(WsdlOperation operation, Map<String, ModuleMemberDeclarationNode> nodes) {
        String requestType = getElementType(operation.getOperationInput(), wsdlDefinition, nodes);
        String requestFieldName = isSimpleType(requestType)
                ? getElementName(operation.getOperationInput(), wsdlDefinition, nodes)
                : requestType;
        String responseType = getElementType(operation.getOperationOutput(), wsdlDefinition, nodes);
        String responseFieldName = isSimpleType(responseType)
                ? getElementName(operation.getOperationOutput(), wsdlDefinition, nodes)
                : responseType;
        OperationContext operationContext = new OperationContext(operation.getOperationName());
        Utils.generateTypeDefinitions(soapNamespace, nodes, requestType, requestFieldName, responseType,
                responseFieldName, operationContext);
        ModuleMemberDeclarationNode headerNode = generateHeaderNode(operation);
        nodes.put(operation.getOperationName() + HEADER, headerNode);
    }

    private ModuleMemberDeclarationNode generateHeaderNode(WsdlOperation operation) {
        Map<String, Header> headers = new HashMap<>();
        List<String> elementNames = operation.getHeaderElements();
        String localPart = operation.getInputHeaderName();
        QName headerName = new QName(wsdlDefinition.getTargetNamespace(), localPart);
        for (String elementName : elementNames) {
            Header header = extractHeader(wsdlDefinition, headerName, elementName);
            headers.put(elementName, header);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(Utils.XMLDATA_NAMESPACE, soapNamespace));
        stringBuilder.append(PUBLIC).append(WHITESPACE).append(TYPE).append(WHITESPACE)
                .append(operation.getOperationName()).append(HEADER).append(WHITESPACE)
                .append(RECORD).append(OPEN_BRACES);
        for (String key: headers.keySet()) {
            String elementName = headers.get(key).getElementName();
            String namespace = headers.get(key).getElementNamespace();
            String field = String.format(XMLDATA_NAMESPACE_URI, namespace, key, elementName);
            stringBuilder.append(field);
        }
        stringBuilder.append(CLOSE_BRACES).append(SEMICOLON);
        return NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
    }

    private WsdlOperation validateAndRetrieveOperation(String operationName,
                                                       Map<String, WsdlOperation> wsdlOperations) throws Exception {
        WsdlOperation operation = wsdlOperations.get(operationName);
        if (operation == null) {
            throw new Exception(OPERATION_NOT_FOUND_ERROR + operationName);
        }
        return operation;
    }

    private ModulePartNode generateClientModule(ArrayList<WsdlOperation> operations) {
        StringBuilder clientContext = Utils.generateClientContext(soapVersion.toString(), serviceUrl);
        return getClientModulePartNode(clientContext, operations, soapVersion.toString());
    }

    private static ModulePartNode getClientModulePartNode(StringBuilder stringBuilder,
                                                          ArrayList<WsdlOperation> operations, String soapVersion) {
        for (WsdlOperation operation: operations) {
            String functionCode = buildRemoteFunctionCode(operation.getOperationName(), operation.getOperationAction());
            stringBuilder.append(functionCode);
        }
        stringBuilder.append(CLOSE_BRACES);
        ModuleMemberDeclarationNode functionNode = NodeParser.parseModuleMemberDeclaration(stringBuilder.toString());
        NodeList<ImportDeclarationNode> imports = generateImportDeclarationNodes(soapVersion);
        NodeList<ModuleMemberDeclarationNode> moduleMembers =
                AbstractNodeFactory.createNodeList(Collections.singletonList(functionNode));
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

    private static String buildRemoteFunctionCode(String operationName, String operationAction) {
        return new StringBuilder()
            .append(REMOTE).append(WHITESPACE).append(ISOLATED).append(WHITESPACE).append(FUNCTION)
            .append(WHITESPACE).append(convertToCamelCase(operationName)).append(OPEN_PARENTHESIS).append(operationName)
            .append(SOAP_REQUEST).append(WHITESPACE).append(ENVELOPE).append(CLOSE_PARENTHESIS).append(WHITESPACE)
            .append(RETURNS).append(WHITESPACE).append(operationName).append(SOAP_RESPONSE).append(VERTICAL_BAR)
            .append(ERROR).append(WHITESPACE).append(OPEN_BRACES)
            .append(XML).append(WHITESPACE).append(RESULT).append(WHITESPACE).append(EQUALS).append(WHITESPACE)
            .append(CHECK).append(WHITESPACE).append(SELF).append(DOT).append(CLIENT_ENDPOINT_FIELD)
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
        processRootElements(nodes, xsdVisitor.getRootElements());
        processNestedElements(nodes, xsdVisitor.getNestedElements());
        processNameResolvers(nodes, xsdVisitor.getNameResolvers());
        processExtensions(nodes, xsdVisitor);
        processEnumerations(nodes, xsdVisitor.getEnumerationElements());
        return nodes;
    }

    private static void generateTypeNode(XSDVisitor xsdVisitor, Schema schema,
                                         Map<String, ModuleMemberDeclarationNode> nodes) throws Exception {
        Element schemaElement = schema.getElement();
        xsdVisitor.setTargetNamespace(schemaElement.getAttribute(TARGET_NAMESPACE));
        generateNodes(schemaElement, nodes, xsdVisitor);
    }

    private String getElementType(String messageName, Definition wsdlDefinition,
                                  Map<String, ModuleMemberDeclarationNode> nodes) {
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
        if (parts.get(firstPartKey).getTypeName() != null) {
            String requestType = parts.get(firstPartKey).getTypeName().getLocalPart();
            if (nodes.containsKey(requestType) || isSimpleType(requestType)) {
                return parts.get(firstPartKey).getTypeName().getLocalPart();
            }
        }
        return parts.get(firstPartKey).getElementName().getLocalPart();
    }

    private String getElementName(String messageName, Definition wsdlDefinition,
                                  Map<String, ModuleMemberDeclarationNode> nodes) {
        QName qName = new QName(wsdlDefinition.getTargetNamespace(), messageName);
        MessageImpl message = (MessageImpl) wsdlDefinition.getMessages().get(qName);
        if (message == null) {
            throw new IllegalArgumentException("Message not found: " + messageName);
        }
        Map<String, PartImpl> parts = message.getParts();
        if (parts.isEmpty()) {
            throw new IllegalStateException("No parts found for message: " + messageName);
        }
        return parts.keySet().iterator().next();
    }


    private WsdlService getSoapService(Definition wsdlDefinition) {
        Collection<Service> services = wsdlDefinition.getAllServices().values();
        for (Service service : services) {
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

    private Map<String, WsdlOperation> getWSDLOperations() throws Exception {
        Map<String, WsdlOperation> wsdlOperations = new HashMap<>();
        for (Object op : soapPort.getBinding().getBindingOperations()) {
            BindingOperation bindingOperation = (BindingOperation) op;
            WsdlOperation wsdlOperation = getWsdlOperation(bindingOperation);
            if (bindingOperation.getBindingInput() == null) {
                throw new Exception("Invalid binding operation: Binding input is null.");
            } else if (bindingOperation.getBindingOutput() == null) {
                throw new Exception("Invalid binding operation: Binding output is null.");
            }
            String inputPayload = bindingOperation.getBindingInput().getName();
            String outputPayload = bindingOperation.getBindingOutput().getName();
            List<String> headerParts = new ArrayList<>();
            String inputHeaderName = generateSOAPInputHeaderParts(bindingOperation, headerParts, soapVersion);
            if (bindingOperation.getOperation().getInput().getMessage() == null
                    || bindingOperation.getOperation().getOutput().getMessage() == null) {
                throw new Exception("Message element is missing in the input/output of the operation: " +
                        bindingOperation.getOperation().getName());
            }
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

    public static String generateSOAPInputHeaderParts(BindingOperation bindingOperation, List<String> partElements,
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
                partElements.add(partName);
                if (soap12Header.getMessage() == null) {
                    throw new Exception("Message element is missing in the <soap:header> for " +
                            "\"" + partName + "\" element");
                }
                headerMessageName = soap12Header.getMessage().getLocalPart();
            } else if (soapVersion.equals(SoapVersion.SOAP11) && element instanceof SOAPHeaderImpl soap11Header) {
                String partName = soap11Header.getPart();
                if (partName == null) {
                    continue;
                }
                partElements.add(partName);
                if (soap11Header.getMessage() == null) {
                    throw new Exception("Message element is missing in the <soap:header> for " +
                            "\"" + partName + "\" element");
                }
                headerMessageName = soap11Header.getMessage().getLocalPart();
            }
        }
        return headerMessageName;
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
