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

package io.ballerina.wsdl.generator.ballerina;

import io.ballerina.wsdl.common.GraphqlTest;
import io.ballerina.wsdl.exception.ClientGenerationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is used to test the functionality of the GraphQL client code generator.
 */
public class ClientGeneratorTest extends GraphqlTest {

    @Test(description = "Test the successful generation of client code")
    public void testGenerateSrc() throws IOException {
        try {
            String wsdl = ""; // TODO: Need to improve this
            String generatedClientContent = ClientGenerator.getInstance().
                    generateSrc(wsdl)
                    .trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "country_queries_client.bal"));
            String expectedClientContent = readContent(expectedClientFile);

            Assert.assertEquals(expectedClientContent, generatedClientContent);

        } catch (ClientGenerationException e) {
            Assert.fail("Error while generating the client code. " + e.getMessage());
        }
    }

    @Test(description = "Test the successful generation of client code with API keys config")
    public void testGenerateSrcWithApiKeysConfig()
            throws IOException {
        try {
            String wsdl = ""; // TODO: Need to improve this
            String generatedClientContent = ClientGenerator.getInstance().
                    generateSrc(wsdl)
                    .trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "apiKeysConfig",
                            "country_queries_client.bal"));
            String expectedClientContent = readContent(expectedClientFile);

            Assert.assertEquals(expectedClientContent, generatedClientContent);

        } catch (ClientGenerationException e) {
            Assert.fail("Error while generating the client code. " + e.getMessage());
        }
    }

    @Test(description = "Test the successful generation of client code with client config")
    public void testGenerateSrcWithClientConfig()
            throws IOException {
        try {
            String wsdl = ""; // TODO: Need to improve this
            String generatedClientContent = ClientGenerator.getInstance().
                    generateSrc(wsdl)
                    .trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");

            Path expectedClientFile =
                    resourceDir.resolve(Paths.get("expectedGenCode", "client", "clientConfig",
                            "country_queries_client.bal"));
            String expectedClientContent = readContent(expectedClientFile);

            Assert.assertEquals(expectedClientContent, generatedClientContent);

        } catch (ClientGenerationException e) {
            Assert.fail("Error while generating the client code. " + e.getMessage());
        }
    }
}
