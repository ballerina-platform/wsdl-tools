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

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WsdlTest {
    protected final Path resourceDir = Paths.get("src/test/resources/").toAbsolutePath();

    @Test
    public void testExecute() throws IOException {
        Path wsdlFile = resourceDir.resolve(Paths.get("files", "OTA2010A.svc.wsdl"));
        WsdlCmd wsdlCmd = new WsdlCmd();
        List<String> inputs = new ArrayList<>();
        wsdlCmd.wsdlToBallerina(String.valueOf(wsdlFile), "http://htng.org/PWSWG/2010/12/Inventory_SubmitRequest");
    }
}
