import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap12;

public isolated client class Client {
    final soap12:Client clientEp;

    public isolated function init(string serviceUrl = "https://propertyconnect-i1.synxis.com/interface/ota2010av2/OTA2010A.svc", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function descriptiveContentSubmitRequest(DescriptiveContent_SubmitRequestSoapRequest envelope) returns DescriptiveContent_SubmitRequestSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://htng.org/PWSWG/2010/12/DescriptiveContent_SubmitRequest");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type DescriptiveContent_SubmitRequestHeader record {
    @xmldata:Namespace {uri: "http://htng.org/PWSWG/2007/02/AsyncHeaders"}
    ReplyTo ReplyTo?;
    @xmldata:Namespace {uri: "http://htng.org/PWSWG/2007/02/AsyncHeaders"}
    RelatesToCorrelationID RelatesToCorrelationID?;
    @xmldata:Namespace {uri: "http://htng.org/PWSWG/2007/02/AsyncHeaders"}
    CorrelationID CorrelationID?;
    @xmldata:Namespace {uri: "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd"}
    Security Security?;
    @xmldata:Namespace {uri: "http://htng.org/1.3/Header/"}
    TimeStamp TimeStamp?;
};

public type DescriptiveContent_SubmitRequestResponseBody record {
    OTA_HotelDescriptiveContentNotifRS OTA_HotelDescriptiveContentNotifRS?;
};

@xmldata:Name {value: "Envelope"}
public type DescriptiveContent_SubmitRequestSoapResponse record {
    DescriptiveContent_SubmitRequestResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type DescriptiveContent_SubmitRequestRequestBody record {
    OTA_HotelDescriptiveContentNotifRQ OTA_HotelDescriptiveContentNotifRQ?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type DescriptiveContent_SubmitRequestSoapRequest record {
    DescriptiveContent_SubmitRequestHeader Header?;
    DescriptiveContent_SubmitRequestRequestBody Body;
};
