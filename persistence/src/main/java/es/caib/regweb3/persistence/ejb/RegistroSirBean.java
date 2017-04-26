package es.caib.regweb3.persistence.ejb;

import es.caib.dir3caib.ws.api.oficina.Dir3CaibObtenerOficinasWs;
import es.caib.dir3caib.ws.api.oficina.OficinaTF;
import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.*;
import es.caib.regweb3.persistence.utils.ArchivoManager;
import es.caib.regweb3.persistence.utils.DataBaseUtils;
import es.caib.regweb3.persistence.utils.FileSystemManager;
import es.caib.regweb3.persistence.utils.Paginacion;
import es.caib.regweb3.sir.core.excepcion.ServiceException;
import es.caib.regweb3.sir.core.excepcion.ValidacionException;
import es.caib.regweb3.sir.core.model.*;
import es.caib.regweb3.sir.core.schema.*;
import es.caib.regweb3.sir.core.schema.types.Documentacion_FisicaType;
import es.caib.regweb3.sir.core.schema.types.Indicador_PruebaType;
import es.caib.regweb3.sir.core.schema.types.Tipo_RegistroType;
import es.caib.regweb3.sir.core.utils.FicheroIntercambio;
import es.caib.regweb3.utils.Dir3CaibUtils;
import es.caib.regweb3.utils.MimeTypeUtils;
import es.caib.regweb3.utils.RegwebConstantes;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static es.caib.regweb3.utils.RegwebConstantes.*;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 16/06/16
 */

