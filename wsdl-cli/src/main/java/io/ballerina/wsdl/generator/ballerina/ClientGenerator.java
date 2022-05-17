/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.wsdl.generator.ballerina;

import com.predic8.schema.Element;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.ExtensibilityOperation;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.Service;
import com.predic8.wsdl.WSDLParser;
import com.predic8.wstool.creator.RequestTemplateCreator;
import com.predic8.wstool.creator.SOARequestCreator;
import groovy.xml.MarkupBuilder;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.wsdl.exception.ClientGenerationException;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTemplateExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.XML_KEYWORD;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.BALLERINA;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.CLIENT;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.CLIENT_EP;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.EMPTY_EXPRESSION;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.ENVELOP;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.EQUAL;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.HTTP;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_PARAM_NAME;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.HTTP_CLIENT_CONFIG_TYPE_NAME;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.IMPORT;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.INIT;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.INIT_RETURN_TYPE;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.RESPONSE;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.SELF;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.SEMICOLON;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.SERVICE_URL_PARAM_NAME;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.SERVICE_URL_TYPE_NAME;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.SLASH;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.WHITESPACE;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.XMLDATA;
import static io.ballerina.wsdl.generator.CodeGeneratorUtils.SINGLE_WS_MINUTIAE;

/**
 * This class is used to generate ballerina client file according to given SDL and query file.
 */
public class ClientGenerator {
    private static ClientGenerator clientGenerator = null;

    public static ClientGenerator getInstance() {
        if (clientGenerator == null) {
            clientGenerator = new ClientGenerator();
        }
        return clientGenerator;
    }

    /**
     * Generates the client file content.
     *
     * @param wsdlPath                          URL of file path of WSDL file
     *
     * @return                                  the client file content
     * @throws ClientGenerationException        when a client code generation error occurs
     */
    public String generateSrc(String wsdlPath) throws ClientGenerationException {
        try {
            return Formatter.format(generateSyntaxTree(wsdlPath)).toString();
        } catch (FormatterException e) {
            throw new ClientGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the client syntax tree.
     *
     * @param wsdlPath          URL of file path of WSDL file
     */
    private SyntaxTree generateSyntaxTree(String wsdlPath) {
        // Generate imports
        NodeList<ImportDeclarationNode> imports = generateImports();
        // Generate auth config records & client class
        NodeList<ModuleMemberDeclarationNode> members =
                generateMembers(wsdlPath);

        ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    /**
     * Generates the imports in the client file.
     *
     * @return                          the node list which represent imports in the client file
     */
    private NodeList<ImportDeclarationNode> generateImports() {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForHttp = getImportDeclarationNode(BALLERINA, HTTP);
        ImportDeclarationNode importForXmlData = getImportDeclarationNode(BALLERINA, XMLDATA);
        imports.add(importForHttp);
        imports.add(importForXmlData);
        return createNodeList(imports);
    }

    /**
     * Gets the `ImportDeclarationNode` instance for a given organization name & module name.
     *
     * @param orgName          the organization name
     * @param moduleName       the module name
     * @return                 the `ImportDeclarationNode` instance
     */
    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {
        Token importKeyword = AbstractNodeFactory.createIdentifierToken(IMPORT, SINGLE_WS_MINUTIAE,
                SINGLE_WS_MINUTIAE);

        Token orgNameToken = AbstractNodeFactory.createIdentifierToken(orgName);
        Token slashToken = AbstractNodeFactory.createIdentifierToken(SLASH);
        ImportOrgNameNode importOrgNameNode = NodeFactory.createImportOrgNameNode(orgNameToken, slashToken);

        Token moduleNameToken = AbstractNodeFactory.createIdentifierToken(moduleName);
        SeparatedNodeList<IdentifierToken> moduleNodeList = AbstractNodeFactory.createSeparatedNodeList(
                moduleNameToken);

        Token semicolon = AbstractNodeFactory.createIdentifierToken(SEMICOLON);

        return NodeFactory.createImportDeclarationNode(importKeyword, importOrgNameNode,
                moduleNodeList, null, semicolon);
    }

    /**
     * Generates the members in the client file. The members include record types & client class nodes.
     *
     * @param wsdlPath              URL of file path of WSDL file
     * @return                      the node list which represent members in the client file
     */
    private NodeList<ModuleMemberDeclarationNode> generateMembers(String wsdlPath) {
        List<ModuleMemberDeclarationNode> members =  new ArrayList<>();
        // Generate client class
        members.add(generateClientClass(wsdlPath));
        return createNodeList(members);
    }

    /**
     * Generates the client class in the client file.
     *
     * @param wsdlPath             URL of file path of WSDL file
     * @return                     the node which represent the client class in the client file
     */
    private ClassDefinitionNode generateClientClass(String wsdlPath) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
        IdentifierToken className = createIdentifierToken(CLIENT);

        List<Node> members =  new ArrayList<>();

        WSDLParser parser = new WSDLParser();
        Definitions wsdl = parser.parse(wsdlPath);
        List<Service> services = wsdl.getServices();
        Service service = services.get(0);
        String serviceURL = '"' + service.getPorts().get(0).getAddress().getLocation() + '"';

        // Add instance variable to class definition node
        members.addAll(createClassInstanceVariables());

        // Generate init function
        members.add(generateInitFunction(serviceURL));

        // Generate remote functions
        members.addAll(generateRemoteFunctions(wsdl));

        return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                createNodeList(members), createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generate client class instance variables.
     *
     * @return {@link List<ObjectFieldNode>}    List of instance variables
     */
    private List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        Token finalKeywordToken = createToken(FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);
        QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(HTTP),
                createToken(COLON_TOKEN), createIdentifierToken(CLIENT));
        IdentifierToken fieldName = createIdentifierToken(CLIENT_EP);
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        ObjectFieldNode httpClientField = createObjectFieldNode(metadataNode, null,
                qualifierList, typeName, fieldName, null, null, createToken(SEMICOLON_TOKEN));
        fieldNodeList.add(httpClientField);
        return fieldNodeList;
    }

    /**
     * Generates the client class init function.
     *
     * @return                  the node which represent the init function
     */
    private FunctionDefinitionNode generateInitFunction(String serviceURL) {
        Node documentation = createIdentifierToken("\n# Gets invoked to initialize the `connector`.");
        MetadataNode metadataNode = createMetadataNode(documentation, createEmptyNodeList());

        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));

