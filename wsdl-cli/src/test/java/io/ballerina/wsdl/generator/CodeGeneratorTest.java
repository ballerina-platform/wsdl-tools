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

import io.ballerina.wsdl.common.GraphqlTest;
import io.ballerina.wsdl.exception.GenerationException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class is used to test the functionality of the GraphQL code generator.
 */
public class CodeGeneratorTest extends GraphqlTest {

    @Test(description = "Test the functionality of the GraphQL code generator")
    public void testGenerate() {
        try {
            String wsdl = "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL";
            CodeGenerator.getInstance().generate(wsdl, "/Users/lakshan/Desktop/fgf");
            Assert.assertTrue(true);
        } catch (
    GenerationException e) {
            Assert.fail("Error while generating the code. " + e.getMessage());
        }
    }
}
