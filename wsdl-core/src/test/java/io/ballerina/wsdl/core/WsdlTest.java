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
import io.ballerina.wsdl.core.diagnostic.WsdlToBallerinaDiagnostic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class WsdlTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String WSDL_DIR = "wsdl";
    private static final String EXPECTED_DIR = "expected";

    private static Stream<Object[]> provideTestPaths() {
        return Stream.of(
                new Object[] {"calculator.xml", "calculator.bal", "http://tempuri.org/Multiply"},
                new Object[] {"calculator.xml", "calculator_with_multiple_operations.bal",
                        "http://tempuri.org/Multiply, http://tempuri.org/Add"},
                new Object[] {"phone_verify.wsdl", "phone_verify.bal",
                        "http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber"},
                new Object[] {"OTA2010A.svc.wsdl", "OTA2010A.svc.bal",
                        "http://htng.org/PWSWG/2010/12/DescriptiveContent_SubmitRequest"},
                new Object[] {"ecommerce_service.wsdl", "ecommerce_service.bal",
                        "http://example.com/ecommerce/GetProduct"},
                new Object[] {"global_weather.wsdl", "global_weather.bal", "http://www.webserviceX.NET/GetWeather"},
                new Object[] {"reservation_service.wsdl", "reservation_service.bal",
                        "http://www.opentravel.org/OTA/2003/05/OTA2010A.ReservationService/CancelReservation"}
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestPaths")
    void testWsdlToRecord(String xmlFilePath, String balFilePath, String operationActions) throws Exception {
        validate(RES_DIR.resolve(WSDL_DIR).resolve(xmlFilePath), RES_DIR.resolve(EXPECTED_DIR).resolve(balFilePath),
                operationActions.split(","));
    }

    private void validate(Path sample, Path expected, String[] operationActions) throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(sample),
                "", operationActions);
        String result = response.getClientSource().content();
        String expectedValue = Files.readString(expected);
        Assert.assertEquals(result, expectedValue);
    }

    @org.junit.jupiter.api.Test
    void testParserError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_wsdl_spec.wsdl")), "", new String[]{});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to parse the WSDL content. WSDLException: " +
                "faultCode=PARSER_ERROR: Problem parsing - WSDL Document -.: org.xml.sax.SAXParseException: " +
                "The element type \"wsdl:types\" must be terminated by the matching end-tag \"</wsdl:types>\".";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testEmptySchemaError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "empty_schema_file.wsdl")), "", new String[]{"http://tempuri.org/multiply"});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "Could not find <wsdl:types> in the file";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testInvalidOperationError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String invalidOperation = "http://tempuri.org/multiply";
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_operation.wsdl")), "", new String[]{invalidOperation});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "WSDL operation is not found: " + invalidOperation;
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testInvalidMultipleOperationsError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String[] invalidOperation = ("http://tempuri.org/Multiply, http://tempuri.org/add").split(",");
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_operation.wsdl")), "", invalidOperation);
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "WSDL operation is not found: http://tempuri.org/add";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testOperationInputError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String invalidOperation = "http://tempuri.org/Add";
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_operation_input.wsdl")), "", new String[]{invalidOperation});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to parse the WSDL content. " +
                "WSDLException (at /wsdl:definitions/wsdl:binding[1]/wsdl:operation[1]/wsdl:input): " +
                "faultCode=INVALID_WSDL: Encountered illegal extension attribute message. " +
                "Extension attributes must be in a namespace other than WSDLs.";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testInvalidHeaderError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String invalidOperation = "http://tempuri.org/Add";
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "empty_header.wsdl")), "", new String[]{invalidOperation});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "Message element is missing in the <soap:header> for \"Security\" element";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testInvalidBindingInputError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String invalidOperation = "http://tempuri.org/Add";
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_binding_input.wsdl")), "", new String[]{invalidOperation});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "Invalid binding operation: Binding input is null.";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testInvalidBindingOutputError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        String invalidOperation = "http://tempuri.org/Add";
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "invalid_binding_output.wsdl")), "", new String[]{invalidOperation});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "Invalid binding operation: Binding output is null.";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }

    @org.junit.jupiter.api.Test
    void testEmptyElementError() throws Exception {
        WsdlCmd wsdlCmd = new WsdlCmd();
        WsdlToBallerinaResponse response = wsdlCmd.wsdlToBallerina(String.valueOf(RES_DIR.resolve(WSDL_DIR).resolve(
                "empty_message_element.wsdl")), "", new String[]{});
        List<WsdlToBallerinaDiagnostic> result = response.getDiagnostics();
        String expectedError = "[ERROR] Failed to generate files from the source. " +
                "Message element is missing in the input of the operation: multiply";
        Assert.assertEquals(result.get(0).toString(), expectedError);
    }
}
