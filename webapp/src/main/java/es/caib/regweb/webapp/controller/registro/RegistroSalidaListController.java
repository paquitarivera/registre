package es.caib.regweb.webapp.controller.registro;

import es.caib.regweb.model.*;
import es.caib.regweb.persistence.ejb.HistoricoRegistroSalidaLocal;
import es.caib.regweb.persistence.ejb.RegistroSalidaLocal;
import es.caib.regweb.persistence.utils.Paginacion;
import es.caib.regweb.persistence.utils.RegistroUtils;
import es.caib.regweb.utils.RegwebConstantes;
import es.caib.regweb.webapp.form.RegistroSalidaBusqueda;
import es.caib.regweb.webapp.validator.RegistroSalidaBusquedaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Fundació BIT.
 * Controller para gestionar los Registros de Salida
 * @author earrivi
 * @author anadal
 * Date: 31/03/14
 */
@Controller
@RequestMapping(value = "/registroSalida")
@SessionAttributes(types = RegistroSalida.class)
public class RegistroSalidaListController extends AbstractRegistroCommonListController {


    @Autowired
    private RegistroSalidaBusquedaValidator registroSalidaBusquedaValidator;

    @EJB(mappedName = "regweb/RegistroSalidaEJB/local")
    public RegistroSalidaLocal registroSalidaEjb;

    @EJB(mappedName = "regweb/HistoricoRegistroSalidaEJB/local")
    public HistoricoRegistroSalidaLocal historicoRegistroSalidaEjb;



    /**
    * Listado de todos los Registros de Salida
    */
   @RequestMapping(value = "", method = RequestMethod.GET)
    public String listado() {
       return "redirect:/registroSalida/list";
    }

  
   
   
   private Set<Organismo> getOrganismosInternosMasExternos(HttpServletRequest request) throws Exception {
	   
	   Set<Organismo> allOrganismos = getOrganismosOficinaActiva(request);
	   /*List<RegistroEntrada> regsEntrada = registroEntradaEjb.getAll();
	   
	   for(int r=0; r<regsEntrada.size(); r++) {
		   if (regsEntrada.get(r).getDestinoExternoCodigo()!=null && !"".equals(regsEntrada.get(r).getDestinoExternoCodigo()) && !"null".equalsIgnoreCase(regsEntrada.get(r).getDestinoExternoCodigo())) {
			   Organismo org = new Organismo();
			   org.setCodigo(regsEntrada.get(r).getDestinoExternoCodigo());
			   org.setDenominacion(regsEntrada.get(r).getDestinoExternoDenominacion());
			   allOrganismos.add(org);
		   }
	   }*/
	   
	   return allOrganismos;
   }   
   
