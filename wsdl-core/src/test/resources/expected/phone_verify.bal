import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class Client {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl, *soap:ClientConfig config) returns error? {
        soap11:Client soap = check new (serviceUrl, config);
        self.clientEp = soap;
        return;
    }

    remote isolated function CheckPhoneNumber(SoapRequest envelope) returns SoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber");
        return xmldata:parseAsType(result);
    }
}
