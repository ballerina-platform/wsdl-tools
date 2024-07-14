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

package io.ballerina.wsdl.core.generator.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.wsdl.core.generator.Constants;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;
import io.ballerina.wsdl.core.handler.model.WsdlService;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * Generates a SOAP client from a provided WSDL service definition.
 *
 * @since 0.1.0
 */
public class SoapClientGenerator {

    private final WsdlService wsdlService;

    protected SoapClientGenerator(WsdlService wsdlService) {
        this.wsdlService = wsdlService;
    }

    protected ClassDefinitionNode getClassDefinitionNode() {
        List<Node> memberNodeList = new ArrayList<>();
        memberNodeList.addAll(createClassInstanceVariables());
        memberNodeList.add(createInitFunction());
        memberNodeList.addAll(getSoapFunctions());
        IdentifierToken className = createIdentifierToken(Constants.CLIENT);
        NodeList<Token> classTypeQualifiers = createNodeList(
                createToken(SyntaxKind.ISOLATED_KEYWORD), createToken(SyntaxKind.CLIENT_KEYWORD));
        return createClassDefinitionNode(null, createToken(SyntaxKind.PUBLIC_KEYWORD), classTypeQualifiers,
                createToken(SyntaxKind.CLASS_KEYWORD), className, createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                createNodeList(memberNodeList), createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }

    protected List<ObjectFieldNode> createClassInstanceVariables() {
        List<ObjectFieldNode> fieldNodeList = new ArrayList<>();
        Token finalKeywordToken = createToken(SyntaxKind.FINAL_KEYWORD);
        NodeList<Token> qualifierList = createNodeList(finalKeywordToken);
        QualifiedNameReferenceNode typeName = getSOAPClientName();
        IdentifierToken fieldName = createIdentifierToken(Constants.SOAP_CLIENT);
        ObjectFieldNode httpClientField = createObjectFieldNode(null, null,
                qualifierList, typeName, fieldName, null, null, createToken(SyntaxKind.SEMICOLON_TOKEN));
        fieldNodeList.add(httpClientField);
        return fieldNodeList;
    }

    protected QualifiedNameReferenceNode getSOAPClientName() {
        return createQualifiedNameReferenceNode(
                createIdentifierToken(wsdlService.getSoapVersion() == SoapVersion.SOAP11 ?
                        Constants.SOAP11 : Constants.SOAP12),
                createToken(SyntaxKind.COLON_TOKEN), createIdentifierToken(Constants.CLIENT));
    }

    private FunctionDefinitionNode createInitFunction() {
        FunctionSignatureNode functionSignatureNode = getInitFunctionSignatureNode();
        FunctionBodyNode functionBodyNode = getInitFunctionBodyNode();
        NodeList<Token> qualifierList =
                createNodeList(createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken(Constants.INIT);
        return createFunctionDefinitionNode(SyntaxKind.FUNCTION_DEFINITION, null, qualifierList,
                createToken(SyntaxKind.FUNCTION_KEYWORD), functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    private List<FunctionDefinitionNode> getSoapFunctions() {
        List<FunctionDefinitionNode> functionDefinitions = new ArrayList<>();
        for (WsdlOperation wsdlOperation : wsdlService.getWSDLOperations()) {
            SoapFunctionGenerator functionGenerator = new SoapFunctionGenerator(wsdlOperation);
            functionDefinitions.add(functionGenerator.generateFunction());
        }
        return functionDefinitions;
    }

    private FunctionSignatureNode getInitFunctionSignatureNode() {
        SeparatedNodeList<ParameterNode> parameterList =
                NodeFactory.createSeparatedNodeList(getServiceURLNode(wsdlService.getSoapServiceUrl()));
        OptionalTypeDescriptorNode returnType =
                NodeFactory.createOptionalTypeDescriptorNode(createToken(SyntaxKind.ERROR_KEYWORD),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        ReturnTypeDescriptorNode returnTypeDescriptorNode = NodeFactory.createReturnTypeDescriptorNode(
                createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(), returnType);
        return NodeFactory.createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN), parameterList,
                createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    private FunctionBodyNode getInitFunctionBodyNode() {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        FieldAccessExpressionNode varRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(Constants.SELF)),
                createToken(SyntaxKind.DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(Constants.SOAP_CLIENT)));
        SeparatedNodeList<FunctionArgumentNode> arguments =
                createSeparatedNodeList(createPositionalArgumentNode(
                        createSimpleNameReferenceNode(createIdentifierToken(Constants.SERVICE_URL))));
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(
                createToken(SyntaxKind.OPEN_PAREN_TOKEN), arguments, createToken(SyntaxKind.CLOSE_PAREN_TOKEN));
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(SyntaxKind.NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(SyntaxKind.CHECK_KEYWORD),
                expressionNode);
        AssignmentStatementNode httpClientAssignmentStatementNode = createAssignmentStatementNode(varRef,
                createToken(SyntaxKind.EQUAL_TOKEN), initializer, createToken(SyntaxKind.SEMICOLON_TOKEN));
        assignmentNodes.add(httpClientAssignmentStatementNode);

        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null, statementList, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }


    private ParameterNode getServiceURLNode(String serviceUrl) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                NodeFactory.createToken(SyntaxKind.STRING_KEYWORD));
        IdentifierToken serviceURLVarName = NodeFactory.createIdentifierToken(Constants.SERVICE_URL);

        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                createIdentifierToken('"' + serviceUrl + '"'));
        return NodeFactory.createDefaultableParameterNode(annotationNodes, serviceURLType,
                serviceURLVarName, NodeFactory.createToken(SyntaxKind.EQUAL_TOKEN), expression);
    }
}
