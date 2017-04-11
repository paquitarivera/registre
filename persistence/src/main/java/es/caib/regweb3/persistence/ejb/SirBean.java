package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.model.utils.CamposNTI;
import es.caib.regweb3.model.utils.EstadoAsientoRegistralSir;
import es.caib.regweb3.model.utils.IndicadorPrueba;
import es.caib.regweb3.persistence.utils.FileSystemManager;
import es.caib.regweb3.persistence.utils.RegwebJustificantePluginManager;
import es.caib.regweb3.plugins.justificante.IJustificantePlugin;
import es.caib.regweb3.sir.core.excepcion.ValidacionException;
import es.caib.regweb3.sir.core.model.Errores;
import es.caib.regweb3.sir.core.model.TipoAnotacion;
import es.caib.regweb3.sir.core.model.TipoMensaje;
import es.caib.regweb3.sir.core.utils.FicheroIntercambio;
import es.caib.regweb3.sir.core.utils.Mensaje;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.StringUtils;
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
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 16/01/14
 */

@Stateless(name = "SirEJB")
@SecurityDomain("seycon")
public class SirBean implements SirLocal{

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName="regweb3")
    private EntityManager em;

    @EJB private RegistroEntradaLocal registroEntradaEjb;
    @EJB private RegistroSalidaLocal registroSalidaEjb;
    @EJB private LibroLocal libroEjb;
    @EJB private OrganismoLocal organismoEjb;
    @EJB private OficinaLocal oficinaEjb;
    @EJB private CatPaisLocal catPaisEjb;
    @EJB private CatProvinciaLocal catProvinciaEjb;
    @EJB private CatLocalidadLocal catLocalidadEjb;
    @EJB private TipoDocumentalLocal tipoDocumentalEjb;
    @EJB private AsientoRegistralSirLocal asientoRegistralSirEjb;
    @EJB private OficioRemisionLocal oficioRemisionEjb;
    @EJB private TrazabilidadLocal trazabilidadEjb;
    @EJB private AnexoLocal anexoEjb;


    /**
     * Recibe un fichero de intercambio en formato SICRES3 desde un nodo distribuido
     * @param ficheroIntercambio
     * @throws Exception
     */
    @Override
    public void recibirFicheroIntercambio(FicheroIntercambio ficheroIntercambio) throws Exception{

        // ENVIO Y REENVIO
        if (TipoAnotacion.ENVIO.getValue().equals(ficheroIntercambio.getTipoAnotacion()) ||
                TipoAnotacion.REENVIO.getValue().equals(ficheroIntercambio.getTipoAnotacion())) {

            log.info("El ficheroIntercambio recibido es un: " + ficheroIntercambio.getDescripcionTipoAnotacion());

            // Buscamos si el Registro recibido ya existe en el sistema
            AsientoRegistralSir asiento = asientoRegistralSirEjb.getAsientoRegistral(ficheroIntercambio.getIdentificadorIntercambio(),ficheroIntercambio.getCodigoEntidadRegistralDestino());

            if(asiento != null) { // Ya existe en el sistema

                if(EstadoAsientoRegistralSir.RECIBIDO.equals(asiento.getEstado())){

                    log.info("El AsientoRegistral" + asiento.getIdentificadorIntercambio() +" ya se ha recibido.");
                    throw new ValidacionException(Errores.ERROR_0205);

                }else if(EstadoAsientoRegistralSir.RECHAZADO.equals(asiento.getEstado()) ||
                        EstadoAsientoRegistralSir.RECHAZADO_Y_ACK.equals(asiento.getEstado()) ||
                        EstadoAsientoRegistralSir.RECHAZADO_Y_ERROR.equals(asiento.getEstado()) ||
                        EstadoAsientoRegistralSir.REENVIADO.equals(asiento.getEstado())){

                   // todo El asiento ya existe pero está Rechazado/Reenviado, que hacemos??

                }else{
                    log.info("Se ha intentado enviar un ficheroIntercambio con estado incompatible: " + ficheroIntercambio.getIdentificadorIntercambio());
                    throw new ValidacionException(Errores.ERROR_0063);
                }


            }else{ // No existe en el sistema, creamos un nuevo AsientoRegistralSir

                // Convertimos el Fichero de Intercambio SICRES3 en {@link es.caib.regweb3.model.AsientoRegistralSir}
                asiento = asientoRegistralSirEjb.transformarFicheroIntercambio(ficheroIntercambio);
                asiento.setEstado(EstadoAsientoRegistralSir.RECIBIDO);

                asientoRegistralSirEjb.crearAsientoRegistralSir(asiento);
                log.info("El asiento no existía en el sistema y se ha creado: " + asiento.getIdentificadorIntercambio());

            }

        // RECHAZO
        }else if (TipoAnotacion.RECHAZO.getValue().equals(ficheroIntercambio.getTipoAnotacion())) {

            log.info("El ficheroIntercambio recibido es un RECHAZO");

            // Buscamos si el Asiento recibido ya existe en el sistema
            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(ficheroIntercambio.getIdentificadorIntercambio());

            if(oficioRemision != null) { // Existe en el sistema

                if(oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_ENVIADO ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_ENVIADO_ACK ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_REENVIADO ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_RECHAZADO_ACK){

                    RegistroEntrada registroEntrada = oficioRemision.getRegistrosEntrada().get(0);

                    // Actualizamos el asiento
                    registroEntrada.setEstado(RegwebConstantes.REGISTRO_RECHAZADO);
                    registroEntrada.getRegistroDetalle().setAplicacion(ficheroIntercambio.getAplicacionEmisora());
                    registroEntrada.getRegistroDetalle().setObservaciones(ficheroIntercambio.getObservacionesApunte());
                    registroEntrada.getRegistroDetalle().setTipoAnotacion(ficheroIntercambio.getTipoAnotacion());
                    registroEntrada.getRegistroDetalle().setDecodificacionTipoAnotacion(ficheroIntercambio.getDescripcionTipoAnotacion());
                    registroEntradaEjb.merge(registroEntrada);

                    // Actualizamos el oficio
                    oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_DEVUELTO);
                    oficioRemision.setFechaEstado(new Date());
                    oficioRemisionEjb.merge(oficioRemision);

                }else if(oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_DEVUELTO){

                    log.info("Se ha intentado rechazar un asiento que ya esta devuelto" + ficheroIntercambio.getIdentificadorIntercambio());
                    throw new ValidacionException(Errores.ERROR_0205);

                }else{
                    log.info("Se ha intentado rechazar cuyo estado no lo permite: " + ficheroIntercambio.getIdentificadorIntercambio());
                    throw new ValidacionException(Errores.ERROR_0063);
                }


            }else{
                log.info("El registro recibido no existe en el sistema: " + ficheroIntercambio.getIdentificadorIntercambio());
                throw new ValidacionException(Errores.ERROR_0063);
            }
        }
    }

    /**
     * Realiza las acciones pertinentes cuando se recibie un mensaje de control
     * @param mensaje
     * @throws Exception
     */
    @Override
    public void recibirMensajeDatosControl(Mensaje mensaje) throws Exception{

        // Mensaje ACK
        if(mensaje.getTipoMensaje().equals(TipoMensaje.ACK)){

            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(mensaje.getIdentificadorIntercambio());
            AsientoRegistralSir asientoRegistralSir = asientoRegistralSirEjb.getAsientoRegistral(mensaje.getIdentificadorIntercambio(),mensaje.getCodigoEntidadRegistralDestino());

            if(oficioRemision != null){
                procesarMensajeACK(oficioRemision);
            }else if(asientoRegistralSir != null){
                procesarMensajeACK(asientoRegistralSir);
            }else{
                log.info("El mensaje de control corresponde a un Asiento registral que no existe en el sistema");
                throw new ValidacionException(Errores.ERROR_0044);
            }

        // Mensaje CONFIRMACIÓN
        }else if(mensaje.getTipoMensaje().equals(TipoMensaje.CONFIRMACION)){

            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(mensaje.getIdentificadorIntercambio());

            if(oficioRemision != null){
                procesarMensajeCONFIRMACION(oficioRemision, mensaje);
            }else{
                log.info("El mensaje de control corresponde a un Asiento registral que no existe en el sistema");
                throw new ValidacionException(Errores.ERROR_0044);
            }


        // Mensaje ERROR
        }else if(mensaje.getTipoMensaje().equals(TipoMensaje.ERROR)){

            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(mensaje.getIdentificadorIntercambio());
            AsientoRegistralSir asientoRegistralSir = asientoRegistralSirEjb.getAsientoRegistral(mensaje.getIdentificadorIntercambio(),mensaje.getCodigoEntidadRegistralDestino());

            if(oficioRemision != null){
                procesarMensajeERROR(oficioRemision, mensaje);
            }else if(asientoRegistralSir != null){
                procesarMensajeERROR(asientoRegistralSir, mensaje);
            }else{
                log.info("El mensaje de control corresponde a un Asiento registral que no existe en el sistema");
                throw new ValidacionException(Errores.ERROR_0044);
            }

        }else{
            log.info("El tipo mensaje de control no es válido: " + mensaje.getTipoMensaje());
            throw new ValidacionException(Errores.ERROR_0044);
        }

    }

    /**
     * Procesa un mensaje de control de tipo ACK
     * @param oficioRemision
     * @throws Exception
     */
    private void procesarMensajeACK(OficioRemision oficioRemision) throws Exception{

        if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO)){

            // Actualizamos el asiento
            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_ENVIADO_ACK);
            oficioRemision.setFechaEstado(new Date());
            oficioRemision.setNumeroReintentos(0);
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_REENVIADO)){

            // Actualizamos el asiento
            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_REENVIADO_ACK);
            oficioRemision.setFechaEstado(new Date());
            oficioRemision.setNumeroReintentos(0);
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_RECHAZADO)){

            // Actualizamos el asiento
            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_RECHAZADO_ACK);
            oficioRemision.setFechaEstado(new Date());
            oficioRemision.setNumeroReintentos(0);
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO_ACK) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_REENVIADO_ACK) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_RECHAZADO_ACK)){

            log.info("Se ha recibido un mensaje duplicado con identificador: " + oficioRemision.getIdentificadorIntercambio());
            throw new ValidacionException(Errores.ERROR_0206);

        }else{
            log.info("Se ha recibido un mensaje que no tiene el estado adecuado para recibir un ACK");
            throw new ValidacionException(Errores.ERROR_0044);
        }
    }

    /**
     * Procesa un mensaje de control de tipo ACK
     * @param asientoRegistralSir
     * @throws Exception
     */
    private void procesarMensajeACK(AsientoRegistralSir asientoRegistralSir) throws Exception{

        if (EstadoAsientoRegistralSir.ENVIADO.equals(asientoRegistralSir.getEstado())){

            // Actualizamos el asiento
            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.ENVIADO_Y_ACK);
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.REENVIADO.equals(asientoRegistralSir.getEstado())){

            // Actualizamos el asiento
            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.REENVIADO_Y_ACK);
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.RECHAZADO.equals(asientoRegistralSir.getEstado())){

            // Actualizamos el asiento
            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.RECHAZADO_Y_ACK);
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.ENVIADO_Y_ACK.equals(asientoRegistralSir.getEstado()) ||
                EstadoAsientoRegistralSir.REENVIADO_Y_ACK.equals(asientoRegistralSir.getEstado()) ||
                EstadoAsientoRegistralSir.RECHAZADO_Y_ACK.equals(asientoRegistralSir.getEstado())){

            log.info("Se ha recibido un asiento duplicado con identificador: " + asientoRegistralSir.getIdentificadorIntercambio());
            throw new ValidacionException(Errores.ERROR_0206);

        }else{
            log.info("Se ha recibido un mensaje que no tiene el estado adecuado para recibir un ACK");
            throw new ValidacionException(Errores.ERROR_0044);
        }
    }

    /**
     * Procesa un mensaje de control de tipo CONFIRMACION
     * @param oficioRemision
     * @throws Exception
     */
    private void procesarMensajeCONFIRMACION(OficioRemision oficioRemision, Mensaje mensaje) throws Exception{

        if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO_ACK) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO_ERROR)){

            oficioRemision.setNumeroRegistroEntradaDestino(mensaje.getNumeroRegistroEntradaDestino());
            oficioRemision.setFechaEntradaDestino(mensaje.getFechaEntradaDestino());
            oficioRemision.setEstado(RegwebConstantes.OFICIO_ACEPTADO);
            oficioRemision.setFechaEstado(mensaje.getFechaEntradaDestino());
            oficioRemisionEjb.merge(oficioRemision);

        }else  if(oficioRemision.getEstado() == (RegwebConstantes.OFICIO_ACEPTADO)){

            log.info("Se ha recibido un mensaje de confirmación duplicado: " + mensaje.toString());
            throw new ValidacionException(Errores.ERROR_0206);

        }else{
            log.info("El asiento registral no tiene el estado necesario para ser Confirmado: " + oficioRemision.getIdentificadorIntercambio());
            throw new ValidacionException(Errores.ERROR_0044);
        }
    }

    /**
     * Procesa un mensaje de control de tipo ERROR
     * @param oficioRemision
     * @param mensaje
     * @throws Exception
     */
    private void procesarMensajeERROR(OficioRemision oficioRemision, Mensaje mensaje) throws Exception{

        if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO)){

            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_ENVIADO_ERROR);
            oficioRemision.setCodigoError(mensaje.getCodigoError());
            oficioRemision.setDescripcionError(mensaje.getDescripcionMensaje());
            oficioRemision.setNumeroReintentos(0);
            oficioRemision.setFechaEstado(new Date());
            oficioRemisionEjb.merge(oficioRemision);


        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_REENVIADO)){

            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_REENVIADO_ERROR);
            oficioRemision.setCodigoError(mensaje.getCodigoError());
            oficioRemision.setDescripcionError(mensaje.getDescripcionMensaje());
            oficioRemision.setNumeroReintentos(0);
            oficioRemision.setFechaEstado(new Date());
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_RECHAZADO)){

            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_RECHAZADO_ERROR);
            oficioRemision.setCodigoError(mensaje.getCodigoError());
            oficioRemision.setDescripcionError(mensaje.getDescripcionMensaje());
            oficioRemision.setNumeroReintentos(0);
            oficioRemision.setFechaEstado(new Date());
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_ENVIADO_ERROR) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_REENVIADO_ERROR) ||
                oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_RECHAZADO_ERROR)){

            log.info("Se ha recibido un mensaje duplicado con identificador: " + oficioRemision.getIdentificadorIntercambio());
            throw new ValidacionException(Errores.ERROR_0206);

        }
    }

    /**
     * Procesa un mensaje de control de tipo ERROR
     * @param asientoRegistralSir
     * @param mensaje
     * @throws Exception
     */
    private void procesarMensajeERROR(AsientoRegistralSir asientoRegistralSir, Mensaje mensaje) throws Exception{

        if (EstadoAsientoRegistralSir.ENVIADO.equals(asientoRegistralSir.getEstado())){

            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.ENVIADO_Y_ERROR);
            /*asientoRegistralSir.setCodigoError(mensaje.getCodigoError());
            asientoRegistralSir.setDescripcionError(mensaje.getDescripcionMensaje());
            asientoRegistralSir.setNumeroReintentos(0);
            asientoRegistralSir.setFechaEstado(new Date());*/
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.REENVIADO.equals(asientoRegistralSir.getEstado())){

            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.REENVIADO_Y_ERROR);
            /*asientoRegistralSir.setCodigoError(mensaje.getCodigoError());
            asientoRegistralSir.setDescripcionError(mensaje.getDescripcionMensaje());
            asientoRegistralSir.setNumeroReintentos(0);
            asientoRegistralSir.setFechaEstado(new Date());*/
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.RECHAZADO.equals(asientoRegistralSir.getEstado())){

            asientoRegistralSir.setEstado(EstadoAsientoRegistralSir.RECHAZADO_Y_ERROR);
            /*asientoRegistralSir.setCodigoError(mensaje.getCodigoError());
            asientoRegistralSir.setDescripcionError(mensaje.getDescripcionMensaje());
            asientoRegistralSir.setNumeroReintentos(0);
            asientoRegistralSir.setFechaEstado(new Date());*/
            asientoRegistralSirEjb.merge(asientoRegistralSir);

        } else if (EstadoAsientoRegistralSir.ENVIADO_Y_ERROR.equals(asientoRegistralSir.getEstado()) ||
                EstadoAsientoRegistralSir.REENVIADO_Y_ERROR.equals(asientoRegistralSir.getEstado()) ||
                EstadoAsientoRegistralSir.RECHAZADO_Y_ERROR.equals(asientoRegistralSir.getEstado())){

            log.info("Se ha recibido un asiento duplicado con identificador: " + asientoRegistralSir.getIdentificadorIntercambio());
            throw new ValidacionException(Errores.ERROR_0206);

        }
    }

    /**
     *
     * @param idRegistro
     * @param codigoEntidadRegistralDestino
     * @param denominacionEntidadRegistralDestino
     * @param oficinaActiva
     * @param usuario
     * @param idLibro
     * @return
     * @throws Exception
     * @throws I18NException
     */
    @Override
    public OficioRemision enviarFicheroIntercambio(String tipoRegistro, Long idRegistro, String codigoEntidadRegistralDestino, String denominacionEntidadRegistralDestino, Oficina oficinaActiva, UsuarioEntidad usuario, Long idLibro) throws Exception, I18NException {

        // Creamos el OficioRemision
        OficioRemision oficioRemision = new OficioRemision();
        oficioRemision.setSir(true);
        oficioRemision.setEstado(RegwebConstantes.OFICIO_EXTERNO);
        oficioRemision.setFechaEstado(new Date());
        oficioRemision.setOficina(oficinaActiva);
        oficioRemision.setUsuarioResponsable(usuario);
        oficioRemision.setLibro(new Libro(idLibro));

        if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){

            RegistroEntrada registroEntrada = registroEntradaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroEntrada.getRegistroDetalle();

            // Si no tiene generado el Justificante, lo hacemos
            if (!registroDetalle.tieneJustificante()) {

                IJustificantePlugin justificantePlugin = RegwebJustificantePluginManager.getInstance(usuario.getEntidad().getId());

                if(justificantePlugin != null) {

                    // Generamos el pdf del Justificante
                    ByteArrayOutputStream baos = justificantePlugin.generarJustificante(registroEntrada);

                    // Creamos el anexo del justificante y se lo añadimos al registro
                    AnexoFull anexoFull = anexoEjb.crearJustificante(usuario, idRegistro, tipoRegistro.toLowerCase(), baos);
                    registroDetalle.getAnexos().add(anexoFull.getAnexo());
                }

            }

            // Actualizamos el Registro con campos SIR
            registroDetalle.setIndicadorPrueba(IndicadorPrueba.NORMAL);
            registroDetalle.setIdentificadorIntercambio(generarIdentificadorIntercambio(registroEntrada.getOficina().getCodigo()));
            registroDetalle.setCodigoEntidadRegistralDestino(codigoEntidadRegistralDestino);
            registroDetalle.setDecodificacionEntidadRegistralDestino(denominacionEntidadRegistralDestino);
            registroDetalle.setTipoAnotacion(TipoAnotacion.ENVIO.getValue());
            registroDetalle.setDecodificacionTipoAnotacion(TipoAnotacion.ENVIO.getName());

            // Actualizamos el registro
            registroEntrada = registroEntradaEjb.merge(registroEntrada);

            // Datos del Oficio de remisión
            oficioRemision.setIdentificadorIntercambio(registroEntrada.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA);
            oficioRemision.setDestinoExternoCodigo(registroEntrada.getDestinoExternoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroEntrada.getDestinoExternoDenominacion());
            oficioRemision.setRegistrosEntrada(Collections.singletonList(registroEntrada));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosSalida(null);

        }else if(tipoRegistro.equals(RegwebConstantes.REGISTRO_SALIDA_ESCRITO)){

            RegistroSalida registroSalida = registroSalidaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroSalida.getRegistroDetalle();

            // Si no tiene generado el Justificante, lo hacemos
            if (!registroDetalle.tieneJustificante()) {

                IJustificantePlugin justificantePlugin = RegwebJustificantePluginManager.getInstance(usuario.getEntidad().getId());

                if(justificantePlugin != null) {

                    // Generamos el pdf del Justificante
                    ByteArrayOutputStream baos = justificantePlugin.generarJustificante(registroSalida);

                    // Creamos el anexo del justificante y se lo añadimos al registro
                    AnexoFull anexoFull = anexoEjb.crearJustificante(usuario, idRegistro, tipoRegistro.toLowerCase(), baos);
                    registroDetalle.getAnexos().add(anexoFull.getAnexo());
                }

            }

            // Actualizamos el Registro con campos SIR
            registroDetalle.setIndicadorPrueba(IndicadorPrueba.NORMAL);
            registroDetalle.setIdentificadorIntercambio(generarIdentificadorIntercambio(registroSalida.getOficina().getCodigo()));
            registroDetalle.setCodigoEntidadRegistralDestino(codigoEntidadRegistralDestino);
            registroDetalle.setDecodificacionEntidadRegistralDestino(denominacionEntidadRegistralDestino);
            registroDetalle.setTipoAnotacion(TipoAnotacion.ENVIO.getValue());
            registroDetalle.setDecodificacionTipoAnotacion(TipoAnotacion.ENVIO.getName());

            // Actualizamos el registro
            registroSalida = registroSalidaEjb.merge(registroSalida);

            // Datos del Oficio de remisión
            oficioRemision.setIdentificadorIntercambio(registroSalida.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA);
            oficioRemision.setDestinoExternoCodigo(registroSalida.interesadoDestinoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroSalida.interesadoDestinoDenominacion());
            oficioRemision.setRegistrosSalida(Collections.singletonList(registroSalida));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosEntrada(null);

        }

        // Registramos el Oficio de Remisión SIR
        try {
            oficioRemision = oficioRemisionEjb.registrarOficioRemisionSIR(oficioRemision);

        } catch (I18NValidationException e) {
            e.printStackTrace();
        }


        return oficioRemision;

    }

    public AsientoRegistralSir reenviarAsientoRegistralSir(AsientoRegistralSir asientoRegistralSir, Oficina oficinaReenvio, Oficina oficinaActiva, Usuario usuario, String observaciones) throws Exception {


        //Actualizamos la oficina destino con la escogida por el usuario
        asientoRegistralSir.setCodigoEntidadRegistralDestino(oficinaReenvio.getCodigo());
        asientoRegistralSir.setDecodificacionEntidadRegistralDestino(oficinaReenvio.getDenominacion());

        //Actualizamos la oficina de origen con la oficina activa
        asientoRegistralSir.setCodigoEntidadRegistralOrigen(oficinaActiva.getCodigo());
        asientoRegistralSir.setDecodificacionEntidadRegistralOrigen(oficinaActiva.getDenominacion());

        //Actualizamos la unidad de tramitación destino con el organismo responsable de la oficina de reenvio
        asientoRegistralSir.setCodigoUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getCodigo());
        asientoRegistralSir.setDecodificacionUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getDenominacion());

        //Modificamos usuario, contacto, aplicacion
        asientoRegistralSir.setAplicacion(RegwebConstantes.CODIGO_APLICACION);
        asientoRegistralSir.setNombreUsuario(usuario.getNombreCompleto());
        asientoRegistralSir.setContactoUsuario(usuario.getEmail());

        asientoRegistralSir.setTipoAnotacion(TipoAnotacion.REENVIO.getValue());
        asientoRegistralSir.setDecodificacionTipoAnotacion(observaciones);


        asientoRegistralSirEjb.merge(asientoRegistralSir);

        return asientoRegistralSirEjb.getAsientoRegistralConAnexos(asientoRegistralSir.getId());

    }

    /**
     * Indica si el asiento registral se puede reenviar, en función de su estado
     * @param estado del asiento registral
     * @return
     */
    public boolean puedeReenviarAsientoRegistralSir(EstadoAsientoRegistralSir estado){
       return  estado.equals(EstadoAsientoRegistralSir.RECIBIDO) ||
               estado.equals(EstadoAsientoRegistralSir.DEVUELTO) ||
               estado.equals(EstadoAsientoRegistralSir.REENVIADO) ||
               estado.equals(EstadoAsientoRegistralSir.REENVIADO_Y_ERROR);

    }

    /**
     *
     * @param asiento
     * @param oficinaActiva
     * @param usuario
     * @return
     * @throws Exception
     */
    @Override
    public AsientoRegistralSir rechazarAsientoRegistralSir(AsientoRegistralSir asiento, Oficina oficinaActiva, Usuario usuario, String observaciones) throws Exception {

        // Modificamos la oficina destino con la de inicio
        asiento.setCodigoEntidadRegistralDestino(asiento.getCodigoEntidadRegistralInicio());
        asiento.setDecodificacionEntidadRegistralDestino(asiento.getDecodificacionEntidadRegistralInicio());

        // Modificamos la oficina de origen con la oficina activa
        asiento.setCodigoEntidadRegistralOrigen(oficinaActiva.getCodigo());
        asiento.setDecodificacionEntidadRegistralOrigen(oficinaActiva.getDenominacion());

        // Modificamos usuario, contacto, aplicacion
        asiento.setAplicacion(RegwebConstantes.CODIGO_APLICACION);
        asiento.setNombreUsuario(usuario.getNombreCompleto());
        asiento.setContactoUsuario(usuario.getEmail());

        asiento.setTipoAnotacion(TipoAnotacion.RECHAZO.getValue());
        asiento.setDecodificacionTipoAnotacion(observaciones);

        asiento = asientoRegistralSirEjb.merge(asiento);

        return asientoRegistralSirEjb.getAsientoRegistralConAnexos(asiento.getId());
    }

    /**
     * Acepta un AsientoRegistralSir, creando un Registro de Entrada o un Registro de Salida
     * @param asientoRegistralSir
     * @throws Exception
     */
    @Override
    public RegistroEntrada aceptarAsientoRegistralSir(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs)
            throws Exception {

            // Creamos y registramos el RegistroEntrada a partir del AsientoRegistral aceptado
            RegistroEntrada registroEntrada = null;
            try {
                registroEntrada = transformarAsientoRegistralEntrada(asientoRegistralSir, usuario, oficinaActiva, idLibro, idIdioma, idTipoAsunto, camposNTIs);

                // CREAMOS LA TRAZABILIDAD
                Trazabilidad trazabilidad = new Trazabilidad(RegwebConstantes.TRAZABILIDAD_RECIBIDO_SIR);
                trazabilidad.setAsientoRegistralSir(asientoRegistralSir);
                trazabilidad.setRegistroEntradaOrigen(null);
                trazabilidad.setOficioRemision(null);
                trazabilidad.setRegistroSalida(null);
                trazabilidad.setRegistroEntradaDestino(registroEntrada);
                trazabilidad.setFecha(new Date());

                trazabilidadEjb.persist(trazabilidad);

                // Modificamos el estado del AsientoRegistralSir
                asientoRegistralSirEjb.modificarEstado(asientoRegistralSir.getId(), EstadoAsientoRegistralSir.ACEPTADO);

                return registroEntrada;

            } catch (I18NException e) {
                e.printStackTrace();
            } catch (I18NValidationException e) {
                e.printStackTrace();
            }

        return null;

    }


    /**
     * Transforma un {@link AsientoRegistralSir} en un {@link es.caib.regweb3.model.RegistroEntrada}
     * @param asientoRegistralSir
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
    public RegistroEntrada transformarAsientoRegistralEntrada(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs)
            throws Exception, I18NException, I18NValidationException {

        Libro libro = libroEjb.findById(idLibro);

        RegistroEntrada registroEntrada = new RegistroEntrada();
        registroEntrada.setUsuario(usuario);
        registroEntrada.setOficina(oficinaActiva);
        registroEntrada.setEstado(RegwebConstantes.REGISTRO_VALIDO);
        registroEntrada.setLibro(libro);

        // Organismo destino
        Organismo organismoDestino;
        if(asientoRegistralSir.getCodigoUnidadTramitacionDestino() != null){
            organismoDestino = organismoEjb.findByCodigoEntidad(asientoRegistralSir.getCodigoUnidadTramitacionDestino(),usuario.getEntidad().getId());
            registroEntrada.setDestino(organismoDestino);
        }else{
            Oficina oficina = oficinaEjb.findByCodigoEntidad(asientoRegistralSir.getCodigoEntidadRegistralDestino(),usuario.getEntidad().getId());
            organismoDestino = organismoEjb.findByCodigoEntidad(oficina.getOrganismoResponsable().getCodigo(),usuario.getEntidad().getId());
        }

        registroEntrada.setDestino(organismoDestino);
        registroEntrada.setDestinoExternoCodigo(null);
        registroEntrada.setDestinoExternoDenominacion(null);

        // RegistroDetalle
        registroEntrada.setRegistroDetalle(getRegistroDetalle(asientoRegistralSir, idIdioma, idTipoAsunto));

        // Interesados
        List<Interesado> interesados = procesarInteresados(asientoRegistralSir.getInteresados());

        // Anexos
        List<AnexoFull> anexosFull = procesarAnexos(asientoRegistralSir, camposNTIs);

        // Registramos el Registro Entrada
        synchronized (this){
            registroEntrada = registroEntradaEjb.registrarEntrada(registroEntrada, usuario,interesados,anexosFull);
        }

        return registroEntrada;
    }

    /**
     * Transforma un {@link AsientoRegistralSir} en un {@link es.caib.regweb3.model.RegistroSalida}
     * @param asientoRegistralSir
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
    public RegistroSalida transformarAsientoRegistralSalida(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs) throws Exception, I18NException, I18NValidationException {

        Libro libro = libroEjb.findById(idLibro);

        RegistroSalida registroSalida = new RegistroSalida();
        registroSalida.setUsuario(usuario);
        registroSalida.setOficina(oficinaActiva);
        registroSalida.setEstado(RegwebConstantes.REGISTRO_VALIDO);
        registroSalida.setLibro(libro);

        // Organismo origen
        // TODO Esta asignación es incorrecta
        Organismo organismoOrigen;
        if(asientoRegistralSir.getCodigoUnidadTramitacionDestino() != null){
            organismoOrigen = organismoEjb.findByCodigoLigero(asientoRegistralSir.getCodigoUnidadTramitacionDestino());
            registroSalida.setOrigen(organismoOrigen);
        }

        registroSalida.setOrigenExternoCodigo(null);
        registroSalida.setOrigenExternoDenominacion(null);

        // RegistroDetalle
        registroSalida.setRegistroDetalle(getRegistroDetalle(asientoRegistralSir, idIdioma, idTipoAsunto));

        // Interesados
        List<Interesado> interesados = procesarInteresados(asientoRegistralSir.getInteresados());

        // Anexos
        List<AnexoFull> anexosFull = procesarAnexos(asientoRegistralSir, camposNTIs);

        // Registramos el Registro Entrada
        synchronized (this){
            registroSalida = registroSalidaEjb.registrarSalida(registroSalida, usuario,interesados, anexosFull);
        }

        return registroSalida;
    }

    /**
     * Obtiene un {@link RegistroDetalle} a partir de los datos de un AsientoRegistralSir
     * @param asientoRegistralSir
     * @param idIdioma
     * @param idTipoAsunto
     * @return
     * @throws Exception
     */
    private RegistroDetalle getRegistroDetalle(AsientoRegistralSir asientoRegistralSir, Long idIdioma, Long idTipoAsunto) throws Exception{

        RegistroDetalle registroDetalle = new RegistroDetalle();

        registroDetalle.setExtracto(asientoRegistralSir.getResumen());
        registroDetalle.setTipoDocumentacionFisica(Long.valueOf(asientoRegistralSir.getDocumentacionFisica()));
        registroDetalle.setIdioma(idIdioma);
        registroDetalle.setTipoAsunto(new TipoAsunto(idTipoAsunto));
        registroDetalle.setCodigoAsunto(null);

        if(asientoRegistralSir.getTipoTransporte() != null){
            registroDetalle.setTransporte(Long.valueOf(asientoRegistralSir.getTipoTransporte()));
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getNumeroTransporte())){
            registroDetalle.setNumeroTransporte(asientoRegistralSir.getNumeroTransporte());
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getObservacionesApunte())){
            registroDetalle.setObservaciones(asientoRegistralSir.getObservacionesApunte());
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getReferenciaExterna())){
            registroDetalle.setReferenciaExterna(asientoRegistralSir.getReferenciaExterna());
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getNumeroExpediente())){
            registroDetalle.setExpediente(asientoRegistralSir.getNumeroExpediente());
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getExpone())){
            registroDetalle.setExpone(asientoRegistralSir.getExpone());
        }
        if(!StringUtils.isEmpty(asientoRegistralSir.getSolicita())){
            registroDetalle.setSolicita(asientoRegistralSir.getSolicita());
        }

        registroDetalle.setOficinaOrigen(null);
        registroDetalle.setOficinaOrigenExternoCodigo(asientoRegistralSir.getCodigoEntidadRegistralOrigen());
        registroDetalle.setOficinaOrigenExternoDenominacion(asientoRegistralSir.getDecodificacionEntidadRegistralOrigen());

        registroDetalle.setNumeroRegistroOrigen(asientoRegistralSir.getNumeroRegistro());
        registroDetalle.setFechaOrigen(asientoRegistralSir.getFechaRegistro());

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
        if (StringUtils.isEmpty(interesadoSir.getRazonSocialInteresado())) {
            interesado.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_FISICA);

        } else {
            interesado.setTipo(RegwebConstantes.TIPO_INTERESADO_PERSONA_JURIDICA);
        }

        if (!StringUtils.isEmpty(interesadoSir.getRazonSocialInteresado())) {
            interesado.setRazonSocial(interesadoSir.getRazonSocialInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getNombreInteresado())) {
            interesado.setNombre(interesadoSir.getNombreInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getPrimerApellidoInteresado())) {
            interesado.setApellido1(interesadoSir.getPrimerApellidoInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getSegundoApellidoInteresado())) {
            interesado.setApellido2(interesadoSir.getSegundoApellidoInteresado());
        }
        if (interesadoSir.getTipoDocumentoIdentificacionInteresado() != null) {
            interesado.setTipoDocumentoIdentificacion(RegwebConstantes.TIPODOCUMENTOID_BY_CODIGO_NTI.get(interesadoSir.getTipoDocumentoIdentificacionInteresado().charAt(0)));
        }
        if (!StringUtils.isEmpty(interesadoSir.getDocumentoIdentificacionInteresado())) {
            interesado.setDocumento(interesadoSir.getDocumentoIdentificacionInteresado());
        }

        if (!StringUtils.isEmpty(interesadoSir.getCodigoPaisInteresado())) {
            try {
                interesado.setPais(catPaisEjb.findByCodigo(Long.valueOf(interesadoSir.getCodigoPaisInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(interesadoSir.getCodigoProvinciaInteresado())) {
            try {
                interesado.setProvincia(catProvinciaEjb.findByCodigo(Long.valueOf(interesadoSir.getCodigoProvinciaInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(interesadoSir.getCodigoMunicipioInteresado())) {
            try {
                interesado.setLocalidad(catLocalidadEjb.findByLocalidadProvincia(Long.valueOf(interesadoSir.getCodigoMunicipioInteresado()), Long.valueOf(interesadoSir.getCodigoProvinciaInteresado())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(interesadoSir.getDireccionInteresado())) {
            interesado.setDireccion(interesadoSir.getDireccionInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getCodigoPostalInteresado())) {
            interesado.setCp(interesadoSir.getCodigoPostalInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getCorreoElectronicoInteresado())) {
            interesado.setEmail(interesadoSir.getCorreoElectronicoInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getTelefonoInteresado())) {
            interesado.setTelefono(interesadoSir.getTelefonoInteresado());
        }
        if (!StringUtils.isEmpty(interesadoSir.getDireccionElectronicaHabilitadaInteresado())) {
            interesado.setDireccionElectronica(interesadoSir.getDireccionElectronicaHabilitadaInteresado());
        }
        if (interesadoSir.getCanalPreferenteComunicacionInteresado() != null) {
            interesado.setCanal(RegwebConstantes.CANALNOTIFICACION_BY_CODIGO.get(interesadoSir.getCanalPreferenteComunicacionInteresado()));
        }
        if (!StringUtils.isEmpty(interesadoSir.getObservaciones())) {
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

        if (!StringUtils.isEmpty(representanteSir.getRazonSocialRepresentante())) {
            representante.setRazonSocial(representanteSir.getRazonSocialRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getNombreRepresentante())) {
            representante.setNombre(representanteSir.getNombreRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getPrimerApellidoRepresentante())) {
            representante.setApellido1(representanteSir.getPrimerApellidoRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getSegundoApellidoRepresentante())) {
            representante.setApellido2(representanteSir.getSegundoApellidoRepresentante());
        }
        if (representanteSir.getTipoDocumentoIdentificacionRepresentante() != null) {
            representante.setTipoDocumentoIdentificacion(RegwebConstantes.TIPODOCUMENTOID_BY_CODIGO_NTI.get(representanteSir.getTipoDocumentoIdentificacionRepresentante().charAt(0)));
        }
        if (!StringUtils.isEmpty(representanteSir.getDocumentoIdentificacionRepresentante())) {
            representante.setDocumento(representanteSir.getDocumentoIdentificacionRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getCodigoPaisRepresentante())) {
            try {
                representante.setPais(catPaisEjb.findByCodigo(Long.valueOf(representanteSir.getCodigoPaisRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(representanteSir.getCodigoProvinciaRepresentante())) {
            try {
                representante.setProvincia(catProvinciaEjb.findByCodigo(Long.valueOf(representanteSir.getCodigoProvinciaRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(representanteSir.getCodigoMunicipioRepresentante())) {
            try {
                representante.setLocalidad(catLocalidadEjb.findByLocalidadProvincia(Long.valueOf(representanteSir.getCodigoMunicipioRepresentante()), Long.valueOf(representanteSir.getCodigoProvinciaRepresentante())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(representanteSir.getDireccionRepresentante())) {
            representante.setDireccion(representanteSir.getDireccionRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getCodigoPostalRepresentante())) {
            representante.setCp(representanteSir.getCodigoPostalRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getCorreoElectronicoRepresentante())) {
            representante.setEmail(representanteSir.getCorreoElectronicoRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getTelefonoRepresentante())) {
            representante.setTelefono(representanteSir.getTelefonoRepresentante());
        }
        if (!StringUtils.isEmpty(representanteSir.getDireccionElectronicaHabilitadaRepresentante())) {
            representante.setDireccionElectronica(representanteSir.getDireccionElectronicaHabilitadaRepresentante());
        }
        if (representanteSir.getCanalPreferenteComunicacionRepresentante() != null) {
            representante.setCanal(RegwebConstantes.CANALNOTIFICACION_BY_CODIGO.get(representanteSir.getCanalPreferenteComunicacionRepresentante()));
        }
        if (!StringUtils.isEmpty(representanteSir.getObservaciones())) {
            representante.setObservaciones(representanteSir.getObservaciones());
        }

        return representante;

    }



    /**
     *
     * @param asientoRegistralSir
     * @param camposNTIs representa la lista de anexos del asiento registral en los que el usuario ha especificado
     *                          los valores de los campos NTI no informados por SICRES (validez Documento, origen, Tipo Documental)
     * @return
     * @throws Exception
     */
    private List<AnexoFull> procesarAnexos(AsientoRegistralSir asientoRegistralSir, List<CamposNTI> camposNTIs) throws Exception {


        List<AnexoFull> anexos = new ArrayList<AnexoFull>();
        HashMap<String,AnexoFull> mapAnexosFull = new HashMap<String, AnexoFull>();

        //Aqui buscamos los camposNTI del anexoSir con el que se corresponde para pasarlo al método transformarAnexo
        for (AnexoSir anexoSir : asientoRegistralSir.getAnexos()) {
            for (CamposNTI cnti : camposNTIs) {
                if (anexoSir.getId().equals(cnti.getId())) {
                    AnexoFull anexoFull = transformarAnexo(anexoSir, asientoRegistralSir.getEntidad().getId(), mapAnexosFull, cnti);
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
        if (camposNTI.getIdTipoDocumental() != null) {
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



        DocumentCustody dc= new DocumentCustody();
        SignatureCustody sc= new SignatureCustody();
        // Si el anexo tiene identificador_documento_firmado, es que es la firma de un anexo anterior.
        if (!StringUtils.isEmpty(anexoSir.getIdentificadorDocumentoFirmado()) && anexoSir.getIdentificadorDocumentoFirmado() != null) {
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



    protected DocumentCustody getDocumentCustody(AnexoSir anexoSir) throws Exception {
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


    protected SignatureCustody getSignatureCustody(AnexoSir anexoSir, DocumentCustody dc,
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

    /**
     * Genera el identificador de intercambio a partir del código de la oficina de origen
     *
     * @param codOficinaOrigen
     * @return
     * @throws Exception
     */
    protected String generarIdentificadorIntercambio(String codOficinaOrigen) {

        String identificador = "";
        SimpleDateFormat anyo = new SimpleDateFormat("yy"); // Just the year, with 2 digits

        identificador = codOficinaOrigen + "_" + anyo.format(Calendar.getInstance().getTime()) + "_" + getIdToken(); //todo: Añadir secuencia real


        return identificador;
    }

    /**
     * Calcula una cadena de ocho dígitos a partir del instante de tiempo actual.
     *
     * @return la cadena (String) de ocho digitos
     */
    private static final AtomicLong TIME_STAMP = new AtomicLong();

    private String getIdToken() {
        long now = System.currentTimeMillis();
        while (true) {
            long last = TIME_STAMP.get();
            if (now <= last)
                now = last + 1;
            if (TIME_STAMP.compareAndSet(last, now))
                break;
        }
        long unsignedValue = Long.toString(now).hashCode() & 0xffffffffl;
        String result = Long.toString(unsignedValue);
        if (result.length() > 8) {
            result = result.substring(result.length() - 8, result.length());
        } else {
            result = String.format("%08d", unsignedValue);
        }
        return result;
    }

}
