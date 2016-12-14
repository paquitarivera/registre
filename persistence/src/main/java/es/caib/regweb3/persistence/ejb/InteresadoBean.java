package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.Interesado;
import es.caib.regweb3.model.RegistroDetalle;
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
 * @author earrivi
 * Date: 16/01/14
 */

@Stateless(name = "InteresadoEJB")
@SecurityDomain("seycon")
public class InteresadoBean extends BaseEjbJPA<Interesado, Long> implements InteresadoLocal{

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName="regweb3")
    private EntityManager em;

    @EJB(mappedName = "regweb3/RegistroDetalleEJB/local")
    public RegistroDetalleLocal registroDetalleEjb;


    @Override
    public Interesado getReference(Long id) throws Exception {

        return em.getReference(Interesado.class, id);
    }

    @Override
    public Interesado findById(Long id) throws Exception {

        return em.find(Interesado.class, id);
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<Interesado> getAll() throws Exception {

        return  em.createQuery("Select interesado from Interesado as interesado order by interesado.id").getResultList();
    }

    @Override
    public Long getTotal() throws Exception {

        Query q = em.createQuery("Select count(interesado.id) from Interesado as interesado");

        return (Long) q.getSingleResult();
    }


    @Override
    public List<Interesado> getPagination(int inicio) throws Exception {

        Query q = em.createQuery("Select interesado from Interesado as interesado order by interesado.id");
        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();
    }

    @Override
    public Interesado findByCodigoDir3RegistroDetalle(String codigoDir3, Long registroDetalle) throws Exception{
        Query q = em.createQuery("Select interesado from Interesado as interesado where interesado.codigoDir3 = :codigoDir3 " +
                "and interesado.registroDetalle.id = :registroDetalle");

        q.setParameter("codigoDir3",codigoDir3);
        q.setParameter("registroDetalle",registroDetalle);

        List<Interesado> interesado = q.getResultList();
        if(interesado.size() > 0){
            return interesado.get(0);
        }else{
            return  null;
        }
        
    }

    @Override
    public void eliminarInteresadoRegistroDetalle(Long idInteresado, Long idRegistroDetalle) throws Exception{

        Interesado interesado = findById(idInteresado);
        RegistroDetalle registroDetalle = registroDetalleEjb.findById(idRegistroDetalle);

        if(interesado != null && registroDetalle != null){
            registroDetalle.getInteresados().remove(interesado);
            registroDetalleEjb.merge(registroDetalle);
            remove(interesado);
        }

    }

    @Override
    public Boolean existeDocumentoNew(String documento) throws Exception{
        Query q = em.createQuery("Select interesado.id from Interesado as interesado where " +
                "interesado.documento = :documento");

        q.setParameter("documento",documento);

        return q.getResultList().size() > 0;
    }

    @Override
    public Boolean existeDocumentoEdit(String documento, Long idInteresado) throws Exception{
        Query q = em.createQuery("Select interesado.id from Interesado as interesado where " +
                "interesado.id != :idInteresado and interesado.documento = :documento");

        q.setParameter("documento",documento);
        q.setParameter("idInteresado",idInteresado);

        return q.getResultList().size() > 0;
    }

    @Override
    public Boolean existeInteresadoAdministracion(Long idRegistroDetalle) throws Exception{
        Query q = em.createQuery("Select interesado.id from Interesado as interesado where " +
                "interesado.registroDetalle.id = :idRegistroDetalle and interesado.tipo = :administracion");

        q.setParameter("idRegistroDetalle",idRegistroDetalle);
        q.setParameter("administracion", RegwebConstantes.TIPO_INTERESADO_ADMINISTRACION);


        return q.getResultList().size() > 0;
    }

    @Override
    public List<Interesado> guardarInteresados(List<Interesado> interesadosGuardar, RegistroDetalle registroDetalle) throws Exception{

        List<Interesado> interesados  = new ArrayList<Interesado>();

        for(Interesado interesado:interesadosGuardar){

            // Lo asociamos al RegistroDetalle
            interesado.setRegistroDetalle(registroDetalle);

            if(!interesado.getIsRepresentante()){ // Solo los Interesados

                if(interesado.getRepresentante() != null){ // Tiene Representante

                    // Obtenemos el Representante
                    Interesado representante = interesadosGuardar.get(interesadosGuardar.indexOf(interesado.getRepresentante()));

                    // Guardamos el Interesado sin referencia al Representante
                    interesado.setRegistroDetalle(registroDetalle);
                    interesado.setId(null); // ponemos su id a null
                    interesado.setRepresentante(null);
                    interesado = persist(interesado);

                    // Guardamos el Representante
                    representante.setId(null);
                    representante.setRegistroDetalle(registroDetalle);
                    representante.setRepresentado(interesado);
                    representante = persist(representante);

                    // Lo asigamos al interesado y actualizamos
                    interesado.setRepresentante(representante);
                    interesado = merge(interesado);

                    // Los añadimos al Array
                    interesados.add(interesado);
                    interesados.add(representante);

                }else{
                    interesado.setRegistroDetalle(registroDetalle);
                    interesado.setId(null); // ponemos su id a null
                    interesado.setRepresentante(null);
                    interesado = persist(interesado);

                    // Lo añadimos al Array
                    interesados.add(interesado);
                }

            }
        }

        return interesados;
    }
}
