import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class PhoneVerifySoapClient {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "http://ws.cdyne.com/phoneverify/phoneverify.asmx", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function checkPhoneNumber(CheckPhoneNumberPhoneVerifySoapSoapRequest envelope) returns CheckPhoneNumberPhoneVerifySoapSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://ws.cdyne.com/PhoneVerify/query/CheckPhoneNumber");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CheckPhoneNumberPhoneVerifySoapHeader record {
};

public type CheckPhoneNumberPhoneVerifySoapResponseBody record {
    CheckPhoneNumberResponse CheckPhoneNumberResponse?;
};

@xmldata:Name {value: "Envelope"}
public type CheckPhoneNumberPhoneVerifySoapSoapResponse record {
    CheckPhoneNumberPhoneVerifySoapResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CheckPhoneNumberPhoneVerifySoapRequestBody record {
    CheckPhoneNumber CheckPhoneNumber?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CheckPhoneNumberPhoneVerifySoapSoapRequest record {
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    CheckPhoneNumberPhoneVerifySoapHeader Header?;
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    CheckPhoneNumberPhoneVerifySoapRequestBody Body;
};
