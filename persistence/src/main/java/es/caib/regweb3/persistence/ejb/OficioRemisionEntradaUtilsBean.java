package es.caib.regweb3.persistence.ejb;


import es.caib.dir3caib.ws.api.oficina.Dir3CaibObtenerOficinasWs;
import es.caib.dir3caib.ws.api.oficina.OficinaTF;
import es.caib.dir3caib.ws.api.unidad.Dir3CaibObtenerUnidadesWs;
import es.caib.dir3caib.ws.api.unidad.UnidadTF;
import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.model.utils.OficioPendienteLlegada;
import es.caib.regweb3.persistence.utils.Oficio;
import es.caib.regweb3.persistence.utils.OficiosRemisionOrganismo;
import es.caib.regweb3.persistence.utils.Paginacion;
import es.caib.regweb3.persistence.utils.PropiedadGlobalUtil;
import es.caib.regweb3.utils.Configuracio;
import es.caib.regweb3.utils.Dir3CaibUtils;
import es.caib.regweb3.utils.RegwebConstantes;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.hibernate.Session;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * @author anadal (Convertir en EJB)
 *         Date: 16/01/14
 */
@Stateless(name = "OficioRemisionEntradaUtilsEJB")
@SecurityDomain("seycon")
public class OficioRemisionEntradaUtilsBean implements OficioRemisionEntradaUtilsLocal {

