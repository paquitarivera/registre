package es.caib.regweb3.ws.v3.impl;

import es.caib.regweb3.model.*;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.persistence.ejb.*;
import es.caib.regweb3.persistence.validator.RegistroSalidaBeanValidator;
import es.caib.regweb3.persistence.validator.RegistroSalidaValidator;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.StringUtils;
import es.caib.regweb3.ws.converter.RegistroSalidaConverter;
import es.caib.regweb3.ws.model.IdentificadorWs;
import es.caib.regweb3.ws.model.RegistroSalidaResponseWs;
import es.caib.regweb3.ws.model.RegistroSalidaWs;
import es.caib.regweb3.ws.utils.UsuarioAplicacionCache;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.genapp.common.ws.WsI18NException;
import org.fundaciobit.genapp.common.ws.WsValidationException;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.wsf.spi.annotation.TransportGuarantee;
import org.jboss.wsf.spi.annotation.WebContext;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * @author anadal
 */
@SecurityDomain(RegwebConstantes.SECURITY_DOMAIN)
@Stateless(name = RegWebRegistroSalidaWsImpl.NAME + "Ejb")
@RolesAllowed({RegwebConstantes.ROL_SUPERADMIN})
@SOAPBinding(style = SOAPBinding.Style.RPC)
@org.apache.cxf.interceptor.InInterceptors(interceptors = {"es.caib.regweb3.ws.utils.RegWebInInterceptor"})
@org.apache.cxf.interceptor.InFaultInterceptors(interceptors = {"es.caib.regweb3.ws.utils.RegWebInInterceptor"})
@WebService(name = RegWebRegistroSalidaWsImpl.NAME_WS, portName = RegWebRegistroSalidaWsImpl.NAME_WS,
        serviceName = RegWebRegistroSalidaWsImpl.NAME_WS + "Service",
        endpointInterface = "es.caib.regweb3.ws.v3.impl.RegWebRegistroSalidaWs")
@WebContext(contextRoot = "/regweb3/ws", urlPattern = "/v3/" + RegWebRegistroSalidaWsImpl.NAME, transportGuarantee = TransportGuarantee.NONE, secureWSDLAccess = false, authMethod = "WSBASIC")
public class RegWebRegistroSalidaWsImpl extends AbstractRegistroWsImpl implements RegWebRegistroSalidaWs {

    protected final Logger log = Logger.getLogger(getClass());

    public static final String NAME = "RegWebRegistroSalida";

    public static final String NAME_WS = NAME + "Ws";


    RegistroSalidaValidator<RegistroSalida> validator = new RegistroSalidaValidator<RegistroSalida>();


    @EJB(mappedName = "regweb3/OficinaEJB/local")
    public OficinaLocal oficinaEjb;

    @EJB(mappedName = "regweb3/OrganismoEJB/local")
    public OrganismoLocal organismoEjb;

    @EJB(mappedName = "regweb3/PermisoLibroUsuarioEJB/local")
    public PermisoLibroUsuarioLocal permisoLibroUsuarioEjb;

    @EJB(mappedName = "regweb3/UsuarioEntidadEJB/local")
    public UsuarioEntidadLocal usuarioEntidadEjb;

    @EJB(mappedName = "regweb3/LibroEJB/local")
    public LibroLocal libroEjb;

    @EJB(mappedName = "regweb3/TipoAsuntoEJB/local")
    public TipoAsuntoLocal tipoAsuntoEjb;

    @EJB(mappedName = "regweb3/CodigoAsuntoEJB/local")
    public CodigoAsuntoLocal codigoAsuntoEjb;

    @EJB(mappedName = "regweb3/RegistroSalidaEJB/local")
    public RegistroSalidaLocal registroSalidaEjb;

    @EJB(mappedName = "regweb3/LopdEJB/local")
    public LopdLocal lopdEjb;

    @EJB(mappedName = "regweb3/InteresadoEJB/local")
    public InteresadoLocal interesadoEjb;

    @EJB(mappedName = "regweb3/PersonaEJB/local")
    public PersonaLocal personaEjb;

    @EJB(mappedName = "regweb3/CatPaisEJB/local")
    public CatPaisLocal catPaisEjb;

    @EJB(mappedName = "regweb3/CatProvinciaEJB/local")
    public CatProvinciaLocal catProvinciaEjb;