    /**
     * Listado de registros de salida
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model, HttpServletRequest request)throws Exception {

        // Obtenemos los Libros donde el usuario tiene permisos de Consulta
        List<Libro> librosConsulta = getLibrosConsultaSalidas(request);

        RegistroSalidaBusqueda registroSalidaBusqueda = new RegistroSalidaBusqueda(new RegistroSalida(),1);
        registroSalidaBusqueda.setFechaFin(new Date());

        Oficina oficina = getOficinaActiva(request);
        model.addAttribute(oficina);
        model.addAttribute("librosConsulta", librosConsulta);
        model.addAttribute("registroSalidaBusqueda", registroSalidaBusqueda);
        model.addAttribute("organosOrigen",  getOrganismosInternosMasExternos(request));
        model.addAttribute("oficinasRegistro",  oficinaEjb.findByEntidadByEstado(getEntidadActiva(request).getId(),RegwebConstantes.ESTADO_ENTIDAD_VIGENTE));

        return "registroSalida/registroSalidaList";

    }

    /**
     * Realiza la busqueda de {@link es.caib.regweb.model.RegistroSalida} según los parametros del formulario
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public ModelAndView list(@ModelAttribute RegistroSalidaBusqueda busqueda, BindingResult result,HttpServletRequest request)throws Exception {

        ModelAndView mav = new ModelAndView("registroSalida/registroSalidaList", result.getModel());
        RegistroSalida registroSalida = busqueda.getRegistroSalida();

        UsuarioEntidad usuarioEntidad = usuarioEntidadEjb.findByUsuarioEntidad(getUsuarioAutenticado(request).getId(), getEntidadActiva(request).getId());
        List<Libro> librosConsulta = permisoLibroUsuarioEjb.getLibrosPermiso(usuarioEntidad.getId(), RegwebConstantes.PERMISO_CONSULTA_REGISTRO_SALIDA);

        registroSalidaBusquedaValidator.validate(busqueda,result);

        Oficina oficina = getOficinaActiva(request);
        mav.addObject(oficina);
        
        Set<Organismo> todosOrganismos = getOrganismosInternosMasExternos(request);
        
        if (busqueda.getOrganOrigen()!=null && !"".equals(busqueda.getOrganOrigen())) {
		    Organismo org = new Organismo();
		    org.setCodigo(busqueda.getOrganOrigen());
		    org.setDenominacion(busqueda.getOrganOrigenNom());
		    todosOrganismos.add(org);
        }
	    
        mav.addObject("organosOrigen",  todosOrganismos);        
        
        if (result.hasErrors()) { // Si hay errores volvemos a la vista del formulario
            mav.addObject("errors", result.getAllErrors());
            mav.addObject("librosConsulta", librosConsulta);
            mav.addObject("registroSalidaBusqueda", busqueda);
            return mav;
        }else { // Si no hay errores realizamos la búsqueda
        	// Ponemos la hora 23:59 a la fecha fin
            Date fechaFin = RegistroUtils.ajustarHoraBusqueda(busqueda.getFechaFin());
            Paginacion paginacion = registroSalidaEjb.busqueda(busqueda.getPageNumber(), busqueda.getFechaInicio(), fechaFin, registroSalida, librosConsulta, busqueda.getInteressatNom(), busqueda.getInteressatDoc(), busqueda.getOrganOrigen(), busqueda.getAnexos());
            // Alta en tabla LOPD
            lopdEjb.insertarRegistrosSalida(paginacion, usuarioEntidad.getId());
            busqueda.setPageNumber(1);
            mav.addObject("paginacion", paginacion);
            mav.addObject("librosConsulta", librosConsulta);
            mav.addObject("registroSalidaBusqueda", busqueda);
            mav.addObject("isAdministradorLibro", permisoLibroUsuarioEjb.isAdministradorLibro(getUsuarioEntidadActivo(request).getId(),registroSalida.getLibro().getId()));
            mav.addObject("puedeEditar", permisoLibroUsuarioEjb.tienePermiso(usuarioEntidad.getId(),registroSalida.getLibro().getId(),RegwebConstantes.PERMISO_MODIFICACION_REGISTRO_SALIDA));
            mav.addObject("oficinasRegistro",  oficinaEjb.findByEntidadByEstado(getEntidadActiva(request).getId(),RegwebConstantes.ESTADO_ENTIDAD_VIGENTE));
        }

        return mav;
    }

    /**
     * Carga el formulario para ver el detalle de un {@link es.caib.regweb.model.RegistroSalida}
     */
    @RequestMapping(value = "/{idRegistro}/detalle", method = RequestMethod.GET)
    public String detalleRegistroSalida(@PathVariable Long idRegistro, Model model, HttpServletRequest request) throws Exception {

        RegistroSalida registro = registroSalidaEjb.findById(idRegistro);
        Entidad entidad = getEntidadActiva(request);
        UsuarioEntidad usuarioEntidad = getUsuarioEntidadActivo(request);

        model.addAttribute("registro",registro);

        ModeloRecibo modeloRecibo = new ModeloRecibo();
        model.addAttribute("modeloRecibo", modeloRecibo);
        model.addAttribute("modelosRecibo", modeloReciboEjb.getByEntidad(getEntidadActiva(request).getId()));

        // Permisos
        model.addAttribute("isAdministradorLibro", permisoLibroUsuarioEjb.isAdministradorLibro(getUsuarioEntidadActivo(request).getId(),registro.getLibro().getId()));
        model.addAttribute("puedeEditar", permisoLibroUsuarioEjb.tienePermiso(usuarioEntidad.getId(),registro.getLibro().getId(),RegwebConstantes.PERMISO_MODIFICACION_REGISTRO_ENTRADA));

        // Interesados, solo si el Registro en Válio o Pendiente
        if(registro.getEstado().equals(RegwebConstantes.ESTADO_VALIDO) || registro.getEstado().equals(RegwebConstantes.ESTADO_PENDIENTE)){

            model.addAttribute("personasFisicas",personaEjb.getAllbyEntidadTipo(entidad.getId(), RegwebConstantes.TIPO_PERSONA_FISICA));
            model.addAttribute("personasJuridicas",personaEjb.getAllbyEntidadTipo(entidad.getId(), RegwebConstantes.TIPO_PERSONA_JURIDICA));
            model.addAttribute("tiposInteresado",RegwebConstantes.TIPOS_INTERESADO);
            model.addAttribute("tiposPersona", RegwebConstantes.TIPOS_PERSONA);
            model.addAttribute("paises",catPaisEjb.getAll());
            model.addAttribute("provincias",catProvinciaEjb.getAll());
            model.addAttribute("canalesNotificacion", RegwebConstantes.CANALES_NOTIFICACION);
            model.addAttribute("tiposDocumento",RegwebConstantes.TIPOS_DOCUMENTOID);
            model.addAttribute("nivelesAdministracion",catNivelAdministracionEjb.getAll());
            model.addAttribute("comunidadesAutonomas",catComunidadAutonomaEjb.getAll());
            model.addAttribute("organismosOficinaActiva",organismoEjb.getByOficinaActiva(getOficinaActiva(request).getId()));

        }
        // Anexos
        model.addAttribute("anexos", anexoEjb.getByRegistroSalida(idRegistro));
        initAnexos(entidad, model, request, registro.getId() );

        // Historicos
        model.addAttribute("historicos", historicoRegistroSalidaEjb.getByRegistroSalida(idRegistro));

        // Trazabilidad
        List<Trazabilidad> trazabilidades = trazabilidadEjb.getByRegistroSalida(registro.getId());
        model.addAttribute("trazabilidades", trazabilidades);

        // Alta en tabla LOPD
        lopdEjb.insertarRegistroSalida(idRegistro, usuarioEntidad.getId());
       
        return "registroSalida/registroSalidaDetalle";
    }

