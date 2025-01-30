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

/**
 * Contains static message constants for user notifications and error handling in the application.
 *
 * @since 0.1.0
 */
public class Messages {
    public static final String MISSING_WSDL_PATH = "Error: Missing input WSDL file path. " +
            "Please specify the path in the command.\n" +
            "Example: bal wsdl <path_to_wsdl_file>";
    public static final String INVALID_DIRECTORY_PATH = "Error: Invalid directory path has been provided. " +
            "Output path '%s' is a file";
}