    @EJB(mappedName = "regweb3/CatLocalidadEJB/local")
    public CatLocalidadLocal catLocalidadEjb;


    @Override
    @RolesAllowed({ROL_USUARI})
    @WebMethod
    public IdentificadorWs altaRegistroSalida(@WebParam(name = "registroSalidaWs") RegistroSalidaWs registroSalidaWs) throws Throwable, WsI18NException, WsValidationException {

        IdentificadorWs identificadorWs = null;
        Entidad entidad = null;

        // Obtenemos la Entidad a la que se realiza el RegistroEntrada
        if(UsuarioAplicacionCache.get().getEntidades().size() > 1){
            log.info("Usuario asociado a varias Entidades");

            Libro libro = libroEjb.findByCodigo(registroSalidaWs.getLibro());
            // todo: Podría darse el hipotético caso que un mismo código de Libro esté presente en dos Entidades
            if(libro != null){
                entidad = libro.getOrganismo().getEntidad();
            }
        }else{
            entidad = UsuarioAplicacionCache.get().getEntidades().get(0);
        }

        // 1.- Comprobar que el Organismo destino está vigente
        Organismo origen = organismoEjb.findByCodigoEntidad(registroSalidaWs.getOrigen(), entidad.getId());

        if (origen == null) { //Si no existe todo: Hay que agregar origenes externos?
            throw new I18NException("registro.organismo.noExiste", registroSalidaWs.getOrigen());

        } else if (!origen.getEstado().getCodigoEstadoEntidad().equals(ESTADO_ENTIDAD_VIGENTE)) { //Si está extinguido
            throw new I18NException("registro.organismo.extinguido", origen.getNombreCompleto());
        }

        // 2.- Comprobar que la Oficina está vigente
        Oficina oficina = oficinaEjb.findByCodigoEntidad(registroSalidaWs.getOficina(), entidad.getId());

        if (oficina == null) { //No existe
            throw new I18NException("registro.oficina.noExiste", registroSalidaWs.getOrigen());

        } else if (!oficina.getEstado().getCodigoEstadoEntidad().equals(ESTADO_ENTIDAD_VIGENTE)) { //Si está extinguido
            throw new I18NException("registro.oficina.extinguido", oficina.getNombreCompleto());
        }

        // 3.- Comprobar que el Libro está vigente
        Libro libro = libroEjb.findByCodigoEntidad(registroSalidaWs.getLibro(), entidad.getId());

        if (libro == null) { //No existe
            throw new I18NException("registro.libro.noExiste", registroSalidaWs.getLibro());

        } else if (!libro.getActivo()) { //Si está extinguido
            throw new I18NException("registro.libro.inactivo", registroSalidaWs.getLibro());
        }

        // 4.- Comprobar que el usuario tiene permisos para realizar el registro de entrada
        UsuarioEntidad usuario = usuarioEntidadEjb.findByIdentificadorEntidad(registroSalidaWs.getCodigoUsuario(), entidad.getId());

        if (usuario == null) {//No existe
            throw new I18NException("registro.usuario.noExiste", registroSalidaWs.getCodigoUsuario(), entidad.getNombre());

        } else if (!permisoLibroUsuarioEjb.tienePermiso(usuario.getId(), libro.getId(), PERMISO_REGISTRO_SALIDA)) {
            throw new I18NException("registro.usuario.permisos", registroSalidaWs.getCodigoUsuario(), libro.getCodigo());
        }

        // Recuperamos el username correcto
        registroSalidaWs.setCodigoUsuario(usuario.getUsuario().getIdentificador());

        // 5.- Convertir RegistroEntradaWs a RegistroEntrada
        RegistroSalida registroSalida = RegistroSalidaConverter.getRegistroSalida(
                registroSalidaWs, usuario, libro, oficina, origen,
                codigoAsuntoEjb, tipoAsuntoEjb, oficinaEjb);

        // 6.- Validar el RegistroEntrada
        validateRegistroSalida(registroSalida);

        // 7.- Validar los Interesados
        List<Interesado> interesados = null;
        if (registroSalidaWs.getInteresados() != null && registroSalidaWs.getInteresados().size() > 0) {

            // Procesamos los interesados
            interesados = procesarInteresados(registroSalidaWs.getInteresados(), interesadoEjb, catPaisEjb, catProvinciaEjb, catLocalidadEjb, personaEjb);

            registroSalida.getRegistroDetalle().setInteresados(null);

        } else {
            throw new I18NException("interesado.registro.obligatorio");
        }


        // 8.- Validar los Anexos
        List<AnexoFull> anexosFull = null;
        if (registroSalidaWs.getAnexos() != null && registroSalidaWs.getAnexos().size() > 0) {

            //Procesamos los anexos
            anexosFull = procesarAnexos(registroSalidaWs.getAnexos(), entidad.getId());

            registroSalida.getRegistroDetalle().setAnexos(null);
        }

        // 7.- Creamos el Registro de Salida
        registroSalida = registroSalidaEjb.registrarSalida(registroSalida, usuario, interesados, anexosFull);

        if (registroSalida.getId() != null) {
            // Componemos la respuesta
            identificadorWs = new IdentificadorWs(registroSalida.getNumeroRegistroFormateado(), registroSalida.getNumeroRegistro(), registroSalida.getFecha());
        }

        return identificadorWs;
    }

