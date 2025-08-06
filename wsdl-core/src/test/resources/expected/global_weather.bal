import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class GlobalWeatherSoapClient {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "http://www.webservicex.com/globalweather.asmx", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function getWeather(GetWeatherGlobalWeatherSoapSoapRequest envelope) returns GetWeatherGlobalWeatherSoapSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://www.webserviceX.NET/GetWeather");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetWeatherGlobalWeatherSoapHeader record {
};

public type GetWeatherGlobalWeatherSoapResponseBody record {
    GetWeatherResponse GetWeatherResponse?;
};

@xmldata:Name {value: "Envelope"}
public type GetWeatherGlobalWeatherSoapSoapResponse record {
    GetWeatherGlobalWeatherSoapResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetWeatherGlobalWeatherSoapRequestBody record {
    GetWeather GetWeather?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetWeatherGlobalWeatherSoapSoapRequest record {
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    GetWeatherGlobalWeatherSoapHeader Header?;
    @xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
    GetWeatherGlobalWeatherSoapRequestBody Body;
};
