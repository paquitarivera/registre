package es.caib.regweb.webapp.controller.catalogoDatos;

import es.caib.regweb.model.*;
import es.caib.regweb.persistence.ejb.*;
import es.caib.regweb.persistence.utils.Paginacion;
import es.caib.regweb.utils.RegwebConstantes;
import es.caib.regweb.webapp.controller.BaseController;
import es.caib.regweb.webapp.utils.Mensaje;
import es.caib.regweb.webapp.validator.TipoAsuntoValidator;
import es.caib.regweb.webapp.validator.TipoDocumentalValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import java.util.List;
import java.util.Locale;

/**
 * Created 19/03/14
 * Controller que gestiona todas las operaciones del Catalogo de Datos
 * @author earrivi
 */
@Controller
@SessionAttributes(types = {TipoAsunto.class,TipoDocumental.class, CodigoAsunto.class})
public class CatalogoDatosController extends BaseController {

    //protected final Logger log = Logger.getLogger(getClass());

    @Autowired
    private TipoAsuntoValidator tipoAsuntoValidator;

    @Autowired
    private TipoDocumentalValidator tipoDocumentalValidator;
    
    @EJB(mappedName = "regweb/CodigoAsuntoEJB/local")
    public CodigoAsuntoLocal codigoAsuntoEjb;
    
    
    @EJB(mappedName = "regweb/TipoDocumentalEJB/local")
    public TipoDocumentalLocal tipoDocumentalEjb;
    
    @EJB(mappedName = "regweb/TipoAsuntoEJB/local")
    public TipoAsuntoLocal tipoAsuntoEjb;


    /* GESTIÓN DE TIPOS DE ASUNTO */


    /**
    * Listado de todos los TiposAsuntos
    */
     @RequestMapping(value = "/tipoAsunto/list", method = RequestMethod.GET)
     public String listadoTipoAsunto() {
       return "redirect:/tipoAsunto/list/1";
     }

    /**
    * Listado de tipos de asunto
    * @param pageNumber
    * @return
    * @throws Exception
    */
    @RequestMapping(value = "/tipoAsunto/list/{pageNumber}", method = RequestMethod.GET)
     public ModelAndView listTipoAsunto(@PathVariable Integer pageNumber, HttpServletRequest request)throws Exception {

       ModelAndView mav = new ModelAndView("catalogoDatos/tipoAsuntoList");

       Entidad entidad = getEntidadActiva(request);

       List<TipoAsunto> listado = tipoAsuntoEjb.getPagination((pageNumber-1)* BaseEjbJPA.RESULTADOS_PAGINACION, entidad.getId());
       Long total = tipoAsuntoEjb.getTotalEntidad(entidad.getId());

       Paginacion paginacion = new Paginacion(total.intValue(), pageNumber);

       mav.addObject("paginacion", paginacion);
       mav.addObject("listado", listado);

       return mav;
     }

     /**
      * Carga el formulario para un nuevo {@link es.caib.regweb.model.TipoAsunto}
      */
     @RequestMapping(value = "/tipoAsunto/new", method = RequestMethod.GET)
     public String nuevoTipoAsunto(Model model, HttpServletRequest request) throws Exception {

         TipoAsunto tipoAsunto = new TipoAsunto();
         Entidad entidad = getEntidadActiva(request);
         tipoAsunto.setEntidad(entidad);

         for(Long idioma: RegwebConstantes.IDIOMAS_UI){
             tipoAsunto.setTraduccion(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idioma), new TraduccionTipoAsunto());
         }

