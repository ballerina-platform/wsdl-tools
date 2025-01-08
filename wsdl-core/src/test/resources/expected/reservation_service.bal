import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class Client {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "https://ota.dollar.com/OTA/2010a/ReservationService.svc", *soap:ClientConfig config) returns error? {
        soap11:Client soap = check new (serviceUrl, config);
        self.clientEp = soap;
        return;
    }

    remote isolated function cancelReservation(CancelReservationSoapRequest envelope) returns CancelReservationSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://www.opentravel.org/OTA/2003/05/OTA2010A.ReservationService/CancelReservation");
        return xmldata:parseAsType(result);
    }
}
