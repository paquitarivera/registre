package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.Integracion;
import es.caib.regweb3.persistence.utils.Paginacion;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 06/03/18
 */

@Stateless(name = "IntegracionEJB")
@SecurityDomain("seycon")
@RunAs("RWE_SUPERADMIN")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class IntegracionBean extends BaseEjbJPA<Integracion, Long> implements IntegracionLocal{

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName="regweb3")
    private EntityManager em;


    @Override
    public Integracion getReference(Long id) throws Exception {

        return em.getReference(Integracion.class, id);
    }

    @Override
    public Integracion findById(Long id) throws Exception {

        return em.find(Integracion.class, id);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Integracion> getAll() throws Exception {

        return  em.createQuery("Select integracion from Integracion as integracion order by integracion.id").getResultList();
    }

    @Override
    public Long getTotal() throws Exception {

        Query q = em.createQuery("Select count(integracion.id) from Integracion as integracion");

        return (Long) q.getSingleResult();
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Integracion> getPagination(int inicio) throws Exception {

        Query q = em.createQuery("Select integracion from Integracion as integracion order by integracion.id");
        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Integracion> getByEntidad(Long idEntidad) throws Exception{

        Query q = em.createQuery("Select integracion from Integracion as integracion where integracion.entidad.id = :idEntidad order by integracion.id");
        q.setParameter("idEntidad",idEntidad);

        return q.getResultList();

    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Integracion> getByEntidadNumReg(Long idEntidad, String numeroRegistro) throws Exception{

        Query q = em.createQuery("Select integracion from Integracion as integracion where " +
                "integracion.entidad.id = :idEntidad and integracion.numRegFormat =:numeroRegistro order by integracion.id");

        q.setParameter("idEntidad",idEntidad);
        q.setParameter("numeroRegistro",numeroRegistro);

        return q.getResultList();

    }

    @Override
    public Paginacion busqueda(Integracion integracion, Long idEntidad) throws Exception {

        Query q;
        Query q2;
        Map<String, Object> parametros = new HashMap<String, Object>();
        List<String> where = new ArrayList<String>();

        StringBuilder query = new StringBuilder("Select integracion from Integracion as integracion ");

        // Tipo integración
        if (integracion.getTipo() != -1) {
            where.add("integracion.tipo = :tipo ");
            parametros.put("tipo", integracion.getTipo());
        }

        // Estado
        if (integracion.getEstado() != null) {
            where.add("integracion.estado = :estado ");
            parametros.put("estado", integracion.getEstado());
        }

        // Entidad
        where.add("integracion.entidad.id = :idEntidad ");
        parametros.put("idEntidad", idEntidad);

        // Numero Registro
        if (StringUtils.isNotEmpty(integracion.getNumRegFormat())) {
            where.add(" (UPPER(integracion.numRegFormat) LIKE UPPER(:numRegFormat)) ");
            parametros.put("numRegFormat", "%" + integracion.getNumRegFormat() + "%");
        }


        // Añadimos los parámetros a la query
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
            q2 = em.createQuery(query.toString().replaceAll("Select integracion from Integracion as integracion ", "Select count(integracion.id) from Integracion as integracion "));
            query.append("order by integracion.id desc");
            q = em.createQuery(query.toString());

            for (Map.Entry<String, Object> param : parametros.entrySet()) {
                q.setParameter(param.getKey(), param.getValue());
                q2.setParameter(param.getKey(), param.getValue());
            }

        } else {
            q2 = em.createQuery(query.toString().replaceAll("Select integracion from Integracion as integracion ", "Select count(integracion.id) from Integracion as integracion "));
            query.append("order by integracion.id desc");
            q = em.createQuery(query.toString());
        }


        Paginacion paginacion;

        if (integracion.getPageNumber() != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), integracion.getPageNumber(), Integracion.RESULTADOS_PAGINACION);
            int inicio = (integracion.getPageNumber() - 1) * Integracion.RESULTADOS_PAGINACION;
            q.setFirstResult(inicio);
            q.setMaxResults(Integracion.RESULTADOS_PAGINACION);
        } else {
            paginacion = new Paginacion(0, 0);
        }

        paginacion.setListado(q.getResultList());

        return paginacion;

    }

    @Override
    public void addIntegracionOk(Long tipo, String descripcion, String peticion, Long tiempo, Long idEntidad, String numRegFormat) throws Exception{

        persist(new Integracion(tipo, RegwebConstantes.INTEGRACION_ESTADO_OK, descripcion, peticion, tiempo, idEntidad, numRegFormat));
    }

    @Override
    public void addIntegracionError(Long tipo, String descripcion, String peticion, Throwable th, String error, Long tiempo, Long idEntidad, String numRegFormat) throws Exception {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        th.printStackTrace(pw);
        String exception = sw.getBuffer().toString();

        if (StringUtils.isEmpty(error)){
            error = th.getMessage();
        }

        persist(new Integracion(tipo, RegwebConstantes.INTEGRACION_ESTADO_ERROR, descripcion, peticion, error, exception,tiempo, idEntidad, numRegFormat));
    }

    @Override
    public Integer purgarIntegraciones(Long idEntidad) throws Exception{

        Calendar hoy = Calendar.getInstance(); //obtiene la fecha de hoy
        hoy.add(Calendar.DATE, -7); //el -7 indica que se le restaran 7 dias


        List<?> integracion =  em.createQuery("select distinct(i.id) from Integracion as i where i.entidad.id = :idEntidad and i.fecha <= :fecha").setParameter("idEntidad",idEntidad).setParameter("fecha", hoy.getTime()).getResultList();
        Integer total = integracion.size();
        log.info("total integraciones a eliminar: " + integracion.size());

        if(integracion.size() > 0){

            // Si hay más de 1000 registros, dividimos las queries (ORA-01795).
            while (integracion.size() > RegwebConstantes.NUMBER_EXPRESSIONS_IN) {

                List<?> subList = integracion.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN);
                em.createQuery("delete from Integracion where id in (:integracion)").setParameter("integracion", subList).executeUpdate();
                integracion.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN).clear();
            }

            em.createQuery("delete from Integracion where id in (:integracion)").setParameter("integracion", integracion).executeUpdate();
        }
        return total;

    }

    @Override
    public Integer eliminarByEntidad(Long idEntidad) throws Exception{

        List<?> integracion =  em.createQuery("select distinct(i.id) from Integracion as i where i.entidad.id = :idEntidad").setParameter("idEntidad",idEntidad).getResultList();
        Integer total = integracion.size();

        if(integracion.size() > 0){

            // Si hay más de 1000 registros, dividimos las queries (ORA-01795).
            while (integracion.size() > RegwebConstantes.NUMBER_EXPRESSIONS_IN) {

                List<?> subList = integracion.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN);
                em.createQuery("delete from Integracion where id in (:integracion)").setParameter("integracion", subList).executeUpdate();
                integracion.subList(0, RegwebConstantes.NUMBER_EXPRESSIONS_IN).clear();
            }

            em.createQuery("delete from Integracion where id in (:integracion)").setParameter("integracion", integracion).executeUpdate();
        }
        return total;

    }

}
