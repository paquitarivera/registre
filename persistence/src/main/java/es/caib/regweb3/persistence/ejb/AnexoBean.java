package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.persistence.utils.I18NLogicUtils;
import es.caib.regweb3.persistence.utils.RegistroUtils;
import es.caib.regweb3.persistence.validator.AnexoBeanValidator;
import es.caib.regweb3.persistence.validator.AnexoValidator;
import es.caib.regweb3.utils.Configuracio;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NArgumentString;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataConstants;
import org.fundaciobit.plugins.utils.PluginsManager;
import org.hibernate.Hibernate;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.*;


/**
 * Created by Fundacio Bit
 *
 * @author earrivi
 * @author anadal
 * @author anadal (Adaptació DocumentCustody 3.0.0)
 *         Date: 6/03/13
 */
@Stateless(name = "AnexoEJB")
@SecurityDomain("seycon")
public class AnexoBean extends BaseEjbJPA<Anexo, Long> implements AnexoLocal {

    protected final Logger log = Logger.getLogger(getClass());


    @Resource
    private javax.ejb.SessionContext ejbContext;

    @PersistenceContext(unitName = "regweb3")
    private EntityManager em;

    @EJB(mappedName = "regweb3/TipoDocumentalEJB/local")
    private TipoDocumentalLocal tipoDocumentalEjb;

    @EJB(mappedName = "regweb3/RegistroEntradaCambiarEstadoEJB/local")
    private RegistroEntradaCambiarEstadoLocal registroEntradaEjb;

    @EJB(mappedName = "regweb3/RegistroSalidaCambiarEstadoEJB/local")
    private RegistroSalidaCambiarEstadoLocal registroSalidaEjb;

    @EJB(mappedName = "regweb3/HistoricoRegistroEntradaEJB/local")
    private HistoricoRegistroEntradaLocal historicoRegistroEntradaEjb;

    @EJB(mappedName = "regweb3/HistoricoRegistroSalidaEJB/local")
    private HistoricoRegistroSalidaLocal historicoRegistroSalidaEjb;

    @EJB(mappedName = "regweb3/SignatureServerEJB/local")
    private SignatureServerLocal signatureServerEjb;

    @EJB(mappedName = "regweb3/PluginEJB/local")
    private PluginLocal pluginEjb;


    @Override
    public Anexo getReference(Long id) throws Exception {

        return em.getReference(Anexo.class, id);
    }

