package es.caib.regweb3.webapp.controller.notificacion;

import es.caib.regweb3.model.Entidad;
import es.caib.regweb3.model.Notificacion;
import es.caib.regweb3.model.Organismo;
import es.caib.regweb3.model.UsuarioEntidad;
import es.caib.regweb3.persistence.ejb.NotificacionLocal;
import es.caib.regweb3.persistence.ejb.PermisoLibroUsuarioLocal;
import es.caib.regweb3.persistence.utils.Paginacion;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.webapp.controller.BaseController;
import es.caib.regweb3.webapp.form.NotificacionForm;
import es.caib.regweb3.webapp.utils.JsonResponse;
import es.caib.regweb3.webapp.utils.Mensaje;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Fundació Bit
 * Date: 17/04/18
 * Controller que gestiona todas las operaciones con {@link es.caib.regweb3.model.Notificacion}
 *
 * @author earivi
 */
@Controller
@RequestMapping(value = "/notificacion")
public class NotificacionController extends BaseController {

    @EJB(mappedName = "regweb3/NotificacionEJB/local")
    private NotificacionLocal notificacionEjb;

    @EJB(mappedName = "regweb3/PermisoLibroUsuarioEJB/local")
    private PermisoLibroUsuarioLocal permisoLibroUsuarioEjb;

    /**
     * Listado de todas las {@link Notificacion}
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listadoNotificaciones() {
        return "redirect:/notificacion/list/1";
    }


    /**
     * Listado de todas las {@link Notificacion}
     */
    @RequestMapping(value = "/list/{pageNumber}", method = RequestMethod.GET)
    public String todas(@PathVariable Integer pageNumber, Model model, HttpServletRequest request) throws Exception{

        Notificacion busqueda = new Notificacion();
        busqueda.setPageNumber(pageNumber);

        UsuarioEntidad usuario = getUsuarioEntidadActivo(request);

        Paginacion paginacion = notificacionEjb.busqueda(busqueda, usuario.getId());

        model.addAttribute("paginacion", paginacion);
        model.addAttribute("integracionBusqueda", busqueda);

        model.addAttribute("nuevas", notificacionEjb.getByEstadoCount(usuario.getId(), RegwebConstantes.NOTIFICACION_ESTADO_NUEVA));
        model.addAttribute("leidas", notificacionEjb.getByEstadoCount(usuario.getId(), RegwebConstantes.NOTIFICACION_ESTADO_LEIDA));
        model.addAttribute("todas", notificacionEjb.getByEstadoCount(usuario.getId(), null));

        return "notificacion/notificacionList";
    }

    /**
     * Listado de todas las {@link Notificacion}
     */
    @RequestMapping(value = "/{estado}/list/{pageNumber}", method = RequestMethod.GET)
    public String listadoEstado(@PathVariable("estado") Long estado,@PathVariable Integer pageNumber, Model model, HttpServletRequest request) throws Exception{

        Notificacion busqueda = new Notificacion();
        busqueda.setEstado(estado);
        busqueda.setPageNumber(pageNumber);

        UsuarioEntidad usuario = getUsuarioEntidadActivo(request);

        Paginacion paginacion = notificacionEjb.busqueda(busqueda, usuario.getId());

        model.addAttribute("paginacion", paginacion);
        model.addAttribute("integracionBusqueda", busqueda);
        model.addAttribute("estado", estado);

        model.addAttribute("nuevas", notificacionEjb.getByEstadoCount(usuario.getId(), RegwebConstantes.NOTIFICACION_ESTADO_NUEVA));
        model.addAttribute("leidas", notificacionEjb.getByEstadoCount(usuario.getId(), RegwebConstantes.NOTIFICACION_ESTADO_LEIDA));
        model.addAttribute("todas", notificacionEjb.getByEstadoCount(usuario.getId(), null));

        return "notificacion/notificacionList";
    }

