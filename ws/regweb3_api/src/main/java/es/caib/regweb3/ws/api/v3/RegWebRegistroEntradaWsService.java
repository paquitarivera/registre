package es.caib.regweb3.ws.api.v3;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class was generated by Apache CXF 2.6.4
 * 2019-05-13T10:13:58.832+02:00
 * Generated source version: 2.6.4
 * 
 */
@WebServiceClient(name = "RegWebRegistroEntradaWsService", 
                  wsdlLocation = "http://localhost:8080/regweb3/ws/v3/RegWebRegistroEntrada?wsdl",
                  targetNamespace = "http://impl.v3.ws.regweb3.caib.es/") 
public class RegWebRegistroEntradaWsService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://impl.v3.ws.regweb3.caib.es/", "RegWebRegistroEntradaWsService");
    public final static QName RegWebRegistroEntradaWs = new QName("http://impl.v3.ws.regweb3.caib.es/", "RegWebRegistroEntradaWs");
    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/regweb3/ws/v3/RegWebRegistroEntrada?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(RegWebRegistroEntradaWsService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://localhost:8080/regweb3/ws/v3/RegWebRegistroEntrada?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public RegWebRegistroEntradaWsService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public RegWebRegistroEntradaWsService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RegWebRegistroEntradaWsService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public RegWebRegistroEntradaWsService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public RegWebRegistroEntradaWsService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public RegWebRegistroEntradaWsService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns RegWebRegistroEntradaWs
     */
    @WebEndpoint(name = "RegWebRegistroEntradaWs")
    public RegWebRegistroEntradaWs getRegWebRegistroEntradaWs() {
        return super.getPort(RegWebRegistroEntradaWs, RegWebRegistroEntradaWs.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RegWebRegistroEntradaWs
     */
    @WebEndpoint(name = "RegWebRegistroEntradaWs")
    public RegWebRegistroEntradaWs getRegWebRegistroEntradaWs(WebServiceFeature... features) {
        return super.getPort(RegWebRegistroEntradaWs, RegWebRegistroEntradaWs.class, features);
    }

}
