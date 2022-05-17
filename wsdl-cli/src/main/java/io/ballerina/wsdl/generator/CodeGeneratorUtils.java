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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.wsdl.generator.model.SrcFilePojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static io.ballerina.wsdl.generator.CodeGeneratorConstants.BAL_KEYWORDS;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.ESCAPE_PATTERN;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.wsdl.generator.CodeGeneratorConstants.WHITESPACE;

/**
 * Utility class for GraphQL client code generation.
 */
public class CodeGeneratorUtils {

    /**
     * Gets the absolute file path of a given source file for code generation.
     *
     * @param file             the source file
     * @param outputPath       the target output path
     * @return                 the client file name of the client file to be generated
     */
    public static Path getAbsoluteFilePath(SrcFilePojo file, Path outputPath) {
        Path filePath;
        File theDir = new File(outputPath.toString());
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        filePath = outputPath.resolve(outputPath + "/" + file.getFileName());
        return filePath;
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();


    private static MinutiaeList getSingleWSMinutiae() {
        Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(WHITESPACE);
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(whitespace);
        return leading;
    }

    /**
     * Gets the remote function signature return type name.
     *
     * @param operationName    the name of the operation
     * @return                 the remote function return type name
     */
    public static String getRemoteFunctionSignatureReturnTypeName(String operationName) {
        return operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1).concat("Response|graphql:ClientError");
    }

    private static final MinutiaeList SINGLE_END_OF_LINE_MINUTIAE = getEndOfLineMinutiae();

    private static MinutiaeList getEndOfLineMinutiae() {
        Minutiae endOfLineMinutiae = AbstractNodeFactory.createEndOfLineMinutiae(LINE_SEPARATOR);
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(endOfLineMinutiae);
        return leading;
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier    identifier or method name
     * @return              escaped string
     */
    public static String escapeIdentifier(String identifier) {

        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b")
                || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

            // TODO: Remove this `if`. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/23045
            if (identifier.equals("error")) {
                identifier = "_error";
            } else {
                identifier = identifier.replaceAll(ESCAPE_PATTERN, "\\\\$1");
                if (identifier.endsWith("?")) {
                    if (identifier.charAt(identifier.length() - 2) == '\\') {
                        StringBuilder stringBuilder = new StringBuilder(identifier);
                        stringBuilder.deleteCharAt(identifier.length() - 2);
                        identifier = stringBuilder.toString();
                    }
                    if (BAL_KEYWORDS.stream().anyMatch(Optional.ofNullable(identifier)
                            .filter(sStr -> sStr.length() != 0)
                            .map(sStr -> sStr.substring(0, sStr.length() - 1))
                            .orElse(identifier)::equals)) {
                        identifier = "'" + identifier;
                    } else {
                        return identifier;
                    }
                } else {
                    identifier = "'" + identifier;
                }
            }
        }
        return identifier;
    }
}
