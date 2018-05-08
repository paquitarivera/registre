package es.caib.regweb3.webapp.controller;

import es.caib.arxiudigital.apirest.ApiArchivoDigital;
import es.caib.arxiudigital.apirest.constantes.Aspectos;
import es.caib.arxiudigital.apirest.constantes.CodigosResultadoPeticion;
import es.caib.arxiudigital.apirest.facade.pojos.Expediente;
import es.caib.arxiudigital.apirest.facade.pojos.FiltroBusquedaFacilExpedientes;
import es.caib.arxiudigital.apirest.facade.pojos.IntervaloFechas;
import es.caib.arxiudigital.apirest.facade.pojos.Nodo;
import es.caib.arxiudigital.apirest.facade.resultados.Resultado;
import es.caib.arxiudigital.apirest.facade.resultados.ResultadoBusqueda;
import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.persistence.ejb.AnexoLocal;
import es.caib.regweb3.persistence.ejb.PluginLocal;
import es.caib.regweb3.persistence.ejb.RegistroEntradaLocal;
import es.caib.regweb3.persistence.ejb.RegistroSalidaLocal;
import es.caib.regweb3.persistence.utils.I18NLogicUtils;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.RegwebUtils;
import es.caib.regweb3.utils.StringUtils;
import es.caib.regweb3.webapp.utils.Mensaje;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.plugins.documentcustody.api.CustodyException;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.arxiudigitalcaib.ArxiuDigitalCAIBDocumentCustodyPlugin;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 16/01/14
 */
@Controller
public class ArxiuController extends BaseController {

    protected final Logger log = Logger.getLogger(getClass());

    @EJB(mappedName = "regweb3/RegistroEntradaEJB/local")
    private RegistroEntradaLocal registroEntradaEjb;

    @EJB(mappedName = "regweb3/RegistroSalidaEJB/local")
    private RegistroSalidaLocal registroSalidaEjb;

    @EJB(mappedName = "regweb3/AnexoEJB/local")
    private AnexoLocal anexoEjb;

    @EJB(mappedName = "regweb3/PluginEJB/local")
    private PluginLocal pluginEjb;


