package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.model.utils.CamposNTI;
import es.caib.regweb3.model.utils.EstadoRegistroSir;
import es.caib.regweb3.model.utils.IndicadorPrueba;
import es.caib.regweb3.plugins.justificante.IJustificantePlugin;
import es.caib.regweb3.sir.core.excepcion.ValidacionException;
import es.caib.regweb3.sir.core.model.Errores;
import es.caib.regweb3.sir.core.model.TipoAnotacion;
import es.caib.regweb3.sir.core.model.TipoMensaje;
import es.caib.regweb3.sir.core.utils.FicheroIntercambio;
import es.caib.regweb3.sir.core.utils.Mensaje;
import es.caib.regweb3.sir.ejb.EmisionLocal;
import es.caib.regweb3.sir.ejb.MensajeLocal;
import es.caib.regweb3.utils.RegwebConstantes;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.plugins.documentcustody.api.IDocumentCustodyPlugin;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataConstants;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.text.MessageFormat;
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

    @EJB private RegistroEntradaLocal registroEntradaEjb;
    @EJB private RegistroSalidaLocal registroSalidaEjb;
    @EJB private RegistroSirLocal registroSirEjb;
    @EJB private OficioRemisionLocal oficioRemisionEjb;
    @EJB private TrazabilidadLocal trazabilidadEjb;
    @EJB private AnexoLocal anexoEjb;
    @EJB private EmisionLocal emisionEjb;
    @EJB private MensajeLocal mensajeEjb;
    @EJB private PluginLocal pluginEjb;


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

            log.info("El ficheroIntercambio recibido es un: " + ficheroIntercambio.getTipoAnotacion() + " - "+ ficheroIntercambio.getDescripcionTipoAnotacion());

            // Buscamos si el Registro recibido ya existe en el sistema
            RegistroSir registroSir = registroSirEjb.getRegistroSir(ficheroIntercambio.getIdentificadorIntercambio(),ficheroIntercambio.getCodigoEntidadRegistralDestino());

            if(registroSir != null) { // Ya existe en el sistema

                if(EstadoRegistroSir.RECIBIDO.equals(registroSir.getEstado())){

                    log.info("El RegistroSir" + registroSir.getIdentificadorIntercambio() +" ya se ha recibido.");
                    throw new ValidacionException(Errores.ERROR_0205);

                }else if(EstadoRegistroSir.RECHAZADO.equals(registroSir.getEstado()) ||
                        EstadoRegistroSir.RECHAZADO_Y_ACK.equals(registroSir.getEstado()) ||
                        EstadoRegistroSir.RECHAZADO_Y_ERROR.equals(registroSir.getEstado()) ||
                        EstadoRegistroSir.REENVIADO_Y_ACK.equals(registroSir.getEstado())){

                    // Eliminamos el RegistroSir para volverlo a crear
                    registroSirEjb.remove(registroSir);

                }else{
                    log.info("Se ha intentado enviar un ficheroIntercambio con estado incompatible: " + ficheroIntercambio.getIdentificadorIntercambio());
                    throw new ValidacionException(Errores.ERROR_0063);
                }


            }

            // Creamos un nuevo RegistroSir
            registroSir = registroSirEjb.transformarFicheroIntercambio(ficheroIntercambio);
            registroSir.setEstado(EstadoRegistroSir.RECIBIDO);

            registroSirEjb.crearRegistroSir(registroSir);
            log.info("El registroSir no existía en el sistema y se ha creado: " + registroSir.getIdentificadorIntercambio());


        // RECHAZO
        }else if (TipoAnotacion.RECHAZO.getValue().equals(ficheroIntercambio.getTipoAnotacion())) {

            log.info("El ficheroIntercambio recibido es un RECHAZO");

            // Buscamos si el FicheroIntercambio ya existe en el sistema
            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(ficheroIntercambio.getIdentificadorIntercambio());

            if(oficioRemision != null) { // Existe en el sistema

                if(oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_ENVIADO ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_ENVIADO_ACK ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_REENVIADO ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_REENVIADO_ACK ||
                        oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_RECHAZADO_ACK){

                    if(oficioRemision.getTipoOficioRemision().equals(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA)){

                        RegistroEntrada registroEntrada = oficioRemision.getRegistrosEntrada().get(0);
                        // Actualizamos el registroSir
                        registroEntrada.setEstado(RegwebConstantes.REGISTRO_RECHAZADO);
                        registroEntrada.getRegistroDetalle().setAplicacion(ficheroIntercambio.getAplicacionEmisora());
                        registroEntrada.getRegistroDetalle().setObservaciones(ficheroIntercambio.getObservacionesApunte());
                        registroEntrada.getRegistroDetalle().setTipoAnotacion(ficheroIntercambio.getTipoAnotacion());
                        registroEntrada.getRegistroDetalle().setDecodificacionTipoAnotacion(ficheroIntercambio.getDescripcionTipoAnotacion());
                        registroEntradaEjb.merge(registroEntrada);

                    }else if(oficioRemision.getTipoOficioRemision().equals(RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA)){

                        RegistroSalida registroSalida = oficioRemision.getRegistrosSalida().get(0);
                        // Actualizamos el registroSir
                        registroSalida.setEstado(RegwebConstantes.REGISTRO_RECHAZADO);
                        registroSalida.getRegistroDetalle().setAplicacion(ficheroIntercambio.getAplicacionEmisora());
                        registroSalida.getRegistroDetalle().setObservaciones(ficheroIntercambio.getObservacionesApunte());
                        registroSalida.getRegistroDetalle().setTipoAnotacion(ficheroIntercambio.getTipoAnotacion());
                        registroSalida.getRegistroDetalle().setDecodificacionTipoAnotacion(ficheroIntercambio.getDescripcionTipoAnotacion());
                        registroSalidaEjb.merge(registroSalida);
                    }

                    // Actualizamos el oficio
                    oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_DEVUELTO);
                    oficioRemision.setFechaEstado(new Date());
                    oficioRemisionEjb.merge(oficioRemision);

                }else if(oficioRemision.getEstado() == RegwebConstantes.OFICIO_SIR_DEVUELTO){

                    log.info("Se ha intentado rechazar un registroSir que ya esta devuelto" + ficheroIntercambio.getIdentificadorIntercambio());
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
            RegistroSir registroSir = registroSirEjb.getRegistroSir(mensaje.getIdentificadorIntercambio(),mensaje.getCodigoEntidadRegistralDestino());

            if(oficioRemision != null){
                procesarMensajeACK(oficioRemision);
            }else if(registroSir != null){
                procesarMensajeACK(registroSir);
            }else{
                log.info("El mensaje de control corresponde a un IdentificadorIntercambio que no existe en el sistema");
                throw new ValidacionException(Errores.ERROR_0044);
            }

            // Mensaje CONFIRMACIÓN
        }else if(mensaje.getTipoMensaje().equals(TipoMensaje.CONFIRMACION)){

            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(mensaje.getIdentificadorIntercambio());

            if(oficioRemision != null){
                procesarMensajeCONFIRMACION(oficioRemision, mensaje);
            }else{
                log.info("El mensaje de control corresponde a un IdentificadorIntercambio que no existe en el sistema");
                throw new ValidacionException(Errores.ERROR_0044);
            }


            // Mensaje ERROR
        }else if(mensaje.getTipoMensaje().equals(TipoMensaje.ERROR)){

            OficioRemision oficioRemision = oficioRemisionEjb.getByIdentificadorIntercambio(mensaje.getIdentificadorIntercambio());
            RegistroSir registroSir = registroSirEjb.getRegistroSir(mensaje.getIdentificadorIntercambio(),mensaje.getCodigoEntidadRegistralDestino());

            if(oficioRemision != null){
                procesarMensajeERROR(oficioRemision, mensaje);
            }else if(registroSir != null){
                procesarMensajeERROR(registroSir, mensaje);
            }else{
                log.info("El mensaje de control corresponde a un IdentificadorIntercambio que no existe en el sistema");
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

            // Actualizamos el OficioRemision
            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_ENVIADO_ACK);
            oficioRemision.setFechaEstado(new Date());
            oficioRemision.setNumeroReintentos(0);
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_REENVIADO)){

            // Actualizamos el OficioRemision
            oficioRemision.setEstado(RegwebConstantes.OFICIO_SIR_REENVIADO_ACK);
            oficioRemision.setFechaEstado(new Date());
            oficioRemision.setNumeroReintentos(0);
            oficioRemisionEjb.merge(oficioRemision);

        } else if (oficioRemision.getEstado() == (RegwebConstantes.OFICIO_SIR_RECHAZADO)){

            // Actualizamos el OficioRemision
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
     * @param registroSir
     * @throws Exception
     */
    private void procesarMensajeACK(RegistroSir registroSir) throws Exception{

        if (EstadoRegistroSir.ENVIADO.equals(registroSir.getEstado())){

            // Actualizamos el registroSir
            registroSir.setEstado(EstadoRegistroSir.ENVIADO_Y_ACK);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.REENVIADO.equals(registroSir.getEstado())){

            // Actualizamos el registroSir
            registroSir.setEstado(EstadoRegistroSir.REENVIADO_Y_ACK);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.RECHAZADO.equals(registroSir.getEstado())){

            // Actualizamos el registroSir
            registroSir.setEstado(EstadoRegistroSir.RECHAZADO_Y_ACK);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.ENVIADO_Y_ACK.equals(registroSir.getEstado()) ||
                EstadoRegistroSir.REENVIADO_Y_ACK.equals(registroSir.getEstado()) ||
                EstadoRegistroSir.RECHAZADO_Y_ACK.equals(registroSir.getEstado())){

            log.info("Se ha recibido un registroSir duplicado con identificador: " + registroSir.getIdentificadorIntercambio());
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
            log.info("El RegistroSir no tiene el estado necesario para ser Confirmado: " + oficioRemision.getIdentificadorIntercambio());
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
     * @param registroSir
     * @param mensaje
     * @throws Exception
     */
    private void procesarMensajeERROR(RegistroSir registroSir, Mensaje mensaje) throws Exception{

        if (EstadoRegistroSir.ENVIADO.equals(registroSir.getEstado())){

            registroSir.setEstado(EstadoRegistroSir.ENVIADO_Y_ERROR);
            registroSir.setCodigoError(mensaje.getCodigoError());
            registroSir.setDescripcionError(mensaje.getDescripcionMensaje());
            registroSir.setNumeroReintentos(0);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.REENVIADO.equals(registroSir.getEstado())){

            registroSir.setEstado(EstadoRegistroSir.REENVIADO_Y_ERROR);
            registroSir.setCodigoError(mensaje.getCodigoError());
            registroSir.setDescripcionError(mensaje.getDescripcionMensaje());
            registroSir.setNumeroReintentos(0);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.RECHAZADO.equals(registroSir.getEstado())){

            registroSir.setEstado(EstadoRegistroSir.RECHAZADO_Y_ERROR);
            registroSir.setCodigoError(mensaje.getCodigoError());
            registroSir.setDescripcionError(mensaje.getDescripcionMensaje());
            registroSir.setNumeroReintentos(0);
            registroSir.setFechaEstado(new Date());
            registroSirEjb.merge(registroSir);

        } else if (EstadoRegistroSir.ENVIADO_Y_ERROR.equals(registroSir.getEstado()) ||
                EstadoRegistroSir.REENVIADO_Y_ERROR.equals(registroSir.getEstado()) ||
                EstadoRegistroSir.RECHAZADO_Y_ERROR.equals(registroSir.getEstado())){

            log.info("Se ha recibido un registroSir duplicado con identificador: " + registroSir.getIdentificadorIntercambio());
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
     * @return
     * @throws Exception
     * @throws I18NException
     */
    @Override
    public OficioRemision enviarFicheroIntercambio(String tipoRegistro, Long idRegistro, String codigoEntidadRegistralDestino, String denominacionEntidadRegistralDestino, Oficina oficinaActiva, UsuarioEntidad usuario) throws Exception, I18NException {

        RegistroSir registroSir = null;

        // Creamos el OficioRemision
        OficioRemision oficioRemision = new OficioRemision();
        oficioRemision.setSir(true);
        oficioRemision.setEstado(RegwebConstantes.OFICIO_EXTERNO);
        oficioRemision.setFechaEstado(new Date());
        oficioRemision.setOficina(oficinaActiva);
        oficioRemision.setUsuarioResponsable(usuario);

        if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){

            RegistroEntrada registroEntrada = registroEntradaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroEntrada.getRegistroDetalle();

            // Si no tiene generado el Justificante, lo hacemos
            if (!registroDetalle.tieneJustificante()) {

                IJustificantePlugin justificantePlugin = (IJustificantePlugin) pluginEjb.getPlugin(usuario.getEntidad().getId(), RegwebConstantes.PLUGIN_JUSTIFICANTE);

                if(justificantePlugin != null) {

                    // Generam la Custòdia per tenir el CSV
                    Map<String,Object> custodyParameters = new HashMap<String, Object>();
                    custodyParameters.put("registre", registroEntrada);
                    IDocumentCustodyPlugin plugin = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA_JUSTIFICANTE);
                    String custodyID = plugin.reserveCustodyID(custodyParameters);
                    Metadata mcsv = plugin.getOnlyOneMetadata(custodyID, MetadataConstants.ENI_CSV);
                    String csv = null;
                    if(mcsv != null){
                        csv = mcsv.getValue();
                    }
                    String url = plugin.getValidationUrl(custodyID, custodyParameters);
                    String specialValue = plugin.getSpecialValue(custodyID,custodyParameters);

                    // TODO aixpo s'ha d'obtenir d'una property
                    String urlVerificacio = url + specialValue;
                    String estampat = MessageFormat.format("Este es un mensaje de estampación {0} {1}", url, specialValue, csv);

                    // Generamos el pdf del Justificante

                    byte[] data = justificantePlugin.generarJustificante(registroEntrada, estampat, urlVerificacio);

                    // Creamos el anexo del justificante y se lo añadimos al registro
                    AnexoFull anexoFull = anexoEjb.crearJustificante(usuario, idRegistro, tipoRegistro.toLowerCase(), data, custodyID, csv);

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
            oficioRemision.setLibro(new Libro(registroEntrada.getLibro().getId()));
            oficioRemision.setIdentificadorIntercambio(registroEntrada.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA);
            oficioRemision.setDestinoExternoCodigo(registroEntrada.getDestinoExternoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroEntrada.getDestinoExternoDenominacion());
            oficioRemision.setRegistrosEntrada(Collections.singletonList(registroEntrada));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosSalida(null);

            // Transformamos el RegistroEntrada en un RegistroSir
            registroEntrada = registroEntradaEjb.getConAnexosFullCompleto(oficioRemision.getRegistrosEntrada().get(0).getId());
            registroSir = registroSirEjb.transformarRegistroEntrada(registroEntrada);

        }else if(tipoRegistro.equals(RegwebConstantes.REGISTRO_SALIDA_ESCRITO)){

            RegistroSalida registroSalida = registroSalidaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroSalida.getRegistroDetalle();

            // Si no tiene generado el Justificante, lo hacemos
            if (!registroDetalle.tieneJustificante()) {

                IJustificantePlugin justificantePlugin = (IJustificantePlugin) pluginEjb.getPlugin(usuario.getEntidad().getId(), RegwebConstantes.PLUGIN_JUSTIFICANTE);

                if(justificantePlugin != null) {

                    // Generam la Custòdia per tenir el CSV
                    Map<String,Object> custodyParameters = new HashMap<String, Object>();
                    custodyParameters.put("registre", registroSalida);
                    IDocumentCustodyPlugin plugin = (IDocumentCustodyPlugin) pluginEjb.getPlugin(null, RegwebConstantes.PLUGIN_CUSTODIA_JUSTIFICANTE);
                    String custodyID = plugin.reserveCustodyID(custodyParameters);
                    Metadata mcsv = plugin.getOnlyOneMetadata(custodyID, MetadataConstants.ENI_CSV);
                    String csv = null;
                    if(mcsv != null){
                        csv = mcsv.getValue();
                    }
                    String url = plugin.getValidationUrl(custodyID, custodyParameters);
                    String specialValue = plugin.getSpecialValue(custodyID,custodyParameters);

                    // TODO aixpo s'ha d'obtenir d'una property
                    String urlVerificacio = url + specialValue;
                    String estampat = MessageFormat.format("Este es un mensaje de estampación {0} {1}", url, specialValue, csv);

                    // Generamos el pdf del Justificante

                    byte[] data = justificantePlugin.generarJustificante(registroSalida, estampat, urlVerificacio);

                    // Creamos el anexo del justificante y se lo añadimos al registro
                    AnexoFull anexoFull = anexoEjb.crearJustificante(usuario, idRegistro, tipoRegistro.toLowerCase(), data, custodyID, csv);

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
            oficioRemision.setLibro(new Libro(registroSalida.getLibro().getId()));
            oficioRemision.setIdentificadorIntercambio(registroSalida.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA);
            oficioRemision.setDestinoExternoCodigo(registroSalida.interesadoDestinoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroSalida.interesadoDestinoDenominacion());
            oficioRemision.setRegistrosSalida(Collections.singletonList(registroSalida));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosEntrada(null);

            // Transformamos el RegistroSalida en un RegistroSir
            registroSalida = registroSalidaEjb.getConAnexosFullCompleto(oficioRemision.getRegistrosSalida().get(0).getId());
            registroSir = registroSirEjb.transformarRegistroSalida(registroSalida);

        }

        // Registramos el Oficio de Remisión SIR
        try {
            oficioRemision = oficioRemisionEjb.registrarOficioRemisionSIR(oficioRemision);

        } catch (I18NValidationException e) {
            e.printStackTrace();
        }

        // Enviamos el Registro al Componente CIR
        emisionEjb.enviarFicheroIntercambio(registroSir);

        // Modificamos el estado del OficioRemision
        oficioRemisionEjb.modificarEstado(oficioRemision.getId(), RegwebConstantes.OFICIO_SIR_ENVIADO);

        return oficioRemision;

    }

    @Override
    public void reenviarRegistro(String tipoRegistro, Long idRegistro, Oficina oficinaReenvio, Oficina oficinaActiva, UsuarioEntidad usuario, String observaciones) throws Exception, I18NException {

        RegistroSir registroSir = null;

        // Creamos el OficioRemision
        OficioRemision oficioRemision = new OficioRemision();
        oficioRemision.setSir(true);
        oficioRemision.setEstado(RegwebConstantes.OFICIO_EXTERNO);
        oficioRemision.setFechaEstado(new Date());
        oficioRemision.setOficina(oficinaActiva);
        oficioRemision.setUsuarioResponsable(usuario);

        if(tipoRegistro.equals(RegwebConstantes.REGISTRO_ENTRADA_ESCRITO)){

            RegistroEntrada registroEntrada = registroEntradaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroEntrada.getRegistroDetalle();

            // Actualizamos el Registro con campos SIR
            registroDetalle.setIndicadorPrueba(IndicadorPrueba.NORMAL);
            registroDetalle.setIdentificadorIntercambio(generarIdentificadorIntercambio(registroEntrada.getOficina().getCodigo()));
            registroDetalle.setCodigoEntidadRegistralDestino(oficinaReenvio.getCodigo());
            registroDetalle.setDecodificacionEntidadRegistralDestino(oficinaReenvio.getDenominacion());
            registroDetalle.setTipoAnotacion(TipoAnotacion.REENVIO.getValue());
            registroDetalle.setDecodificacionTipoAnotacion(observaciones);

            // Actualizamos el registro
            registroEntrada = registroEntradaEjb.merge(registroEntrada);

            // Datos del Oficio de remisión
            oficioRemision.setLibro(new Libro(registroEntrada.getLibro().getId()));
            oficioRemision.setIdentificadorIntercambio(registroEntrada.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA);
            oficioRemision.setDestinoExternoCodigo(registroEntrada.getDestinoExternoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroEntrada.getDestinoExternoDenominacion());
            oficioRemision.setRegistrosEntrada(Collections.singletonList(registroEntrada));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosSalida(null);

            // Transformamos el RegistroEntrada en un RegistroSir
            registroEntrada = registroEntradaEjb.getConAnexosFullCompleto(oficioRemision.getRegistrosEntrada().get(0).getId());
            registroSir = registroSirEjb.transformarRegistroEntrada(registroEntrada);

        }else if(tipoRegistro.equals(RegwebConstantes.REGISTRO_SALIDA_ESCRITO)){

            RegistroSalida registroSalida = registroSalidaEjb.findById(idRegistro);
            RegistroDetalle registroDetalle = registroSalida.getRegistroDetalle();

            // Actualizamos el Registro con campos SIR
            registroDetalle.setIndicadorPrueba(IndicadorPrueba.NORMAL);
            registroDetalle.setIdentificadorIntercambio(generarIdentificadorIntercambio(registroSalida.getOficina().getCodigo()));
            registroDetalle.setCodigoEntidadRegistralDestino(oficinaReenvio.getCodigo());
            registroDetalle.setDecodificacionEntidadRegistralDestino(oficinaReenvio.getDenominacion());
            registroDetalle.setTipoAnotacion(TipoAnotacion.REENVIO.getValue());
            registroDetalle.setDecodificacionTipoAnotacion(observaciones);

            // Actualizamos el registro
            registroSalida = registroSalidaEjb.merge(registroSalida);

            // Datos del Oficio de remisión
            oficioRemision.setLibro(new Libro(registroSalida.getLibro().getId()));
            oficioRemision.setIdentificadorIntercambio(registroSalida.getRegistroDetalle().getIdentificadorIntercambio());
            oficioRemision.setTipoOficioRemision(RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA);
            oficioRemision.setDestinoExternoCodigo(registroSalida.interesadoDestinoCodigo());
            oficioRemision.setDestinoExternoDenominacion(registroSalida.interesadoDestinoDenominacion());
            oficioRemision.setRegistrosSalida(Collections.singletonList(registroSalida));
            oficioRemision.setOrganismoDestinatario(null);
            oficioRemision.setRegistrosEntrada(null);

            // Transformamos el RegistroSalida en un RegistroSir
            registroSalida = registroSalidaEjb.getConAnexosFullCompleto(oficioRemision.getRegistrosSalida().get(0).getId());
            registroSir = registroSirEjb.transformarRegistroSalida(registroSalida);

        }

        // Registramos el Oficio de Remisión SIR
        try {
            oficioRemision = oficioRemisionEjb.registrarOficioRemisionSIR(oficioRemision);

        } catch (I18NValidationException e) {
            e.printStackTrace();
        }

        // Actualizamos la unidad de tramitación destino con el organismo responsable de la oficina de reenvio
        registroSir.setCodigoUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getCodigo());
        registroSir.setDecodificacionUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getDenominacion());

        // Enviamos el Registro al Componente CIR
        emisionEjb.reenviarFicheroIntercambio(registroSir);

        // Modificamos el estado del OficioRemision
        oficioRemisionEjb.modificarEstado(oficioRemision.getId(), RegwebConstantes.OFICIO_SIR_REENVIADO);
    }

    @Override
    public void reenviarRegistroSir(RegistroSir registroSir, Oficina oficinaReenvio, Oficina oficinaActiva, Usuario usuario, String observaciones) throws Exception {

        // Actualizamos la oficina destino con la escogida por el usuario
        registroSir.setCodigoEntidadRegistralDestino(oficinaReenvio.getCodigo());
        registroSir.setDecodificacionEntidadRegistralDestino(oficinaReenvio.getDenominacion());

        // Actualizamos la oficina de origen con la oficina activa
        registroSir.setCodigoEntidadRegistralOrigen(oficinaActiva.getCodigo());
        registroSir.setDecodificacionEntidadRegistralOrigen(oficinaActiva.getDenominacion());

        // Actualizamos la unidad de tramitación destino con el organismo responsable de la oficina de reenvio
        registroSir.setCodigoUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getCodigo());
        registroSir.setDecodificacionUnidadTramitacionDestino(oficinaReenvio.getOrganismoResponsable().getDenominacion());

        // Modificamos usuario, contacto, aplicacion
        registroSir.setAplicacion(RegwebConstantes.CODIGO_APLICACION);
        registroSir.setNombreUsuario(usuario.getNombreCompleto());
        registroSir.setContactoUsuario(usuario.getEmail());
        registroSir.setTipoAnotacion(TipoAnotacion.REENVIO.getValue());
        registroSir.setDecodificacionTipoAnotacion(observaciones);

        // Actualizamos el RegistroSir
        registroSirEjb.merge(registroSir);

        // Enviamos el Registro al Componente CIR
        emisionEjb.reenviarFicheroIntercambio(registroSirEjb.getRegistroSirConAnexos(registroSir.getId()));

        // Modificamos el estado del RegistroSir
        registroSirEjb.modificarEstado(registroSir.getId(), EstadoRegistroSir.REENVIADO);
    }

    @Override
    public void reintentarEnvios(Long idEntidad) {

        try {

            List<OficioRemision> oficios = oficioRemisionEjb.getByEstadoEntidad(RegwebConstantes.OFICIO_SIR_ENVIADO, idEntidad);

            log.info("Hay " + oficios.size() + " pendientes de volver a enviar al nodo CIR");

            for (OficioRemision oficio : oficios) {

                if(oficio.getTipoOficioRemision().equals(RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA)){

                    // Transformamos el RegistroEntrada en un RegistroSir
                    RegistroEntrada registroEntrada = registroEntradaEjb.getConAnexosFullCompleto(oficio.getRegistrosEntrada().get(0).getId());
                    RegistroSir registroSir = registroSirEjb.transformarRegistroEntrada(registroEntrada);

                    // Enviamos el Registro al Componente CIR
                    log.info("Reintentando envio a: " + registroSir.getDecodificacionEntidadRegistralDestino());
                    emisionEjb.enviarFicheroIntercambio(registroSir);

                    // Contabilizamos los reintentos
                    oficio.setNumeroReintentos(oficio.getNumeroReintentos() + 1);
                    oficio.setFechaEstado(new Date());

                    oficioRemisionEjb.merge(oficio);


                }else if(oficio.getTipoOficioRemision().equals(RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA)){

                }
            }

        } catch (I18NException e) {
            e.printStackTrace();
        }catch (Exception e){
            log.info("Error al reintenar el envio");
            e.printStackTrace();
        }
    }

    /**
     * Indica si el RegistroSir  se puede reenviar, en función de su estado
     * @param estado del registroSir
     * @return
     */
    public boolean puedeReenviarRegistroSir(EstadoRegistroSir estado){
        return  estado.equals(EstadoRegistroSir.RECIBIDO) ||
                estado.equals(EstadoRegistroSir.DEVUELTO) ||
                estado.equals(EstadoRegistroSir.REENVIADO) ||
                estado.equals(EstadoRegistroSir.REENVIADO_Y_ERROR);

    }

    /**
     *
     * @param registroSir
     * @param oficinaActiva
     * @param usuario
     * @return
     * @throws Exception
     */
    @Override
    public void rechazarRegistroSir(RegistroSir registroSir, Oficina oficinaActiva, Usuario usuario, String observaciones) throws Exception {

        // Modificamos la oficina destino con la de inicio
        registroSir.setCodigoEntidadRegistralDestino(registroSir.getCodigoEntidadRegistralInicio());
        registroSir.setDecodificacionEntidadRegistralDestino(registroSir.getDecodificacionEntidadRegistralInicio());

        // Modificamos la oficina de origen con la oficina activa
        registroSir.setCodigoEntidadRegistralOrigen(oficinaActiva.getCodigo());
        registroSir.setDecodificacionEntidadRegistralOrigen(oficinaActiva.getDenominacion());

        // Modificamos usuario, contacto, aplicacion
        registroSir.setAplicacion(RegwebConstantes.CODIGO_APLICACION);
        registroSir.setNombreUsuario(usuario.getNombreCompleto());
        registroSir.setContactoUsuario(usuario.getEmail());

        registroSir.setTipoAnotacion(TipoAnotacion.RECHAZO.getValue());
        registroSir.setDecodificacionTipoAnotacion(observaciones);

        registroSir = registroSirEjb.merge(registroSir);

        // Rechazamos el RegistroSir
        emisionEjb.rechazarFicheroIntercambio(registroSirEjb.getRegistroSirConAnexos(registroSir.getId()));

        // Modificamos el estado del RegistroSir
        registroSirEjb.modificarEstado(registroSir.getId(), EstadoRegistroSir.RECHAZADO);
    }

    /**
     * Acepta un RegistroSir, creando un Registro de Entrada
     * @param registroSir
     * @throws Exception
     */
    @Override
    public RegistroEntrada aceptarRegistroSir(RegistroSir registroSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs)
            throws Exception {

        log.info("");
        log.info("Aceptando RegistroSir " + registroSir.getIdentificadorIntercambio());

        // Creamos y registramos el RegistroEntrada a partir del RegistroSir aceptado
        RegistroEntrada registroEntrada = null;
        try {
            registroEntrada = registroSirEjb.transformarRegistroSirEntrada(registroSir, usuario, oficinaActiva, idLibro, idIdioma, idTipoAsunto, camposNTIs);

            // CREAMOS LA TRAZABILIDAD
            Trazabilidad trazabilidad = new Trazabilidad(RegwebConstantes.TRAZABILIDAD_RECIBIDO_SIR);
            trazabilidad.setRegistroSir(registroSir);
            trazabilidad.setRegistroEntradaOrigen(null);
            trazabilidad.setOficioRemision(null);
            trazabilidad.setRegistroSalida(null);
            trazabilidad.setRegistroEntradaDestino(registroEntrada);
            trazabilidad.setFecha(new Date());

            trazabilidadEjb.persist(trazabilidad);

            // Modificamos el estado del RegistroSir
            registroSirEjb.modificarEstado(registroSir.getId(), EstadoRegistroSir.ACEPTADO);

            // Enviamos el Mensaje de Confirmación
            mensajeEjb.enviarMensajeConfirmacion(registroSir, registroEntrada.getNumeroRegistroFormateado());

            return registroEntrada;

        } catch (I18NException e) {
            e.printStackTrace();
        } catch (I18NValidationException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Genera el identificador de intercambio a partir del código de la oficina de origen
     *
     * @param codOficinaOrigen
     * @return
     * @throws Exception
     */
    private String generarIdentificadorIntercambio(String codOficinaOrigen) {

        SimpleDateFormat anyo = new SimpleDateFormat("yy"); // Just the year, with 2 digits

        String identificador = codOficinaOrigen + "_" + anyo.format(Calendar.getInstance().getTime()) + "_" + getIdToken(); //todo: Añadir secuencia real

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
