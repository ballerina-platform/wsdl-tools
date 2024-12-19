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
import org.xml.sax.InputSource;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import static io.ballerina.wsdl.cli.Messages.INVALID_DIRECTORY_PATH;
import static io.ballerina.wsdl.cli.Messages.MISSING_WSDL_PATH;
import static io.ballerina.xsd.core.visitor.XSDVisitorImpl.EMPTY_STRING;

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
    public static final String FILE_OVERWRITE_PROMPT = "The file '%s' already exists." +
            " Overwrite? [y/N]: ";
    public static final String MISSING_OPERATION_ERROR = "Error: Operation name is required to generate the client";
    public static final String SUCCESSFUL_MESSAGE = "Output is successfully written to %s";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;

    @CommandLine.Parameters(description = "Input file path of the WSDL schema")
    private List<String> inputPath = new ArrayList<>();

    @CommandLine.Option(names = {"--operation"})
    private String operation = "";

    @CommandLine.Option(names = {"--help", "-h"}, hidden = true)
    private boolean helpFlag;

    public static final String INVALID_BALLERINA_DIRECTORY_ERROR =
            "Invalid Ballerina package directory: %s, cannot find 'Ballerina.toml' file";

    @CommandLine.Option(names = {"-o", "--output"}, description = "Destination file path of the generated types from " +
            "the WSDL file")
    private String outputPath = "";

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
        Path outputDirPath = Path.of(outputPath);
        if (Files.exists(outputDirPath) && !Files.isDirectory(outputDirPath)) {
            outStream.printf((INVALID_DIRECTORY_PATH) + "%n", outputPath);
            exitOnError();
            return;
        }
        if (inputPath.isEmpty()) {
            outStream.println(MISSING_WSDL_PATH);
            exitOnError();
            return;
        }
        try {
            if (Files.notExists(Path.of(outputPath))) {
                Files.createDirectories(Path.of(outputPath));
            }
            if (!Files.exists(Path.of(inputPath.get(0)))) {
                outStream.println(inputPath.get(0) + " file does not exist.");
                return;
            }
            if (this.operation.equals(EMPTY_STRING)) {
                outStream.println(MISSING_OPERATION_ERROR);
                return;
            }
            WsdlToBallerinaResponse response = wsdlToBallerina(inputPath.get(0), outputPath, this.operation);
            writeSourceToFiles(response.getTypesSource());
            writeSourceToFiles(response.getClientSource());
            outStream.printf((SUCCESSFUL_MESSAGE) + "%n", Path.of(outputPath).toAbsolutePath());
        } catch (Exception e) {
            outStream.println("Error: " + e.getLocalizedMessage());
            exitOnError();
        }
    }

    private void writeSourceToFiles(GeneratedSource response) throws IOException {
        Path clientPath = Paths.get(response.fileName());
        Path destinationClientFile = Files.exists(clientPath)
                ? handleFileOverwrite(clientPath, outStream) : clientPath;
        Files.writeString(destinationClientFile, response.content());
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
    public WsdlToBallerinaResponse wsdlToBallerina(String fileName, String outputDirectory,
                                                   String operation) throws Exception {
        File wsdlFile = new File(fileName);
        Path wsdlFilePath = Paths.get(wsdlFile.getCanonicalPath());
        String fileContent = Files.readString(wsdlFilePath);
        WsdlToBallerina wsdlToBallerina = new WsdlToBallerina();
        Definition wsdlDefinition = parseWSDLContent(fileContent);
        return wsdlToBallerina.generateFromWSDL(wsdlDefinition, outputDirectory, operation);
    }

    public static Path handleFileOverwrite(Path destinationFile, PrintStream outStream) {
        if (!Files.exists(destinationFile)) {
            return destinationFile;
        }
        String filePath = destinationFile.toString();
        outStream.printf(FILE_OVERWRITE_PROMPT, filePath);
        String response = new Scanner(System.in).nextLine().trim().toLowerCase();
        if (response.equals("y")) {
            return destinationFile;
        }
        int counter = 1;
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
        String extension = dotIndex == -1 ? EMPTY_STRING : fileName.substring(dotIndex);
        String parentPath = new File(filePath).getParent() != null ? new File(filePath).getParent() : EMPTY_STRING;
        while (Files.exists(destinationFile)) {
            String newFileName = baseName + "." + counter + extension;
            destinationFile = Path.of(parentPath, newFileName);
            counter++;
        }
        return destinationFile;
    }

    private Definition parseWSDLContent(String wsdlDefinitionText) throws WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.verbose", false);
        reader.setFeature("javax.wsdl.importDocuments", true);
        InputStream wsdlStream = new ByteArrayInputStream(wsdlDefinitionText.getBytes(Charset.defaultCharset()));
        return reader.readWSDL(null, new InputSource(wsdlStream));
    }

    private void exitOnError() {
        if (exitWhenFinish) {
            Runtime.getRuntime().exit(1);
        }
    }
}
