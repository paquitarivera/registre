
/*
 * 
 */

package es.caib.regweb.ws.api.v3;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.12-patch-04
 * Mon Mar 09 09:30:00 CET 2015
 * Generated source version: 2.2.12-patch-04
 * 
 */


@WebServiceClient(name = "RegWebRegistroSalidaWsService", 
                  wsdlLocation = "http://localhost:8080/regweb/ws/v3/RegWebRegistroSalida?wsdl",
                  targetNamespace = "http://impl.v3.ws.regweb.caib.es/") 
public class RegWebRegistroSalidaWsService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://impl.v3.ws.regweb.caib.es/", "RegWebRegistroSalidaWsService");
    public final static QName RegWebRegistroSalidaWs = new QName("http://impl.v3.ws.regweb.caib.es/", "RegWebRegistroSalidaWs");
    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/regweb/ws/v3/RegWebRegistroSalida?wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from http://localhost:8080/regweb/ws/v3/RegWebRegistroSalida?wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public RegWebRegistroSalidaWsService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public RegWebRegistroSalidaWsService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RegWebRegistroSalidaWsService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     * 
     * @return
     *     returns RegWebRegistroSalidaWs
     */
    @WebEndpoint(name = "RegWebRegistroSalidaWs")
    public RegWebRegistroSalidaWs getRegWebRegistroSalidaWs() {
        return super.getPort(RegWebRegistroSalidaWs, RegWebRegistroSalidaWs.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RegWebRegistroSalidaWs
     */
    @WebEndpoint(name = "RegWebRegistroSalidaWs")
    public RegWebRegistroSalidaWs getRegWebRegistroSalidaWs(WebServiceFeature... features) {
        return super.getPort(RegWebRegistroSalidaWs, RegWebRegistroSalidaWs.class, features);
    }

}
