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

package io.ballerina.wsdl.core.handler;

import io.ballerina.wsdl.core.generator.xsdtorecord.balir.Field;
import io.ballerina.wsdl.core.handler.model.WsdlPart;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the processing of individual WSDL parts, converting them into Ballerina fields.
 *
 * @since 0.1.0
 */
public class PartHandler {

    List<Field> generateFields(WsdlPart wsdlPart) {
        XmlSchema schema = SchemaHandler.getInstance().getSchema(wsdlPart.getElementNsUri());
        if (schema == null) {
            return new ArrayList<>();
        }
        XmlSchemaElement schemaElement = schema.getElementByName(wsdlPart.getElementName());
        if (schemaElement == null) {
            return new ArrayList<>();
        }
        SchemaElementParser schemaParser = new SchemaElementParser(schemaElement);
        return schemaParser.parseRootElement();
    }
}
