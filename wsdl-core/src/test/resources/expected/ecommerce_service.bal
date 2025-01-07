import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class Client {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "http://example.com/ecommerce/service", *soap:ClientConfig config) returns error? {
        soap11:Client soap = check new (serviceUrl, config);
        self.clientEp = soap;
        return;
    }

    remote isolated function getProduct(GetProductSoapRequest envelope) returns GetProductSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://example.com/ecommerce/GetProduct");
        return xmldata:parseAsType(result);
    }
}