    @RolesAllowed({ROL_USUARI})
    @Override
    @WebMethod
    public void anularRegistroSalida(@WebParam(name = "numeroRegistro") String numeroRegistro,
                                     @WebParam(name = "usuario") String usuario,
                                     @WebParam(name = "entidad") String entidad,
                                     @WebParam(name = "anular") boolean anular)
            throws Throwable, WsI18NException, WsValidationException {

        // 1.- Validaciones comunes
        validarObligatorios(numeroRegistro, usuario, entidad);

        // 2.- Comprobar que el usuario existe en la Entidad proporcionada
        UsuarioEntidad usuarioEntidad = usuarioEntidadEjb.findByIdentificadorCodigoEntidad(usuario, entidad);

        if (usuarioEntidad == null) {//No existe
            throw new I18NException("registroEntrada.usuario.noExiste", usuario, entidad);
        }

        // 3.- Obtenemos el RegistroSalida
        RegistroSalida registroSalida = registroSalidaEjb.findByNumeroRegistroFormateado(numeroRegistro);

        if (registroSalida == null) {
            throw new I18NException("registroEntrada.noExiste", numeroRegistro);
        }

        // 4.- Comprobamos si el RegistroSalida se puede anular según su estado.
        final List<Long> estados = new ArrayList<Long>();
        estados.add(RegwebConstantes.REGISTRO_RESERVA);
        estados.add(RegwebConstantes.REGISTRO_VALIDO);
        estados.add(RegwebConstantes.REGISTRO_PENDIENTE_VISAR);

        if (!estados.contains(registroSalida.getEstado())) {
            throw new I18NException("registroEntrada.anulado");
        }

        // 5.- Comprobamos que el usuario tiene permisos de modificación para el RegistroSalida
        if (!permisoLibroUsuarioEjb.tienePermiso(usuarioEntidad.getId(), registroSalida.getLibro().getId(), PERMISO_MODIFICACION_REGISTRO_SALIDA)) {
            throw new I18NException("registroEntrada.usuario.permisos", usuario);
        }

        // 6.- Anulamos el RegistroSalida
        // TODO Falta Afegir paràmetre
        registroSalidaEjb.anularRegistroSalida(registroSalida, usuarioEntidad);

    }

