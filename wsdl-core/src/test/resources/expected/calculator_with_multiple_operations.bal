import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap12;

public isolated client class CalculatorSoap12Client {
    final soap12:Client clientEp;

    public isolated function init(string serviceUrl = "http://www.dneonline.com/calculator.asmx", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function multiply(MultiplyCalculatorSoap12SoapRequest envelope) returns MultiplyCalculatorSoap12SoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://tempuri.org/Multiply");
        return xmldata:parseAsType(result);
    }

    remote isolated function add(AddCalculatorSoap12SoapRequest envelope) returns AddCalculatorSoap12SoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://tempuri.org/Add");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type AddCalculatorSoap12Header record {
};

public type AddCalculatorSoap12ResponseBody record {
    AddResponse AddResponse?;
};

@xmldata:Name {value: "Envelope"}
public type AddCalculatorSoap12SoapResponse record {
    AddCalculatorSoap12ResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type AddCalculatorSoap12RequestBody record {
    Add Add?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type AddCalculatorSoap12SoapRequest record {
    AddCalculatorSoap12Header Header?;
    AddCalculatorSoap12RequestBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type MultiplyCalculatorSoap12Header record {
};

public type MultiplyCalculatorSoap12ResponseBody record {
    MultiplyResponse MultiplyResponse?;
};

@xmldata:Name {value: "Envelope"}
public type MultiplyCalculatorSoap12SoapResponse record {
    MultiplyCalculatorSoap12ResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type MultiplyCalculatorSoap12RequestBody record {
    Multiply Multiply?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://www.w3.org/2003/05/soap-envelope"}
public type MultiplyCalculatorSoap12SoapRequest record {
    MultiplyCalculatorSoap12Header Header?;
    MultiplyCalculatorSoap12RequestBody Body;
};
