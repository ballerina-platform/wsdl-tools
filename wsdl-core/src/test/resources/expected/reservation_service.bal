import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class BasicHttpBindingOTA2010AReservationService1Client {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "https://ota.dollar.com/OTA/2010a/ReservationService.svc", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function cancelReservation(CancelReservationBasicHttpBindingOTA2010AReservationService1SoapRequest envelope) returns CancelReservationBasicHttpBindingOTA2010AReservationService1SoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://www.opentravel.org/OTA/2003/05/OTA2010A.ReservationService/CancelReservation");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CancelReservationBasicHttpBindingOTA2010AReservationService1Header record {
};

public type CancelReservationBasicHttpBindingOTA2010AReservationService1ResponseBody record {
    CancelReservationResponse CancelReservationResponse?;
};

@xmldata:Name {value: "Envelope"}
public type CancelReservationBasicHttpBindingOTA2010AReservationService1SoapResponse record {
    CancelReservationBasicHttpBindingOTA2010AReservationService1ResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CancelReservationBasicHttpBindingOTA2010AReservationService1RequestBody record {
    CancelReservation CancelReservation?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type CancelReservationBasicHttpBindingOTA2010AReservationService1SoapRequest record {
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    CancelReservationBasicHttpBindingOTA2010AReservationService1Header Header?;
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    CancelReservationBasicHttpBindingOTA2010AReservationService1RequestBody Body;
};