    public final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName = "regweb3")
    private EntityManager em;

    @EJB(mappedName = "regweb3/RegistroEntradaEJB/local")
    private RegistroEntradaLocal registroEntradaEjb;

    @EJB(mappedName = "regweb3/OficioRemisionEJB/local")
    private OficioRemisionLocal oficioRemisionEjb;

    @EJB(mappedName = "regweb3/OrganismoEJB/local")
    private OrganismoLocal organismoEjb;

    @EJB(mappedName = "regweb3/LibroEJB/local")
    private LibroLocal libroEjb;

    @EJB(mappedName = "regweb3/TrazabilidadEJB/local")
    private TrazabilidadLocal trazabilidadEjb;

    @EJB(name = "OficinaEJB")
    private OficinaLocal oficinaEjb;

    @EJB(name = "CatEstadoEntidadEJB")
    private CatEstadoEntidadLocal catEstadoEntidadEjb;

    @EJB(mappedName = "regweb3/JustificanteEJB/local")
    private JustificanteLocal justificanteEjb;



    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Organismo> organismosEntradaPendientesRemision(Long idOficina, List<Libro> libros, Set<Long> organismos, Integer total) throws Exception {

        List<Organismo> organismosDestino =  new ArrayList<Organismo>();

        // Si el array de organismos está vacío, no incluimos la condición.
        String organismosWhere = "";
        if (organismos.size() > 0) {
            organismosWhere = " and re.destino.id not in (:organismos) ";
        }

        // Obtenemos los Organismos destinatarios PROPIOS que tiene Oficios de Remision pendientes de tramitar
        Query q;
        q = em.createQuery("Select distinct re.destino.codigo, re.destino.denominacion from RegistroEntrada as re where " +
                "re.estado = :valido and re.oficina.id = :idOficina and re.libro in (:libros) and " +
                "re.destino != null and re.destino.estado.codigoEstadoEntidad = :vigente " + organismosWhere);

        // Parámetros
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q.setParameter("vigente", RegwebConstantes.ESTADO_ENTIDAD_VIGENTE);
        q.setParameter("idOficina", idOficina);
        q.setParameter("libros", libros);
        if (organismos.size() > 0) {
            q.setParameter("organismos", organismos);
        }

        if(total != null){
            q.setMaxResults(total);
        }

        List<Object[]> organismosInternos = q.getResultList();
        for (Object[] organismoInterno : organismosInternos){
            Organismo organismo = new Organismo(null,(String) organismoInterno[0], (String) organismoInterno[1]);

            organismosDestino.add(organismo);
        }

        // Obtenemos los Organismos destinatarios EXTERNOS que tiene Oficios de Remision pendientes de tramitar
        Query q1;
        q1 = em.createQuery("Select distinct re.destinoExternoCodigo, re.destinoExternoDenominacion from RegistroEntrada as re where " +
                "re.estado = :valido and re.oficina.id = :idOficina and re.libro in (:libros) and " +
                "re.destino is null ");

        // Parámetros
        q1.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q1.setParameter("idOficina", idOficina);
        q1.setParameter("libros", libros);

        if(total != null){
            q1.setMaxResults(total);
        }

        List<Object[]> organismosExternos = q1.getResultList();

        for (Object[] organismoExterno : organismosExternos){
            Organismo organismo = new Organismo(null,(String) organismoExterno[0], (String) organismoExterno[1]);

            // Cercam si el destí extern ja existeix com a intern, per no duplicar-los a la llista (EPD creada com Entitat)
            Boolean existeix = false;
            for(Organismo organ : organismosDestino) {
                if(organismo.getCodigo().equals(organ.getCodigo())) {
                    existeix= true;
                    break;
                }
            }
            if(!existeix){
                organismosDestino.add(organismo);
            }

        }

        return organismosDestino;

    }

    @Override
    public Long oficiosEntradaPendientesRemisionCount(Long idOficina, List<Libro> libros, Set<Long> organismos) throws Exception {

        Long total;

        // Si el array de organismos está vacío, no incluimos la condición.
        String organismosWhere = "";
        if (organismos.size() > 0) {
            organismosWhere = "and re.destino.id not in (:organismos)";
        }

        // Total oficios internos
        Query q;
        q = em.createQuery("Select count(re.id) from RegistroEntrada as re where " +
                "re.estado = :valido and re.oficina.id = :idOficina and re.libro in (:libros) and " +
                "re.destino != null and re.destino.estado.codigoEstadoEntidad = :vigente " + organismosWhere);

        // Parámetros
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q.setParameter("vigente", RegwebConstantes.ESTADO_ENTIDAD_VIGENTE);
        q.setParameter("idOficina", idOficina);
        q.setParameter("libros", libros);

        if (organismos.size() > 0) {
            q.setParameter("organismos", organismos);
        }

        total = (Long) q.getSingleResult();

        // Total oficios externos
        Query q1;
        q1 = em.createQuery("Select count(re.id) from RegistroEntrada as re where " +
                "re.estado = :valido and re.oficina.id = :idOficina and re.libro in (:libros) and " +
                "re.destino is null ");

        q1.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q1.setParameter("idOficina", idOficina);
        q1.setParameter("libros", libros);

        total = total +  (Long) q1.getSingleResult();

        return total;
    }



    @Override
    @SuppressWarnings(value = "unchecked")
    public OficiosRemisionOrganismo oficiosEntradaPendientesRemision(Integer pageNumber, final Integer resultsPerPage, Integer any, Oficina oficinaActiva, Long idLibro, String codigoOrganismo, Set<Long> organismos, Entidad entidadActiva) throws Exception {

        OficiosRemisionOrganismo oficios = new OficiosRemisionOrganismo();

        Oficio oficio = oficioRemisionEjb.obtenerTipoOficio(codigoOrganismo, entidadActiva.getId());

        if(oficio.getInterno()) { // Destinatario organismo interno

            Organismo organismo = organismoEjb.findByCodigoEntidadSinEstadoLigero(codigoOrganismo, entidadActiva.getId());
            oficios.setOrganismo(organismo);
            oficios.setVigente(organismo.getEstado().getCodigoEstadoEntidad().equals(RegwebConstantes.ESTADO_ENTIDAD_VIGENTE));
            oficios.setOficinas(oficinaEjb.tieneOficinasServicio(organismo.getId(), RegwebConstantes.OFICINA_VIRTUAL_NO));

            //Buscamos los Registros de Entrada internos, pendientes de tramitar mediante un Oficio de Remision
            oficios.setPaginacion(oficiosRemisionByOrganismoInterno(pageNumber,resultsPerPage,organismo.getId(), any, oficinaActiva.getId(), idLibro));

        }else if (oficio.getExterno() || oficio.getEdpExterno()) { // Destinatario organismo externo

            oficios.setExterno(true);

            // Obtenemos el Organismo externo de Dir3Caib
            Dir3CaibObtenerUnidadesWs unidadesService = Dir3CaibUtils.getObtenerUnidadesService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());
            UnidadTF unidadTF = unidadesService.obtenerUnidad(codigoOrganismo,null,null);

            if(unidadTF != null){
                Organismo organismoExterno = new Organismo(null,codigoOrganismo,unidadTF.getDenominacion());
                organismoExterno.setEstado(catEstadoEntidadEjb.findByCodigo(RegwebConstantes.ESTADO_ENTIDAD_VIGENTE));
                oficios.setVigente(true);
                oficios.setOrganismo(organismoExterno);

                // Comprueba si la Entidad Actual está en SIR
                //Boolean isSir = (Boolean) em.createQuery("select e.sir from Entidad as e where e.id = :id").setParameter("id", idEntidadActiva).getSingleResult();
                if (entidadActiva.getSir() && oficinaActiva.getSirEnvio()) {
                    // Averiguamos si el Organismo Externo está en Sir o no
                    Dir3CaibObtenerOficinasWs oficinasService = Dir3CaibUtils.getObtenerOficinasService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());
                    List<OficinaTF> oficinasSIR = oficinasService.obtenerOficinasSIRUnidad(organismoExterno.getCodigo());
                    if (oficinasSIR.size() > 0) {
                        oficios.setSir(true);
                        oficios.setOficinasSIR(oficinasSIR);
                        log.info("El organismo externo " + organismoExterno + " TIENE oficinas Sir: " + oficinasSIR.size());
                    } else {
                        oficios.setOficinasSIR(null);
                        log.info("El organismo externo " + organismoExterno + " no tiene oficinas Sir");
                    }

                }else {
                    oficios.setSir(false);
                    oficios.setOficinasSIR(null);
                    log.info("Nuestra entidad no esta en SIR, se creara un oficio de remision tradicional");
                }

            }else{
                log.info("Organismo externo extinguido");
                oficios.setVigente(false);
                oficios.setOrganismo(new Organismo(null,codigoOrganismo,null));
            }

            //Buscamos los Registros de Entrada externos, pendientes de tramitar mediante un Oficio de Remision
            if (oficio.getExterno()) {
                oficios.setPaginacion(oficiosRemisionByOrganismoExterno(pageNumber, resultsPerPage, codigoOrganismo, any, oficinaActiva.getId(), idLibro));
            }else if(oficio.getEdpExterno()){
                oficios.setPaginacion(oficiosRemisionByOrganismoInterno(pageNumber,resultsPerPage, organismoEjb.findByCodigoLigero(codigoOrganismo).getId(), any, oficinaActiva.getId(), idLibro));
            }

        }

        return oficios;
    }


    @SuppressWarnings(value = "unchecked")
    private Paginacion oficiosRemisionByOrganismoInterno(Integer pageNumber,final Integer resultsPerPage, Long idOrganismo, Integer any, Long idOficina, Long idLibro) throws Exception {

        String anyWhere = "";
        if (any != null) {
            anyWhere = "year(re.fecha) = :any and ";
        }

        Query q;
        Query q2;

        StringBuilder query = new StringBuilder("Select re.id, re.numeroRegistroFormateado, re.fecha, re.oficina, re.destino, re.registroDetalle.extracto from RegistroEntrada as re where " + anyWhere +
                " re.libro.id = :idLibro and re.oficina.id = :idOficina " +
                "and re.destino.id = :idOrganismo and re.estado = :valido ");

        q2 = em.createQuery(query.toString().replaceAll("Select re.id, re.numeroRegistroFormateado, re.fecha, re.oficina, re.destino, re.registroDetalle.extracto", "Select count(re.id)"));
        query.append(" order by re.fecha desc ");
        q = em.createQuery(query.toString());


        // Parámetros
        if (any != null) {
            q.setParameter("any", any);
            q2.setParameter("any", any);
        }
        q.setParameter("idOrganismo", idOrganismo);
        q.setParameter("idOficina", idOficina);
        q.setParameter("idLibro", idLibro);
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q2.setParameter("idOrganismo", idOrganismo);
        q2.setParameter("idOficina", idOficina);
        q2.setParameter("idLibro", idLibro);
        q2.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);

        Paginacion paginacion;

        if (pageNumber != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), pageNumber, resultsPerPage);
            int inicio = (pageNumber - 1) * resultsPerPage;
            q.setFirstResult(inicio);
            q.setMaxResults(resultsPerPage);
        } else {
            paginacion = new Paginacion(0, 0, resultsPerPage);
        }

        List<Object[]> result = q.getResultList();
        List<RegistroEntrada> registros = new ArrayList<RegistroEntrada>();

        for (Object[] object : result) {
            RegistroEntrada re = new RegistroEntrada();
            re.setId((Long)  object[0]);
            re.setNumeroRegistroFormateado((String) object[1]);
            re.setFecha((Date) object[2]);
            re.setOficina((Oficina) object[3]);
            re.setDestino((Organismo) object[4]);
            re.setRegistroDetalle(new RegistroDetalle());
            re.getRegistroDetalle().setExtracto((String) object[5]);
            //re.getRegistroDetalle().setInteresados((List<Interesado>) object[6]);

            registros.add(re);
        }

        paginacion.setListado(registros);

        return paginacion;
    }

    @Override
    public Oficio isOficio(Long idRegistro, Set<Long> organismos, Entidad entidadActiva) throws Exception{

        Oficio oficio = new Oficio();

        if(isOficioRemisionExterno(idRegistro)){ // Externo

            oficio.setOficioRemision(true);

            List<OficinaTF> oficinasSIR = isOficioRemisionSir(idRegistro);

            if(!oficinasSIR.isEmpty() && entidadActiva.getSir()){
                oficio.setSir(true);

            }else{
                oficio.setExterno(true);
            }

        }else{

            Boolean interno = isOficioRemisionInterno(idRegistro, organismos);

            oficio.setOficioRemision(interno);
            oficio.setInterno(interno);
        }

        return oficio;
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public Boolean isOficioRemisionInterno(Long idRegistro, Set<Long> organismos) throws Exception {

        // Si el array de organismos está vacío, no incluimos la condición.
        String organismosWhere = "";
        if (organismos.size() > 0) {
            organismosWhere = " and re.destino.id not in (:organismos)";
        }

        Query q;
        q = em.createQuery("Select re.id from RegistroEntrada as re where " +
                "re.id = :idRegistro and re.estado = :valido and " +
                "re.destino != null and re.destino.estado.codigoEstadoEntidad = :vigente " + organismosWhere);

        // Parámetros
        q.setParameter("idRegistro", idRegistro);
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q.setParameter("vigente", RegwebConstantes.ESTADO_ENTIDAD_VIGENTE);

        if (organismos.size() > 0) {
            q.setParameter("organismos", organismos);
        }

        return q.getResultList().size() > 0;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Boolean isOficioRemisionExterno(Long idRegistro) throws Exception {

        Query q;
        q = em.createQuery("Select re.id from RegistroEntrada as re where " +
                "re.id = :idRegistro and re.destino is null and re.estado = :valido");

        // Parámetros
        q.setParameter("idRegistro", idRegistro);
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);


        return q.getResultList().size() > 0;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<OficinaTF> isOficioRemisionSir(Long idRegistro) throws Exception {

        Query q;
        q = em.createQuery("Select re.destinoExternoCodigo from RegistroEntrada as re where " +
                "re.id = :idRegistro and re.destino is null and re.estado = :valido");

        // Parámetros
        q.setParameter("idRegistro", idRegistro);
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);

        List<String> result = q.getResultList();

        if(result.size() > 0){

            String codigoDir3 = result.get(0);
            Dir3CaibObtenerOficinasWs oficinasService = Dir3CaibUtils.getObtenerOficinasService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());

            return oficinasService.obtenerOficinasSIRUnidad(codigoDir3);
        }

        return null;
    }


    @SuppressWarnings(value = "unchecked")
    private Paginacion oficiosRemisionByOrganismoExterno(Integer pageNumber, final Integer resultsPerPage, String codigoOrganismo, Integer any, Long idOficina, Long idLibro) throws Exception {

        String anyWhere = "";
        if (any != null) {
            anyWhere = "year(re.fecha) = :any and ";
        }

        StringBuilder query = new StringBuilder("Select re.id, re.numeroRegistroFormateado, re.fecha, re.oficina, re.destinoExternoCodigo, re.destinoExternoDenominacion, re.registroDetalle.extracto from RegistroEntrada as re where " + anyWhere +
                " re.libro.id = :idLibro and re.oficina.id = :idOficina " +
                " and re.destino is null and re.destinoExternoCodigo = :codigoOrganismo and re.estado = :valido ");

        Query q;
        Query q2;

        q2 = em.createQuery(query.toString().replaceAll("Select re.id, re.numeroRegistroFormateado, re.fecha, re.oficina, re.destinoExternoCodigo, re.destinoExternoDenominacion, re.registroDetalle.extracto", "Select count(re.id)"));
        query.append(" order by re.fecha desc ");
        q = em.createQuery(query.toString());

        // Parámetros
        if (any != null) {
            q.setParameter("any", any);
            q2.setParameter("any", any);
        }
        q.setParameter("codigoOrganismo", codigoOrganismo);
        q.setParameter("idOficina", idOficina);
        q.setParameter("idLibro", idLibro);
        q.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);
        q2.setParameter("codigoOrganismo", codigoOrganismo);
        q2.setParameter("idOficina", idOficina);
        q2.setParameter("idLibro", idLibro);
        q2.setParameter("valido", RegwebConstantes.REGISTRO_VALIDO);

        Paginacion paginacion;

        if (pageNumber != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), pageNumber, resultsPerPage);
            int inicio = (pageNumber - 1) * resultsPerPage;
            q.setFirstResult(inicio);
            q.setMaxResults(resultsPerPage);
        } else {
            paginacion = new Paginacion(0, 0, resultsPerPage);
        }

        List<Object[]> result = q.getResultList();
        List<RegistroEntrada> registros = new ArrayList<RegistroEntrada>();

        for (Object[] object : result) {
            RegistroEntrada re = new RegistroEntrada();
            re.setId((Long)  object[0]);
            re.setNumeroRegistroFormateado((String) object[1]);
            re.setFecha((Date) object[2]);
            re.setOficina((Oficina) object[3]);
            re.setDestinoExternoCodigo((String) object[4]);
            re.setDestinoExternoDenominacion((String) object[5]);
            re.setRegistroDetalle(new RegistroDetalle());
            re.getRegistroDetalle().setExtracto((String) object[6]);
            registros.add(re);
        }

        paginacion.setListado(registros);

        return paginacion;
    }


    /**
     * Crea un OficioRemision con todos los ResgistroEntrada seleccionados
     * Crea un RegistroSalida por cada uno de los RegistroEntrada que contenga el OficioRemision
     * Crea la trazabilidad para los RegistroEntrada y RegistroSalida
     *
     * @param registrosEntrada Listado de RegistrosEntrada que forman parte del Oficio de remisión
     * @param oficinaActiva    Oficia en la cual se realiza el OficioRemision
     * @param usuarioEntidad   Usuario que realiza el OficioRemision
     * @param idOrganismo
     * @param idLibro
     * @return
     * @throws Exception
     */
    @Override
    public OficioRemision crearOficioRemisionInterno(List<RegistroEntrada> registrosEntrada,
                                                     Oficina oficinaActiva, UsuarioEntidad usuarioEntidad, Long idOrganismo, Long idLibro)
            throws Exception, I18NException, I18NValidationException {

        OficioRemision oficioRemision = new OficioRemision();
        oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA);
        oficioRemision.setEstado(RegwebConstantes.OFICIO_INTERNO_ENVIADO);
        oficioRemision.setOficina(oficinaActiva);
        oficioRemision.setFecha(new Date());
        oficioRemision.setFechaEstado(new Date());
        oficioRemision.setRegistrosEntrada(registrosEntrada);
        oficioRemision.setUsuarioResponsable(usuarioEntidad);
        oficioRemision.setLibro(new Libro(idLibro));
        oficioRemision.setOrganismoDestinatario(new Organismo(idOrganismo));

        synchronized (this) {
            oficioRemision = oficioRemisionEjb.registrarOficioRemision(oficioRemision, RegwebConstantes.REGISTRO_OFICIO_INTERNO);
        }

        return oficioRemision;

    }

    /**
     * Crea un OficioRemision con todos los ResgistroEntrada seleccionados
     *
     * @param registrosEntrada Listado de RegistrosEntrada que forman parte del Oficio de remisión
     * @param oficinaActiva    Oficia en la cual se realiza el OficioRemision
     * @param usuarioEntidad   Usuario que realiza el OficioRemision
     * @param organismoExternoCodigo
     * @param idLibro
     * @return
     * @throws Exception
     */

    public OficioRemision crearOficioRemisionExterno(List<RegistroEntrada> registrosEntrada,
                                                     Oficina oficinaActiva, UsuarioEntidad usuarioEntidad, String organismoExternoCodigo,
                                                     String organismoExternoDenominacion, Long idLibro)
            throws Exception, I18NException, I18NValidationException {

        OficioRemision oficioRemision = new OficioRemision();
        oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA);
        oficioRemision.setEstado(RegwebConstantes.OFICIO_EXTERNO_ENVIADO);
        oficioRemision.setOficina(oficinaActiva);
        oficioRemision.setFecha(new Date());
        oficioRemision.setFechaEstado(new Date());
        oficioRemision.setRegistrosEntrada(registrosEntrada);
        oficioRemision.setUsuarioResponsable(usuarioEntidad);
        oficioRemision.setLibro(new Libro(idLibro));
        oficioRemision.setDestinoExternoCodigo(organismoExternoCodigo);
        oficioRemision.setDestinoExternoDenominacion(organismoExternoDenominacion);
        oficioRemision.setOrganismoDestinatario(null);

        synchronized (this) {
            oficioRemision = oficioRemisionEjb.registrarOficioRemision(oficioRemision, RegwebConstantes.REGISTRO_OFICIO_EXTERNO);
        }

        return oficioRemision;

    }

    @Override
    public List<RegistroEntrada> crearJustificantesRegistros(List<RegistroEntrada> registros, UsuarioEntidad usuario) throws Exception, I18NException, I18NValidationException {

        List<RegistroEntrada> correctos = new ArrayList<RegistroEntrada>();

        for (RegistroEntrada registro : registros) {

            RegistroEntrada registroEntrada = registroEntradaEjb.getConAnexosFull(registro.getId());

            //Justificante, Si no tiene generado el Justificante, lo hacemos
            if (!registroEntrada.getRegistroDetalle().getTieneJustificante()) {

                try{

                    // Creamos el anexo del justificante y se lo añadimos al registro
                    AnexoFull anexoFull = justificanteEjb.crearJustificante(usuario, registroEntrada, RegwebConstantes.REGISTRO_ENTRADA_ESCRITO.toLowerCase(), Configuracio.getDefaultLanguage());
                    registroEntrada.getRegistroDetalle().getAnexosFull().add(anexoFull);
                    // Añadimos el Correcto
                    correctos.add(registro);
                }catch (I18NException e){
                    log.info("Error generando justificante: " + e.getMessage());
                    e.printStackTrace();
                }

            }else{
                // Añadimos el Correcto
                correctos.add(registro);
            }

        }

        return correctos;
    }


    /**
     * Aceptar un OficioRemision pendiente de llegada, creando tantos Registros de Entrada,
     * como contenga el Oficio.
     *
     * @param oficioRemision
     * @throws Exception
     */
    @Override
    public List<RegistroEntrada> aceptarOficioRemision(OficioRemision oficioRemision,
                                                        UsuarioEntidad usuario, Oficina oficinaActiva,
                                                        List<OficioPendienteLlegada> oficios) throws Exception, I18NException, I18NValidationException {

        List<RegistroEntrada> registros = new ArrayList<RegistroEntrada>();

        // Recorremos los RegistroEntrada del Oficio
        for (OficioPendienteLlegada oficio : oficios) {

            RegistroEntrada registroEntrada = registroEntradaEjb.getConAnexosFull(oficio.getIdRegistro());
            List<Interesado> interesados = registroEntrada.getRegistroDetalle().getInteresados();
            List<AnexoFull> anexos = registroEntrada.getRegistroDetalle().getAnexosFull();
            Libro libro = libroEjb.findById(oficio.getIdLibro());

            // Detach de la sesion para poder duplicar el registro
            Session session = (Session) em.getDelegate();
            session.evict(registroEntrada);
            session.evict(registroEntrada.getRegistroDetalle());
            session.evict(registroEntrada.getRegistroDetalle().getAnexos());
            session.evict(registroEntrada.getRegistroDetalle().getInteresados());

            // Creamos un Nuevo RegistroEntrada
            RegistroEntrada nuevoRE = new RegistroEntrada();
            nuevoRE.setUsuario(usuario);
            nuevoRE.setDestino(organismoEjb.findByIdLigero(oficio.getIdOrganismoDestinatario()));
            nuevoRE.setOficina(oficinaActiva);
            nuevoRE.setEstado(RegwebConstantes.REGISTRO_VALIDO);
            nuevoRE.setLibro(libro);

            // Creamos un nuevo RegistroDetalle, modificando las propiedades Origen
            RegistroDetalle registroDetalle = registroEntrada.getRegistroDetalle();

            // Set Id's a null
            registroDetalle.setId(null);
            registroDetalle.setAnexos(null);
            registroDetalle.setInteresados(null);

            for (AnexoFull anexo : anexos) {
                anexo.getAnexo().setId(null);
                anexo.getAnexo().setJustificante(false);
            }

            nuevoRE.setRegistroDetalle(registroDetalle);

            // Registramos el nuevo RegistroEntrada
            synchronized (this) {
                nuevoRE = registroEntradaEjb.registrarEntrada(nuevoRE, usuario, interesados, anexos);
            }

            registros.add(nuevoRE);

            // ACTUALIZAMOS LA TRAZABILIDAD
            Trazabilidad trazabilidad = trazabilidadEjb.getByOficioRegistroEntrada(oficioRemision.getId(), registroEntrada.getId());
            trazabilidad.setRegistroEntradaDestino(nuevoRE);
            trazabilidadEjb.merge(trazabilidad);

            // Marcamos el RegistroEntrada original como ACEPTADO
            registroEntradaEjb.cambiarEstadoHistorico(registroEntrada,RegwebConstantes.REGISTRO_OFICIO_ACEPTADO, usuario);

        }

        oficioRemision.setEstado(RegwebConstantes.OFICIO_ACEPTADO);
        oficioRemision.setFechaEstado(new Date());

        // Actualizamos el oficio de remisión
        oficioRemisionEjb.merge(oficioRemision);

        return registros;

    }


}
