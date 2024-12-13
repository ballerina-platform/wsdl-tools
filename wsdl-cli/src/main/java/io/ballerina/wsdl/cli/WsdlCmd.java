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
import io.ballerina.wsdl.core.generator.GeneratedSource;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
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
        description = "Generate a Ballerina client and records for a given WSDL definition."
)
public class WsdlCmd implements BLauncherCmd {
    private static final String CMD_NAME = "wsdl";
    private static final String COMMAND_IDENTIFIER = "ballerina-wsdl";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;

    @CommandLine.Parameters(description = "Input file path of the WSDL schema")
    private List<String> inputPath = new ArrayList<>();

    @CommandLine.Option(names = {"--operations"},
                        description = "Comma-separated operation names to generate", split = ",")
    private String[] operations;

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public static final String INVALID_BALLERINA_DIRECTORY_ERROR =
            "Invalid Ballerina package directory: %s, cannot find 'Ballerina.toml' file";

    @CommandLine.Option(names = {"-o", "--output"}, description = "Destination file path of the generated types from " +
            "the WSDL file")
    private String outputPath = "types.bal";

    public WsdlCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute() {
        if (helpFlag) {
            StringBuilder stringBuilder = new StringBuilder();
            printLongDesc(stringBuilder);
            outStream.println(stringBuilder);
            return;
        }
        Path currentDir = Paths.get("").toAbsolutePath();
        Path commandPath = currentDir.resolve("Ballerina.toml");
        if (!Files.exists(commandPath)) {
            outStream.printf((INVALID_BALLERINA_DIRECTORY_ERROR) + "%n", commandPath);
            exitOnError();
            return;
        }
        if (inputPath.isEmpty()) {
            outStream.println(MISSING_WSDL_PATH);
            exitOnError();
            return;
        }
        try {
            if (!Files.exists(Path.of(inputPath.get(0)))) {
                outStream.println(inputPath.get(0) + " file does not exist.");
                return;
            }
            try {
                String[] operations = this.operations.split(",");
                wsdlToBallerina(inputPath.get(0), operations);
            } catch (IOException e) {
                outStream.println("Error: " + e.getLocalizedMessage());
                exitOnError();
            }
            outStream.println("Output is successfully written to " + inputPath.get(0));
        } catch (Exception e) {
            outStream.println("Error: " + e.getLocalizedMessage());
            exitOnError();
        }
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        Class<?> clazz = WsdlCmd.class;
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("cli-docs/wsdl-help.help");
        if (inputStream != null) {
            try (InputStreamReader inputStreamREader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(inputStreamREader)) {
                String content = br.readLine();
                outStream.append(content);
                while ((content = br.readLine()) != null) {
                    outStream.append('\n').append(content);
                }
            } catch (IOException e) {
                outStream.append("Helper text is not available.");
            }
        }
    }

    public void setInputPath(List<String> inputPath) {
        this.inputPath = inputPath;
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
    public void wsdlToBallerina(String fileName, String... operations) throws IOException {
        File wsdlFile = new File(fileName);
        Path wsdlFilePath = Paths.get(wsdlFile.getCanonicalPath());
        String fileContent = Files.readString(wsdlFilePath);

        WsdlToBallerina wsdlToBallerina = new WsdlToBallerina();
        wsdlToBallerina.generateFromWSDL(fileContent, operations[0]);

//        writeFile(response.getTypesSource());
//        writeFile(response.getClientSource());
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
        System.out.println(filePath);
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

    public void setOperations(String operations) {
        this.operations = operations;
    }

    private void exitOnError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }
}