    /**
     * Carga el formulario para ver el detalle de un {@link es.caib.regweb.model.RegistroSalida}
     */
    @RequestMapping(value = "/{idRegistro}/sello", method = RequestMethod.GET)
    public ModelAndView sello(@PathVariable Long idRegistro, HttpServletRequest request) throws Exception {

        ModelAndView mav = new ModelAndView("sello");

        RegistroSalida registroSalida = registroSalidaEjb.findById(idRegistro);

        mav.addObject("registro", registroSalida);
        mav.addObject("x", request.getParameter("x"));
        mav.addObject("y", request.getParameter("y"));
        mav.addObject("orientacion", request.getParameter("orientacion"));
        mav.addObject("tipoRegistro", getMessage("informe.salida"));

        return mav;
    }



    @InitBinder("registroSalidaBusqueda")
    public void registroSalidaBusqueda(WebDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        CustomDateEditor dateEditor = new CustomDateEditor(sdf, true);
        binder.registerCustomEditor(java.util.Date.class,dateEditor);
    }

    /**
     * Eliminamos los posibles interesados de la Sesion
     * @param request
     * @throws Exception
     */
    public void eliminarInteresadosSesion(HttpServletRequest request) throws Exception{
        HttpSession session = request.getSession();

        session.setAttribute("interesados", null);
    }

}