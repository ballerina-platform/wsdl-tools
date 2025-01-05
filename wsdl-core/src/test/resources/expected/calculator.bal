import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap12;

public isolated client class Client {
    final soap12:Client clientEp;

    public isolated function init(string serviceUrl, *soap:ClientConfig config) returns error? {
        soap12:Client soap = check new (serviceUrl, config);
        self.clientEp = soap;
        return;
    }

    remote isolated function multiply(MultiplySoapRequest envelope) returns MultiplySoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://tempuri.org/Multiply");
        return xmldata:parseAsType(result);
    }
}