    /**
     * Listado de todas las {@link Notificacion}
     */
    @RequestMapping(value = "/{idNotificacion}/eliminar", method = RequestMethod.GET)
    public String eliminarNotificacion(@PathVariable("idNotificacion") Long idNotificacion, Model model, HttpServletRequest request) throws Exception{

        UsuarioEntidad usuario = getUsuarioEntidadActivo(request);
        Notificacion notificacion = notificacionEjb.findById(idNotificacion);

        if(usuario.equals(notificacion.getDestinatario())){

            notificacionEjb.remove(notificacion);
            Mensaje.saveMessageInfo(request, getMessage("notificacion.eliminada"));

        }else{
            Mensaje.saveMessageError(request, getMessage("notificacion.error.permisos"));
        }

        return "redirect:/notificacion/list";
    }

    /**
     * Marca como leída una {@link Notificacion}
     */
    @RequestMapping(value = "/{idNotificacion}/leer", method = RequestMethod.GET)
    public String nueva(@PathVariable("idNotificacion") Long idNotificacion, HttpServletRequest request) throws Exception{

        UsuarioEntidad usuario = getUsuarioEntidadActivo(request);
        Notificacion notificacion = notificacionEjb.findById(idNotificacion);

        if(usuario.equals(notificacion.getDestinatario()) && notificacion.getEstado().equals(RegwebConstantes.NOTIFICACION_ESTADO_NUEVA)){

            notificacionEjb.leerNotificacion(idNotificacion);
            Mensaje.saveMessageInfo(request, getMessage("notificacion.leida"));

        }else{
            Mensaje.saveMessageError(request, getMessage("notificacion.error.permisos"));
        }

        return "redirect:/notificacion/list";
    }

    /**
     * Crea una nueva {@link Notificacion}
     * @param notificacion
     * @return
     */
    @RequestMapping(value="/nueva", method= RequestMethod.POST)
    @ResponseBody
    public JsonResponse nueva(@RequestBody NotificacionForm notificacion, HttpServletRequest request) {

        JsonResponse jsonResponse = new JsonResponse();

        try {

            UsuarioEntidad usuarioActivo = getUsuarioEntidadActivo(request);

            List<UsuarioEntidad> usuarios = new ArrayList<UsuarioEntidad>();

            if(notificacion.getDestinatarios() != null){
                for (Long destinatario : notificacion.getDestinatarios()) {

                    if(destinatario.equals((long) -1)){ // Se ha seleccionado todos los usuarios
                        usuarios.addAll(permisoLibroUsuarioEjb.getUsuariosRegistroEntidad(usuarioActivo.getEntidad().getId()));
                    }else{
                        usuarios.addAll(permisoLibroUsuarioEjb.getUsuariosRegistroOrganismo(notificacion.getDestinatarios()));
                    }
                }
            }

            log.info("Total notificaciones: " +usuarios.size());

            for (UsuarioEntidad usuario : usuarios) {
                Notificacion nueva = new Notificacion(notificacion.getTipo());
                nueva.setRemitente(usuarioActivo);
                nueva.setAsunto(notificacion.getAsunto());
                nueva.setMensaje(notificacion.getMensaje());
                nueva.setDestinatario(usuario);

                notificacionEjb.persist(nueva);
            }


        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.setStatus("FAIL");
        }

        jsonResponse.setStatus("SUCCESS");

        return jsonResponse;
    }



    @ModelAttribute("destinatarios")
    public List<Organismo> destinatarios(HttpServletRequest request) throws Exception {
        Entidad entidad = getEntidadActiva(request);
        return organismoEjb.findByEntidadLibros(entidad.getId());
    }

    @ModelAttribute("tipos")
    public Long[] tipos() throws Exception {
        return RegwebConstantes.NOTIFICACION_TIPOS;
    }

    /**
     *
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String nueva(HttpServletRequest request) throws Exception{

        Notificacion notificacion = new Notificacion(RegwebConstantes.NOTIFICACION_TIPO_COMUNICADO);

        //notificacion.setRemitente(getUsuarioEntidadActivo(request));
        notificacion.setDestinatario(getUsuarioEntidadActivo(request));
        notificacion.setAsunto("Prueba de Notificación");
        notificacion.setMensaje("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore");
        notificacion.setFechaEnviado(new Date());

        notificacionEjb.persist(notificacion);

        return "redirect:/notificacion/list/0";
    }
}