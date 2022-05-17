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

package io.ballerina.wsdl.generator;

import io.ballerina.wsdl.exception.ClientGenerationException;
import io.ballerina.wsdl.exception.GenerationException;
import io.ballerina.wsdl.exception.TypesGenerationException;
import io.ballerina.wsdl.generator.ballerina.ClientGenerator;
import io.ballerina.wsdl.generator.ballerina.TypesGenerator;
import io.ballerina.wsdl.generator.model.SrcFilePojo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.wsdl.generator.CodeGeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.TYPES_FILE_NAME;

/**
 * This class implements the GraphQL client code generator tool.
 */
public class CodeGenerator {
    private static CodeGenerator codeGenerator = null;

    public static CodeGenerator getInstance() {
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator();
        }
        return codeGenerator;
    }

    /**
     * Generates the ballerina client code for a given WSDL file.
     *
     * @param wsdlPath                              URL or file path of WSDL file
     * @throws GenerationException                  when a code generation error occurs
     */
    public void generate(String wsdlPath, String outputPath) throws GenerationException {
        try {

            List<SrcFilePojo> genSources = generateBalSources(wsdlPath);
            writeGeneratedSources(genSources, Path.of(outputPath));
        } catch (ClientGenerationException | TypesGenerationException | IOException e) {
            throw new GenerationException(e.getMessage());
        }
    }

    /**
     * Generates the Ballerina source codes for a given GraphQL project.
     *
     * @param wsdlPath                              URL of path of WSDL file
     * @return                                      the list of generated Ballerina source file pojo
     * @throws ClientGenerationException            when a client code generation error occurs
     * @throws TypesGenerationException             when a types code generation error occurs
     */
    private List<SrcFilePojo> generateBalSources(String wsdlPath) throws ClientGenerationException,
            TypesGenerationException {
        List<SrcFilePojo> sourceFiles = new ArrayList<>();
        generateClients(wsdlPath, sourceFiles);
        generateTypes(wsdlPath, sourceFiles);
        return sourceFiles;
    }

    /**
     * Generates the Ballerina clients source codes for a given GraphQL project.
     *
     * @param wsdlPath                              URL of path of WSDL file
     * @param sourceFiles                           the list of generated Ballerina source file pojo
     * @throws ClientGenerationException            when a client code generation error occurs
     */
    private void generateClients(String wsdlPath, List<SrcFilePojo> sourceFiles) throws ClientGenerationException {
            String clientSrc = ClientGenerator.getInstance().generateSrc(wsdlPath);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, "generatedBalProject",
                    CLIENT_FILE_NAME, clientSrc));
    }

    /**
     * Generates the Ballerina types source codes for a given GraphQL project.
     *
     * @param wsdlPath                              URL of path of WSDL file
     * @param sourceFiles                           the list of generated Ballerina source file pojo
     * @throws TypesGenerationException             when a types code generation error occurs
     */
    private void generateTypes(String wsdlPath, List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
        String typesFileContent = TypesGenerator.getInstance().generateSrc(wsdlPath);
        sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, "generatedBalProject",
                TYPES_FILE_NAME, typesFileContent));
    }

    /**
     * Writes the generated Ballerina source codes to the files in the specified {@code outputPath}.
     *
     * @param sources                               the list of generated Ballerina source file pojo
     * @param outputPath                            the target output path for the code generation
     * @throws IOException                          If an I/O error occurs
     */
    private void writeGeneratedSources(List<SrcFilePojo> sources, Path outputPath) throws IOException {
        if (!sources.isEmpty()) {
            for (SrcFilePojo file : sources) {
                if (file.getType().isOverwritable()) {
                    Path filePath = CodeGeneratorUtils.getAbsoluteFilePath(file, outputPath);
                    String fileContent = file.getContent();
                    CodeGeneratorUtils.writeFile(filePath, fileContent);
                }
            }
        }
    }
}