    @RequestMapping(value = "/asociarJustificante/{idExpediente}")
    public String asociarJustificante(@PathVariable String idExpediente,HttpServletRequest request) {

        Entidad entidad = getEntidadActiva(request);

        try {

            log.info("Asociando Justificante del expediente: " + idExpediente);

            ArxiuDigitalCAIBDocumentCustodyPlugin custody = (ArxiuDigitalCAIBDocumentCustodyPlugin) pluginEjb.getPlugin(entidad.getId(), RegwebConstantes.PLUGIN_CUSTODIA_JUSTIFICANTE);

            ApiArchivoDigital apiArxiu = custody.getApiArxiu(null);

            Resultado<Expediente> expedientes = apiArxiu.obtenerExpediente(idExpediente);

            Expediente expediente = expedientes.getElementoDevuelto();

            List<Nodo> nodos = expediente.getChilds();

            if(nodos != null) {

                for (Nodo nodo : nodos) {

                    log.info("Documento: " + nodo.getName().toLowerCase());

                    if (nodo.getName().toLowerCase().endsWith(".pdf")) {

                        RegistroEntrada registroEntrada = null;
                        RegistroSalida registroSalida = null;
                        RegistroDetalle registroDetalle = null;
                        String redirect;

                        // Obtenemos el codigoLibro y el tipoRegistro
                        String tipoRegistro = getTipoRegistro(expediente.getName());
                        String codigoLibro = getCodigoLibro(expediente.getName());
                        String numeroRegistroFormateado = getNumeroRegistroFormateado(expediente);

                        log.info("numeroRegistroFormateado: " + numeroRegistroFormateado);

                        String custodyId = idExpediente + "#" + nodo.getId();
                        Metadata mcsv = custody.getOnlyOneMetadata(custodyId, MetadataConstants.ENI_CSV);
                        log.info("custodyId: " + custodyId);
                        String csv = null;
                        if (mcsv != null) {
                            csv = mcsv.getValue();
                        }

                        if(StringUtils.isNotEmpty(tipoRegistro) && StringUtils.isNotEmpty(codigoLibro)){

                            if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){
                                registroEntrada = registroEntradaEjb.findByNumeroRegistroFormateado(entidad.getCodigoDir3(), numeroRegistroFormateado, codigoLibro);

                                if(registroEntrada != null){
                                    registroDetalle = registroEntrada.getRegistroDetalle();
                                    redirect = "redirect:/registroEntrada/" + registroEntrada.getId() + "/detalle";
                                }else{
                                    Mensaje.saveMessageError(request,"No se ha encontrado el registro de entrada: " + numeroRegistroFormateado);
                                    return  "redirect:/inici";
                                }

                            }else{
                                registroSalida = registroSalidaEjb.findByNumeroRegistroFormateado(entidad.getCodigoDir3(), numeroRegistroFormateado, codigoLibro);

                                if(registroSalida != null){
                                    registroDetalle = registroSalida.getRegistroDetalle();
                                    redirect= "redirect:/registroSalida/" + registroSalida.getId() + "/detalle";
                                }else {
                                    Mensaje.saveMessageError(request,"No se ha encontrado el registro de salida: " + numeroRegistroFormateado);
                                    return  "redirect:/inici";
                                }
                            }

                            // Crea el anexo del justificante firmado
                            AnexoFull anexoFull = new AnexoFull();
                            Anexo anexo = anexoFull.getAnexo();
                            anexo.setTitulo(I18NLogicUtils.tradueix(new Locale(RegwebConstantes.IDIOMA_CATALAN_CODIGO), "justificante.anexo.titulo"));
                            anexo.setValidezDocumento(RegwebConstantes.TIPOVALIDEZDOCUMENTO_ORIGINAL);
                            anexo.setTipoDocumental(tipoDocumentalEjb.findByCodigoEntidad("TD99", entidad.getId()));
                            anexo.setTipoDocumento(RegwebConstantes.TIPO_DOCUMENTO_DOC_ADJUNTO);
                            anexo.setOrigenCiudadanoAdmin(RegwebConstantes.ANEXO_ORIGEN_ADMINISTRACION);
                            anexo.setObservaciones(I18NLogicUtils.tradueix(new Locale(RegwebConstantes.IDIOMA_CATALAN_CODIGO), "justificante.anexo.observaciones"));
                            anexo.setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED);
                            anexo.setJustificante(true);
                            anexo.setSignType("PAdES");
                            anexo.setSignFormat("implicit_enveloped/attached");
                            anexo.setSignProfile("AdES-EPES");
                            anexo.setCustodiaID(custodyId);
                            anexo.setCsv(csv);

                            String fechaCaptura = custody.getOnlyOneMetadata(custodyId, MetadataConstants.ENI_FECHA_INICIO).getValue();
                            anexo.setFechaCaptura(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fechaCaptura.substring(0, 10) +" "+ fechaCaptura.substring(11,23)));


                            anexo.setHash(RegwebUtils.obtenerHash(custody.getSignatureInfo(custodyId).getData()));
                            anexo.setRegistroDetalle(registroDetalle);
                            anexo.setFirmaValida(false);

                            anexoEjb.persist(anexo);

                            Mensaje.saveMessageInfo(request,"Se ha asociado el justificante correctamente");

                            return redirect;

                        }
                    }
                }
            }


        } catch (I18NException e) {
            e.printStackTrace();
            Mensaje.saveMessageError(request,"Error asociando justificante: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.saveMessageError(request,"Error asociando justificante: " + e.getMessage());
        }

        Mensaje.saveMessageError(request,"Faltan datos para obtener el numero de registro");

        return "redirect:/inici";

    }

    @RequestMapping(value = "/arxiu/{serie}/{initialDate}/{endDate}/{onlyCount}/{expedientPattern}")
    public ModelAndView expedientes(@PathVariable String serie, @PathVariable String initialDate,
                                    @PathVariable String endDate,@PathVariable Boolean onlyCount,@PathVariable String expedientPattern,HttpServletRequest request) {

        ModelAndView mav = new ModelAndView("arxiu");

        Entidad entidad = getEntidadActiva(request);

        try {
            ArxiuDigitalCAIBDocumentCustodyPlugin custody = (ArxiuDigitalCAIBDocumentCustodyPlugin) pluginEjb.getPlugin(entidad.getId(), RegwebConstantes.PLUGIN_CUSTODIA_JUSTIFICANTE);

            ApiArchivoDigital apiArxiu = custody.getApiArxiu(null);

            mav.addObject("serie",serie);
            mav.addObject("initialDate",initialDate);
            mav.addObject("endDate",endDate);
            mav.addObject("onlyCount",onlyCount);
            mav.addObject("expedientPattern",expedientPattern);

            //String serie = "S0002";
            //String initialDate = "2018-04-19T00:00:00.000Z";
            //String endDate = "2018-04-25T23:59:59.999Z";
            //boolean onlyCount = false;
            //String expedientPattern = "INNOE33*";

            ResultadoBusqueda<Expediente> result = busquedaEdu(custody,apiArxiu, serie, initialDate, endDate, expedientPattern, onlyCount);

            List<ExpedienteArxiu> expedientes = new ArrayList<ExpedienteArxiu>();

            for (Expediente exp : result.getListaResultado()) {

                log.info("Expediente: " + exp.getName());

                ExpedienteArxiu expedienteArxiu = new ExpedienteArxiu();

                Resultado<Expediente> expediente = apiArxiu.obtenerExpediente(exp.getId());

                List<Nodo> nodos = expediente.getElementoDevuelto().getChilds();

                if(nodos != null){

                    for (Nodo nodo : nodos) {
                        System.out.println("       >> NODO: " + nodo.getName() + " (" + nodo.getId() + ")");
                        if (nodo.getName().toLowerCase().endsWith(".pdf")) {

                            String custodyId = exp.getId() + "#" + nodo.getId();

                            System.err.println("       >> ID CUST: " + custodyId);

                            Metadata mcsv = custody.getOnlyOneMetadata(custodyId, MetadataConstants.ENI_CSV);

                            /*String csv = null;
                            if (mcsv != null) {
                                csv = mcsv.getValue();
                            }*/

                            String tipoRegistro = getTipoRegistro(exp.getName());
                            String codigoLibro = getCodigoLibro(exp.getName());

                            expedienteArxiu.setId(exp.getId());
                            expedienteArxiu.setName(exp.getName());
                            expedienteArxiu.setCustodyId(custodyId);
                            expedienteArxiu.setTipoRegistro(tipoRegistro);
                            expedienteArxiu.setCodigoLibro(codigoLibro);
                            expedienteArxiu.setNumeroRegistroFormateado(getNumeroRegistroFormateado(exp));

                            if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){
                                RegistroEntrada registroEntrada = registroEntradaEjb.findByNumeroRegistroFormateado(entidad.getCodigoDir3(), expedienteArxiu.getNumeroRegistroFormateado(), codigoLibro);

                                if(registroEntrada != null){
                                    expedienteArxiu.setJustificante(registroEntrada.getRegistroDetalle().getTieneJustificante());
                                }

                            }else if(tipoRegistro.equals(RegwebConstantes.REGISTRO_SALIDA_ESCRITO)){
                                RegistroSalida registroSalida = registroSalidaEjb.findByNumeroRegistroFormateado(entidad.getCodigoDir3(), expedienteArxiu.getNumeroRegistroFormateado(), codigoLibro);

                                if(registroSalida != null){
                                    expedienteArxiu.setJustificante(registroSalida.getRegistroDetalle().getTieneJustificante());
                                }
                            }



                            //expedienteArxiu.setCsv(csv);

                            expedientes.add(expedienteArxiu);

                        }
                    }
                }
            }

            mav.addObject("expedientes",expedientes);

        } catch (I18NException e) {
            e.printStackTrace();
        } catch (CustodyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mav;
    }

    /**
     *
     * @param nombreExpediente
     * @return
     */
    private String getCodigoLibro(String nombreExpediente){

        String tipoRegistro = getTipoRegistro(nombreExpediente);

        if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){

            return nombreExpediente.substring(0,nombreExpediente.indexOf("E"));

        }else if(tipoRegistro.equals(RegwebConstantes.REGISTRO_SALIDA_ESCRITO)){

            return nombreExpediente.substring(0,nombreExpediente.indexOf("S"));

        }

        return null;

    }

    /**
     *
     * @param nombreExpediente
     * @return
     */
    private String getTipoRegistro(String nombreExpediente){

        if(nombreExpediente.lastIndexOf("E") != -1){
            return RegwebConstantes.REGISTRO_ENTRADA_ESCRITO;
        }else if(nombreExpediente.lastIndexOf("S")!= -1){
            return RegwebConstantes.REGISTRO_SALIDA_ESCRITO;
        }

        return null;

    }

    /**
     *
     * @param expediente
     * @return
     */
    private String getNumeroRegistro(Expediente expediente){

        String codigoLibro = getCodigoLibro(expediente.getName());

        return expediente.getName().substring(codigoLibro.length()+1, expediente.getName().indexOf("_"));
    }


    private String getNumeroRegistroFormateado(Expediente expediente){

        String codigoLibro = getCodigoLibro(expediente.getName());
        String tipoRegistro = getTipoRegistro(expediente.getName());
        String numeroRegistro = getNumeroRegistro(expediente);

        return codigoLibro + tipoRegistro.substring(0, 1) + numeroRegistro+"/"+expediente.getName().substring(expediente.getName().length() - 4);
    }


    private static void busquedaFacilExpedientes(IDocumentCustodyPlugin documentCustodyPlugin) throws Exception {


        ArxiuDigitalCAIBDocumentCustodyPlugin plugin;
        plugin = (ArxiuDigitalCAIBDocumentCustodyPlugin) documentCustodyPlugin;

        ApiArchivoDigital apiArxiu = plugin.getApiArxiu(null);


        String serie = "S0002";
        String initialDate = "2018-04-19T00:00:00.000Z";
        String endDate = "2018-04-25T23:59:59.999Z";
        boolean onlyCount = false;
        String expedientPattern = "INNOE33*";
        busquedaFacilExpedientes(documentCustodyPlugin,apiArxiu, serie, initialDate, endDate, expedientPattern, onlyCount);
    }

    private static ResultadoBusqueda<Expediente> busquedaEdu(IDocumentCustodyPlugin documentCustodyPlugin,
                                                             ApiArchivoDigital api, String serie,
                                                             String initialDate, String endDate, String expedientPattern, boolean onlyCount) throws Exception{

        FiltroBusquedaFacilExpedientes filtrosRequeridos = new FiltroBusquedaFacilExpedientes();

        filtrosRequeridos.setName(expedientPattern);
        filtrosRequeridos.setAppName("REGWEB3");
        filtrosRequeridos.setDocSeries(serie);
        IntervaloFechas ife = new IntervaloFechas();
        // yyyy-MM-dd'T'HH:mm:ss.sss'Z'
        ife.setInitialDate(initialDate);
        ife.setEndDate(endDate);
        filtrosRequeridos.setClosingDate(ife);

        ResultadoBusqueda<Expediente> res = api.busquedaFacilExpedientes(filtrosRequeridos,
                null, 0);

        return res;

    }

    public static void busquedaFacilExpedientes(IDocumentCustodyPlugin documentCustodyPlugin,
                                                ApiArchivoDigital api, String serie,
                                                String initialDate, String endDate, String expedientPattern, boolean onlyCount) throws Exception {

        FiltroBusquedaFacilExpedientes filtrosRequeridos = new FiltroBusquedaFacilExpedientes();
        // filtrosRequeridos.setName("L15-E-25021_2017");
        // filtrosRequeridos.setAppName("REGWEB");
        // filtrosRequeridos.setDocSeries("A57");

        // ES_OFI44556677_2017_9agsidmemadig23m9nkpoq2ebfvdte

        // ES_OFI44556677_2017_EXP_ps6s1llujptc0f4if2pldaenbs12fj

        // filtrosRequeridos.setEniId("*_ps6s1llujptc0f4if2pldaenbs12fj");
        /*
         * String[] expes = { //"INNOE98_2018",
         * "INNOE102_2018","INNOE103_2018","INNOE104_2018","INNOE105_2018" "INNOE33*"
         *
         * };
         *
         * for (int i = 0; i < expes.length; i++) {
         */
        //String expdientPattern = null; // "INNOE33*"

        filtrosRequeridos.setName(expedientPattern);
        filtrosRequeridos.setAppName("REGWEB3");
        filtrosRequeridos.setDocSeries(serie);
        IntervaloFechas ife = new IntervaloFechas();
        // yyyy-MM-dd'T'HH:mm:ss.sss'Z'
        ife.setInitialDate(initialDate);
        ife.setEndDate(endDate);
        filtrosRequeridos.setClosingDate(ife);

        int total = 0;
        int pagina = 0;
        int paginaresultat = 1;
        int numitems = 0;

        Set<String> llistatTotal = new HashSet<String>();

        do {

            ResultadoBusqueda<Expediente> res = api.busquedaFacilExpedientes(filtrosRequeridos,
                    null, pagina);

            pagina++;

            if (hiHaErrorEnCerca(res.getCodigoResultado())) {
                throw new Exception("Error Consultant si Expedient existeix: "
                        + res.getCodigoResultado() + "-" + res.getMsjResultado());
            }

            List<Expediente> llista2 = res.getListaResultado();
            total = res.getNumeroTotalResultados();
            System.out.println();
            System.out.println();
            System.out.println(" -----------------------------------------------------");
            System.out.println(" TOTAL REGISTRES: " + total);
            //System.out.println(" NUMITEMS " + numitems);
            System.out.println(" PAGINA BUCLE " + pagina);
            System.out.println(" PAGINA RESULTAT " + res.getNumeroPagina());
            //System.out.println();
            //System.out.println();

            if (onlyCount) {
                return;
            }

            paginaresultat = res.getNumeroPagina();

            if (numitems >= total) {

                System.out.println("SORTIM !!!!!!");
                break;
            }

            // if (true) return;

            if (llista2 == null || llista2.size() == 0) {
                System.out.println("Retornada llista buida. Sortim !!!! ");
                break;
            }
            numitems = numitems + llista2.size();
            System.out.println(" SUMATORI:" + numitems);
            System.out.println(" ITEMS RECUPERATS: " + llista2.size());

            for (Expediente expedienteItem : llista2) {
                System.out.println(" + Expedient trobat: " + expedienteItem.getName() + "("
                        + expedienteItem.getId() + ")");
                llistatTotal.add(expedienteItem.getId());
/** **/
                if (expedienteItem.getAspects() != null) {
                    System.out.print("        - Aspectos: ");
                    for (Aspectos asp : expedienteItem.getAspects()) {
                        System.out.print(asp.getValue() + ", ");

                    }

                    System.out.println();

                    // / XYZ ZZZZ AQUI CODI REGISTRE A PARTIR DE CODI

                    boolean registreTeJustificant = false;

                    if (!registreTeJustificant) {

                        Resultado<Expediente> expedient = api.obtenerExpediente(expedienteItem.getId());

                        if (hiHaErrorEnCerca(expedient.getCodigoResultado())) {
                            throw new CustodyException(
                                    "Error intentant obtenir informació de l'expedient amb uuid "
                                            + expedienteItem.getId() + ": " + expedient.getCodigoResultado() + "-"
                                            + expedient.getMsjResultado());
                        }

                        List<Nodo> nodos = expedient.getElementoDevuelto().getChilds();

                        if (nodos != null) {
                            for (Nodo nodo : nodos) {
                                System.out.println("       >> NODO: " + nodo.getName() + " (" + nodo.getId()
                                        + ")");
                                if (nodo.getName().toLowerCase().endsWith(".pdf")) {


                                    String idCust = expedienteItem.getId() + "#" + nodo.getId();

                                    System.err.println("       >> ID CUST: " + idCust);

                                    // Generam la Custòdia per tenir el CSV
                                    //Map<String, Object> custodyParameters = getCustodyParameters(registro, anexo, anexoFull, usuarioEntidad);
                                    //custodyID = documentCustodyPlugin.reserveCustodyID(custodyParameters);
                                    Metadata mcsv = documentCustodyPlugin.getOnlyOneMetadata(idCust, MetadataConstants.ENI_CSV);


                                    String csv = null;
                                    if (mcsv != null) {
                                        csv = mcsv.getValue();
                                    }
                                    System.err.println("       >> ID CSV: " + csv );

                                        // / XYZ ZZZZ AQUI FICAR DINS BBDD DE REGISTRE !!!!!


                                }

                            }
                        }

                        System.out.println();
                    }

                }
                /**       **/

            } // final for

        } while (pagina < paginaresultat);


        System.out.println("Processats = " + llistatTotal.size());


    }

    public static boolean hiHaErrorEnCerca(String code) {
        return !CodigosResultadoPeticion.PETICION_CORRECTA.equals(code)
                && !CodigosResultadoPeticion.LISTA_VACIA.equals(code);
    }


}