@Stateless(name = "RegistroSirEJB")
@SecurityDomain("seycon")
public class RegistroSirBean extends BaseEjbJPA<RegistroSir, Long> implements RegistroSirLocal {

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName = "regweb3")
    private EntityManager em;

    @EJB private InteresadoSirLocal interesadoSirEjb;
    @EJB private AnexoSirLocal anexoSirEjb;
    @EJB private RegistroEntradaLocal registroEntradaEjb;
    @EJB private RegistroSalidaLocal registroSalidaEjb;
    @EJB private ArchivoLocal archivoEjb;
    @EJB private LibroLocal libroEjb;
    @EJB private CatProvinciaLocal catProvinciaEjb;
    @EJB private CatLocalidadLocal catLocalidadEjb;
    @EJB private OrganismoLocal organismoEjb;
    @EJB private OficinaLocal oficinaEjb;
    @EJB private CatPaisLocal catPaisEjb;
    @EJB private TipoDocumentalLocal tipoDocumentalEjb;


    @Override
    public RegistroSir getReference(Long id) throws Exception {

        return em.getReference(RegistroSir.class, id);
    }

    @Override
    public RegistroSir findById(Long id) throws Exception {

        return em.find(RegistroSir.class, id);
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public List<RegistroSir> getAll() throws Exception {

        return em.createQuery("Select registroSir from RegistroSir as registroSir order by registroSir.id").getResultList();
    }

    @Override
    public Long getTotal() throws Exception {

        Query q = em.createQuery("Select count(registroSir.id) from RegistroSir as registroSir");

        return (Long) q.getSingleResult();
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public List<RegistroSir> getPagination(int inicio) throws Exception {

        Query q = em.createQuery("Select registroSir from RegistroSir as registroSir order by registroSir.id");
        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }

    @Override
    public RegistroSir getRegistroSir(String identificadorIntercambio, String codigoEntidadRegistralDestino) throws Exception {

        Query q = em.createQuery("Select registroSir from RegistroSir as registroSir where " +
                "registroSir.identificadorIntercambio = :identificadorIntercambio and registroSir.codigoEntidadRegistralOrigen = :codigoEntidadRegistralDestino");

        q.setParameter("identificadorIntercambio",identificadorIntercambio);
        q.setParameter("codigoEntidadRegistralDestino",codigoEntidadRegistralDestino);

        List<RegistroSir> registroSir = q.getResultList();
        if(registroSir.size() == 1){
            return registroSir.get(0);
        }else{
            return  null;
        }
    }

    @Override
    public RegistroSir getRegistroSir(String identificadorIntercambio) throws Exception{

        Query q = em.createQuery("Select registroSir from RegistroSir as registroSir where " +
                "registroSir.identificadorIntercambio = :identificadorIntercambio");

        q.setParameter("identificadorIntercambio",identificadorIntercambio);

        List<RegistroSir> registroSir = q.getResultList();
        if(registroSir.size() == 1){
            return registroSir.get(0);
        }else{
            return  null;
        }
    }

    @Override
    public RegistroSir getRegistroSirConAnexos(Long idRegistroSirsir) throws Exception{

        RegistroSir registroSir = findById(idRegistroSirsir);

        List<AnexoSir> anexosFull = new ArrayList<AnexoSir>();
        for (AnexoSir anexoSir : registroSir.getAnexos()) {

            anexoSir.setAnexoData(FileSystemManager.getBytesArchivo(anexoSir.getAnexo().getId()));
            anexosFull.add(anexoSir);
        }

        registroSir.setAnexos(anexosFull);

        return registroSir;

    }

    @Override
    public RegistroSir crearRegistroSir(RegistroSir registroSir) throws Exception{

        try{

            // En caso de recepción, le asignamos la entidad a la que va dirigida
            if(registroSir.getEntidad() == null){
                Entidad entidad = new Entidad(oficinaEjb.obtenerEntidad(registroSir.getCodigoEntidadRegistralDestino()));
                registroSir.setEntidad(entidad);
            }

            registroSir = persist(registroSir);

            // Guardamos los Interesados
            if(registroSir.getInteresados() != null && registroSir.getInteresados().size() > 0){
                for(InteresadoSir interesadoSir: registroSir.getInteresados()){
                    interesadoSir.setRegistroSir(registroSir);

                    interesadoSirEjb.persist(interesadoSir);
                }
            }

            // Guardamos los Anexos
            if(registroSir.getAnexos() != null && registroSir.getAnexos().size() > 0){
                for(AnexoSir anexoSir: registroSir.getAnexos()){
                    anexoSir.setRegistroSir(registroSir);

                    anexoSirEjb.persist(anexoSir);
                }
            }
            em.flush();

        }catch (Exception e){
            log.info("Error al crear el RegistroSir, eliminamos los posibles anexos creados");
            for(AnexoSir anexoSir: registroSir.getAnexos()){
                ArchivoManager am = new ArchivoManager(anexoSir.getAnexo(),archivoEjb);
                am.processError();
            }
            e.printStackTrace();
            throw e;
        }

        return registroSir;
    }

    @Override
    public Boolean tieneRegistroSir(String codigoOficinaActiva) throws Exception {

        Query q = em.createQuery("Select count(registroSir.id) from RegistroSir as registroSir where " +
                "registroSir.codigoEntidadRegistralDestino = :codigoOficinaActiva");

        q.setParameter("codigoOficinaActiva",codigoOficinaActiva);

        return (Long) q.getSingleResult() > 0;
    }

    public Paginacion busqueda(Integer pageNumber, Integer any, RegistroSir registroSir, Set<String> organismos, String estado) throws Exception{

        Query q;
        Query q2;
        Map<String, Object> parametros = new HashMap<String, Object>();
        List<String> where = new ArrayList<String>();

        StringBuffer query = new StringBuffer("Select asr from RegistroSir as asr ");

        /*if (organismos != null && organismos.size() > 0) {
            where.add(" asr.codigoUnidadTramitacionDestino in (:organismos) "); parametros.put("organismos",organismos);
        }*/

        if (registroSir.getResumen() != null && registroSir.getResumen().length() > 0) {
            where.add(DataBaseUtils.like("asr.resumen", "resumen", parametros, registroSir.getResumen()));
        }

        if (registroSir.getNumeroRegistro() != null && registroSir.getNumeroRegistro().length() > 0) {
            where.add(DataBaseUtils.like("asr.numeroRegistro", "numeroRegistro", parametros, registroSir.getNumeroRegistro()));
        }

        if (!StringUtils.isEmpty(estado)) {
            where.add(" asr.estado = :estado "); parametros.put("estado",EstadoRegistroSir.valueOf(estado));
        }

        if(any!= null){where.add(" year(asr.fechaRegistro) = :any "); parametros.put("any",any);}

        if (parametros.size() != 0) {
            query.append("where ");
            int count = 0;
            for (String w : where) {
                if (count != 0) {
                    query.append(" and ");
                }
                query.append(w);
                count++;
            }
            q2 = em.createQuery(query.toString().replaceAll("Select asr from RegistroSir as asr ", "Select count(asr.id) from RegistroSir as asr "));
            query.append(" order by asr.id desc");
            q = em.createQuery(query.toString());

            for (Map.Entry<String, Object> param : parametros.entrySet()) {

                q.setParameter(param.getKey(), param.getValue());
                q2.setParameter(param.getKey(), param.getValue());
            }

        } else {
            q2 = em.createQuery(query.toString().replaceAll("Select asr from RegistroSir as asr ", "Select count(asr.id) from RegistroSir as asr "));
            query.append("order by asr.id desc");
            q = em.createQuery(query.toString());
        }


        Paginacion paginacion = null;

        if (pageNumber != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), pageNumber);
            int inicio = (pageNumber - 1) * BaseEjbJPA.RESULTADOS_PAGINACION;
            q.setFirstResult(inicio);
            q.setMaxResults(RESULTADOS_PAGINACION);
        } else {
            paginacion = new Paginacion(0, 0);
        }

        paginacion.setListado(q.getResultList());

        return paginacion;
    }

    public List<RegistroSir> getUltimosPendientesProcesar(Set<String> organismos, Integer total) throws Exception{

        Query q = em.createQuery("Select ars from RegistroSir as ars " +
                "where ars.codigoUnidadTramitacionDestino in (:organismos) and ars.estado = :idEstadoPreRegistro " +
                "order by ars.id desc");

        q.setMaxResults(total);
        q.setParameter("organismos", organismos);
        q.setParameter("idEstadoPreRegistro", EstadoRegistroSir.RECIBIDO);

        return  q.getResultList();
    }

    @Override
    public void modificarEstado(Long idRegistroSir, EstadoRegistroSir estado) throws Exception {

        Query q = em.createQuery("update RegistroSir set estado=:estado where id = :idRegistroSir");
        q.setParameter("estado", estado);
        q.setParameter("idRegistroSir", idRegistroSir);
        q.executeUpdate();

    }

    @Override
    public Integer eliminarByEntidad(Long idEntidad) throws Exception{

        List<?> result = em.createQuery("Select distinct(a.id) from RegistroSir as a where a.entidad.id =:idEntidad").setParameter("idEntidad",idEntidad).getResultList();
        Integer total = result.size();

        if(result.size() > 0){

            // Si hay más de 1000 registros, dividimos las queries (ORA-01795).
            while (result.size() > RegwebConstantes.NUMBER_EXPRESSIONS_IN) {

                List<?> subList = result.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN);
                em.createQuery("delete from RegistroSir where id in (:result) ").setParameter("result", subList).executeUpdate();
                result.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN).clear();
            }

            em.createQuery("delete from RegistroSir where id in (:result) ").setParameter("result", result).executeUpdate();
        }

        return total;

    }

    /**
     * Crea un RegistroSir, a partir del FicheroIntercambio.
     *
     * @return Información del registroSir registral.
     */
    @Override
    public RegistroSir transformarFicheroIntercambio(FicheroIntercambio ficheroIntercambio)throws Exception{

        final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

        RegistroSir registroSir = null;

        if (ficheroIntercambio.getFicheroIntercambio() != null) {

            registroSir = new RegistroSir();
            registroSir.setFechaRecepcion(new Date());

            // Segmento De_Origen_o_Remitente
            De_Origen_o_Remitente de_Origen_o_Remitente = ficheroIntercambio.getFicheroIntercambio().getDe_Origen_o_Remitente();
            if (de_Origen_o_Remitente != null) {

                registroSir.setCodigoEntidadRegistralOrigen(de_Origen_o_Remitente.getCodigo_Entidad_Registral_Origen());

                if (!StringUtils.isEmpty(de_Origen_o_Remitente.getDecodificacion_Entidad_Registral_Origen())) {
                    registroSir.setDecodificacionEntidadRegistralOrigen(de_Origen_o_Remitente.getDecodificacion_Entidad_Registral_Origen());
                } else {
                    registroSir.setDecodificacionEntidadRegistralOrigen(Dir3CaibUtils.denominacion(de_Origen_o_Remitente.getCodigo_Entidad_Registral_Origen(), RegwebConstantes.OFICINA));
                }

                registroSir.setCodigoUnidadTramitacionOrigen(de_Origen_o_Remitente.getCodigo_Unidad_Tramitacion_Origen());

                if (!StringUtils.isEmpty(de_Origen_o_Remitente.getDecodificacion_Unidad_Tramitacion_Origen())) {
                    registroSir.setDecodificacionUnidadTramitacionOrigen(de_Origen_o_Remitente.getDecodificacion_Unidad_Tramitacion_Origen());
                } else {
                    registroSir.setDecodificacionUnidadTramitacionOrigen(Dir3CaibUtils.denominacion(de_Origen_o_Remitente.getCodigo_Unidad_Tramitacion_Origen(), RegwebConstantes.UNIDAD));
                }


                registroSir.setNumeroRegistro(de_Origen_o_Remitente.getNumero_Registro_Entrada());
                registroSir.setTimestampRegistro(de_Origen_o_Remitente.getTimestamp_Entrada());

                String fechaRegistro = de_Origen_o_Remitente.getFecha_Hora_Entrada();
                if (StringUtils.isNotBlank(fechaRegistro)) {
                    try {
                        registroSir.setFechaRegistro(SDF.parse(fechaRegistro));
                    } catch (ParseException e) {
                        log.error("Error al parsear la fecha de registro: [" + fechaRegistro + "]", e);
                        throw new ValidacionException(Errores.ERROR_0037, e);
                    }
                }
            }

            // Segmento De_Destino
            De_Destino de_Destino = ficheroIntercambio.getFicheroIntercambio().getDe_Destino();
            if (de_Destino != null) {

                registroSir.setCodigoEntidadRegistralDestino(de_Destino.getCodigo_Entidad_Registral_Destino());
                if (!StringUtils.isEmpty(de_Destino.getDecodificacion_Entidad_Registral_Destino())) {
                    registroSir.setDecodificacionEntidadRegistralDestino(de_Destino.getDecodificacion_Entidad_Registral_Destino());
                } else {
                    registroSir.setDecodificacionEntidadRegistralDestino(Dir3CaibUtils.denominacion(de_Destino.getCodigo_Entidad_Registral_Destino(), RegwebConstantes.OFICINA));
                }

                if (!StringUtils.isEmpty(de_Destino.getCodigo_Unidad_Tramitacion_Destino())) {
                    registroSir.setCodigoUnidadTramitacionDestino(de_Destino.getCodigo_Unidad_Tramitacion_Destino());
                    if (!StringUtils.isEmpty(de_Destino.getDecodificacion_Unidad_Tramitacion_Destino())) {
                        registroSir.setDecodificacionUnidadTramitacionDestino(de_Destino.getDecodificacion_Unidad_Tramitacion_Destino());
                    } else {
                        registroSir.setDecodificacionUnidadTramitacionDestino(Dir3CaibUtils.denominacion(de_Destino.getCodigo_Unidad_Tramitacion_Destino(), RegwebConstantes.UNIDAD));
                    }
                }

            }

            // Segmento De_Asunto de_Asunto
            De_Asunto de_Asunto = ficheroIntercambio.getFicheroIntercambio().getDe_Asunto();
            if (de_Asunto != null) {
                registroSir.setResumen(de_Asunto.getResumen());
                registroSir.setCodigoAsunto(de_Asunto.getCodigo_Asunto_Segun_Destino());
                registroSir.setReferenciaExterna(de_Asunto.getReferencia_Externa());
                registroSir.setNumeroExpediente(de_Asunto.getNumero_Expediente());
            }

            // Segmento De_Internos_Control
            De_Internos_Control de_Internos_Control = ficheroIntercambio.getFicheroIntercambio().getDe_Internos_Control();
            if (de_Internos_Control != null) {

                registroSir.setIdentificadorIntercambio(de_Internos_Control.getIdentificador_Intercambio());
                registroSir.setAplicacion(de_Internos_Control.getAplicacion_Version_Emisora());
                registroSir.setTipoAnotacion(ficheroIntercambio.getTipoAnotacion());
                registroSir.setDecodificacionTipoAnotacion(de_Internos_Control.getDescripcion_Tipo_Anotacion());
                registroSir.setNumeroTransporte(de_Internos_Control.getNumero_Transporte_Entrada());
                registroSir.setNombreUsuario(de_Internos_Control.getNombre_Usuario());
                registroSir.setContactoUsuario(de_Internos_Control.getContacto_Usuario());
                registroSir.setObservacionesApunte(de_Internos_Control.getObservaciones_Apunte());

                registroSir.setCodigoEntidadRegistralInicio(de_Internos_Control.getCodigo_Entidad_Registral_Inicio());
                if (!StringUtils.isEmpty(de_Internos_Control.getDecodificacion_Entidad_Registral_Inicio())) {
                    registroSir.setDecodificacionEntidadRegistralInicio(de_Internos_Control.getDecodificacion_Entidad_Registral_Inicio());
                } else {
                    registroSir.setDecodificacionEntidadRegistralInicio(Dir3CaibUtils.denominacion(de_Internos_Control.getCodigo_Entidad_Registral_Inicio(), RegwebConstantes.OFICINA));
                }


                // Tipo de transporte
                String tipoTransporte = de_Internos_Control.getTipo_Transporte_Entrada();
                if (StringUtils.isNotBlank(tipoTransporte)) {
                    registroSir.setTipoTransporte(TipoTransporte.getTipoTransporteValue(tipoTransporte));
                }

                // Tipo de registro
                Tipo_RegistroType tipo_Registro = de_Internos_Control.getTipo_Registro();
                if ((tipo_Registro != null) && StringUtils.isNotBlank(tipo_Registro.value())) {
                    registroSir.setTipoRegistro(TipoRegistro.getTipoRegistro(tipo_Registro.value()));
                }

                // Documentación física
                Documentacion_FisicaType documentacion_Fisica = de_Internos_Control.getDocumentacion_Fisica();
                if ((documentacion_Fisica != null) && StringUtils.isNotBlank(documentacion_Fisica.value())) {
                    registroSir.setDocumentacionFisica(DocumentacionFisica.getDocumentacionFisicaValue(documentacion_Fisica.value()));
                }

                // Indicador de prueba
                Indicador_PruebaType indicadorPrueba = de_Internos_Control.getIndicador_Prueba();
                if ((indicadorPrueba != null) && StringUtils.isNotBlank(indicadorPrueba.value())){
                    registroSir.setIndicadorPrueba(IndicadorPrueba.getIndicadorPrueba(indicadorPrueba.value()));
                }

            }

            // Segmento De_Formulario_Generico
            De_Formulario_Generico de_Formulario_Generico = ficheroIntercambio.getFicheroIntercambio().getDe_Formulario_Generico();
            if (de_Formulario_Generico != null) {
                registroSir.setExpone(de_Formulario_Generico.getExpone());
                registroSir.setSolicita(de_Formulario_Generico.getSolicita());
            }

            // Segmento De_Interesado
            De_Interesado[] de_Interesados = ficheroIntercambio.getFicheroIntercambio().getDe_Interesado();
            if (ArrayUtils.isNotEmpty(de_Interesados)) {
                for (De_Interesado de_Interesado : de_Interesados) {
                    if (de_Interesado != null) {

                        // Si se trata de una Salida y no tiene Interesados
                        if(ficheroIntercambio.getTipoRegistro().equals(TipoRegistro.SALIDA) &&
                                StringUtils.isBlank(de_Interesado.getRazon_Social_Interesado())
                                && (StringUtils.isBlank(de_Interesado.getNombre_Interesado()) && StringUtils.isBlank(de_Interesado.getPrimer_Apellido_Interesado()))){

                            // Creamos uno a partir de la Entidad destino
                            registroSir.getInteresados().add(crearInteresadoJuridico(ficheroIntercambio));

                        }else{

                            InteresadoSir interesado = new InteresadoSir();

                            // Información del interesado
                            interesado.setDocumentoIdentificacionInteresado(de_Interesado.getDocumento_Identificacion_Interesado());
                            interesado.setRazonSocialInteresado(de_Interesado.getRazon_Social_Interesado());
                            interesado.setNombreInteresado(de_Interesado.getNombre_Interesado());
                            interesado.setPrimerApellidoInteresado(de_Interesado.getPrimer_Apellido_Interesado());
                            interesado.setSegundoApellidoInteresado(de_Interesado.getSegundo_Apellido_Interesado());
                            interesado.setCodigoPaisInteresado(de_Interesado.getPais_Interesado());
                            interesado.setCodigoProvinciaInteresado(de_Interesado.getProvincia_Interesado());
                            interesado.setCodigoMunicipioInteresado(de_Interesado.getMunicipio_Interesado());
                            interesado.setDireccionInteresado(de_Interesado.getDireccion_Interesado());
                            interesado.setCodigoPostalInteresado(de_Interesado.getCodigo_Postal_Interesado());
                            interesado.setCorreoElectronicoInteresado(de_Interesado.getCorreo_Electronico_Interesado());
                            interesado.setTelefonoInteresado(de_Interesado.getTelefono_Contacto_Interesado());
                            interesado.setDireccionElectronicaHabilitadaInteresado(de_Interesado.getDireccion_Electronica_Habilitada_Interesado());

                            String tipoDocumento = de_Interesado.getTipo_Documento_Identificacion_Interesado();
                            if (StringUtils.isNotBlank(tipoDocumento)) {
                                interesado.setTipoDocumentoIdentificacionInteresado(TipoDocumentoIdentificacion.getTipoDocumentoIdentificacionValue(tipoDocumento));
                            }

                            String canalPreferente = de_Interesado.getCanal_Preferente_Comunicacion_Interesado();
                            if (StringUtils.isNotBlank(canalPreferente)) {
                                interesado.setCanalPreferenteComunicacionInteresado(CanalNotificacion.getCanalNotificacionValue(canalPreferente));
                            }

                            // Información del representante
                            interesado.setDocumentoIdentificacionRepresentante(de_Interesado.getDocumento_Identificacion_Representante());
                            interesado.setRazonSocialRepresentante(de_Interesado.getRazon_Social_Representante());
                            interesado.setNombreRepresentante(de_Interesado.getNombre_Representante());
                            interesado.setPrimerApellidoRepresentante(de_Interesado.getPrimer_Apellido_Representante());
                            interesado.setSegundoApellidoRepresentante(de_Interesado.getSegundo_Apellido_Representante());
                            interesado.setCodigoPaisRepresentante(de_Interesado.getPais_Representante());
                            interesado.setCodigoProvinciaRepresentante(de_Interesado.getProvincia_Representante());
                            interesado.setCodigoMunicipioRepresentante(de_Interesado.getMunicipio_Representante());
                            interesado.setDireccionRepresentante(de_Interesado.getDireccion_Representante());
                            interesado.setCodigoPostalRepresentante(de_Interesado.getCodigo_Postal_Representante());
                            interesado.setCorreoElectronicoRepresentante(de_Interesado.getCorreo_Electronico_Representante());
                            interesado.setTelefonoRepresentante(de_Interesado.getTelefono_Contacto_Representante());
                            interesado.setDireccionElectronicaHabilitadaRepresentante(de_Interesado.getDireccion_Electronica_Habilitada_Representante());

                            tipoDocumento = de_Interesado.getTipo_Documento_Identificacion_Representante();
                            if (StringUtils.isNotBlank(tipoDocumento)) {
                                interesado.setTipoDocumentoIdentificacionRepresentante(TipoDocumentoIdentificacion.getTipoDocumentoIdentificacionValue(tipoDocumento));
                            }

                            canalPreferente = de_Interesado.getCanal_Preferente_Comunicacion_Representante();
                            if (StringUtils.isNotBlank(canalPreferente)) {
                                interesado.setCanalPreferenteComunicacionRepresentante(CanalNotificacion.getCanalNotificacionValue(canalPreferente));
                            }

                            interesado.setObservaciones(de_Interesado.getObservaciones());

                            registroSir.getInteresados().add(interesado);
                        }

                    }
                }
            }

            // Segmento De_Anexos
            De_Anexo[] de_Anexos = ficheroIntercambio.getFicheroIntercambio().getDe_Anexo();
            if (ArrayUtils.isNotEmpty(de_Anexos)) {
                for (De_Anexo de_Anexo : de_Anexos) {
                    if (de_Anexo != null) {
                        AnexoSir anexo = new AnexoSir();

                        anexo.setNombreFichero(de_Anexo.getNombre_Fichero_Anexado());
                        anexo.setIdentificadorFichero(de_Anexo.getIdentificador_Fichero());
                        anexo.setIdentificadorDocumentoFirmado(de_Anexo.getIdentificador_Documento_Firmado());
                        anexo.setCertificado(de_Anexo.getCertificado());
                        anexo.setFirma(de_Anexo.getFirma_Documento());
                        anexo.setTimestamp(de_Anexo.getTimeStamp());
                        anexo.setValidacionOCSPCertificado(de_Anexo.getValidacion_OCSP_Certificado());
                        anexo.setHash(de_Anexo.getHash());
                        //Si el tipo mime es null, se obtiene del nombre del fichero
                        if (de_Anexo.getTipo_MIME() == null || de_Anexo.getTipo_MIME().isEmpty()) {
                            anexo.setTipoMIME(MimeTypeUtils.getMimeTypeFileName(de_Anexo.getNombre_Fichero_Anexado()));
                        } else {
                            anexo.setTipoMIME(de_Anexo.getTipo_MIME());
                        }

                        ArchivoManager am = null;
                        try {
                            am = new ArchivoManager(archivoEjb, de_Anexo.getNombre_Fichero_Anexado(), anexo.getTipoMIME(), de_Anexo.getAnexo());
                            anexo.setAnexo(am.prePersist());
                        } catch (Exception e) {
                            log.info("Error al crear el Anexo en el sistema de archivos", e);
                            am.processError();
                            throw new ServiceException(Errores.ERROR_0045,e);
                        }

                        anexo.setObservaciones(de_Anexo.getObservaciones());

                        String validezDocumento = de_Anexo.getValidez_Documento();
                        if (StringUtils.isNotBlank(validezDocumento)) {
                            anexo.setValidezDocumento(ValidezDocumento.getValidezDocumentoValue(validezDocumento));
                        }

                        String tipoDocumento = de_Anexo.getTipo_Documento();
                        if (StringUtils.isNotBlank(tipoDocumento)) {
                            anexo.setTipoDocumento(TipoDocumento.getTipoDocumentoValue(tipoDocumento));
                        }

                        registroSir.getAnexos().add(anexo);
                    }
                }
            }
        }

        return registroSir;

    }

    /**
     * Transforma un {@link RegistroEntrada} en un {@link RegistroSir}
     * @param registroEntrada
     * @return
     * @throws Exception
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public RegistroSir transformarRegistroEntrada(RegistroEntrada registroEntrada)
            throws Exception, I18NException {

        RegistroDetalle registroDetalle = registroEntrada.getRegistroDetalle();

        RegistroSir registroSir = new RegistroSir();

        registroSir.setIndicadorPrueba(IndicadorPrueba.NORMAL);
        registroSir.setEstado(EstadoRegistroSir.PENDIENTE_ENVIO);
        registroSir.setEntidad(registroEntrada.getOficina().getOrganismoResponsable().getEntidad());

        // Segmento De_Origen_O_Remitente
        registroSir.setCodigoEntidadRegistralOrigen(registroEntrada.getOficina().getCodigo());
        registroSir.setDecodificacionEntidadRegistralOrigen(registroEntrada.getOficina().getDenominacion());
        registroSir.setNumeroRegistro(registroEntrada.getNumeroRegistroFormateado());
        registroSir.setFechaRegistro(registroEntrada.getFecha());
        registroSir.setCodigoUnidadTramitacionOrigen(registroEntrada.getOficina().getOrganismoResponsable().getCodigo());
        registroSir.setDecodificacionUnidadTramitacionOrigen(registroEntrada.getOficina().getOrganismoResponsable().getDenominacion());

        // Segmento De_Destino
        registroSir.setCodigoEntidadRegistralDestino(registroDetalle.getCodigoEntidadRegistralDestino());
        registroSir.setDecodificacionEntidadRegistralDestino(registroDetalle.getDecodificacionEntidadRegistralDestino());
        registroSir.setCodigoUnidadTramitacionDestino(registroEntrada.getDestinoExternoCodigo());
        registroSir.setDecodificacionUnidadTramitacionDestino(registroEntrada.getDestinoExternoDenominacion());

        // Segmento De_Asunto
        registroSir.setResumen(registroDetalle.getExtracto());
        if(registroEntrada.getDestino() != null){
            TraduccionCodigoAsunto tra = (TraduccionCodigoAsunto) registroDetalle.getCodigoAsunto().getTraduccion(RegwebConstantes.IDIOMA_CASTELLANO_CODIGO);
            registroSir.setCodigoAsunto(tra.getNombre());
        }
        registroSir.setReferenciaExterna(registroDetalle.getReferenciaExterna());
        registroSir.setNumeroExpediente(registroDetalle.getExpediente());

        // Segmento De_Internos_Control
        registroSir.setTipoTransporte(CODIGO_SICRES_BY_TRANSPORTE.get(registroDetalle.getTransporte()));
        registroSir.setNumeroTransporte(registroDetalle.getNumeroTransporte());
        registroSir.setNombreUsuario(registroEntrada.getUsuario().getNombreCompleto());
        registroSir.setContactoUsuario(registroEntrada.getUsuario().getUsuario().getEmail());
        registroSir.setIdentificadorIntercambio(registroDetalle.getIdentificadorIntercambio());
        registroSir.setAplicacion(registroDetalle.getAplicacion());
        registroSir.setTipoAnotacion(registroDetalle.getTipoAnotacion());
        registroSir.setDecodificacionTipoAnotacion(registroDetalle.getDecodificacionTipoAnotacion());
        registroSir.setTipoRegistro(TipoRegistro.ENTRADA);
        registroSir.setDocumentacionFisica(String.valueOf(registroDetalle.getTipoDocumentacionFisica()));
        registroSir.setObservacionesApunte(registroDetalle.getObservaciones());
        registroSir.setCodigoEntidadRegistralInicio(obtenerCodigoOficinaOrigen(registroDetalle,registroEntrada.getOficina().getCodigo()));
        registroSir.setDecodificacionEntidadRegistralInicio(obtenerDenominacionOficinaOrigen(registroDetalle, registroEntrada.getOficina().getDenominacion()));

        // Segmento De_Formulario_Genérico
        registroSir.setExpone(registroDetalle.getExpone());
        registroSir.setSolicita(registroDetalle.getSolicita());

        // Segmento De_Interesados
        registroSir.setInteresados(procesarInteresadosSir(registroDetalle.getInteresados()));

        // Segmento De_Anexos
        registroSir.setAnexos(procesarAnexosSir(registroDetalle.getAnexosFull(), registroSir.getIdentificadorIntercambio()));

        return registroSir;
    }

    /**
     * Transforma un {@link RegistroSalida} en un {@link RegistroSir}
     * @param registroSalida
     * @return
     * @throws Exception
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public RegistroSir transformarRegistroSalida(RegistroSalida registroSalida)
            throws Exception, I18NException{

        RegistroDetalle registroDetalle = registroSalida.getRegistroDetalle();

        RegistroSir registroSir = new RegistroSir();

        registroSir.setIndicadorPrueba(IndicadorPrueba.NORMAL); // todo Modificar cuando entremos en Producción
        registroSir.setEstado(EstadoRegistroSir.PENDIENTE_ENVIO);
        registroSir.setEntidad(registroSalida.getOficina().getOrganismoResponsable().getEntidad());

        // Segmento De_Origen_O_Remitente
        registroSir.setCodigoEntidadRegistralOrigen(registroSalida.getOficina().getCodigo());
        registroSir.setDecodificacionEntidadRegistralOrigen(registroSalida.getOficina().getDenominacion());
        registroSir.setNumeroRegistro(registroSalida.getNumeroRegistroFormateado());
        registroSir.setFechaRegistro(registroSalida.getFecha());
        registroSir.setCodigoUnidadTramitacionOrigen(registroSalida.getOficina().getOrganismoResponsable().getCodigo());
        registroSir.setDecodificacionUnidadTramitacionOrigen(registroSalida.getOficina().getOrganismoResponsable().getDenominacion());

        // Segmento De_Destino
        registroSir.setCodigoEntidadRegistralDestino(registroDetalle.getCodigoEntidadRegistralDestino());
        registroSir.setDecodificacionEntidadRegistralDestino(registroDetalle.getDecodificacionEntidadRegistralDestino());
        registroSir.setCodigoUnidadTramitacionDestino(obtenerCodigoUnidadTramitacionDestino(registroDetalle));
        registroSir.setDecodificacionUnidadTramitacionDestino(obtenerDenominacionUnidadTramitacionDestino(registroDetalle));

        // Segmento De_Asunto
        registroSir.setResumen(registroDetalle.getExtracto());
        /*if(registroSalida.getDestino() != null){ //todo Revisar
            TraduccionCodigoAsunto tra = (TraduccionCodigoAsunto) registroDetalle.getCodigoAsunto().getTraduccion(RegwebConstantes.IDIOMA_CASTELLANO_CODIGO);
            registroSir.setCodigoAsunto(tra.getNombre());
        }*/
        registroSir.setReferenciaExterna(registroDetalle.getReferenciaExterna());
        registroSir.setNumeroExpediente(registroDetalle.getExpediente());

        // Segmento De_Internos_Control
        registroSir.setTipoTransporte(CODIGO_SICRES_BY_TRANSPORTE.get(registroDetalle.getTransporte()));
        registroSir.setNumeroTransporte(registroDetalle.getNumeroTransporte());
        registroSir.setNombreUsuario(registroSalida.getUsuario().getNombreCompleto());
        registroSir.setContactoUsuario(registroSalida.getUsuario().getUsuario().getEmail());
        registroSir.setIdentificadorIntercambio(registroDetalle.getIdentificadorIntercambio());
        registroSir.setAplicacion(registroDetalle.getAplicacion());
        registroSir.setTipoAnotacion(registroDetalle.getTipoAnotacion());
        registroSir.setDecodificacionTipoAnotacion(registroDetalle.getDecodificacionTipoAnotacion());
        registroSir.setTipoRegistro(TipoRegistro.SALIDA);
        registroSir.setDocumentacionFisica(String.valueOf(registroDetalle.getTipoDocumentacionFisica()));
        registroSir.setObservacionesApunte(registroDetalle.getObservaciones());
        registroSir.setCodigoEntidadRegistralInicio(obtenerCodigoOficinaOrigen(registroDetalle, registroSalida.getOficina().getCodigo()));
        registroSir.setDecodificacionEntidadRegistralInicio(obtenerDenominacionOficinaOrigen(registroDetalle, registroSalida.getOficina().getDenominacion()));

        // Segmento De_Formulario_Genérico
        registroSir.setExpone(registroDetalle.getExpone());
        registroSir.setSolicita(registroDetalle.getSolicita());

        // Segmento De_Interesados
        registroSir.setInteresados(procesarInteresadosSir(registroDetalle.getInteresados()));

        // Segmento De_Anexos
        registroSir.setAnexos(procesarAnexosSir(registroDetalle.getAnexosFull(), registroSir.getIdentificadorIntercambio()));

        return registroSir;
    }

    /**
     * Transforma una Lista de {@link InteresadoSir} en una Lista de {@link Interesado}
     * @param interesados
     * @return
     * @throws Exception
     */
    private List<InteresadoSir> procesarInteresadosSir(List<Interesado> interesados) throws Exception{
        List<InteresadoSir> interesadosSir = new ArrayList<InteresadoSir>();

        for (Interesado interesado : interesados) {
            InteresadoSir interesadoSir = null;

            if (!interesado.getIsRepresentante()){
                interesadoSir = transformarInteresadoSir(interesado, interesado.getRepresentante());
                interesadosSir.add(interesadoSir);
            }


        }
        return interesadosSir;
    }

    /**
     * Transforma un {@link InteresadoSir} en un {@link Interesado}
     * @param interesado
     * @param representante
     * @return Interesado de tipo {@link Interesado}
     * @throws Exception
     */
    private InteresadoSir transformarInteresadoSir(Interesado interesado, Interesado representante) throws Exception{

        InteresadoSir interesadoSir = new InteresadoSir();

        if (interesado.getTipoDocumentoIdentificacion() != null) {
            interesadoSir.setTipoDocumentoIdentificacionInteresado(String.valueOf(CODIGO_NTI_BY_TIPODOCUMENTOID.get(interesado.getTipoDocumentoIdentificacion())));
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getDocumento())) {
            interesadoSir.setDocumentoIdentificacionInteresado(interesado.getDocumento());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getRazonSocial())) {
            interesadoSir.setRazonSocialInteresado(interesado.getRazonSocial());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getNombre())) {
            interesadoSir.setNombreInteresado(interesado.getNombre());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getApellido1())) {
            interesadoSir.setPrimerApellidoInteresado(interesado.getApellido1());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getApellido2())) {
            interesadoSir.setSegundoApellidoInteresado(interesado.getApellido2());
        }

        if(interesado.getPais() != null){
            interesadoSir.setCodigoPaisInteresado(interesado.getPais().getCodigoPais().toString());
        }

        if(interesado.getProvincia() != null){
            interesadoSir.setCodigoProvinciaInteresado(interesado.getProvincia().getCodigoProvincia().toString());
        }

        if(interesado.getLocalidad() != null){
            interesadoSir.setCodigoMunicipioInteresado(interesado.getLocalidad().getCodigoLocalidad().toString());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getDireccion())) {
            interesadoSir.setDireccionInteresado(interesado.getDireccion());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getCp())) {
            interesadoSir.setCodigoPostalInteresado(interesado.getCp());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getEmail())) {
            interesadoSir.setCorreoElectronicoInteresado(interesado.getEmail());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getTelefono())) {
            interesadoSir.setTelefonoInteresado(interesado.getTelefono());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getDireccionElectronica())) {
            interesadoSir.setDireccionElectronicaHabilitadaInteresado(interesado.getDireccionElectronica());
        }

        if (interesado.getCanal() != null) {
            interesadoSir.setCanalPreferenteComunicacionInteresado(CODIGO_BY_CANALNOTIFICACION.get(interesado.getCanal()));
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesado.getObservaciones())) {
            interesadoSir.setObservaciones(interesado.getObservaciones());
        }

        // Si tiene representante, también lo transformamos
        if(representante != null){

            if (representante.getTipoDocumentoIdentificacion() != null) {
                interesadoSir.setTipoDocumentoIdentificacionRepresentante(String.valueOf(CODIGO_NTI_BY_TIPODOCUMENTOID.get(representante.getTipoDocumentoIdentificacion())));
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getDocumento())) {
                interesadoSir.setDocumentoIdentificacionRepresentante(representante.getDocumento());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getRazonSocial())) {
                interesadoSir.setRazonSocialRepresentante(representante.getRazonSocial());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getNombre())) {
                interesadoSir.setNombreRepresentante(representante.getNombre());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getApellido1())) {
                interesadoSir.setPrimerApellidoRepresentante(representante.getApellido1());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getApellido2())) {
                interesadoSir.setSegundoApellidoRepresentante(representante.getApellido2());
            }

            if(representante.getPais() != null){
                interesadoSir.setCodigoPaisRepresentante(representante.getPais().getCodigoPais().toString());
            }

            if(representante.getProvincia() != null){
                interesadoSir.setCodigoProvinciaRepresentante(representante.getProvincia().getCodigoProvincia().toString());
            }

            if(representante.getLocalidad() != null){
                interesadoSir.setCodigoMunicipioRepresentante(representante.getLocalidad().getCodigoLocalidad().toString());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getDireccion())) {
                interesadoSir.setDireccionRepresentante(representante.getDireccion());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getCp())) {
                interesadoSir.setCodigoPostalRepresentante(representante.getCp());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getEmail())) {
                interesadoSir.setCorreoElectronicoRepresentante(representante.getEmail());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getTelefono())) {
                interesadoSir.setTelefonoRepresentante(representante.getTelefono());
            }

            if (!es.caib.regweb3.utils.StringUtils.isEmpty(representante.getDireccionElectronica())) {
                interesadoSir.setDireccionElectronicaHabilitadaRepresentante(representante.getDireccionElectronica());
            }

            if (representante.getCanal() != null) {
                interesadoSir.setCanalPreferenteComunicacionRepresentante(CODIGO_BY_CANALNOTIFICACION.get(representante.getCanal()));
            }

        }

        return interesadoSir;

    }

    /**
     *
     * @param anexosFull
     * @param identificadorIntercambio
     * @return
     */
    private List<AnexoSir> procesarAnexosSir(List<AnexoFull> anexosFull, String identificadorIntercambio) throws Exception{

        List<AnexoSir> anexosSir = new ArrayList<AnexoSir>();
        int secuencia = 0;

        for(AnexoFull anexoFull:anexosFull){


            final int modoFirma = anexoFull.getAnexo().getModoFirma();
            Anexo anexo = anexoFull.getAnexo();

            switch (modoFirma){

                case MODO_FIRMA_ANEXO_ATTACHED:

                    SignatureCustody sc = anexoFull.getSignatureCustody();

                    String identificador_fichero = generateIdentificadorFichero(identificadorIntercambio, secuencia, sc.getName());
                    secuencia++;

                    AnexoSir anexoSir = crearAnexoSir(sc.getName(),identificador_fichero,
                            CODIGO_SICRES_BY_TIPOVALIDEZDOCUMENTO.get(anexo.getValidezDocumento()),
                            CODIGO_SICRES_BY_TIPO_DOCUMENTO.get(anexo.getTipoDocumento()),anexo.getCertificado(),
                            anexo.getFirma(),anexo.getTimestamp(), anexo.getValidacionOCSPCertificado(),
                            anexo.getHash(),sc.getMime(),sc.getData(),identificador_fichero,
                            anexo.getObservaciones());

                    anexosSir.add(anexoSir);

                    break;

                case MODO_FIRMA_ANEXO_DETACHED:

                    // ================= SEGMENTO 1: DOCUMENT ==================
                    anexoSir = new AnexoSir();

                    DocumentCustody dc = anexoFull.getDocumentoCustody();

                    identificador_fichero = generateIdentificadorFichero(identificadorIntercambio, secuencia, dc.getName());
                    secuencia++;

                    anexoSir = crearAnexoSir(dc.getName(),identificador_fichero,
                            CODIGO_SICRES_BY_TIPOVALIDEZDOCUMENTO.get(anexo.getValidezDocumento()),
                            CODIGO_SICRES_BY_TIPO_DOCUMENTO.get(anexo.getTipoDocumento()),anexo.getCertificado(),
                            anexo.getFirma(),anexo.getTimestamp(), anexo.getValidacionOCSPCertificado(),
                            anexo.getHash(),dc.getMime(),dc.getData(),null,
                            anexo.getObservaciones());

                    anexosSir.add(anexoSir);

                    // ================= SEGMENTO 2: FIRMA ==================
                    anexoSir = new AnexoSir();

                    sc = anexoFull.getSignatureCustody();

                    String identificador_fichero_FIRMA = generateIdentificadorFichero(identificadorIntercambio, secuencia, sc.getName());
                    secuencia++;

                    anexoSir = crearAnexoSir(sc.getName(),identificador_fichero_FIRMA,
                            CODIGO_SICRES_BY_TIPOVALIDEZDOCUMENTO.get(anexo.getValidezDocumento()),
                            CODIGO_SICRES_BY_TIPO_DOCUMENTO.get(TIPO_DOCUMENTO_FICHERO_TECNICO), null,
                            null,anexo.getTimestamp(), null,
                            anexo.getHash(),sc.getMime(),sc.getData(),identificador_fichero,
                            anexo.getObservaciones());

                    anexosSir.add(anexoSir);

                    break;
            }


        }

        return anexosSir;
    }

    /**
     *
     * @param nombreFichero
     * @param identificadorFichero
     * @param validezDocumento
     * @param tipoDocumento
     * @param certificado
     * @param firma
     * @param timeStamp
     * @param validacionOCSPCertificado
     * @param hash
     * @param tipoMime
     * @param anexoData
     * @param identificadorDocumentoFirmado
     * @param observaciones
     * @return
     */
    private AnexoSir crearAnexoSir(String nombreFichero, String identificadorFichero, String validezDocumento, String tipoDocumento, byte[] certificado,
                                   byte[] firma, byte[] timeStamp, byte[] validacionOCSPCertificado, byte[] hash, String tipoMime,
                                   byte[] anexoData, String identificadorDocumentoFirmado, String observaciones){
        AnexoSir anexoSir = new AnexoSir();

        anexoSir.setNombreFichero(nombreFichero);
        anexoSir.setIdentificadorFichero(identificadorFichero);
        if(validezDocumento != null){
            anexoSir.setValidezDocumento(validezDocumento);
        }
        anexoSir.setTipoDocumento(tipoDocumento);
        if(certificado != null){
            anexoSir.setCertificado(certificado);
        }
        if(firma != null){
            anexoSir.setFirma(firma);
        }
        if(timeStamp != null){
            anexoSir.setTimestamp(timeStamp);
        }
        if(validacionOCSPCertificado != null){
            anexoSir.setValidacionOCSPCertificado(validacionOCSPCertificado);
        }

        anexoSir.setHash(hash);
        if(tipoMime != null){
            anexoSir.setTipoMIME(tipoMime);
        }
        if(anexoData != null){
            anexoSir.setAnexoData(anexoData);
        }
        if(identificadorDocumentoFirmado != null){
            anexoSir.setIdentificadorDocumentoFirmado(identificadorDocumentoFirmado);
        }

        anexoSir.setObservaciones(observaciones);

        return anexoSir;
    }

    /**
     * Metodo que genera identificador de anxso según el patron
     * identificadorIntercambio_01_secuencia.extension
     * donde secuencia es cadena que repesenta secuencia en formato 0001 (leftpading con 0 y máximo de 4 caracteres)
     * donde extesion es la extension del anexo
     *
     * @param identificadorIntercambio
     * @param secuencia
     * @param fileName
     * @return
     */
    protected String generateIdentificadorFichero(String identificadorIntercambio, int secuencia, String fileName) {

        String result = new StringBuffer()
                .append(identificadorIntercambio)
                .append("_01_")
                .append(StringUtils.leftPad(
                        String.valueOf(secuencia), 4, "0"))
                .append(".").append(getExtension(fileName)).toString();

        return result;
    }

    /**
     * Obtiene la Extensión de un Fichero a partir de su nombre
     *
     * @param nombreFichero
     * @return extensión del fichero
     */
    private String getExtension(String nombreFichero) {
        String extension = "";

        int i = nombreFichero.lastIndexOf('.');
        if (i > 0) {
            extension = nombreFichero.substring(i + 1);
        }

        return extension;
    }

    /**
     * Genera el Hash mediante SHA-256 del contenido del documento y lo codifica en base64
     *
     * @param documentoData
     * @return
     * @throws Exception
     */
    private byte[] obtenerHash(byte[] documentoData) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(documentoData);

        return Base64.encodeBase64(digest);

    }

    /**
     * Obtiene el código Oficina de Origen dependiendo de si es interna o externa
     *
     * @param registroDetalle
     * @param codigoOficia
     * @return
     * @throws Exception
     */
    private String obtenerCodigoOficinaOrigen(RegistroDetalle registroDetalle, String codigoOficia) {
        String codOficinaOrigen = null;

        if ((registroDetalle.getOficinaOrigenExternoCodigo() == null) && (registroDetalle.getOficinaOrigen() == null)) {
            codOficinaOrigen = codigoOficia;
        } else if (registroDetalle.getOficinaOrigenExternoCodigo() != null) {
            codOficinaOrigen = registroDetalle.getOficinaOrigenExternoCodigo();
        } else {
            codOficinaOrigen = registroDetalle.getOficinaOrigen().getCodigo();
        }

        return codOficinaOrigen;
    }

    /**
     * Obtiene el denominación Oficina de Origen dependiendo de si es interna o externa
     *
     * @param registroDetalle
     * @param denominacionOficia
     * @return
     * @throws Exception
     */
    private String obtenerDenominacionOficinaOrigen(RegistroDetalle registroDetalle, String denominacionOficia) {
        String denominacionOficinaOrigen = null;

        if ((registroDetalle.getOficinaOrigenExternoCodigo() == null) && (registroDetalle.getOficinaOrigen() == null)) {
            denominacionOficinaOrigen = denominacionOficia;
        } else if (registroDetalle.getOficinaOrigenExternoCodigo() != null) {
            denominacionOficinaOrigen = registroDetalle.getOficinaOrigenExternoDenominacion();
        } else {
            denominacionOficinaOrigen = registroDetalle.getOficinaOrigen().getDenominacion();
        }

        return denominacionOficinaOrigen;
    }

    /**
     *
     * @param registroDetalle
     * @return
     */
    private String obtenerCodigoUnidadTramitacionDestino(RegistroDetalle registroDetalle){

        List<Interesado> interesados = registroDetalle.getInteresados();

        for (Interesado interesado : interesados) {
            if(interesado.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_ADMINISTRACION)){

                return interesado.getCodigoDir3();
            }
        }

        return null;
    }

    /**
     *
     * @param registroDetalle
     * @return
     */
    private String obtenerDenominacionUnidadTramitacionDestino(RegistroDetalle registroDetalle){

        List<Interesado> interesados = registroDetalle.getInteresados();

        for (Interesado interesado : interesados) {
            if(interesado.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_ADMINISTRACION)){

                return interesado.getRazonSocial();
            }
        }

        return null;
    }

    /**
     * Crea un Interesado tipo Persona Juridica a partir del Código Unidad De Gestión de destino o si no está informado,
     * a partir del Código Entidad Registral de destino
     * @return
     */
    private InteresadoSir crearInteresadoJuridico(FicheroIntercambio ficheroIntercambio){

        InteresadoSir interesadoSalida = new InteresadoSir();

        if(StringUtils.isNotBlank(ficheroIntercambio.getCodigoUnidadTramitacionDestino())){

            interesadoSalida.setTipoDocumentoIdentificacionInteresado(TipoDocumentoIdentificacion.CODIGO_ORIGEN_VALUE.getValue());
            interesadoSalida.setDocumentoIdentificacionInteresado(ficheroIntercambio.getCodigoUnidadTramitacionDestino());

            if(StringUtils.isNotBlank(ficheroIntercambio.getDescripcionUnidadTramitacionDestino())){
                interesadoSalida.setRazonSocialInteresado(ficheroIntercambio.getDescripcionUnidadTramitacionDestino());
            }else{
                interesadoSalida.setRazonSocialInteresado(Dir3CaibUtils.denominacion(ficheroIntercambio.getCodigoUnidadTramitacionDestino(),RegwebConstantes.UNIDAD));

            }


        }else{
            try {
                Dir3CaibObtenerOficinasWs oficinasService = Dir3CaibUtils.getObtenerOficinasService();

                OficinaTF oficinaTF = oficinasService.obtenerOficina(ficheroIntercambio.getCodigoEntidadRegistralDestino(),null,null);

                interesadoSalida.setTipoDocumentoIdentificacionInteresado(TipoDocumentoIdentificacion.CODIGO_ORIGEN_VALUE.getValue());
                interesadoSalida.setDocumentoIdentificacionInteresado(oficinaTF.getCodUoResponsable());
                interesadoSalida.setRazonSocialInteresado(Dir3CaibUtils.denominacion(oficinaTF.getCodUoResponsable(),RegwebConstantes.UNIDAD));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return interesadoSalida;
    }

    /**
     * Transforma un {@link RegistroSir} en un {@link RegistroEntrada}
     * @param registroSir
     * @param usuario
     * @param oficinaActiva
     * @param idLibro
     * @param idIdioma
     * @param idTipoAsunto
     * @return
     * @throws Exception
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public RegistroEntrada transformarRegistroSirEntrada(RegistroSir registroSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs)
            throws Exception, I18NException, I18NValidationException {

        Libro libro = libroEjb.findById(idLibro);

        RegistroEntrada registroEntrada = new RegistroEntrada();
        registroEntrada.setUsuario(usuario);
        registroEntrada.setOficina(oficinaActiva);
        registroEntrada.setEstado(RegwebConstantes.REGISTRO_VALIDO);
        registroEntrada.setLibro(libro);

        // Organismo destino
        Organismo organismoDestino;
        if(registroSir.getCodigoUnidadTramitacionDestino() != null){
            organismoDestino = organismoEjb.findByCodigoEntidad(registroSir.getCodigoUnidadTramitacionDestino(),usuario.getEntidad().getId());
            registroEntrada.setDestino(organismoDestino);
        }else{
            Oficina oficina = oficinaEjb.findByCodigoEntidad(registroSir.getCodigoEntidadRegistralDestino(),usuario.getEntidad().getId());
            organismoDestino = organismoEjb.findByCodigoEntidad(oficina.getOrganismoResponsable().getCodigo(),usuario.getEntidad().getId());
        }

        registroEntrada.setDestino(organismoDestino);
        registroEntrada.setDestinoExternoCodigo(null);
        registroEntrada.setDestinoExternoDenominacion(null);

        // RegistroDetalle
        registroEntrada.setRegistroDetalle(getRegistroDetalle(registroSir, idIdioma, idTipoAsunto));

        // Interesados
        List<Interesado> interesados = procesarInteresados(registroSir.getInteresados());

        // Anexos
        List<AnexoFull> anexosFull = procesarAnexos(registroSir, camposNTIs);

        // Registramos el Registro Entrada
        synchronized (this){
            registroEntrada = registroEntradaEjb.registrarEntrada(registroEntrada, usuario,interesados,anexosFull);
        }

        return registroEntrada;
    }

    /**
     * Transforma un {@link RegistroSir} en un {@link RegistroSalida}
     * @param registroSir
     * @param usuario
     * @param oficinaActiva
     * @param idLibro
     * @param idIdioma
     * @param idTipoAsunto
     * @return
     * @throws Exception
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public RegistroSalida transformarRegistroSirSalida(RegistroSir registroSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs) throws Exception, I18NException, I18NValidationException {

        Libro libro = libroEjb.findById(idLibro);

        RegistroSalida registroSalida = new RegistroSalida();
        registroSalida.setUsuario(usuario);
        registroSalida.setOficina(oficinaActiva);
        registroSalida.setEstado(RegwebConstantes.REGISTRO_VALIDO);
        registroSalida.setLibro(libro);

        // Organismo origen
        // TODO Esta asignación es incorrecta
        Organismo organismoOrigen;
        if(registroSir.getCodigoUnidadTramitacionDestino() != null){
            organismoOrigen = organismoEjb.findByCodigoLigero(registroSir.getCodigoUnidadTramitacionDestino());
            registroSalida.setOrigen(organismoOrigen);
        }

        registroSalida.setOrigenExternoCodigo(null);
        registroSalida.setOrigenExternoDenominacion(null);

        // RegistroDetalle
        registroSalida.setRegistroDetalle(getRegistroDetalle(registroSir, idIdioma, idTipoAsunto));

        // Interesados
        List<Interesado> interesados = procesarInteresados(registroSir.getInteresados());

        // Anexos
        List<AnexoFull> anexosFull = procesarAnexos(registroSir, camposNTIs);

        // Registramos el Registro Entrada
        synchronized (this){
            registroSalida = registroSalidaEjb.registrarSalida(registroSalida, usuario,interesados, anexosFull);
        }

        return registroSalida;
    }

    /**
     * Obtiene un {@link RegistroDetalle} a partir de los datos de un RegistroSir
     * @param registroSir
     * @param idIdioma
     * @param idTipoAsunto
     * @return
     * @throws Exception
     */
    private RegistroDetalle getRegistroDetalle(RegistroSir registroSir, Long idIdioma, Long idTipoAsunto) throws Exception{

        RegistroDetalle registroDetalle = new RegistroDetalle();

        registroDetalle.setExtracto(registroSir.getResumen());
        registroDetalle.setTipoDocumentacionFisica(Long.valueOf(registroSir.getDocumentacionFisica()));
        registroDetalle.setIdioma(idIdioma);
        registroDetalle.setTipoAsunto(new TipoAsunto(idTipoAsunto));
        registroDetalle.setCodigoAsunto(null);

        if(registroSir.getTipoTransporte() != null){
            registroDetalle.setTransporte(Long.valueOf(registroSir.getTipoTransporte()));
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getNumeroTransporte())){
            registroDetalle.setNumeroTransporte(registroSir.getNumeroTransporte());
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getObservacionesApunte())){
            registroDetalle.setObservaciones(registroSir.getObservacionesApunte());
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getReferenciaExterna())){
            registroDetalle.setReferenciaExterna(registroSir.getReferenciaExterna());
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getNumeroExpediente())){
            registroDetalle.setExpediente(registroSir.getNumeroExpediente());
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getExpone())){
            registroDetalle.setExpone(registroSir.getExpone());
        }
        if(!es.caib.regweb3.utils.StringUtils.isEmpty(registroSir.getSolicita())){
            registroDetalle.setSolicita(registroSir.getSolicita());
        }

        registroDetalle.setOficinaOrigen(null);
        registroDetalle.setOficinaOrigenExternoCodigo(registroSir.getCodigoEntidadRegistralOrigen());
        registroDetalle.setOficinaOrigenExternoDenominacion(registroSir.getDecodificacionEntidadRegistralOrigen());

        registroDetalle.setNumeroRegistroOrigen(registroSir.getNumeroRegistro());
        registroDetalle.setFechaOrigen(registroSir.getFechaRegistro());

        // Interesados
        registroDetalle.setInteresados(null);

        // Anexos
        registroDetalle.setAnexos(null);

        return registroDetalle;
    }




    /**
     * Transforma una Lista de {@link InteresadoSir} en una Lista de {@link Interesado}
     * @param interesadosSir
     * @return
     * @throws Exception
     */
    private List<Interesado> procesarInteresados(List<InteresadoSir> interesadosSir) throws Exception{
        List<Interesado> interesados = new ArrayList<Interesado>();
        for (InteresadoSir interesadoSir : interesadosSir) {
            Interesado interesado = transformarInteresado(interesadoSir);

            if (interesadoSir.getRepresentante()) {
                log.info("Tiene representante");
                Interesado representante = transformarRepresentante(interesadoSir);
                representante.setIsRepresentante(true);
                representante.setRepresentado(interesado);
                interesado.setRepresentante(representante);

                interesados.add(interesado);
                interesados.add(representante);
            }else{
                interesados.add(interesado);
            }


        }
        return interesados;
    }

    /**
     * Transforma un {@link InteresadoSir} en un {@link Interesado}
     * @param interesadoSir
     * @return Interesado de tipo {@link Interesado}
     * @throws Exception
     */
    private Interesado transformarInteresado(InteresadoSir interesadoSir) throws Exception{

        Interesado interesado = new Interesado();
        interesado.setId((long) (Math.random() * 10000));
        interesado.setIsRepresentante(false);

        // Averiguamos que tipo es el Interesado
        if (es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getRazonSocialInteresado())) {
            interesado.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_FISICA);

        } else {
            interesado.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_JURIDICA);
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getRazonSocialInteresado())) {
            interesado.setRazonSocial(interesadoSir.getRazonSocialInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getNombreInteresado())) {
            interesado.setNombre(interesadoSir.getNombreInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getPrimerApellidoInteresado())) {
            interesado.setApellido1(interesadoSir.getPrimerApellidoInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getSegundoApellidoInteresado())) {
            interesado.setApellido2(interesadoSir.getSegundoApellidoInteresado());
        }
        if (interesadoSir.getTipoDocumentoIdentificacionInteresado() != null) {
            interesado.setTipoDocumentoIdentificacion(RegwebConstantes.TIPODOCUMENTOID_BY_CODIGO_NTI.get(interesadoSir.getTipoDocumentoIdentificacionInteresado().charAt(0)));
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getDocumentoIdentificacionInteresado())) {
            interesado.setDocumento(interesadoSir.getDocumentoIdentificacionInteresado());
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getCodigoPaisInteresado())) {
            try {
                interesado.setPais(catPaisEjb.findByCodigo(Long.valueOf(interesadoSir.getCodigoPaisInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getCodigoProvinciaInteresado())) {
            try {
                interesado.setProvincia(catProvinciaEjb.findByCodigo(Long.valueOf(interesadoSir.getCodigoProvinciaInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getCodigoMunicipioInteresado())) {
            try {
                interesado.setLocalidad(catLocalidadEjb.findByLocalidadProvincia(Long.valueOf(interesadoSir.getCodigoMunicipioInteresado()), Long.valueOf(interesadoSir.getCodigoProvinciaInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getDireccionInteresado())) {
            interesado.setDireccion(interesadoSir.getDireccionInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getCodigoPostalInteresado())) {
            interesado.setCp(interesadoSir.getCodigoPostalInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getCorreoElectronicoInteresado())) {
            interesado.setEmail(interesadoSir.getCorreoElectronicoInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getTelefonoInteresado())) {
            interesado.setTelefono(interesadoSir.getTelefonoInteresado());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getDireccionElectronicaHabilitadaInteresado())) {
            interesado.setDireccionElectronica(interesadoSir.getDireccionElectronicaHabilitadaInteresado());
        }
        if (interesadoSir.getCanalPreferenteComunicacionInteresado() != null) {
            interesado.setCanal(RegwebConstantes.CANALNOTIFICACION_BY_CODIGO.get(interesadoSir.getCanalPreferenteComunicacionInteresado()));
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(interesadoSir.getObservaciones())) {
            interesado.setObservaciones(interesadoSir.getObservaciones());
        }

        return interesado;

    }


    /** Transforma un {@link InteresadoSir} en un {@link Interesado}
     *
     * @param representanteSir
     * @return Interesado de tipo {@link Interesado}
     */
    private Interesado transformarRepresentante(InteresadoSir representanteSir) {

        Interesado representante = new Interesado();
        representante.setId((long) (Math.random() * 10000));
        representante.setIsRepresentante(true);

        // Averiguamos que tipo es el Representante
        if (es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getRazonSocialRepresentante())) {
            representante.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_FISICA);

        } else {
            representante.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_JURIDICA);
        }

        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getRazonSocialRepresentante())) {
            representante.setRazonSocial(representanteSir.getRazonSocialRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getNombreRepresentante())) {
            representante.setNombre(representanteSir.getNombreRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getPrimerApellidoRepresentante())) {
            representante.setApellido1(representanteSir.getPrimerApellidoRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getSegundoApellidoRepresentante())) {
            representante.setApellido2(representanteSir.getSegundoApellidoRepresentante());
        }
        if (representanteSir.getTipoDocumentoIdentificacionRepresentante() != null) {
            representante.setTipoDocumentoIdentificacion(RegwebConstantes.TIPODOCUMENTOID_BY_CODIGO_NTI.get(representanteSir.getTipoDocumentoIdentificacionRepresentante().charAt(0)));
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getDocumentoIdentificacionRepresentante())) {
            representante.setDocumento(representanteSir.getDocumentoIdentificacionRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getCodigoPaisRepresentante())) {
            try {
                representante.setPais(catPaisEjb.findByCodigo(Long.valueOf(representanteSir.getCodigoPaisRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getCodigoProvinciaRepresentante())) {
            try {
                representante.setProvincia(catProvinciaEjb.findByCodigo(Long.valueOf(representanteSir.getCodigoProvinciaRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getCodigoMunicipioRepresentante())) {
            try {
                representante.setLocalidad(catLocalidadEjb.findByLocalidadProvincia(Long.valueOf(representanteSir.getCodigoMunicipioRepresentante()), Long.valueOf(representanteSir.getCodigoProvinciaRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getDireccionRepresentante())) {
            representante.setDireccion(representanteSir.getDireccionRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getCodigoPostalRepresentante())) {
            representante.setCp(representanteSir.getCodigoPostalRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getCorreoElectronicoRepresentante())) {
            representante.setEmail(representanteSir.getCorreoElectronicoRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getTelefonoRepresentante())) {
            representante.setTelefono(representanteSir.getTelefonoRepresentante());
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getDireccionElectronicaHabilitadaRepresentante())) {
            representante.setDireccionElectronica(representanteSir.getDireccionElectronicaHabilitadaRepresentante());
        }
        if (representanteSir.getCanalPreferenteComunicacionRepresentante() != null) {
            representante.setCanal(RegwebConstantes.CANALNOTIFICACION_BY_CODIGO.get(representanteSir.getCanalPreferenteComunicacionRepresentante()));
        }
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(representanteSir.getObservaciones())) {
            representante.setObservaciones(representanteSir.getObservaciones());
        }

        return representante;

    }



    /**
     *
     * @param registroSir
     * @param camposNTIs representa la lista de anexos del RegistroSir en los que el usuario ha especificado
     *                          los valores de los campos NTI no informados por SICRES (validez Documento, origen, Tipo Documental)
     * @return
     * @throws Exception
     */
    private List<AnexoFull> procesarAnexos(RegistroSir registroSir, List<CamposNTI> camposNTIs) throws Exception {


        List<AnexoFull> anexos = new ArrayList<AnexoFull>();
        HashMap<String,AnexoFull> mapAnexosFull = new HashMap<String, AnexoFull>();

        //Aqui buscamos los camposNTI del anexoSir con el que se corresponde para pasarlo al método transformarAnexo
        for (AnexoSir anexoSir : registroSir.getAnexos()) {
            for (CamposNTI cnti : camposNTIs) {
                if (anexoSir.getId().equals(cnti.getId())) {
                    AnexoFull anexoFull = transformarAnexo(anexoSir, registroSir.getEntidad().getId(), mapAnexosFull, cnti);
                    mapAnexosFull.put(anexoSir.getIdentificadorFichero(), anexoFull);
                }
            }
        }
        anexos = new ArrayList<AnexoFull>(mapAnexosFull.values());
        return anexos;
    }

    /**
     * Transforma un {@link AnexoSir} en un {@link AnexoFull}
     * A partir de la clase AnexoSir transformamos a un AnexoFull para poder guardarlo en regweb3.
     * La particularidad de este método, es que se necesita pasar una lista de los anexos que se han procesado anteriormente
     * del AnexoSir que nos envian, ya que puede haber anexos que son firma de uno anteriormente procesado y lo necesitamos
     * para acabar de montar el anexo ya que para regweb3 el anexo y su firma van en el mismo AnexoFull.
     * Además ahora se pasa una lista de anexosSirRecibidos ya que para cada anexo el usuario debe escoger 3 campos que
     * pueden no venir informados en SICRES y son obligatorios en NTI.
     * Los campos en concreto son (validezDocumento, origen, tipo Documental)
     * @param anexoSir
     * @param idEntidad
     * @param anexosProcesados Lista de anexos procesados anteriores.
     * @return AnexoFull tipo {@link AnexoFull}
     */
    private AnexoFull transformarAnexo(AnexoSir anexoSir, Long idEntidad, Map<String, AnexoFull> anexosProcesados, CamposNTI camposNTI) throws Exception {

        AnexoFull anexoFull = new AnexoFull();
        Anexo anexo = new Anexo();

        anexo.setTitulo(anexoSir.getNombreFichero());

        if (anexoSir.getValidezDocumento() != null) {
            anexo.setValidezDocumento(Long.valueOf(anexoSir.getValidezDocumento()));
        } else {//Campo NTI Cogemos la validez de documento indicada por el usuario
            if (camposNTI.getIdValidezDocumento() != null) {
                anexo.setValidezDocumento(Long.valueOf(camposNTI.getIdValidezDocumento()));
            }
        }

        if (anexoSir.getTipoDocumento() != null) {
            anexo.setTipoDocumento(Long.valueOf(anexoSir.getTipoDocumento()));
        }
        anexo.setObservaciones(anexoSir.getObservaciones());

        //Campo NTI no informados, asignamos lo que indica el usuario
        if (camposNTI.getIdOrigen() != null) {
            anexo.setOrigenCiudadanoAdmin(camposNTI.getIdOrigen().intValue());
        }

        // Si el usuario no especifica el tipo Documental, por defecto se pone TD99 - Otros
        if (camposNTI.getIdTipoDocumental() == null || camposNTI.getIdTipoDocumental().equals("")) {
            anexo.setTipoDocumental(tipoDocumentalEjb.findByCodigoEntidad("TD99", idEntidad));
        }else{
            anexo.setTipoDocumental(tipoDocumentalEjb.findByCodigoEntidad(camposNTI.getIdTipoDocumental(), idEntidad));
        }

        if(anexoSir.getCertificado()!= null) {
            anexo.setCertificado(anexoSir.getCertificado());
        };

        if (anexoSir.getFirma() != null) {
            anexo.setFirma(anexoSir.getFirma());

        };
        if (anexoSir.getTimestamp() != null) {
            anexo.setTimestamp(anexoSir.getTimestamp());
        }

        if (anexoSir.getValidacionOCSPCertificado() != null) {
            anexo.setValidacionOCSPCertificado(anexoSir.getValidacionOCSPCertificado());
        }

        if(anexoSir.getHash()!= null){
            anexo.setHash(anexoSir.getHash());
        }



        DocumentCustody dc;
        SignatureCustody sc;
        // Si el anexo tiene identificador_documento_firmado, es que es la firma de un anexo anterior.
        if (!es.caib.regweb3.utils.StringUtils.isEmpty(anexoSir.getIdentificadorDocumentoFirmado()) && anexoSir.getIdentificadorDocumentoFirmado() != null) {
            String identificadorDocumentoFirmado = anexoSir.getIdentificadorDocumentoFirmado();
            if(identificadorDocumentoFirmado.equals(anexoSir.getIdentificadorFichero())){
                //Caso Firma Attached caso 5, se guarda el documento en signatureCustody, como lo especifica el API DE CUSTODIA(II)
                anexo.setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED);
                sc = getSignatureCustody(anexoSir, null, anexo.getModoFirma());
                anexoFull.setDocumentoCustody(null);
                anexoFull.setSignatureCustody(sc);
                anexoFull.setAnexo(anexo);

            }else{
                //Caso Firma Detached, caso 4, se guarda 1 anexo, con el doc original en documentCustody y la firma en SignatureCustody
                anexoFull = anexosProcesados.get(identificadorDocumentoFirmado);//obtenemos el documento original previamente procesado
                anexoFull.getAnexo().setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED); // asignamos el modo de firma
                sc = getSignatureCustody(anexoSir, anexoFull.getDocumentoCustody(), anexoFull.getAnexo().getModoFirma());
                anexoFull.setSignatureCustody(sc);
                //eliminamos de los procesados el documento cuya firma es este anexo que estamos tratando ahora.
                //si no guardariamos 2 anexos, el documento original y el documento original con la firma.
                anexosProcesados.remove(identificadorDocumentoFirmado);

            }
            // Al ser un anexo con firma, si sicres no la informa, la informará el usuario. Si el usuario indica "COPIA"
            // regweb la cambia a COPIA_COMPULSADA porque aqui ya sabe que hay firma y si hay firma la validezDocumento no puede ser "COPIA".
            if (anexoFull.getAnexo().getValidezDocumento().equals( RegwebConstantes.TIPOVALIDEZDOCUMENTO_COPIA)) {
                anexoFull.getAnexo().setValidezDocumento(RegwebConstantes.TIPOVALIDEZDOCUMENTO_COPIA_COMPULSADA);
            }
        } else { // El anexo no es firma de nadie
            if (anexoSir.getFirma() == null) { //Anexo normal
                anexo.setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_SINFIRMA);
            } else { //La firma es un CSV.
                anexo.setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_SINFIRMA);
                //anexo.setCsv(anexoSir.getFirma_Documento());
                //TODO Metadada a custodia pel csv.

            }
            dc = getDocumentCustody(anexoSir);
            anexoFull.setAnexo(anexo);
            anexoFull.setDocumentoCustody(dc);
        }


        return anexoFull;

    }


    /**
     *
     * @param anexoSir
     * @return
     * @throws Exception
     */
    private DocumentCustody getDocumentCustody(AnexoSir anexoSir) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("  ------------------------------");
            log.debug(" anexoSir.getAnexo = " + anexoSir.getAnexo());
        }
        DocumentCustody dc = null;
        if (anexoSir.getAnexo() != null) {
            dc = new DocumentCustody();
            dc.setData(FileSystemManager.getBytesArchivo(anexoSir.getAnexo().getId()));
            dc.setMime(anexoSir.getTipoMIME());
            dc.setName(anexoSir.getNombreFichero());
        }
        return dc;
    }

    /**
     *
     * @param anexoSir
     * @param dc
     * @param modoFirma
     * @return
     * @throws Exception
     */
    private SignatureCustody getSignatureCustody(AnexoSir anexoSir, DocumentCustody dc,
                                                 int modoFirma) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("  ------------------------------");
            log.debug(" anexoSir.getAnexo = " + anexoSir.getAnexo());
        }

        SignatureCustody sc = null;
        if (anexoSir.getAnexo() == null) {

            if (modoFirma ==  RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED
                    || modoFirma ==  RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED) {
                String msg = "L'usuari ens indica que hi ha una firma i no ve (modoFirma = " + modoFirma + ")";
                log.error(msg, new Exception());
                throw new Exception(msg);
            }

        } else {

            if (modoFirma !=  RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED
                    && modoFirma !=  RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED) {
                String msg = "L'usuari ens indica que NO hi ha una firma pero n'envia una"
                        + " (modoFirma = " + modoFirma + ")";
                log.error(msg, new Exception());
                throw new Exception(msg);
            }



            sc = new SignatureCustody();

            sc.setData(FileSystemManager.getBytesArchivo(anexoSir.getAnexo().getId()));
            sc.setMime(anexoSir.getTipoMIME());
            sc.setName(anexoSir.getNombreFichero());


            if (modoFirma ==  RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED) {
                // Document amb firma adjunta
                sc.setAttachedDocument(null);

                // TODO Emprar mètode per descobrir tipus de signatura
                sc.setSignatureType(SignatureCustody.OTHER_DOCUMENT_WITH_ATTACHED_SIGNATURE);

            } else if (modoFirma ==  RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED) {
                // Firma en document separat CAS 4
                if (dc == null) {
                    throw new Exception("Aquesta firma requereix el document original"
                            + " i no s'ha enviat");
                }

                sc.setAttachedDocument(false);
                // TODO Emprar mètode per descobrir tipus de signatura
                sc.setSignatureType(SignatureCustody.OTHER_SIGNATURE_WITH_DETACHED_DOCUMENT);
            }
        }
        return sc;
    }

}