         CodigoAsunto codigoAsunto = new CodigoAsunto();
         for(Long idioma: RegwebConstantes.IDIOMAS_UI){
             codigoAsunto.setTraduccion(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idioma), new TraduccionCodigoAsunto());
         }


         model.addAttribute(tipoAsunto);
         model.addAttribute(codigoAsunto);

         return "/catalogoDatos/tipoAsuntoForm";
     }

     /**
      * Guardar un nuevo {@link es.caib.regweb.model.TipoAsunto}
      */
     @RequestMapping(value = "/tipoAsunto/new", method = RequestMethod.POST)
     public String nuevoTipoAsunto(@ModelAttribute TipoAsunto tipoAsunto, BindingResult result, SessionStatus status, HttpServletRequest request) {


         tipoAsuntoValidator.validate(tipoAsunto, result);

         if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
             return "catalogoDatos/tipoAsuntoForm";
         }else{ // Si no hay errores guardamos el registro

             try {
                 tipoAsuntoEjb.persist(tipoAsunto);
                 Mensaje.saveMessageInfo(request, getMessage("regweb.guardar.registro"));
                 status.setComplete();
             }catch (Exception e) {
                 Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
                 e.printStackTrace();
             }

             return "redirect:/tipoAsunto/list";
         }
     }

     /**
      * Carga el formulario para modificar un {@link es.caib.regweb.model.TipoAsunto}
      */
     @RequestMapping(value = "/tipoAsunto/{tipoAsuntoId}/edit", method = RequestMethod.GET)
     public String editarTipoAsunto(@PathVariable("tipoAsuntoId") Long tipoAsuntoId, Model model) throws Exception{

         TipoAsunto tipoAsunto = null;
         try {
             tipoAsunto = tipoAsuntoEjb.findById(tipoAsuntoId);
         }catch (Exception e) {
             e.printStackTrace();
         }

         CodigoAsunto codigoAsunto = new CodigoAsunto();
         codigoAsunto.setTipoAsunto(tipoAsunto);

         for(Long idioma: RegwebConstantes.IDIOMAS_UI){
           codigoAsunto.setTraduccion(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idioma), new TraduccionCodigoAsunto());
         }

         model.addAttribute(tipoAsunto);
         model.addAttribute(codigoAsunto);

         return "catalogoDatos/tipoAsuntoForm";
     }


     /**
      * Editar un {@link es.caib.regweb.model.TipoAsunto}
      */
     @RequestMapping(value = "/tipoAsunto/{tipoAsuntoId}/edit", method = RequestMethod.POST)
     public String editarTipoAsunto(@ModelAttribute @Valid TipoAsunto tipoAsunto,
                                    BindingResult result, SessionStatus status, HttpServletRequest request) {

         tipoAsuntoValidator.validate(tipoAsunto, result);

         if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
             return "catalogoDatos/tipoAsuntoForm";
         }else { // Si no hay errores actualizamos el registro
             try {
                 tipoAsuntoEjb.merge(tipoAsunto);

                 Mensaje.saveMessageInfo(request, getMessage("regweb.actualizar.registro"));
                 status.setComplete();

             }catch (Exception e) {
                 e.printStackTrace();
                 Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
             }

             return "redirect:/tipoAsunto/list";
         }
     }

     /**
      * Eliminar un {@link es.caib.regweb.model.TipoAsunto}
      */
     @RequestMapping(value = "/tipoAsunto/{tipoAsuntoId}/delete")
     public String eliminarTipoAsunto(@PathVariable Long tipoAsuntoId, HttpServletRequest request) {

         try {

             TipoAsunto tipoAsunto = tipoAsuntoEjb.findById(tipoAsuntoId);
             tipoAsuntoEjb.remove(tipoAsunto);

             Mensaje.saveMessageInfo(request, getMessage("regweb.eliminar.registro"));

         } catch (Exception e) {
             Mensaje.saveMessageError(request, getMessage("regweb.relaciones.registro"));
             e.printStackTrace();
         }

         return "redirect:/tipoAsunto/list";
     }

    /**
     * Obtiene el nombre traducido de un TipoAsunto.
     */
    @RequestMapping(value = "/obtenerTipoAsunto", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    @ResponseBody
    public String obtenerTipoAsunto(@RequestParam Long id) throws Exception {

        TipoAsunto tipoAsunto = tipoAsuntoEjb.findById(id);

        if(tipoAsunto != null){
            Locale locale = LocaleContextHolder.getLocale();
            TraduccionTipoAsunto traduccionTipoAsunto = (TraduccionTipoAsunto) tipoAsunto.getTraduccion(locale.getLanguage());
            return traduccionTipoAsunto.getNombre();

        }
        return null;
    }

     /* GESTION CODIGO ASUNTO */

    /**
     * Crear un {@link es.caib.regweb.model.TipoAsunto}
     */
    @RequestMapping(value = "/codigoAsunto/new", method = RequestMethod.POST)
    public String nuevoCodigoAsunto(@ModelAttribute CodigoAsunto codigoAsunto,
                                   BindingResult result, SessionStatus status, HttpServletRequest request) {


        if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
            return "catalogoDatos/tipoAsuntoForm";
        }else { // Si no hay errores actualizamos el registro
            try {

                codigoAsuntoEjb.persist(codigoAsunto);

                Mensaje.saveMessageInfo(request, getMessage("regweb.guardar.registro"));
                status.setComplete();

            }catch (Exception e) {
                e.printStackTrace();
                Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
            }

            return "redirect:/tipoAsunto/"+codigoAsunto.getTipoAsunto().getId()+"/edit";
        }
    }

    /**
     * Crear un {@link es.caib.regweb.model.TipoAsunto}
     */
    @RequestMapping(value = "/codigoAsunto/edit", method = RequestMethod.POST)
    public String editarCodigoAsunto(@ModelAttribute CodigoAsunto codigoAsunto,
                                   BindingResult result, SessionStatus status, HttpServletRequest request) {


        if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
            return "catalogoDatos/tipoAsuntoForm";
        }else { // Si no hay errores actualizamos el registro
            try {

                codigoAsuntoEjb.merge(codigoAsunto);

                Mensaje.saveMessageInfo(request, getMessage("regweb.actualizar.registro"));
                status.setComplete();

            }catch (Exception e) {
                e.printStackTrace();
                Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
            }

            return "redirect:/tipoAsunto/"+codigoAsunto.getTipoAsunto().getId()+"/edit";
        }
    }


  /**
     * Eliminar un {@link es.caib.regweb.model.TipoAsunto}
     */
    @RequestMapping(value = "/codigoAsunto/{codigoAsuntoId}/delete")
    public String eliminarCodigoAsunto(@PathVariable Long codigoAsuntoId, HttpServletRequest request) {

        try {

            CodigoAsunto codigoAsunto = codigoAsuntoEjb.findById(codigoAsuntoId);
            TipoAsunto tipoAsunto = tipoAsuntoEjb.findById(codigoAsunto.getTipoAsunto().getId());
            List<CodigoAsunto> codigosAsuntos = tipoAsunto.getCodigosAsunto();


            if(codigosAsuntos.contains(codigoAsunto)){
              codigosAsuntos.remove(codigoAsunto);
              tipoAsunto.setCodigosAsunto(codigosAsuntos);

                  codigoAsunto.setTipoAsunto(null);
                  CodigoAsunto codigoAsuntoGuardado = codigoAsuntoEjb.merge(codigoAsunto);

                  codigoAsuntoEjb.remove(codigoAsuntoGuardado);
                  tipoAsuntoEjb.merge(tipoAsunto);
            }


            Mensaje.saveMessageInfo(request, getMessage("regweb.eliminar.registro"));

        } catch (Exception e) {
            Mensaje.saveMessageError(request, getMessage("regweb.relaciones.registro"));
            e.printStackTrace();
        }

        return "redirect:/tipoAsunto/list";
    }

    /**
     * Obtiene el nombre traducido de un CodigoAsunto.
     */
    @RequestMapping(value = "/obtenerCodigoAsunto", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
    @ResponseBody
    public String obtenerCodigoAsunto(@RequestParam Long id) throws Exception {

        CodigoAsunto codigoAsunto = codigoAsuntoEjb.findById(id);

        if(codigoAsunto != null){
            Locale locale = LocaleContextHolder.getLocale();
            TraduccionCodigoAsunto traduccionCodigoAsunto = (TraduccionCodigoAsunto) codigoAsunto.getTraduccion(locale.getLanguage());
            return traduccionCodigoAsunto.getNombre();

        }
        return null;
    }

    /* GESTIÓN DE TIPOS DOCUMENTAL */

    /**
     * Listado de todos los TipoDocumental
     */
    @RequestMapping(value = "/tipoDocumental/list", method = RequestMethod.GET)
    public String listadoTipoDocumental() {
        return "redirect:/tipoDocumental/list/1";
    }

    /**
     * Listado de tipos documentales
     * @param pageNumber
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tipoDocumental/list/{pageNumber}", method = RequestMethod.GET)
    public ModelAndView listTipoDocumental(@PathVariable Integer pageNumber, HttpServletRequest request)throws Exception {

        ModelAndView mav = new ModelAndView("catalogoDatos/tipoDocumentalList");

        Entidad entidad = getEntidadActiva(request);

        List<TipoDocumental> listado = tipoDocumentalEjb.getPagination((pageNumber-1)* BaseEjbJPA.RESULTADOS_PAGINACION, entidad.getId());
        Long total = tipoDocumentalEjb.getTotal(entidad.getId());

        Paginacion paginacion = new Paginacion(total.intValue(), pageNumber);

        mav.addObject("paginacion", paginacion);
        mav.addObject("listado", listado);

        return mav;
    }

    /**
     * Carga el formulario para un nuevo {@link es.caib.regweb.model.TipoDocumental}
     */
    @RequestMapping(value = "/tipoDocumental/new", method = RequestMethod.GET)
    public String nuevoTipoDocumental(Model model, HttpServletRequest request) throws Exception {

        TipoDocumental tipoDocumental = new TipoDocumental();
        Entidad entidad = getEntidadActiva(request);
        tipoDocumental.setEntidad(entidad);

        for(Long idioma: RegwebConstantes.IDIOMAS_UI){
            tipoDocumental.setTraduccion(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idioma), new TraduccionTipoDocumental());
        }
        model.addAttribute(tipoDocumental);

        return "/catalogoDatos/tipoDocumentalForm";
    }

    /**
     * Guardar un nuevo {@link es.caib.regweb.model.TipoDocumental}
     */
    @RequestMapping(value = "/tipoDocumental/new", method = RequestMethod.POST)
    public String nuevoTipoDocumental(@ModelAttribute TipoDocumental tipoDocumental, BindingResult result, SessionStatus status, HttpServletRequest request) {

        tipoDocumentalValidator.validate(tipoDocumental, result);

        if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
            return "catalogoDatos/tipoDocumentalForm";
        }else{ // Si no hay errores guardamos el registro

            try {
                tipoDocumentalEjb.persist(tipoDocumental);
                Mensaje.saveMessageInfo(request, getMessage("regweb.guardar.registro"));
                status.setComplete();
            }catch (Exception e) {
                Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
                e.printStackTrace();
            }

            return "redirect:/tipoDocumental/list";
        }
    }

    /**
     * Carga el formulario para modificar un {@link es.caib.regweb.model.TipoDocumental}
     */
    @RequestMapping(value = "/tipoDocumental/{tipoDocumentalId}/edit", method = RequestMethod.GET)
    public String editarTipoDocumental(@PathVariable("tipoDocumentalId") Long tipoDocumentalId, Model model) {

        TipoDocumental tipoDocumental = null;
        try {
            tipoDocumental = tipoDocumentalEjb.findById(tipoDocumentalId);
        }catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute(tipoDocumental);
        return "catalogoDatos/tipoDocumentalForm";
    }

    /**
     * Editar un {@link es.caib.regweb.model.TipoDocumental}
     */
    @RequestMapping(value = "/tipoDocumental/{tipoDocumentalId}/edit", method = RequestMethod.POST)
    public String editarTipoDocumental(@ModelAttribute @Valid TipoDocumental tipoDocumental,BindingResult result,
                                   SessionStatus status, HttpServletRequest request) {

        tipoDocumentalValidator.validate(tipoDocumental, result);

        if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
            return "catalogoDatos/tipoDocumentalForm";
        }else { // Si no hay errores actualizamos el registro

            try {
                tipoDocumentalEjb.merge(tipoDocumental);
                Mensaje.saveMessageInfo(request, getMessage("regweb.actualizar.registro"));
                status.setComplete();

            }catch (Exception e) {
                e.printStackTrace();
                Mensaje.saveMessageError(request, getMessage("regweb.error.registro"));
            }

            return "redirect:/tipoDocumental/list";
        }
    }

    /**
     * Eliminar un {@link es.caib.regweb.model.TipoDocumental}
     */
    @RequestMapping(value = "/tipoDocumental/{tipoDocumentalId}/delete")
    public String eliminarTipoDocumental(@PathVariable Long tipoDocumentalId, HttpServletRequest request) {

        try {

            TipoDocumental tipoDocumental = tipoDocumentalEjb.findById(tipoDocumentalId);
            tipoDocumentalEjb.remove(tipoDocumental);

            Mensaje.saveMessageInfo(request, getMessage("regweb.eliminar.registro"));

        } catch (Exception e) {
            Mensaje.saveMessageError(request, getMessage("regweb.relaciones.registro"));
            e.printStackTrace();
        }

        return "redirect:/tipoDocumental/list";
    }



     @ModelAttribute("idiomas")
     public Long[] idiomas() throws Exception {
         return RegwebConstantes.IDIOMAS_UI;
     }


     @InitBinder("tipoAsunto")
     public void initBinderTipoAsunto(WebDataBinder binder) {
         binder.setDisallowedFields("id");
         binder.setValidator(this.tipoAsuntoValidator);
     }

    @InitBinder("tipoDocumental")
    public void initBinderTipoDocumental(WebDataBinder binder) {
        binder.setDisallowedFields("id");
        binder.setValidator(this.tipoDocumentalValidator);
    }
}
