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

package io.ballerina.wsdl.cmd;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.wsdl.exception.CmdException;
import io.ballerina.wsdl.exception.GenerationException;
import io.ballerina.wsdl.generator.CodeGenerator;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static io.ballerina.wsdl.cmd.Constants.MESSAGE_FOR_MISSING_INPUT_ARGUMENT;

/**
 * Main class to implement "wsdl" command for Ballerina.
 * Commands for Client generation from a WSDL.
 */
@CommandLine.Command(
        name = "wsdl",
        description = "Generates Ballerina clients from a WSDL."
)
public class WSDLCmd implements BLauncherCmd {
    private static final String CMD_NAME = "wsdl";
    private PrintStream outStream;
    private boolean exitWhenFinish;
    private Path executionPath = Paths.get(System.getProperty("user.dir"));

    @CommandLine.Option(names = {"-h", "--help"}, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = {"-i", "--input"}, description = "URL or File path to the WSDL.")
    private boolean inputPathFlag;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "Directory to store the generated Ballerina clients. " +
                    "If this is not provided, the generated files will be stored in the current execution directory.")
    private String outputPath;

    @CommandLine.Parameters
    private List<String> argList;

    /**
     * Constructor that initialize with the default values.
     */
    public WSDLCmd() {
        this.outStream = System.err;
        this.exitWhenFinish = true;
    }

    /**
     * Constructor override, which takes output stream and execution dir as inputs.
     *
     * @param outStream      output stream from ballerina
     * @param executionDir   defines the directory location of  execution of ballerina command
     */
    public WSDLCmd(PrintStream outStream, Path executionDir) {
        new WSDLCmd(outStream, executionDir, true);
    }

    /**
     * Constructor override, which takes output stream and execution dir and exits when finish as inputs.
     *
     * @param outStream         output stream from ballerina
     * @param executionDir      defines the directory location of  execution of ballerina command
     * @param exitWhenFinish    exit when finish the execution
     */
    public WSDLCmd(PrintStream outStream, Path executionDir, boolean exitWhenFinish) {
        this.outStream = outStream;
        this.executionPath = executionDir;
        this.exitWhenFinish = exitWhenFinish;
    }

    @Override
    public void execute() {
        try {
            validateInputFlags();
            String wsdlPath = readWSDLPath();
            String outputPath = getTargetOutputPath().toString();
            CodeGenerator.getInstance().generate(wsdlPath, outputPath);
        } catch (CmdException | GenerationException e) {
            outStream.println(e.getMessage());
            exitError(this.exitWhenFinish);
        } catch (Exception e) {
            outStream.println(e);
            exitError(this.exitWhenFinish);
        }

        // Successfully exit if no error occurs
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Validates the input flags in the GraphQL command line tool.
     *
     * @throws CmdException               when a graphql command related error occurs
     */
    private void validateInputFlags() throws CmdException {
        // Check if CLI help flag argument is present
        if (helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            return;
        }

        // Check if CLI input path flag argument is present
        if (inputPathFlag) {
            // Check if GraphQL configuration file is provided
            if (argList == null) {
                throw new CmdException(MESSAGE_FOR_MISSING_INPUT_ARGUMENT);
            }
        } else {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(getName());
            outStream.println(commandUsageInfo);
            exitError(this.exitWhenFinish);
        }
    }

    /**
     * Get the URL or file path of WSDL file.
     *
     * @return  URL or file path of WSDL file
     */
    private String readWSDLPath() {
            String filePath = argList.get(0);
            return filePath;
    }

    /**
     * Gets the target output path for the code generation.
     *
     * @return      the target output path for the code generation
     */
    private Path getTargetOutputPath() {
        Path targetOutputPath = executionPath;
        if (this.outputPath != null) {
            if (Paths.get(outputPath).isAbsolute()) {
                targetOutputPath = Paths.get(outputPath);
            } else {
                targetOutputPath = Paths.get(targetOutputPath.toString(), outputPath);
            }
        }
        return targetOutputPath;
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {}

    @Override
    public void printUsage(StringBuilder stringBuilder) {}

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {}

    /**
     * Exit with error code 1.
     *
     * @param exit Whether to exit or not.
     */
    private static void exitError(boolean exit) {
        if (exit) {
            Runtime.getRuntime().exit(1);
        }
    }
}
