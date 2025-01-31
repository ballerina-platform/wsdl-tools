import ballerina/data.xmldata;
import ballerina/soap;
import ballerina/soap.soap11;

public isolated client class Client {
    final soap11:Client clientEp;

    public isolated function init(string serviceUrl = "http://example.com/ecommerce/service", *soap:ClientConfig config) returns error? {
        self.clientEp = check new (serviceUrl, config);
    }

    remote isolated function getProduct(GetProductSoapRequest envelope) returns GetProductSoapResponse|error {
        xml result = check self.clientEp->sendReceive(check xmldata:toXml(envelope), "http://example.com/ecommerce/GetProduct");
        return xmldata:parseAsType(result);
    }
}

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetProductHeader record {
};

public type GetProductResponseBody record {
    Product Product?;
};

@xmldata:Name {value: "Envelope"}
public type GetProductSoapResponse record {
    GetProductResponseBody Body;
};

@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetProductRequestBody record {
    ProductId ProductId?;
};

@xmldata:Name {value: "Envelope"}
@xmldata:Namespace {prefix: "soap", uri: "http://schemas.xmlsoap.org/soap/envelope/"}
public type GetProductSoapRequest record {
    GetProductHeader Header?;
    GetProductRequestBody Body;
};
