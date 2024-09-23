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
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.wsdl.core.generator.Constants;
import io.ballerina.wsdl.core.handler.model.WsdlOperation;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;

/**
 * Generates Ballerina function definitions for SOAP operations based on WSDL operation descriptions.
 *
 * @since 0.1.0
 */
public class SoapFunctionGenerator {

    private final WsdlOperation wsdlOperation;

    protected SoapFunctionGenerator(WsdlOperation wsdlOperation) {
        this.wsdlOperation = wsdlOperation;
    }

    public FunctionDefinitionNode generateFunction() {
        NodeList<Token> qualifierList =
                createNodeList(createToken(SyntaxKind.PUBLIC_KEYWORD), createToken(SyntaxKind.ISOLATED_KEYWORD));
        Token functionKeyWord = createToken(SyntaxKind.FUNCTION_KEYWORD);
        IdentifierToken functionName = createIdentifierToken(wsdlOperation.getOperationName());
        FunctionSignatureNode functionSignatureNode = getfunctionSignatureNode();
        FunctionBodyNode functionBodyNode = getFunctionBodyNode();

        return NodeFactory.createFunctionDefinitionNode(SyntaxKind.OBJECT_METHOD_DEFINITION, null,
                qualifierList, functionKeyWord, functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    private FunctionSignatureNode getfunctionSignatureNode() {
        SeparatedNodeList<ParameterNode> parameterList =
                NodeFactory.createSeparatedNodeList(getInputNode(wsdlOperation.getOperationInput().toString()));
        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                getReturnType(wsdlOperation.getOperationOutput().toString());
        return NodeFactory.createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN), parameterList,
                createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    private FunctionBodyNode getFunctionBodyNode() {
        List<StatementNode> assignmentNodes = new ArrayList<>();
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        return createFunctionBodyBlockNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null, statementList, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
    }

    private ParameterNode getInputNode(String inputTypeName) {
        NodeList<AnnotationNode> annotationNodes = NodeFactory.createEmptyNodeList();
        BuiltinSimpleNameReferenceNode inputType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(inputTypeName));
        IdentifierToken inputName = NodeFactory.createIdentifierToken(Constants.INPUT);
        return NodeFactory.createRequiredParameterNode(annotationNodes, inputType, inputName);
    }

    private ReturnTypeDescriptorNode getReturnType(String outputTypeName) {
        BuiltinSimpleNameReferenceNode inputType = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(outputTypeName));
        SimpleNameReferenceNode errorType =
                NodeFactory.createSimpleNameReferenceNode(createToken(SyntaxKind.ERROR_KEYWORD));
        UnionTypeDescriptorNode unionTypeDescriptorNode =
                NodeFactory.createUnionTypeDescriptorNode(inputType, createToken(SyntaxKind.PIPE_TOKEN), errorType);
        return NodeFactory.createReturnTypeDescriptorNode(
                createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(), unionTypeDescriptorNode);

    }
}
