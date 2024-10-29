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

package io.ballerina.wsdl.cli;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.wsdl.core.WsdlToBallerina;
import io.ballerina.wsdl.core.WsdlToBallerinaResponse;
import io.ballerina.wsdl.core.generator.GeneratedSource;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.wsdl.cli.Messages.MISSING_WSDL_PATH;

/**
 * Command-line utility class for generating Ballerina source code from WSDL files.
 * It uses the specified WSDL path and optional operation names to generate the code.
 *
 * @since 0.1.0
 */
@CommandLine.Command(
        name = "wsdl",
        description = "Generate the Ballerina sources for a given WSDL definition."
)
public class WsdlCmd implements BLauncherCmd {
    private static final String CMD_NAME = "wsdl";
    private static final String COMMAND_IDENTIFIER = "ballerina-wsdl";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;

    @CommandLine.Option(names = {"-i", "--input"}, description = "Relative path to the WSDL file")
    private String inputPath;

    @CommandLine.Option(names = {"--operations"},
                        description = "Comma-separated operation names to generate", split = ",")
    private String[] operations;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public WsdlCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(COMMAND_IDENTIFIER);
            outStream.println(commandUsageInfo);
            return;
        }
        if (inputPath == null || inputPath.isBlank()) {
            outStream.println(MISSING_WSDL_PATH);
            exitOnError();
            return;
        }
        List<String> operationList = new ArrayList<>();
        if (operations != null && operations.length > 0) {
            operationList.addAll(Arrays.asList(operations));
        }

        try {
            wsdlToBallerina(inputPath, operationList);
        } catch (IOException e) {
            outStream.println("Error occurred while generating client and types from WSDL" + e.getLocalizedMessage());
            exitOnError();
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generate client stubs and records from a WSDL file").append(System.lineSeparator());
        out.append(System.lineSeparator());
    }

    @Override
    public void printUsage(StringBuilder out) {

    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {

    }

    /**
     * Converts a WSDL file into Ballerina source files based on the specified operations.
     *
     * @param fileName   the path to the WSDL file
     * @param operations a list of operation names to be generated
     * @throws IOException if reading or writing files fails
     */
    private void wsdlToBallerina(String fileName, List<String> operations) throws IOException {
        File wsdlFile = new File(fileName);
        Path wsdlFilePath = Paths.get(wsdlFile.getCanonicalPath());
        String fileContent = Files.readString(wsdlFilePath);

        WsdlToBallerina wsdlToBallerina = new WsdlToBallerina();
        WsdlToBallerinaResponse response = wsdlToBallerina.generateFromWSDL(fileContent, operations);

        writeFile(response.getTypesSource());
    }

    /**
     * Writes the generated content to a file, with confirmation for overwriting if the file already exists.
     *
     * @param sourceFile generated source file details
     * @throws IOException if file writing fails
     */
    private void writeFile(GeneratedSource sourceFile) throws IOException {
        File file = new File(sourceFile.fileName());
        Path filePath = Paths.get(file.getCanonicalPath());

        if (file.exists()) {
            String userInput = System.console().readLine(String.format("The file '%s' already exists." +
                    " Overwrite? [y/N]: ", sourceFile.fileName()));
            if (!"y".equalsIgnoreCase(userInput.trim())) {
                outStream.println("Action canceled: No changes have been made.");
                return;
            } else {
                outStream.println("File " + sourceFile.fileName() + " has been overwritten.");
            }
        }

        try (FileWriter writer = new FileWriter(filePath.toFile(), StandardCharsets.UTF_8)) {
            writer.write(sourceFile.content());
        }
    }

    private void exitOnError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }
}
