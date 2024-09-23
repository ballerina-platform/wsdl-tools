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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.wsdl.core.generator.util.GeneratorUtils;
import io.ballerina.wsdl.core.generator.Constants;
import io.ballerina.wsdl.core.handler.model.SoapVersion;
import io.ballerina.wsdl.core.handler.model.WsdlService;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the module part node for a SOAP client,
 * organizing all necessary components including imports and class definitions.
 *
 * @since 0.1.0
 */
public class SoapClientModulePartGenerator {

    private final WsdlService wsdlService;

    protected SoapClientModulePartGenerator(WsdlService wsdlService) {
        this.wsdlService = wsdlService;
    }

    protected ModulePartNode getSoapClientModulePart() {
        NodeList<ImportDeclarationNode> importDeclarationNodes =
                AbstractNodeFactory.createNodeList(getImportDeclarations());
        ClassDefinitionNode classDefinitionNode = new SoapClientGenerator(wsdlService).getClassDefinitionNode();
        List<ModuleMemberDeclarationNode> moduleMemberDeclarations = new ArrayList<>();
        moduleMemberDeclarations.add(classDefinitionNode);
        NodeList<ModuleMemberDeclarationNode> moduleMemberDeclarationNodes =
                AbstractNodeFactory.createNodeList(moduleMemberDeclarations);
        Token eofToken = AbstractNodeFactory.createIdentifierToken("");

        return NodeFactory.createModulePartNode(importDeclarationNodes, moduleMemberDeclarationNodes, eofToken);
    }

    private List<ImportDeclarationNode> getImportDeclarations() {
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(Constants.BALLERINA,
                wsdlService.getSoapVersion() == SoapVersion.SOAP11 ?
                        Constants.SOAP_SOAP11 : Constants.SOAP_SOAP12);
        return new ArrayList<>(List.of(importForHttp));
    }
}