    @RolesAllowed({ROL_USUARI})
    @Override
    @WebMethod
    public RegistroSalidaResponseWs obtenerRegistroSalida(
            @WebParam(name = "numeroRegistro") String numeroRegistro,
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "entidad") String entidad) throws Throwable, WsI18NException, WsValidationException {

        // 1.- Validaciones comunes
        validarObligatorios(numeroRegistro, usuario, entidad);

        // 2.- Comprobar que el usuario existe en la Entidad proporcionada
        UsuarioEntidad usuarioEntidad = usuarioEntidadEjb.findByIdentificadorCodigoEntidad(usuario, entidad);

        if (usuarioEntidad == null) {//No existe
            throw new I18NException("registroEntrada.usuario.noExiste", usuario, entidad);
        }

        // 3.- Obtenemos el RegistroSalida
        RegistroSalida registro = registroSalidaEjb.findByNumeroRegistroFormateado(numeroRegistro);

        if (registro == null) {
            throw new I18NException("registroEntrada.noExiste", numeroRegistro);
        }

        // 4.- Comprobamos que el usuario tiene permisos de lectura para el RegistroSalida
        if (!permisoLibroUsuarioEjb.tienePermiso(usuarioEntidad.getId(), registro.getLibro().getId(), PERMISO_CONSULTA_REGISTRO_SALIDA)) {
            throw new I18NException("registroEntrada.usuario.permisos", usuario);
        }

        // LOPD
        lopdEjb.insertarRegistroSalida(registro.getNumeroRegistro(), registro.getFecha(), registro.getLibro().getId(), usuarioEntidad.getId());

        // Retornamos el RegistroSalidaWs
        return RegistroSalidaConverter.getRegistroSalidaResponseWs(registro,
                UsuarioAplicacionCache.get().getIdioma(), anexoEjb);


    }


    /**
     * @param anyo
     * @param numeroRegistro
     * @param libro
     * @param usuario
     * @param entidad
     * @return
     * @throws Throwable
     * @throws WsI18NException
     */
    @RolesAllowed({ROL_USUARI})
    @Override
    @WebMethod
    public IdentificadorWs obtenerRegistroSalidaID(
            @WebParam(name = "anyo") int anyo,
            @WebParam(name = "numeroRegistro") int numeroRegistro,
            @WebParam(name = "libro") String libro,
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "entidad") String entidad)
            throws Throwable, WsI18NException {


        // 1.- Validaciones comunes
        if (anyo <= 0) {
            throw new I18NException("error.valor.requerido.ws", "anyo");
        }

        if (numeroRegistro <= 0) {
            throw new I18NException("error.valor.requerido.ws", "numeroRegistro");
        }

        if (StringUtils.isEmpty(libro)) {
            throw new I18NException("error.valor.requerido.ws", "libro");
        }

        if (StringUtils.isEmpty(usuario)) {
            throw new I18NException("error.valor.requerido.ws", "usuario");
        }

        if (StringUtils.isEmpty(entidad)) {
            throw new I18NException("error.valor.requerido.ws", "entidad");
        }


        // 2.- Comprobar que el usuario existe en la Entidad proporcionada
        UsuarioEntidad usuarioEntidad = usuarioEntidadEjb.findByIdentificadorCodigoEntidad(usuario, entidad);

        if (usuarioEntidad == null) {//No existe
            throw new I18NException("registroEntrada.usuario.noExiste", usuario, entidad);
        }

        // 3.- Existe libro
        Libro libroObj = libroEjb.findByCodigoEntidad(libro, usuarioEntidad.getEntidad().getId());
        if (libroObj == null) {
            throw new I18NException("registro.libro.noExiste", libro);
        }

        // 4.- Comprobamos que el usuario tiene permisos de lectura para el RegistroEntrada
        if (!permisoLibroUsuarioEjb.tienePermiso(usuarioEntidad.getId(), libroObj.getId(), PERMISO_CONSULTA_REGISTRO_ENTRADA)) {
            throw new I18NException("registroSalida.usuario.permisos", usuario);
        }

        // 3.- Obtenemos el registro
        RegistroSalida registro;
        registro = registroSalidaEjb.findByNumeroAnyoLibro(numeroRegistro, anyo, libro);
        if (registro == null) {
            throw new I18NException("registroSalida.noExiste", numeroRegistro
                    + "/" + anyo + " (" + libro + ")");
        }

        // LOPD
        lopdEjb.insertarRegistroSalida(registro.getNumeroRegistro(), registro.getFecha(), registro.getLibro().getId(), usuarioEntidad.getId());

        return new IdentificadorWs(registro.getNumeroRegistroFormateado(), numeroRegistro, registro.getFecha());
    }


    /**
     * @param registroSalida
     * @throws org.fundaciobit.genapp.common.i18n.I18NValidationException
     */
    private void validateRegistroSalida(RegistroSalida registroSalida) throws I18NValidationException, I18NException {

        RegistroSalidaBeanValidator rsbv = new RegistroSalidaBeanValidator(validator);

        rsbv.throwValidationExceptionIfErrors(registroSalida, true);
    }

}