        IdentifierToken functionName = createIdentifierToken(INIT);

        FunctionSignatureNode functionSignatureNode = generateInitFunctionSignature(serviceURL);
        FunctionBodyNode functionBodyNode = generateInitFunctionBody();

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    /**
     * Generates the client class init function body.
     *
     * @return                  the node which represent the init function body
     */
    public FunctionBodyNode generateInitFunctionBody() {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(CLIENT_EP)));

        // Expression node
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(SERVICE_URL_PARAM_NAME)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(",");

        PositionalArgumentNode positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(HTTP_CLIENT_CONFIG_PARAM_NAME)));
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(createToken(OPEN_PAREN_TOKEN), arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));

        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));

        assignmentNodes.add(httpClientAssignmentStatementNode);

        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN));
    }

    /**
     * Generates the client class init function signature.
     *
     * @return                  the node which represent the init function signature
     */
    public FunctionSignatureNode generateInitFunctionSignature(String serviceURL) {
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                generateInitFunctionParams(serviceURL));

        OptionalTypeDescriptorNode returnType = createOptionalTypeDescriptorNode(
                createIdentifierToken(INIT_RETURN_TYPE),
                createToken(QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    /**
     * Generates the client class init function parameters.
     *
     * @return                  the list of nodes which represent the init function parameters
     */
    private List<Node> generateInitFunctionParams(String serviceURL) {
        List<Node> parameters = new ArrayList<>();

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode httpClientConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(HTTP_CLIENT_CONFIG_TYPE_NAME));
        IdentifierToken httpClientConfigParamName = createIdentifierToken(HTTP_CLIENT_CONFIG_PARAM_NAME);
        IdentifierToken equalToken = createIdentifierToken(EQUAL);
        BasicLiteralNode emptyExpression = createBasicLiteralNode(null, createIdentifierToken(EMPTY_EXPRESSION));
        DefaultableParameterNode defaultHTTPConfig = createDefaultableParameterNode(annotationNodes,
                httpClientConfigTypeName, httpClientConfigParamName, equalToken, emptyExpression);

        Node serviceURLNode = generateServiceURLNode(serviceURL);
        parameters.add(serviceURLNode);
        parameters.add(createToken(COMMA_TOKEN));
        parameters.add(defaultHTTPConfig);

        return parameters;
    }

    /**
     * Generates the service URL {@code string serviceUrl} node.
     *
     * @return                  the service URL {@code string serviceUrl} node
     */
    private Node generateServiceURLNode(String serviceURL) {
        Node serviceURLNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(SERVICE_URL_TYPE_NAME));
        IdentifierToken serviceURLParamName = createIdentifierToken(SERVICE_URL_PARAM_NAME);
        serviceURLNode = createDefaultableParameterNode(annotationNodes, serviceURLTypeName, serviceURLParamName,
                createToken(EQUAL_TOKEN), createIdentifierToken(serviceURL));
        return serviceURLNode;
    }

    private List<FunctionDefinitionNode> generateRemoteFunctions(Definitions wsdl) {
        List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();

        List<PortType> portTypes = wsdl.getPortTypes();
        PortType portType = portTypes.get(0);
        List<Operation> operations = portType.getOperations();

        List<Binding> bindings = wsdl.getBindings();
        Binding binding = bindings.get(0);

        for (Operation operation: operations) {
            FunctionDefinitionNode functionDefinitionNode = generateRemoteFunction(operation, binding, portType, wsdl);
            functionDefinitionNodeList.add(functionDefinitionNode);
        }

        return functionDefinitionNodeList;
    }

    private FunctionDefinitionNode generateRemoteFunction(Operation operation, Binding binding, PortType portType,
                                                          Definitions wsdl) {
        MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
        NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));

        String operationName = operation.getName();
        operationName = operationName.substring(0, 1).toLowerCase() + operationName.substring(1);
        IdentifierToken functionName = createIdentifierToken(operationName);

        FunctionSignatureNode functionSignatureNode = generateRemoteFunctionSignature(operation, portType, wsdl);
        FunctionBodyNode functionBodyNode = generateRemoteFunctionBody(operation, binding, portType, wsdl);

        return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
    }

    public FunctionSignatureNode generateRemoteFunctionSignature(Operation operation, PortType portType,
                                                                 Definitions wsdl) {
        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
                generateRemoteFunctionParams(operation, portType, wsdl));

        Element outputElement = wsdl.getOutputElementForOperation(portType.getName(), operation.getName());
        String returnTypeName = EMPTY_STRING;
        if (outputElement != null) {
            returnTypeName = outputElement.getName();
            if (outputElement.getType() != null) {
                String typeName = outputElement.getType().getLocalPart();
                returnTypeName = typeName.concat("|error");
            }
        } else {
            returnTypeName = "error?";
        }


        BuiltinSimpleNameReferenceNode returnType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(returnTypeName));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    private List<Node> generateRemoteFunctionParams(Operation operation, PortType portType, Definitions wsdl) {
        List<Node> parameters = new ArrayList<>();
        Element inputElement = wsdl.getInputElementForOperation(portType.getName(), operation.getName());
        String inputParamName = inputElement.getName();
        if (inputElement.getType() != null) {
            String typeName = inputElement.getType().getLocalPart();
            inputParamName = typeName;
        }

        IdentifierToken inputParam = createIdentifierToken(inputParamName + WHITESPACE
                + inputParamName.substring(0, 1).toLowerCase() + inputParamName.substring(1));
        parameters.add(inputParam);
        return parameters;
    }

    public FunctionBodyNode generateRemoteFunctionBody(Operation operation, Binding binding, PortType portType,
                                                       Definitions wsdl) {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        StringWriter sw = new StringWriter();
        SOARequestCreator creator = new SOARequestCreator(wsdl, new RequestTemplateCreator(), new MarkupBuilder(sw));
        creator.createRequest(portType.getName(), operation.getName(), binding.getName());
        VariableDeclarationNode queryVariableDeclarationNode = generateEnvelopVariableDeclarationNode(sw.toString());
        assignmentNodes.add(queryVariableDeclarationNode);

        ExpressionStatementNode requestStatementNode = getSimpleExpressionStatementNode(
                "http:Request request = new");
        assignmentNodes.add(requestStatementNode);

        ExpressionStatementNode setXMLPayload = getSimpleExpressionStatementNode(
                "request.setXmlPayload(envelop)");
        assignmentNodes.add(setXMLPayload);

        // Set SOAPAction header
        BindingOperation bindingOperation = binding.getOperation(operation.getName());
        ExtensibilityOperation extensibilityOperation = bindingOperation.getOperation();
        if (extensibilityOperation != null) {
            if (extensibilityOperation.getSoapAction() != null) {
                String soapAction = binding.getOperation(operation.getName()).getOperation().getSoapAction();
                if (!soapAction.equals(EMPTY_STRING)) {
                    ExpressionStatementNode setSOAPActionHeader = getSimpleExpressionStatementNode(
                            "request.setHeader(\"SOAPAction\", \" " + soapAction + "\")");
                    assignmentNodes.add(setSOAPActionHeader);
                }
            }
        }

        ExpressionStatementNode setContentType = getSimpleExpressionStatementNode(
                "_ = check request.setContentType(\"text/xml\")");
        assignmentNodes.add(setContentType);

        String responseStatement = "check self.clientEp->post(\"\", request)";
        VariableDeclarationNode responseVarDeclaration = getSimpleStatement("http:Response", RESPONSE,
                responseStatement);
        assignmentNodes.add(responseVarDeclaration);

        Element outputElement = wsdl.getOutputElementForOperation(portType.getName(), operation.getName());
        if (outputElement != null) {
            String outputRecordType = outputElement.getName();
            if (outputElement.getType() != null) {
                String typeName = outputElement.getType().getLocalPart();
                outputRecordType = typeName;
            }
            String getXMLPayloadStatement = "check response.getXmlPayload()";
            VariableDeclarationNode xmlPayloadVarDeclaration = getSimpleStatement(XML_KEYWORD.stringValue(),
                    "responsePayload",
                    getXMLPayloadStatement);
            assignmentNodes.add(xmlPayloadVarDeclaration);

            ExpressionStatementNode xmlNameSpace = getSimpleExpressionStatementNode(
                    "xmlns \"http://schemas.xmlsoap.org/soap/envelope/\" as soapenv");
            assignmentNodes.add(xmlNameSpace);

            VariableDeclarationNode xmlBodyDeclaration = getSimpleStatement(XML_KEYWORD.stringValue(), "body",
                    "responsePayload/**/<soapenv:Body>");
            assignmentNodes.add(xmlBodyDeclaration);

            String soapBodyToReordStatement = "check xmldata:toRecord(body/*/*, false)";

            String responseVarName = outputRecordType.substring(0, 1).toLowerCase() + outputRecordType.substring(1);
            VariableDeclarationNode responseRecordDeclaration = getSimpleStatement(outputRecordType, responseVarName,
                    soapBodyToReordStatement);
            assignmentNodes.add(responseRecordDeclaration);

            Token returnKeyWord = createIdentifierToken("return");
            SimpleNameReferenceNode returns = createSimpleNameReferenceNode(createIdentifierToken(responseVarName));
            ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returns,
                    createToken(SEMICOLON_TOKEN));
            assignmentNodes.add(returnStatementNode);
        }




        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null, statementList,
                createToken(CLOSE_BRACE_TOKEN));
    }

    private VariableDeclarationNode generateEnvelopVariableDeclarationNode(String envelop) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();

        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createToken(XML_KEYWORD));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(ENVELOP));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        NodeList<Node> content = createNodeList(createIdentifierToken(envelop));
        TemplateExpressionNode initializer = createTemplateExpressionNode(null, createToken(XML_KEYWORD),
                createToken(BACKTICK_TOKEN), content, createToken(BACKTICK_TOKEN));

        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /*
     * Generate variableDeclarationNode.
     */
    public static VariableDeclarationNode getSimpleStatement(String responseType, String variable,
                                                             String initializer) {
        SimpleNameReferenceNode resTypeBind = createSimpleNameReferenceNode(createIdentifierToken(responseType));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken(variable));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(resTypeBind, bindingPattern);
        SimpleNameReferenceNode init = createSimpleNameReferenceNode(createIdentifierToken(initializer));

        return createVariableDeclarationNode(createEmptyNodeList(), null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), init, createToken(SEMICOLON_TOKEN));
    }

    /*
     * Generate expressionStatementNode.
     */
    public static ExpressionStatementNode getSimpleExpressionStatementNode(String expression) {
        SimpleNameReferenceNode expressionNode = createSimpleNameReferenceNode(
                createIdentifierToken(expression));
        return createExpressionStatementNode(null, expressionNode, createToken(SEMICOLON_TOKEN));
    }


}
