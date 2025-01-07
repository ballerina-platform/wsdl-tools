import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap12;

public isolated client class Client {
    final soap12:Client clientEp;

    public isolated function init(string serviceUrl = "https://propertyconnect-i1.synxis.com/interface/ota2010av2/OTA2010A.svc", *soap:ClientConfig config) returns error? {
        soap12:Client soap = check new (serviceUrl, config);
        self.clientEp = soap;
        return;
    }

    remote isolated function descriptiveContentSubmitRequest(DescriptiveContent_SubmitRequestSoapRequest envelope) returns DescriptiveContent_SubmitRequestSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://htng.org/PWSWG/2010/12/DescriptiveContent_SubmitRequest");
        return xmldata:parseAsType(result);
    }
}
