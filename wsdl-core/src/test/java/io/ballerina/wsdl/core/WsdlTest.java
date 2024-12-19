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

package io.ballerina.wsdl.core;

import io.ballerina.wsdl.cli.WsdlCmd;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class WsdlTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String WSDL_DIR = "wsdl";
    private static final String EXPECTED_DIR = "expected";

    private static Stream<Object[]> provideTestPaths() {
        return Stream.of(
                new Object[] {"calculator.xml", "calculator.bal", "http://tempuri.org/Multiply"},
                new Object[] {"phone_verify.wsdl", "phone_verify.bal", "http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber"},
                new Object[] {"OTA2010A.svc.wsdl", "OTA2010A.svc.bal", "http://htng.org/PWSWG/2010/12/DescriptiveContent_SubmitRequest"}
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestPaths")
    void testWsdlToRecord(String xmlFilePath, String balFilePath, String operationAction) throws Exception {
        validate(RES_DIR.resolve(WSDL_DIR).resolve(xmlFilePath), RES_DIR.resolve(EXPECTED_DIR).resolve(balFilePath),
                operationAction);
    }

    private void validate(Path sample, Path expected, String operationAction) throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(sample), "", operationAction);
        String result = response.getClientSource().content();
        String expectedValue = Files.readString(expected);
        Assert.assertEquals(result, expectedValue);
    }
}