    @Override
    public AnexoFull getAnexoFull(Long anexoID) throws I18NException {

        try {
            Anexo anexo = em.find(Anexo.class, anexoID);

            String custodyID = anexo.getCustodiaID();

            AnexoFull anexoFull = new AnexoFull(anexo);

            //IDocumentCustodyPlugin custody = AnnexDocumentCustodyManager.getInstance();
            IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);

            anexoFull.setDocumentoCustody(custody.getDocumentInfoOnly(custodyID));
            anexoFull.setDocumentoFileDelete(false);
            anexoFull.setSignatureCustody(custody.getSignatureInfoOnly(custodyID));
            anexoFull.setSignatureFileDelete(false);

            if (log.isDebugEnabled()) {
              log.debug("SIGNATURE " + custody.getSignatureInfoOnly(custodyID));
              log.debug("DOCUMENT " + custody.getDocumentInfoOnly(custodyID));
              log.debug("modoFirma " + anexo.getModoFirma());
            }


            return anexoFull;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new I18NException(e, "anexo.error.obteniendo",
                    new I18NArgumentString(String.valueOf(anexoID)),
                    new I18NArgumentString(e.getMessage()));

        }
    }


    @Override
    //TODO CAMBIAR NOMBRE A ESTE METODO POR OTRO MAS ADECUADO, ES REDUNDANTE
    public AnexoFull getAnexoFullCompleto(Long anexoID) throws I18NException {

        try {
            Anexo anexo = em.find(Anexo.class, anexoID);

            String custodyID = anexo.getCustodiaID();

            AnexoFull anexoFull = new AnexoFull(anexo);

            //IDocumentCustodyPlugin custody = AnnexDocumentCustodyManager.getInstance();
            IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);

            anexoFull.setDocumentoCustody(custody.getDocumentInfo(custodyID));

            anexoFull.setDocumentoFileDelete(false);

            anexoFull.setSignatureCustody(custody.getSignatureInfo(custodyID));

            anexoFull.setSignatureFileDelete(false);

            return anexoFull;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new I18NException(e, "anexo.error.obteniendo",
                    new I18NArgumentString(String.valueOf(anexoID)),
                    new I18NArgumentString(e.getMessage()));

        }
    }


    /**
     * Método que crea un anexo
     *
     * @param anexoFull
     * @param usuarioEntidad
     * @param registroID
     * @param tipoRegistro
     * @return
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public AnexoFull crearAnexo(AnexoFull anexoFull, UsuarioEntidad usuarioEntidad,
                                Long registroID, String tipoRegistro) throws I18NException, I18NValidationException {

        IDocumentCustodyPlugin custody = null;
        boolean error = false;
        String custodyID = null;
        final boolean isNew = true;
        try {

            Anexo anexo = anexoFull.getAnexo();

            // Validador
            validateAnexo(anexo, isNew);

            anexo.setFechaCaptura(new Date());

            //Si firmaValida es null, por defecto marcamos como false
            if (anexo.getFirmaValida() == null) {
                anexo.setFirmaValida(false);
            }
            //Si justificante es null, por defecto marcamos como false
            if (anexo.getJustificante() == null) {
                anexo.setJustificante(false);
            }


            // Revisar si tipusdocumental està carregat
            Long id = anexo.getTipoDocumental().getId();
            TipoDocumental td = tipoDocumentalEjb.findById(id);
            if (td == null) {
                I18NException i18n = new I18NException("anexo.tipoDocumental.obligatorio");
                log.error("No trob tipoDocumental amb ID = ]" + id + "[");
                throw i18n;
            } else {
                anexo.setTipoDocumental(td);
            }

            custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);

            //Obtenemos el registro con sus anexos, interesados y tipo Asunto
            IRegistro registro = getIRegistro(registroID, tipoRegistro, anexo, isNew);

            anexo.setRegistroDetalle(registro.getRegistroDetalle());

            final Map<String, Object> custodyParameters = getCustodyParameters(registro, anexo);

            //Reservamos el custodyID
            custodyID = custody.reserveCustodyID(custodyParameters);
            anexo.setCustodiaID(custodyID);

            //Guardamos los documentos asociados al anexo en custodia
            updateCustodyInfoOfAnexo(anexoFull, custody, custodyParameters, custodyID,
                    registro, isNew);

            //Guardamos el anexo
            anexo = this.persist(anexo);

            //Creamos el histórico de las modificaciones del registro debido a los anexos
            if (!anexo.getJustificante()) {
                crearHistorico(anexoFull, usuarioEntidad, registroID, tipoRegistro, isNew);
            }

            anexoFull.setAnexo(anexo);

            return anexoFull;

        } catch (I18NException i18n) {
            error = true;
            throw i18n;
        } catch (Exception e) {
            error = true;
            log.error("Error creant un anexe: " + e.getMessage(), e);
            throw new I18NException(e, "anexo.error.guardando", new I18NArgumentString(e.getMessage()));
        } finally {
            if (error) {
                ejbContext.setRollbackOnly();

                if (custody != null && custodyID != null) {

                    try {
                        custody.deleteCustody(custodyID);
                    } catch (Throwable th) {
                        log.warn("Error borrant custodia: " + th.getMessage(), th);
                    }
                }
            }
        }

    }


    protected void validateAnexo(Anexo anexo, final boolean isNou)
            throws I18NValidationException, I18NException {
        AnexoValidator<Anexo> anexoValidator = new AnexoValidator<Anexo>();
        AnexoBeanValidator pfbv = new AnexoBeanValidator(anexoValidator);
        pfbv.throwValidationExceptionIfErrors(anexo, isNou);
    }

    /**
     * Método que actualiza un anexo
     *
     * @param anexoFull
     * @param usuarioEntidad
     * @param registroID
     * @param tipoRegistro
     * @return
     * @throws I18NException
     * @throws I18NValidationException
     */
    @Override
    public AnexoFull actualizarAnexo(AnexoFull anexoFull, UsuarioEntidad usuarioEntidad,
                                     Long registroID, String tipoRegistro) throws I18NException, I18NValidationException {

        try {

            Anexo anexo = anexoFull.getAnexo();

            // Validador
            final boolean isNew = false;
            validateAnexo(anexo, isNew);

            anexo.setFechaCaptura(new Date());


            IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);

            //Obtenemos el registro con sus anexos, interesados y tipo Asunto
            IRegistro registro = getIRegistro(registroID, tipoRegistro, anexo, isNew);

            final Map<String, Object> custodyParameters = getCustodyParameters(registro, anexo);

            final String custodyID = anexo.getCustodiaID();

            //Actualizamos los datos de anexo
            anexo = this.merge(anexo);
            anexoFull.setAnexo(anexo);

            // Crea historico y lo enlaza con el RegistroDetalle
            crearHistorico(anexoFull, usuarioEntidad, registroID, tipoRegistro, isNew);

            //Guardamos los cambios en custodia
            updateCustodyInfoOfAnexo(anexoFull, custody, custodyParameters, custodyID,
                    registro, isNew);

            return anexoFull;

        } catch (I18NException i18n) {
            ejbContext.setRollbackOnly();
            throw i18n;
        } catch (Exception e) {
            ejbContext.setRollbackOnly();
            log.error("Error actualitzant un anexe: " + e.getMessage(), e);
            throw new I18NException(e, "anexo.error.guardando", new I18NArgumentString(e.getMessage()));
        }

    }


    protected IRegistro getIRegistro(Long registroID, String tipoRegistro, Anexo anexo, boolean isNou) throws Exception {
        IRegistro registro;
        IRegistro cloneRegistro;
        if ("entrada".equals(tipoRegistro)) {
            registro = registroEntradaEjb.findById(registroID);

        } else {
            registro = registroSalidaEjb.findById(registroID);

        }

        Hibernate.initialize(registro.getRegistroDetalle().getTipoAsunto());
        Hibernate.initialize(registro.getRegistroDetalle().getInteresados());

        if ("entrada".equals(tipoRegistro)) {
            cloneRegistro = new RegistroEntrada((RegistroEntrada) registro);
        } else {
            cloneRegistro = new RegistroSalida((RegistroSalida) registro);
        }


        List<Anexo> anexos = Anexo.clone(registro.getRegistroDetalle().getAnexos());

        cloneRegistro.getRegistroDetalle().setAnexos(anexos);


        return cloneRegistro;
    }

    /**
     *
     * @param anexoFull
     * @param usuarioEntidad
     * @param registroID
     * @param tipoRegistro
     * @param isNew
     * @throws Exception
     * @throws I18NException
     */
    protected void crearHistorico(AnexoFull anexoFull, UsuarioEntidad usuarioEntidad,
                                  Long registroID, String tipoRegistro, boolean isNew) throws Exception, I18NException {
        Entidad entidadActiva = usuarioEntidad.getEntidad();
        if ("entrada".equals(tipoRegistro)) {
            RegistroEntrada registroEntrada = registroEntradaEjb.findById(registroID);
            // Dias que han pasado desde que se creó el registroEntrada
            Long dias = RegistroUtils.obtenerDiasRegistro(registroEntrada.getFecha());

            if (isNew) {//NUEVO ANEXO
                // Si han pasado más de los dias de visado de la entidad se crearan historicos de todos los
                // cambios y se cambia el estado del registroEntrada a pendiente visar
                if (dias >= entidadActiva.getDiasVisado()) {
                    registroEntradaEjb.cambiarEstado(registroID, RegwebConstantes.REGISTRO_PENDIENTE_VISAR);

                    // Creamos el historico de registro de entrada
                    historicoRegistroEntradaEjb.crearHistoricoRegistroEntrada(registroEntrada, usuarioEntidad, I18NLogicUtils.tradueix(new Locale(Configuracio.getDefaultLanguage()), "registro.modificacion.anexos"), true);

                }

            } else {// MODIFICACION DE ANEXO

                if (dias >= entidadActiva.getDiasVisado()) { // Si han pasado más de los dias de visado cambiamos estado registro
                    registroEntradaEjb.cambiarEstado(registroID, RegwebConstantes.REGISTRO_PENDIENTE_VISAR);
                }

                // Creamos el historico de registro de entrada, siempre creamos histórico independiente de los dias.
                historicoRegistroEntradaEjb.crearHistoricoRegistroEntrada(registroEntrada, usuarioEntidad, I18NLogicUtils.tradueix(new Locale(Configuracio.getDefaultLanguage()), "registro.modificacion.anexos"), true);
            }

            anexoFull.getAnexo().setRegistroDetalle(registroEntrada.getRegistroDetalle());

        } else {
            RegistroSalida registroSalida = registroSalidaEjb.findById(registroID);
            // Dias que han pasado desde que se creó el registroEntrada
            Long dias = RegistroUtils.obtenerDiasRegistro(registroSalida.getFecha());

            if (isNew) {//NUEVO ANEXO
                // Si han pasado más de los dias de visado de la entidad se crearan historicos de todos los
                // cambios y se cambia el estado del registroEntrada a pendiente visar
                if (dias >= entidadActiva.getDiasVisado()) {
                    registroSalidaEjb.cambiarEstado(registroID, RegwebConstantes.REGISTRO_PENDIENTE_VISAR);

                    // Creamos el historico de registro de entrada
                    historicoRegistroSalidaEjb.crearHistoricoRegistroSalida(registroSalida, usuarioEntidad, I18NLogicUtils.tradueix(new Locale(Configuracio.getDefaultLanguage()), "registro.modificacion.anexos"), true);
                }

            } else {// MODIFICACION DE ANEXO

                if (dias >= entidadActiva.getDiasVisado()) { // Si han pasado más de los dias de visado cambiamos estado registro
                    registroSalidaEjb.cambiarEstado(registroID, RegwebConstantes.REGISTRO_PENDIENTE_VISAR);
                }
                // Creamos el historico de registro de entrada, siempre creamos histórico independiente de los dias.
                historicoRegistroSalidaEjb.crearHistoricoRegistroSalida(registroSalida, usuarioEntidad, I18NLogicUtils.tradueix(new Locale(Configuracio.getDefaultLanguage()), "registro.modificacion.anexos"), true);
            }
            anexoFull.getAnexo().setRegistroDetalle(registroSalida.getRegistroDetalle());
        }
    }


    /**
     * Método que crea/actualiza un anexo en función de lo que recibe en anexoFull
     *
     * @param anexoFull
     * @param custody
     * @param custodyParameters
     * @param custodyID
     * @param registro
     * @param isNou
     * @throws Exception
     * @throws I18NException
     */
    protected void updateCustodyInfoOfAnexo(AnexoFull anexoFull, IDocumentCustodyPlugin custody2,
                                            final Map<String, Object> custodyParameters, final String custodyID,
                                            IRegistro registro, boolean isNou) throws Exception, I18NException {

        // Validador: Sempre amb algun arxiu
        int modoFirma = anexoFull.getAnexo().getModoFirma();
        if (isNou) { //Creación
            if (anexoFull.getDocumentoCustody() == null && anexoFull.getSignatureCustody() == null) {
                //"No ha definit cap fitxer en aquest annex"
                throw new I18NException("anexo.error.sinfichero");
            }
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED && anexoFull.getSignatureCustody() == null) {
                throw new I18NException("anexo.error.sinfichero");
            }
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED && (anexoFull.getDocumentoCustody() == null || anexoFull.getSignatureCustody() == null)) {
                throw new I18NException("anexo.error.faltadocumento");
            }
        } else {//Actualización
            //Controlamos que el anexo no quede sin archivo, hay que controlar con modofirma
            int total = 0;
            //Si no tenia documento, pero ahora envian uno nuevo, sumamos 1
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_SINFIRMA) {
                if (custody2.getDocumentInfoOnly(custodyID) == null) {
                    // Afegim un
                    if (anexoFull.getDocumentoCustody() != null) {
                        total += 1;
                    }
                } else { // ya tenia, sumamos 1
                    total += 1;
                }
                log.info("TOTAL " + total);
                if (total <= 0) {
                    //La combinació elegida deixa aquest annex sense cap fitxer
                    throw new I18NException("anexo.error.quedarsesinfichero");
                }
            }
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED) {
                //Si no tenia firma, pero envian 1 nueva, sumamos 1
                if (custody2.getSignatureInfoOnly(custodyID) == null) {
                    // Afegim un
                    if (anexoFull.getSignatureCustody() != null) {
                        total += 1;
                    }
                } else { // si ya tenia, sumamos 1
                    total += 1;
                }
                log.info("TOTAL " + total);
             /* if (total <= 0) {
                  //La combinació elegida deixa aquest annex sense cap fitxer
                  throw new I18NException("anexo.error.quedarsesinfichero");
              }*/
                //PARCHE API ANTIGUA
                if (custody2.getDocumentInfoOnly(custodyID) == null) {
                    // Afegim un
                    if (anexoFull.getDocumentoCustody() != null) {
                        total += 1;
                    }
                } else { // ya tenia, sumamos 1
                    total += 1;
                }
                log.info("TOTAL " + total);
                if (total <= 0) {
                    //La combinació elegida deixa aquest annex sense cap fitxer
                    throw new I18NException("anexo.error.quedarsesinfichero");
                }
            }
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED) {
                if (custody2.getDocumentInfoOnly(custodyID) == null) {
                    // Afegim un
                    if (anexoFull.getDocumentoCustody() != null) {
                        total += 1;
                    }
                } else { // ya tenia, sumamos 1
                    total += 1;
                }

                //Si no tenia firma, pero envian 1 nueva, sumamos 1
                if (custody2.getSignatureInfoOnly(custodyID) == null) {
                    // Afegim un
                    if (anexoFull.getSignatureCustody() != null) {
                        total += 1;
                    }
                } else { // si ya tenia, sumamos 1
                    total += 1;
                }
                log.info("TOTAL " + total);
                if (total <= 1) {
                    //La combinació elegida deixa aquest annex sense cap fitxer
                    throw new I18NException("anexo.error.faltadocumento");
                }
            }

        }


        // TODO Falta Check DOC
        Anexo anexo = anexoFull.getAnexo();

        boolean updateDate = false;
        final DocumentCustody documentCustody;
        final SignatureCustody signatureCustody;

        String mimeFinal = null;
        //Actualización o creación de los documentos de los anexos en función del modo de firma
        //Si el anexo es nuevo o el modo de firma es detached, el comportamiento es el mismo
        if (isNou || modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_DETACHED) {

            //Guardamos el documentCustody
            // XMAS SAVEALL DocumentCustody doc = guardarDocumentCustody(anexoFull.getDocumentoCustody(),
            //      custody, custodyID, custodyParameters, anexo,  mimeFinal);
            documentCustody = anexoFull.getDocumentoCustody();
            mimeFinal = arreglarDocumentCustody(documentCustody, custodyID, custodyParameters,
                anexo, mimeFinal);

            //Guardamos la signatureCustody
            // XMAS SAVEALL guardarSignatureCustody(anexoFull.getSignatureCustody(), doc, custody, custodyID, custodyParameters, anexo, updateDate, mimeFinal);
            signatureCustody = anexoFull.getSignatureCustody();
            mimeFinal = arreglarSignatureCustody(signatureCustody, documentCustody, custodyID,
                custodyParameters, anexo, mimeFinal);
            
            updateDate = true;

        } else { //es modificación Tratamos todos los modos firma como corresponda
            DocumentCustody doc = anexoFull.getDocumentoCustody();
            if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_SINFIRMA) {

                //Guardamos el documentCustody
                // XMAS SAVEALL guardarDocumentCustody(anexoFull.getDocumentoCustody(), custody, custodyID, custodyParameters, anexo, updateDate, mimeFinal);
                documentCustody = anexoFull.getDocumentoCustody();
                mimeFinal = arreglarDocumentCustody(documentCustody, custodyID, custodyParameters,
                  anexo, mimeFinal);

                //Borrar lo que haya en signature custody
                // XMAS SAVEALL custody.deleteSignature(custodyID);
                signatureCustody = null;
                
                updateDate = true;
            } else if (modoFirma == RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED) {
                //obtenemos el document custody para crear bien el documento
                
                if (doc == null) {//CASO API NUEVA
                  
                  documentCustody = null;
                  //Guardamos la signatureCustody. Los documentos con firma attached se guardan en SignatureCustody.
                    
                  signatureCustody = anexoFull.getSignatureCustody();
                  mimeFinal = arreglarSignatureCustody(signatureCustody, documentCustody, custodyID,
                      custodyParameters, anexo, mimeFinal);
                  
                    // XMAS SAVEALL guardarSignatureCustody(anexoFull.getSignatureCustody(), doc, custody, custodyID, custodyParameters, anexo, updateDate, mimeFinal);
                    //Borramos el documentcustody que habia por si venimos de otro modo de firma
                    // custody.deleteDocument(custodyID);

                    updateDate = true;
                } else { //PARCHE PARA API ANTIGUA
                    log.info("PARCHE DC " + anexoFull.getDocumentoCustody());
                    documentCustody = doc;
                    mimeFinal = arreglarDocumentCustody(documentCustody, custodyID, custodyParameters,
                      anexo, mimeFinal);
                    
                    signatureCustody = null;
                    
                 // XMAS SAVEALL guardarDocumentCustody(anexoFull.getDocumentoCustody(), custody, custodyID, custodyParameters, anexo, updateDate, mimeFinal);
                    updateDate = true;
                }
                
            } else {
              //el caso de modoFirma detached es igual que si fuese nuevo.
              
              
              // CASO:  no tocam res
              documentCustody = null;
              signatureCustody = null;
            }
            
        }

        
        if (documentCustody == null && signatureCustody == null) {
          // OK No feim res.
        } else {

          // Actualitzar Metadades
          final String lang = Configuracio.getDefaultLanguage();
          final Locale loc = new Locale(lang);
          List<Metadata> metadades = new ArrayList<Metadata>();
  
          // Metadades que venen de Scan
          List<Metadata> metasScan = anexoFull.getMetadatas();
          
          final boolean debug = log.isDebugEnabled();
  
          if (debug) {
            log.info("MESTAS SCAN = " + metasScan);
          }
  
          if (metasScan != null && metasScan.size() != 0) {
  
              if (debug) {
                log.info("MESTAS SCAN SIZE = " + metasScan.size());
                log.info("MESTAS ORIG SIZE PRE = " + metadades.size());
              }
  
              metadades.addAll(metasScan);
  
              if (debug) {
                log.info("MESTAS ORIG SIZE POST = " + metadades.size());
              }
  
          }
  
          // fechaDeEntradaEnElSistema
          if (updateDate) {

              metadades.add(new Metadata("anexo.fechaCaptura", anexo.getFechaCaptura()));
            
              // Afegida Nova Metadada
              metadades.add(new Metadata(MetadataConstants.ENI_FECHA_INICIO, 
                  org.fundaciobit.plugins.utils.ISO8601.dateToISO8601(anexo.getFechaCaptura())));
          }
  
          // String tipoDeDocumento; //  varchar(100)
          if (anexo.getTitulo() != null) {
            metadades.add(new Metadata("anexo.titulo", anexo.getTitulo()));
            
            // Afegida Nova Metadada
            // MetadataConstants.ENI_DESCRIPCION = "eni:descripcion"
            metadades.add(new Metadata(MetadataConstants.ENI_DESCRIPCION, anexo.getTitulo()));
          }
  
          //  String tipoDeDocumento; //  varchar(100)
          if (anexo.getTipoDocumento() != null) {
            // TODO A quin tipus ENI es correspon AIXÒ !!!!
            metadades.add(new Metadata("anexo.tipoDocumento",
                      I18NLogicUtils.tradueix(loc, "tipoDocumento.0" + anexo.getTipoDocumento())));
          }
  
          if (registro.getOficina() != null && registro.getOficina().getNombreCompleto() != null) {
            
            metadades.add(new Metadata("oficina", registro.getOficina().getNombreCompleto()));
            
            // Afegida Nova Metadada
            // MetadataConstants.ENI_CODIGO_OFICINA_REGISTRO = "eni:codigo_oficina_registro"
            metadades.add(new Metadata(MetadataConstants.ENI_CODIGO_OFICINA_REGISTRO,
                      registro.getOficina().getCodigo()));
          }
  
  
          if (anexo.getOrigenCiudadanoAdmin() != null) {
              metadades.add(new Metadata("anexo.origen",
                      I18NLogicUtils.tradueix(loc, "anexo.origen." + anexo.getOrigenCiudadanoAdmin())));
              
              // Afegida Nova Metadada
              // MetadataConstants.ENI_ORIGEN = "eni:origen"
              metadades.add(new Metadata(MetadataConstants.ENI_ORIGEN,
                  anexo.getOrigenCiudadanoAdmin()));
          }
  
          /**
           * tipoValidezDocumento.1=Còpia
           * tipoValidezDocumento.2=Còpia Compulsada
           * tipoValidezDocumento.3=Còpia Original
           * tipoValidezDocumento.4=Original
           */
          if (anexo.getValidezDocumento() != null && anexo.getValidezDocumento() != -1) {
              metadades.add(new Metadata("anexo.validezDocumento",
                      I18NLogicUtils.tradueix(loc, "tipoValidezDocumento." + anexo.getValidezDocumento())));

              // Afegida Nova Metadada
              // MetadataConstants.ENI_ESTADO_ELABORACION = "eni:estado_elaboracion"
              metadades.add(new Metadata(MetadataConstants.ENI_ESTADO_ELABORACION,
                  RegwebConstantes.CODIGO_NTI_BY_TIPOVALIDEZDOCUMENTO.get(anexo.getValidezDocumento())));
          }
  
          if (mimeFinal != null) {
              metadades.add(new Metadata("anexo.formato", mimeFinal));
          }

          if (anexo.getTipoDocumental() != null &&
                  anexo.getTipoDocumental().getCodigoNTI() != null) {

            // Afegida Nova Metadada
            // MetadataConstants.ENI_TIPO_DOCUMENTAL = "eni:tipo_doc_ENI"
            metadades.add(new Metadata(MetadataConstants.ENI_TIPO_DOCUMENTAL,
                anexo.getTipoDocumental().getCodigoNTI()));

            metadades.add(new Metadata("anexo.tipoDocumental.codigo", anexo.getTipoDocumental().getCodigoNTI()));

            try {
              metadades.add(new Metadata("anexo.tipoDocumental.descripcion",
                  ((TraduccionTipoDocumental) anexo.getTipoDocumental().getTraduccion(loc.getLanguage())).getNombre()));
            } catch (Throwable th) {
              log.error("Error en la traduccion de tipo documental: " + th.getMessage(), th);
            }
          }
          if (anexo.getObservaciones() != null) {
              metadades.add(new Metadata("anexo.observaciones", anexo.getObservaciones()));
          }
  
          // XMAS SAVEALL custody.updateMetadata(custodyID, metadades.toArray(new Metadata[metadades.size()]), custodyParameters);
          custody2.saveAll(custodyID, custodyParameters, documentCustody,
              signatureCustody, metadades.toArray(new Metadata[metadades.size()]));
        }

    }


    /**
     * Método que guarda el DocumentCustody de un anexo en custodia.
     *
     * @param dc                DocumentCustody que nos pasan
     * @param custody           custodia donde guardarlo
     * @param custodyID         identificador de custodia
     * @param custodyParameters parametros de custodia
     * @param anexo             anexo a actualizar
     * @param updateDate
     * @param mimeFinal
     * @return
     * @throws Exception
     */
    /*
    public DocumentCustody guardarDocumentCustody(DocumentCustody doc,
                                                  IDocumentCustodyPlugin custody, String custodyID,
                                                  final Map<String, Object> custodyParameters, Anexo anexo,
                                                  String mimeFinal) throws Exception {

        if (doc != null && doc.getData() != null) {// si nos envian documento

            //Borramos el anterior (pruebas marilen, quitado checkbox de esborrar)
            custody.deleteDocument(custodyID);

            //Asignamos los datos nuevos recibidos
            if (doc.getMime() == null) {
                doc.setMime("application/octet-stream");
            }
            mimeFinal = doc.getMime();

            doc.setName(checkFileName(doc.getName(), "file.bin"));

            anexo.setFechaCaptura(new Date());
            anexo.setHash(obtenerHash(doc.getData()));

            //guardamos documento en custodia
            custody.saveDocument(custodyID, custodyParameters, doc);

        }
        return doc;
    }
    */
    public String arreglarDocumentCustody(DocumentCustody doc,
         String custodyID,
        final Map<String, Object> custodyParameters, Anexo anexo,
        String mimeFinal) throws Exception {

        if (doc != null && doc.getData() != null) {// si nos envian documento
        
        
        //Asignamos los datos nuevos recibidos
        if (doc.getMime() == null) {
          doc.setMime("application/octet-stream");
        }
        mimeFinal = doc.getMime();
        
        doc.setName(checkFileName(doc.getName(), "file.bin"));
        
        anexo.setFechaCaptura(new Date());
        anexo.setHash(obtenerHash(doc.getData()));
        
        }
        return mimeFinal;
     }

    /**
     * Método que guarda la SignatureCustody de un anexo en custodia
     *
     * @param sc                SignatureCustody que nos pasan
     * @param doc               DocumentCustody relacionado con la SignatureCustody
     * @param custody           custodia donde guardarlo
     * @param custodyID         identificador de custodia
     * @param custodyParameters parametros de custodia
     * @param anexo             anexo a actualizar
     * @param updateDate
     * @param mimeFinal
     * @return
     * @throws Exception
     */
    /*
    public SignatureCustody guardarSignatureCustody(SignatureCustody sc,
                                                    DocumentCustody doc, IDocumentCustodyPlugin custody,
                                                    String custodyID, final Map<String, Object> custodyParameters,
                                                    Anexo anexo, boolean updateDate, String mimeFinal) throws Exception {
        //Obtenemos la firma que nos envian
        SignatureCustody signature = sc;
        if (signature != null && signature.getData() != null) {//Si nos envian firma

            //Borramos la anterior (pruebas marilen, quitado checkbox de esborrar)
            custody.deleteSignature(custodyID);
            updateDate = true;

            //Preparamos todos los datos para guardar la firma en custodia.
            String signType = (doc == null) ? SignatureCustody.OTHER_SIGNATURE_WITH_ATTACHED_DOCUMENT : SignatureCustody.OTHER_SIGNATURE_WITH_DETACHED_DOCUMENT;

            signature.setName(checkFileName(signature.getName(), "signature.bin"));

            final String mime = signature.getMime();
            if (mime == null) {
                signature.setMime("application/octet-stream");
            } else {

                if ("application/pdf".equals(mime)) {
                    signType = SignatureCustody.PADES_SIGNATURE;
                } else if ("application/xml".equals(mime) ||
                        "text/xml".equals(mime)) {
                    signType = SignatureCustody.XADES_SIGNATURE;
                }
            }

            mimeFinal = signature.getMime(); // Sobreescriu Mime de doc

            signature.setSignatureType(signType);
            // TODO Fallarà en update
            signature.setAttachedDocument(doc == null ? true : false);

            if (doc == null) {
                anexo.setHash(obtenerHash(signature.getData()));
            }
            custody.saveSignature(custodyID, custodyParameters, signature);

            updateDate = true;
        }
        return sc;

    }
    */
    public String arreglarSignatureCustody(SignatureCustody signature,
        DocumentCustody doc, 
        String custodyID, final Map<String, Object> custodyParameters,
        Anexo anexo, String mimeFinal) throws Exception {
      //Obtenemos la firma que nos envian

      if (signature != null && signature.getData() != null) {//Si nos envian firma
      
       
        //Preparamos todos los datos para guardar la firma en custodia.
        String signType = (doc == null) ? SignatureCustody.OTHER_SIGNATURE_WITH_ATTACHED_DOCUMENT : SignatureCustody.OTHER_SIGNATURE_WITH_DETACHED_DOCUMENT;
        
        signature.setName(checkFileName(signature.getName(), "signature.bin"));
        
        final String mime = signature.getMime();
        if (mime == null) {
          signature.setMime("application/octet-stream");
        } else {
          if ("application/pdf".equals(mime)) {
            signType = SignatureCustody.PADES_SIGNATURE;
          } else if ("application/xml".equals(mime) || "text/xml".equals(mime)) {
            signType = SignatureCustody.XADES_SIGNATURE;
          }
        }
        
        mimeFinal = signature.getMime(); // Sobreescriu Mime de doc
        
        signature.setSignatureType(signType);
        // TODO Fallarà en update
        signature.setAttachedDocument(doc == null ? true : false);
        
        if (doc == null) {
          anexo.setHash(obtenerHash(signature.getData()));
        }

      }
      
      return mimeFinal;
    }


    protected static String checkFileName(String name, String defaultName) throws Exception {
        if (name == null || name.trim().length() == 0) {
            return defaultName;
        } else {
            return StringUtils.recortarNombre(name, RegwebConstantes.ANEXO_NOMBREARCHIVO_MAXLENGTH);
        }
    }


    public static class java_util_Date_PersistenceDelegate extends PersistenceDelegate {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            Date date = (Date) oldInstance;
            return new Expression(date, date.getClass(), "new", new Object[]{date.getTime()});
        }
    }


    protected Map<String, Object> getCustodyParameters(IRegistro registro, Anexo anexo) throws Exception {

        Map<String, Object> map = new HashMap<String, Object>();

        map.put("registro", registro);
        map.put("anexo", anexo);

        return map;

    }


    @Override
    public Anexo findById(Long id) throws Exception {

        return em.find(Anexo.class, id);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Anexo> getAll() throws Exception {

        return em.createQuery("Select anexo from Anexo as anexo order by anexo.id").getResultList();
    }


    @Override
    public Long getTotal() throws Exception {

        Query q = em.createQuery("Select count(anexo.id) from Anexo as anexo");

        return (Long) q.getSingleResult();
    }

    @Override
    public List<Anexo> getPagination(int inicio) throws Exception {

        Query q = em.createQuery("Select anexo from Anexo as anexo order by anexo.id");

        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }


    @Override
    public List<Anexo> getByRegistroEntrada(RegistroEntrada registroEntrada) throws Exception {
        Hibernate.initialize(registroEntrada.getRegistroDetalle().getAnexos());
        return registroEntrada.getRegistroDetalle().getAnexos();
    }

    @Override
    public List<Anexo> getByRegistroSalida(RegistroSalida registroSalida) throws Exception {
        Hibernate.initialize(registroSalida.getRegistroDetalle().getAnexos());
        return registroSalida.getRegistroDetalle().getAnexos();

    }

    @Override
    public List<Anexo> getByRegistroDetalle(Long idRegistroDetalle) throws Exception {
        Query query = em.createQuery("Select anexo from Anexo as anexo where anexo.registroDetalle.id=:idRegistroDetalle order by anexo.id");
        query.setParameter("idRegistroDetalle", idRegistroDetalle);
        return query.getResultList();

    }

    @Override
    public List<Anexo> getByRegistroDetalleLectura(Long idRegistroDetalle) throws Exception {
        Query query = em.createQuery("Select anexo.titulo, anexo.tipoDocumento from Anexo as anexo where anexo.registroDetalle.id=:idRegistroDetalle");
        query.setParameter("idRegistroDetalle", idRegistroDetalle);

        List<Anexo> anexos = new ArrayList<Anexo>();
        List<Object[]> result = query.getResultList();

        for (Object[] object : result) {
            anexos.add(new Anexo((String) object[0], (Long) object[1]));
        }
        return anexos;
    }

    @Override
    public Long getIdJustificante(Long idRegistroDetalle) throws Exception {
        Query query = em.createQuery("Select anexo.id from Anexo as anexo where anexo.registroDetalle.id=:idRegistroDetalle and " +
                "anexo.justificante = true");
        query.setParameter("idRegistroDetalle", idRegistroDetalle);

        List<Long> justificante = query.getResultList();

        if (justificante.size() == 1) {
            return justificante.get(0);
        } else {
            return null;
        }

    }


    /* METODOS DEL AnnexDocumentCustodyManager.java hecho por marilen del TODO DE TONI*/


    private static IDocumentCustodyPlugin cacheDocumentCustodyPlugin = null;


    /**
     * Obtiene una instancia del plugin de custodia
     *
     * @return
     * @throws Exception
     */
    public IDocumentCustodyPlugin getInstance() throws Exception {

        if (cacheDocumentCustodyPlugin == null) {
            // Valor de la Clau
            final String propertyName = RegwebConstantes.REGWEB3_PROPERTY_BASE + "annex.documentcustodyplugin";
            String className = System.getProperty(propertyName);
            if (className == null || className.trim().length() <= 0) {
                throw new Exception("No hi ha cap propietat " + propertyName
                        + " definint la classe que gestiona el plugin de login");
            }
            // Carregant la classe
            Object obj;
            obj = PluginsManager.instancePluginByClassName(className, RegwebConstantes.REGWEB3_PROPERTY_BASE + "annex.");
            // TODO Falta mirar si retorna un null !!!!!

            cacheDocumentCustodyPlugin = (IDocumentCustodyPlugin) obj;
        }

        return cacheDocumentCustodyPlugin;
    }


    /**
     * Obtiene la info del fichero existente en el sistema de archivos
     * (No obtiene el array de bytes)
     *
     * @param custodiaID
     * @return
     */
    public DocumentCustody getArchivo(String custodiaID) throws Exception {

        if (custodiaID == null) {
            log.warn("getArchivo :: CustodiaID vale null !!!!!", new Exception());
            return null;
        }
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getDocumentInfo(custodiaID);

    }


    /**
     * Obtiene la info del fichero existente en el sistema de archivos
     * (No obtiene el array de bytes)
     *
     * @param custodiaID
     * @return
     */
    @Override
    public byte[] getArchivoContent(String custodiaID) throws Exception {

        if (custodiaID == null) {
            log.warn("getArchivo :: CustodiaID vale null !!!!!", new Exception());
            return null;
        }
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getDocument(custodiaID);

    }


    @Override
    public DocumentCustody getDocumentInfoOnly(String custodiaID) throws Exception {
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getDocumentInfoOnly(custodiaID);
    }

    @Override
    public SignatureCustody getSignatureInfoOnly(String custodiaID) throws Exception {
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getSignatureInfoOnly(custodiaID);
    }

    /**
     * Obtiene la info de la firma existente en el sistema de archivos
     * (No obtiene el array de bytes)
     *
     * @param custodiaID
     * @return
     */
    public SignatureCustody getFirma(String custodiaID) throws Exception {

        if (custodiaID == null) {
            log.warn("getFirma :: CustodiaID vale null !!!!!", new Exception());
            return null;
        }
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getSignatureInfo(custodiaID);
    }


    @Override
    public byte[] getFirmaContent(String custodiaID) throws Exception {

        if (custodiaID == null) {
            log.warn("getFirma :: CustodiaID vale null !!!!!", new Exception());
            return null;
        }
        IDocumentCustodyPlugin custody = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA);
        return custody.getSignature(custodiaID);
    }


    /**
     * Elimina completamente una custodia ( = elimicion completa de Anexo)
     *
     * @param custodiaID
     * @return true si l'arxiu no existeix o s'ha borrat. false en els altres
     * casos.
     */
    public boolean eliminarCustodia(String custodiaID) throws Exception {

        if (custodiaID == null) {
            log.warn("eliminarCustodia :: CustodiaID vale null !!!!!", new Exception());
            return false;
        } else {
            getInstance().deleteCustody(custodiaID);
            return true;
        }

    }


    /**
     * Crea o actualiza un anexos en el sistema de custodia
     * TODO PENDENT D'EMPLEAR PER LES PROVES RECEPCIO SIR, pero hauria de retornar
     *
     * @param name
     * @param file
     * @param signatureName
     * @param signature
     * @param signatureMode
     * @param custodyID         Si vale null significa que creamos el archivo. Otherwise actualizamos el fichero.
     * @param custodyParameters JSON del registre
     * @return Identificador de custodia
     * @throws Exception
     */
    // TODO mime de doc i firma
    /*
    public String crearArchivo(String name, byte[] file, String signatureName,
                               byte[] signature, int signatureMode, String custodyID,
                               Map<String, Object> custodyParameters) throws Exception {

        IDocumentCustodyPlugin instance = getInstance();

        if (custodyID == null) {
            custodyID = instance.reserveCustodyID(custodyParameters);
        }


        if (signatureMode == RegwebConstantes.TIPO_FIRMA_CSV) {
            // CSV == Referencia a DocumentCustody
            throw new Exception("Modo de firma(signatureMode) RegwebConstantes.TIPO_FIRMA_CSV "
                    + " no suportat. Es suportarà a partir de la posada en marxa de l'Arxiu Electrònic");
        }


        if (name != null && file != null) {
            DocumentCustody document = new DocumentCustody(name, file);
            instance.saveDocument(custodyID, custodyParameters, document);
        }


        if (signatureName != null && signature != null) {

            SignatureCustody docSignature = new SignatureCustody();
            docSignature.setName(signatureName);
            docSignature.setData(signature);

            final long signType = (long) signatureMode;

            // Cases en doc. doc/AnalisiGestioDocumentsSIRDocumentCustodyAPI2Regweb.odt

            if (signType == RegwebConstantes.TIPO_FIRMA_XADES_DETACHED_SIGNATURE) {
                // Cas 4
                docSignature.setSignatureType(SignatureCustody.XADES_SIGNATURE);
                docSignature.setAttachedDocument(false);
            } else if (signType == RegwebConstantes.TIPO_FIRMA_XADES_ENVELOPE_SIGNATURE) {
                // CAS 3
                docSignature.setSignatureType(SignatureCustody.XADES_SIGNATURE);
                docSignature.setAttachedDocument(true);
            } else if (signType == RegwebConstantes.TIPO_FIRMA_CADES_DETACHED_EXPLICIT_SIGNATURE) {
                // CAS 4
                docSignature.setSignatureType(SignatureCustody.CADES_SIGNATURE);
                docSignature.setAttachedDocument(false);
            } else if (signType == RegwebConstantes.TIPO_FIRMA_CADES_ATTACHED_IMPLICIT_SIGNAUTRE) {
                // CAS 3
                docSignature.setSignatureType(SignatureCustody.CADES_SIGNATURE);
                docSignature.setAttachedDocument(true);
            } else if (signType == RegwebConstantes.TIPO_FIRMA_PADES) {
                // CAS 5
                docSignature.setSignatureType(SignatureCustody.PADES_SIGNATURE);
                docSignature.setAttachedDocument(null);
            } else { //    default:
                String msg = "No es suporta signatureMode amb valor  " + signatureMode;
                log.error(msg, new Exception());
                throw new Exception(msg);
            }


            instance.saveSignature(custodyID, custodyParameters, docSignature);
        }

        //log.info("Creamos el file: " + getArchivosPath()+dstId.toString());

        return custodyID;
    }
*/

    /**
     * Crea un Jusitificante, lo firma y lo crea como anexo al registro
     *
     * @param usuarioEntidad
     * @param idRegistro
     * @param tipoRegistro
     * @param baos
     * @return
     * @throws Exception
     */
    @Override
    public AnexoFull crearJustificante(UsuarioEntidad usuarioEntidad, Long idRegistro,
                                       String tipoRegistro, byte[] data) throws Exception {

        File justificanteFile = null;
        File signedFile = null;
        try {

            Long idEntidad = usuarioEntidad.getEntidad().getId();

            Locale locale = new Locale("es");
            String nombreFichero = I18NLogicUtils.tradueix(locale, "justificante.fichero") + ".pdf";
            String tituloAnexo = I18NLogicUtils.tradueix(locale, "justificante.anexo.titulo");
            String observacionesAnexo = I18NLogicUtils.tradueix(locale, "justificante.anexo.observaciones");

            // Crea el justificante como fichero temporal
            justificanteFile = File.createTempFile("regweb3_", ".justificant");
            FileOutputStream fos = new FileOutputStream(justificanteFile);
            fos.write(data);
            fos.flush();
            fos.close();

            // Firma el justificant
            signedFile = signatureServerEjb.signFile(justificanteFile, "es", idEntidad);

            // Crea el anexo del justificante firmado
            AnexoFull anexoFull = new AnexoFull();
            anexoFull.getAnexo().setTitulo(tituloAnexo);
            anexoFull.getAnexo().setValidezDocumento(RegwebConstantes.TIPOVALIDEZDOCUMENTO_ORIGINAL);
            TipoDocumental tipoDocumental = tipoDocumentalEjb.findByCodigoEntidad("TD99", idEntidad);
            anexoFull.getAnexo().setTipoDocumental(tipoDocumental);
            anexoFull.getAnexo().setTipoDocumento(RegwebConstantes.TIPO_DOCUMENTO_DOC_ADJUNTO);
            anexoFull.getAnexo().setOrigenCiudadanoAdmin(RegwebConstantes.ANEXO_ORIGEN_ADMINISTRACION.intValue());
            anexoFull.getAnexo().setObservaciones(observacionesAnexo);
            anexoFull.getAnexo().setModoFirma(RegwebConstantes.MODO_FIRMA_ANEXO_ATTACHED);

            // Fichero Anexado
            anexoFull.getAnexo().setJustificante(true);

            SignatureCustody sign = new SignatureCustody();
            sign.setData(FileUtils.readFileToByteArray(signedFile));
            sign.setMime("application/pdf");
            sign.setName(nombreFichero);

            anexoFull.setSignatureCustody(sign);
            anexoFull.setSignatureFileDelete(false);
            anexoFull = crearAnexo(anexoFull, usuarioEntidad, idRegistro, tipoRegistro);

            return anexoFull;

        } catch (I18NValidationException e) {
            e.printStackTrace();
            throw new Exception(e);
        } catch (I18NException e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {
          
          if (justificanteFile != null) {
            if (!justificanteFile.delete()) {
              justificanteFile.deleteOnExit(); 
            }
          }
          
          
          if (signedFile != null) {
            if (!signedFile.delete()) {
              signedFile.deleteOnExit(); 
            }
          }
          
        }

    }


    /* FIN METODOS DEL AnnexDocumentCustodyManager.java hecho por marilen del TODO DE TONI*/

    /**
     * Genera el Hash mediante SHA-256 del contenido del documento y lo codifica en base64
     *
     * @param documentoData
     * @return
     * @throws Exception
     */
    protected byte[] obtenerHash(byte[] documentoData) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(documentoData);

        return Base64.encodeBase64(digest);

    }
}
