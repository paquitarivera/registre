package es.caib.regweb3.persistence.ejb;

import es.caib.dir3caib.ws.api.oficina.Dir3CaibObtenerOficinasWs;
import es.caib.dir3caib.ws.api.oficina.OficinaTF;
import es.caib.dir3caib.ws.api.unidad.Dir3CaibObtenerUnidadesWs;
import es.caib.dir3caib.ws.api.unidad.UnidadTF;
import es.caib.regweb3.model.Entidad;
import es.caib.regweb3.model.Oficina;
import es.caib.regweb3.model.Organismo;
import es.caib.regweb3.model.Plantilla;
import es.caib.regweb3.model.utils.PlantillaJson;
import es.caib.regweb3.persistence.utils.PropiedadGlobalUtil;
import es.caib.regweb3.persistence.utils.RegistroUtils;
import es.caib.regweb3.utils.Dir3CaibUtils;
import es.caib.regweb3.utils.RegwebConstantes;
import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Fundació BIT.
 *
 * @author jpernia
 * Date: 16/07/14
 */

@Stateless(name = "PlantillaEJB")
@SecurityDomain("seycon")
public class PlantillaBean extends BaseEjbJPA<Plantilla, Long> implements PlantillaLocal{

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName="regweb3")
    private EntityManager em;

    @EJB private OrganismoLocal organismoEjb;
    @EJB public OficinaLocal oficinaEjb;


    @Override
    public Plantilla getReference(Long id) throws Exception {

        return em.getReference(Plantilla.class, id);
    }

    @Override
    public Plantilla findById(Long id) throws Exception {

        return em.find(Plantilla.class, id);
    }

    @Override
    public Plantilla findByOrden(Long idUsuario, int orden) throws Exception {

        Query q = em.createQuery("Select plantilla from Plantilla as plantilla where " +
                "plantilla.usuario.id = :idUsuario and plantilla.orden = :orden");

        q.setParameter("idUsuario",idUsuario);
        q.setParameter("orden",orden);

        return (Plantilla) q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Plantilla> getAll() throws Exception {

        return  em.createQuery("Select plantilla from Plantilla as plantilla order by plantilla.id").getResultList();
    }

    @Override
    public Long getTotal() throws Exception {

        Query q = em.createQuery("Select count(plantilla.id) from Plantilla as plantilla");

        return (Long) q.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Plantilla> getPagination(int inicio) throws Exception {

        Query q = em.createQuery("Select plantilla from Plantilla as plantilla order by plantilla.id");
        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Plantilla> getPaginationUsuario(int inicio, Long idUsuario) throws Exception {

        Query q = em.createQuery("Select plantilla from Plantilla as plantilla  " +
                "where plantilla.usuario.id = :idUsuario order by plantilla.orden");

        q.setParameter("idUsuario",idUsuario);
        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Plantilla> getAllbyUsuario(Long idUsuario) throws Exception {

        Query q = em.createQuery("Select plantilla from Plantilla as plantilla  " +
                "where plantilla.usuario.id = :idUsuario order by plantilla.orden");

        q.setParameter("idUsuario", idUsuario);

        return  q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Plantilla> getActivasbyUsuario(Long idUsuario, Long tipoRegistro) throws Exception {

        Query q = em.createQuery("Select plantilla.id, plantilla.nombre from Plantilla as plantilla  " +
                "where plantilla.usuario.id = :idUsuario and plantilla.tipoRegistro = :tipoRegistro and plantilla.activo = true order by plantilla.orden");

        q.setParameter("idUsuario",idUsuario);
        q.setParameter("tipoRegistro",tipoRegistro);

        List<Plantilla> plantillas = new ArrayList<Plantilla>();

        List<Object[]> result = q.getResultList();

        for (Object[] object : result) {
            Plantilla plantilla = new Plantilla((Long) object[0], (String) object[1]);

            plantillas.add(plantilla);
        }

        return plantillas;
    }

    @Override
    public Long getTotalbyUsuario(Long idUsuario) throws Exception {

        Query q = em.createQuery("Select count(plantilla.id) from Plantilla as plantilla " +
                "where plantilla.usuario.id = :idUsuario");

        q.setParameter("idUsuario",idUsuario);

        return (Long) q.getSingleResult();
    }

    @Override
    public Integer maxOrdenPlantilla(Long idUsuario) throws Exception{

        Query q = em.createQuery("Select max(plantilla.orden) from Plantilla as plantilla  " +
                "where plantilla.usuario.id = :idUsuario");

        q.setParameter("idUsuario",idUsuario);

        return (Integer) q.getSingleResult();
    }

    @Override
    public Long obtenerUsuarioPlantilla(Long idPlantilla) throws Exception{

        Query q = em.createQuery("Select plantilla.usuario.id from Plantilla as plantilla  " +
                "where plantilla.id = :idPlantilla");

        q.setParameter("idPlantilla",idPlantilla);

        return (Long) q.getSingleResult();
    }


    @Override
    public void modificarOrden(Long idPlantilla, int orden) throws Exception{

        Query q = em.createQuery("Update Plantilla set orden = :orden where " +
                "id = :idPlantilla");

        q.setParameter("idPlantilla",idPlantilla);
        q.setParameter("orden",orden);
        q.executeUpdate();
    }


    @Override
    public Boolean subirOrden(Long idPlantilla) throws Exception{

        boolean result;

        try{
            Long idUsuario = obtenerUsuarioPlantilla(idPlantilla);

            Plantilla plantilla = findById(idPlantilla);

            int ordenActual = plantilla.getOrden();

            int ordenNuevo = 1;
            if(ordenActual > 1){
                ordenNuevo = ordenActual - 1;
            }

            Plantilla plantillaAnterior = findByOrden(idUsuario, ordenNuevo);

            modificarOrden(idPlantilla, ordenNuevo);

            modificarOrden(plantillaAnterior.getId(), ordenActual);
            result = true;

        } catch(Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;

    }

    @Override
    public Boolean bajarOrden(Long idPlantilla) throws Exception{

        boolean result;

        try{
            Long idUsuario = obtenerUsuarioPlantilla(idPlantilla);
            List<Plantilla> plantillas = getAllbyUsuario(idUsuario);

            Plantilla plantilla = findById(idPlantilla);

            int ordenActual = plantilla.getOrden();

            int ordenNuevo = plantillas.size();
            if(ordenActual < plantillas.size()){
                ordenNuevo = ordenActual + 1;
            }

            Plantilla plantillaPosterior = findByOrden(idUsuario, ordenNuevo);

            modificarOrden(idPlantilla, ordenNuevo);

            modificarOrden(plantillaPosterior.getId(), ordenActual);
            result = true;

        } catch(Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;

    }

    @Override
    public Boolean cambiarEstado(Long idPlantilla) throws Exception{

        boolean result;

        try{
            Plantilla plantilla = findById(idPlantilla);

            if(plantilla.getActivo()){
                plantilla.setActivo(false);
            }else{
                plantilla.setActivo(true);
            }

            merge(plantilla);
            result = true;

        } catch(Exception e){
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public Integer eliminarByEntidad(Long idEntidad) throws Exception{

        List<?> plantillas =  em.createQuery("select distinct(r.id) from Plantilla as r where r.usuario.entidad.id =:idEntidad").setParameter("idEntidad",idEntidad).getResultList();
        Integer total = plantillas.size();

        if(plantillas.size() > 0){

            // Si hay más de 1000 registros, dividimos las queries (ORA-01795).
            while (plantillas.size() > RegwebConstantes.NUMBER_EXPRESSIONS_IN) {

                List<?> subList = plantillas.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN);
                em.createQuery("delete from Plantilla where id in (:plantillas)").setParameter("plantillas", subList).executeUpdate();
                plantillas.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN).clear();
            }

            em.createQuery("delete from Plantilla where id in (:plantillas)").setParameter("plantillas", plantillas).executeUpdate();
        }
        return total;

    }

    @Override
    public Boolean obtenerPorUsuario(Long idUsuarioEntidad) throws Exception {

        Query q;

        q = em.createQuery("Select count(plantilla.id) from Plantilla as plantilla where plantilla.usuario.id = :idUsuarioEntidad ");

        q.setParameter("idUsuarioEntidad", idUsuarioEntidad);

        return (Long) q.getSingleResult() > 0;
    }

    public PlantillaJson obtenerPlantilla(Long idPlantilla, Entidad entidad) throws Exception{

        Plantilla plantilla = findById(idPlantilla);
        PlantillaJson plantillaJson = RegistroUtils.desSerilizarPlantillaXml(plantilla.getRepro());

        switch (plantilla.getTipoRegistro().intValue()){

            case 1: //RegistroEntrada

                // Comprobamos la unidad destino
                if(plantillaJson.getDestinoCodigo()!= null && plantillaJson.isDestinoExterno()){ // Preguntamos a DIR3 si está Vigente
                    Dir3CaibObtenerUnidadesWs unidadesService = Dir3CaibUtils.getObtenerUnidadesService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());
                    UnidadTF unidad = unidadesService.obtenerUnidad(plantillaJson.getDestinoCodigo(), null, null);

                    if(!unidad.getCodigoEstadoEntidad().equals(RegwebConstantes.ESTADO_ENTIDAD_VIGENTE)){// Ya no es vigente
                        plantillaJson.setDestinoExterno(null);
                        plantillaJson.setDestinoCodigo(null);
                        plantillaJson.setDestinoDenominacion(null);
                        plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                        merge(plantilla);
                    }

                }else{ // Comprobamos en REGWEB3 si está vigente
                    Organismo organismoDestino = organismoEjb.findByCodigoEntidad(plantillaJson.getDestinoCodigo(), entidad.getId());

                    if(organismoDestino == null){ // Ya no es vigente
                        plantillaJson.setDestinoExterno(null);
                        plantillaJson.setDestinoCodigo(null);
                        plantillaJson.setDestinoDenominacion(null);
                        plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                        merge(plantilla);
                    }
                }
                break;

            case 2: //RegistroSalida

                // Comprobamos la unidad origen
                if(plantillaJson.getOrigenCodigo()!= null && plantillaJson.isOrigenExterno()){ // Preguntamos a DIR3 si está Vigente
                    Dir3CaibObtenerUnidadesWs unidadesService = Dir3CaibUtils.getObtenerUnidadesService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());
                    UnidadTF unidad = unidadesService.obtenerUnidad(plantillaJson.getOrigenCodigo(), null, null);

                    if(!unidad.getCodigoEstadoEntidad().equals(RegwebConstantes.ESTADO_ENTIDAD_VIGENTE)){// Ya no es vigente
                        plantillaJson.setOrigenExterno(null);
                        plantillaJson.setOrigenCodigo(null);
                        plantillaJson.setOrigenDenominacion(null);
                        plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                        merge(plantilla);
                    }

                }else{ // Comprobamos en REGWEB3 si está vigente
                    Organismo organismoOrigen = organismoEjb.findByCodigoEntidad(plantillaJson.getOrigenCodigo(), entidad.getId());
                    if(organismoOrigen == null){ // Ya no es vigente
                        plantillaJson.setOrigenExterno(null);
                        plantillaJson.setOrigenCodigo(null);
                        plantillaJson.setOrigenDenominacion(null);
                        plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                        merge(plantilla);
                    }
                }

                break;


        }

        // Oficina Origen
        if(plantillaJson.getOficinaCodigo()!= null  && !plantillaJson.getOficinaCodigo().equals("-1") && plantillaJson.isOficinaExterna()){// Preguntamos a DIR3 si está Vigente
            Dir3CaibObtenerOficinasWs oficinasService = Dir3CaibUtils.getObtenerOficinasService(PropiedadGlobalUtil.getDir3CaibServer(), PropiedadGlobalUtil.getDir3CaibUsername(), PropiedadGlobalUtil.getDir3CaibPassword());
            OficinaTF oficina = oficinasService.obtenerOficina(plantillaJson.getOficinaCodigo(),null,null);

            if(!oficina.getEstado().equals(RegwebConstantes.ESTADO_ENTIDAD_VIGENTE)){// Ya no es vigente
                plantillaJson.setOficinaCodigo(null);
                plantillaJson.setOficinaDenominacion(null);
                plantillaJson.setOficinaExterna(null);
                plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                merge(plantilla);
            }

        }else{// Comprobamos en REGWEB3 si está vigente
            Oficina oficinaOrigen = oficinaEjb.findByCodigoVigente(plantillaJson.getOficinaCodigo());
            if(oficinaOrigen == null){
                plantillaJson.setOficinaCodigo(null);
                plantillaJson.setOficinaDenominacion(null);
                plantillaJson.setOficinaExterna(null);
                plantilla.setRepro(RegistroUtils.serilizarXml(plantillaJson));
                merge(plantilla);
            }
        }

        return plantillaJson;
    }

}