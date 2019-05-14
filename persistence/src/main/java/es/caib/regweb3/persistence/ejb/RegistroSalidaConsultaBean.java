package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.Anexo;
import es.caib.regweb3.model.Libro;
import es.caib.regweb3.model.Organismo;
import es.caib.regweb3.model.RegistroSalida;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.model.utils.RegistroBasico;
import es.caib.regweb3.persistence.utils.DataBaseUtils;
import es.caib.regweb3.persistence.utils.Paginacion;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.utils.StringUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.jboss.ejb3.annotation.SecurityDomain;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

import static es.caib.regweb3.persistence.ejb.BaseEjbJPA.RESULTADOS_PAGINACION;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 15/05/19
 */

@Stateless(name = "RegistroSalidaConsultaEJB")
@SecurityDomain("seycon")
public class RegistroSalidaConsultaBean implements RegistroSalidaConsultaLocal {

    protected final Logger log = Logger.getLogger(getClass());

    @PersistenceContext(unitName = "regweb3")
    private EntityManager em;

    @EJB private AnexoLocal anexoEjb;
    @EJB private OrganismoLocal organismoEjb;


    @EJB(mappedName = "regweb3/PluginEJB/local")
    private PluginLocal pluginEjb;

    @Override
    @SuppressWarnings("unchecked")
    public RegistroBasico findByIdLigero(Long idRegistroSalida) throws Exception{
        Query q;

        q = em.createQuery("Select rs.id, rs.numeroRegistroFormateado, rs.fecha, rs.libro.nombre, rs.usuario.usuario.identificador, rs.estado " +
                "from RegistroSalida as rs where rs.id = :idRegistroSalida ");


        q.setParameter("idRegistroSalida", idRegistroSalida);

        List<Object[]> result = q.getResultList();

        if(result.size() == 1){
            Object[] object = result.get(0);

            RegistroBasico registroBasico = new RegistroBasico();
            registroBasico.setId((Long)  object[0]);
            registroBasico.setNumeroRegistroFormateado((String) object[1]);
            registroBasico.setFecha((Date) object[2]);
            registroBasico.setLibro((String) object[3]);
            registroBasico.setUsuario((String) object[4]);
            registroBasico.setEstado((Long) object[5]);

            return registroBasico;
        }

        return null;
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public Paginacion busqueda(Integer pageNumber, Date fechaInicio, Date fechaFin, RegistroSalida registroSalida, String interesadoNom, String interesadoLli1, String interesadoLli2, String interesadoDoc, String organoOrigen, Boolean anexos, String observaciones, String usuario, Long idEntidad) throws Exception {

        Query q;
        Query q2;
        Map<String, Object> parametros = new HashMap<String, Object>();
        List<String> where = new ArrayList<String>();

        String queryBase = "Select DISTINCT registroSalida from RegistroSalida as registroSalida left outer join registroSalida.registroDetalle.interesados interessat ";
        StringBuilder query = new StringBuilder(queryBase);

        // Numero registro
        if (StringUtils.isNotEmpty(registroSalida.getNumeroRegistroFormateado())) {
            where.add(" registroSalida.numeroRegistroFormateado LIKE :numeroRegistroFormateado");
            parametros.put("numeroRegistroFormateado", "%" + registroSalida.getNumeroRegistroFormateado() + "%");
        }

        // Extracto
        if (StringUtils.isNotEmpty(registroSalida.getRegistroDetalle().getExtracto())) {
            where.add(DataBaseUtils.like("registroSalida.registroDetalle.extracto", "extracto", parametros, new String(registroSalida.getRegistroDetalle().getExtracto().getBytes("ISO-8859-1"), "UTF-8")));
        }

        // Observaciones
        if (StringUtils.isNotEmpty(observaciones)) {
            where.add(DataBaseUtils.like("registroSalida.registroDetalle.observaciones", "observaciones", parametros, observaciones));
        }

        // Usuario
        if (StringUtils.isNotEmpty(usuario)) {
            where.add(DataBaseUtils.like("registroSalida.usuario.usuario.identificador", "usuario", parametros, usuario));
        }

        // Nombre interesado
        if (StringUtils.isNotEmpty(interesadoNom)) {
            where.add("((" + DataBaseUtils.like("interessat.nombre", "interesadoNom", parametros, interesadoNom) +
                    ") or (" + DataBaseUtils.like("interessat.razonSocial", "interesadoNom", parametros, interesadoNom) +
                    "))");
        }

        // Primer apellido interesado
        if (StringUtils.isNotEmpty(interesadoLli1)) {
            where.add(DataBaseUtils.like("interessat.apellido1", "interesadoLli1", parametros, interesadoLli1));
        }

        // Segundo apellido interesado
        if (StringUtils.isNotEmpty(interesadoLli2)) {
            where.add(DataBaseUtils.like("interessat.apellido2", "interesadoLli2", parametros, interesadoLli2));
        }

        // Documento interesado
        if (StringUtils.isNotEmpty(interesadoDoc)) {
            where.add(" (UPPER(interessat.documento) LIKE UPPER(:interesadoDoc)) ");
            parametros.put("interesadoDoc", "%" + interesadoDoc.trim() + "%");
        }

        // Organismo origen
        if (StringUtils.isNotEmpty((organoOrigen))) {
            Organismo organismo = organismoEjb.findByCodigoEntidadSinEstadoLigero(organoOrigen, idEntidad);
            if (organismo == null) {
                where.add(" registroSalida.origenExternoCodigo = :organoOrigen ");
            } else {
                where.add(" registroSalida.origen.codigo = :organoOrigen ");
            }

            parametros.put("organoOrigen", organoOrigen);
        }

        // Estado registro
        if (registroSalida.getEstado() != null && registroSalida.getEstado() > 0) {
            where.add(" registroSalida.estado = :idEstadoRegistro ");
            parametros.put("idEstadoRegistro", registroSalida.getEstado());
        }

        // Tipo documentación física
        if (registroSalida.getRegistroDetalle().getTipoDocumentacionFisica() != null && registroSalida.getRegistroDetalle().getTipoDocumentacionFisica() > 0) {
            where.add(" registroSalida.registroDetalle.tipoDocumentacionFisica = :tipoDocumentacion ");
            parametros.put("tipoDocumentacion", registroSalida.getRegistroDetalle().getTipoDocumentacionFisica());
        }

        // Oficina Registro
        if (registroSalida.getOficina().getId() != null && registroSalida.getOficina().getId() > 0) {
            where.add(" registroSalida.oficina.id = :idOficina ");
            parametros.put("idOficina", registroSalida.getOficina().getId());
        }

        // Intervalo fechas
        where.add(" (registroSalida.fecha >= :fechaInicio  ");
        parametros.put("fechaInicio", fechaInicio);
        where.add(" registroSalida.fecha <= :fechaFin) ");
        parametros.put("fechaFin", fechaFin);

        // Libro
        where.add(" registroSalida.libro.id = :idLibro");
        parametros.put("idLibro", registroSalida.getLibro().getId());

        // Buscamos registros de sañida con anexos
        if (anexos) {
            where.add(" registroSalida.registroDetalle.id in (select distinct(a.registroDetalle.id) from Anexo as a) ");
        }

        //Presencial
        if(registroSalida.getRegistroDetalle().getPresencial() != null){
            where.add(" registroSalida.registroDetalle.presencial = :presencial ");
            parametros.put("presencial", registroSalida.getRegistroDetalle().getPresencial());
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
            // Duplicamos la query solo para obtener los resultados totales
            q2 = em.createQuery(query.toString().replaceAll(queryBase, "Select count(DISTINCT registroSalida.id) from RegistroSalida as registroSalida left outer join registroSalida.registroDetalle.interesados interessat "));
            query.append(" order by registroSalida.id desc");
            q = em.createQuery(query.toString());

            for (Map.Entry<String, Object> param : parametros.entrySet()) {

                q.setParameter(param.getKey(), param.getValue());
                q2.setParameter(param.getKey(), param.getValue());
            }

        } else {
            // Duplicamos la query solo para obtener los resultados totales
            q2 = em.createQuery(query.toString().replaceAll(queryBase, "Select count(DISTINCT registroSalida.id) from RegistroSalida as registroSalida left outer join registroSalida.registroDetalle.interesados interessat "));
            query.append("order by registroSalida.id desc");
            q = em.createQuery(query.toString());
        }


        Paginacion paginacion;

        if (pageNumber != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), pageNumber);
            int inicio = (pageNumber - 1) * RESULTADOS_PAGINACION;
            q.setFirstResult(inicio);
            q.setMaxResults(RESULTADOS_PAGINACION);
        } else {
            paginacion = new Paginacion(0, 0);
        }

        paginacion.setListado(q.getResultList());

        return paginacion;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RegistroSalida findByNumeroAnyoLibro(int numero, int anyo, String libro) throws Exception {

        Query q = em.createQuery("Select registroSalida "
                + " from RegistroSalida as registroSalida"
                + " where registroSalida.numeroRegistro = :numero "
                + " AND  YEAR(registroSalida.fecha) = :anyo "
                + " AND  registroSalida.libro.codigo = :libro ");

        q.setParameter("numero", numero);
        q.setParameter("anyo", anyo);
        q.setParameter("libro", libro);

        List<RegistroSalida> registro = q.getResultList();

        if (registro.size() == 1) {
            return registro.get(0);
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public RegistroSalida findByNumeroRegistroFormateado(String codigoEntidad, String numeroRegistroFormateado) throws Exception {


        Query q = em.createQuery("Select rs from RegistroSalida as rs where rs.numeroRegistroFormateado = :numeroRegistroFormateado " +
                "and rs.usuario.entidad.codigoDir3 = :codigoEntidad ");

        q.setParameter("numeroRegistroFormateado", numeroRegistroFormateado);
        q.setParameter("codigoEntidad", codigoEntidad);

        List<RegistroSalida> registro = q.getResultList();

        if (registro.size() == 1) {
            return registro.get(0);
        } else {
            return null;
        }
    }

    @Override
    public RegistroSalida findByNumeroRegistroFormateadoConAnexos(String codigoEntidad, String numeroRegistroFormateado) throws Exception, I18NException {

        RegistroSalida registroSalida = findByNumeroRegistroFormateado(codigoEntidad,numeroRegistroFormateado);
        if(registroSalida!= null){
            return cargarAnexosFull(registroSalida);
        }else{
            return null;
        }
    }



    @Override
    @SuppressWarnings("unchecked")
    public String findNumeroRegistroFormateadoByRegistroDetalle(Long idRegistroDetalle) throws Exception {

        Query q = em.createQuery("Select registroSalida.numeroRegistroFormateado "
                + " from RegistroSalida as registroSalida"
                + " where registroSalida.registroDetalle.id = :idRegistroDetalle "
        );

        q.setParameter("idRegistroDetalle", idRegistroDetalle);


        List<String> registro = q.getResultList();

        if (registro.size() == 1) {
            return registro.get(0);
        } else {
            return null;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public Long getLibro(Long idRegistroSalida) throws Exception {

        Query q;

        q = em.createQuery("Select registroSalida.libro.id from RegistroSalida as registroSalida where registroSalida.id = :idRegistroSalida ");

        q.setParameter("idRegistroSalida", idRegistroSalida);

        List<Long> libros = q.getResultList();

        if (libros.size() > 0) {
            return libros.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Long getByLibrosEstadoCount(List<Libro> libros, Long idEstado) throws Exception {

        Query q;

        q = em.createQuery("Select count(re.id) from RegistroSalida as re where re.libro in (:libros) " +
                "and re.estado = :idEstado");

        q.setParameter("libros", libros);
        q.setParameter("idEstado", idEstado);

        return (Long) q.getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RegistroSalida> getByLibrosEstado(int inicio, List<Libro> libros, Long idEstado) throws Exception {

        Query q;

        q = em.createQuery("Select re from RegistroSalida as re where re.libro in (:libros) " +
                "and re.estado = :idEstado order by re.fecha desc");

        q.setParameter("libros", libros);
        q.setParameter("idEstado", idEstado);

        q.setFirstResult(inicio);
        q.setMaxResults(RESULTADOS_PAGINACION);

        return q.getResultList();

    }

    @Override
    public Long getTotalByLibro(Long idLibro) throws Exception {

        Query q;

        q = em.createQuery("Select count(rs.id) from RegistroSalida as rs where rs.libro.id = :idLibro ");

        q.setParameter("idLibro", idLibro);

        return (Long) q.getSingleResult();
    }

    @Override
    public Boolean obtenerPorUsuario(Long idUsuarioEntidad) throws Exception {

        Query q;

        q = em.createQuery("Select count(rs.id) from RegistroSalida as rs where rs.usuario.id = :idUsuarioEntidad ");

        q.setParameter("idUsuarioEntidad", idUsuarioEntidad);

        return (Long) q.getSingleResult() > 0;
    }


    @Override
    public RegistroSalida getConAnexosFullLigero(Long id) throws Exception, I18NException {

        RegistroSalida rs = em.find(RegistroSalida.class, id);
        Long idEntidad = rs.getOficina().getOrganismoResponsable().getEntidad().getId();
        List<Anexo> anexos = rs.getRegistroDetalle().getAnexos();
        List<AnexoFull> anexosFull = new ArrayList<AnexoFull>();
        for (Anexo anexo : anexos) {
            AnexoFull anexoFull = anexoEjb.getAnexoFullLigero(anexo.getId(), idEntidad);
            anexosFull.add(anexoFull);
        }
        //Asignamos los documentos recuperados de custodia al registro de salida.
        rs.getRegistroDetalle().setAnexosFull(anexosFull);
        return rs;
    }

    @Override
    public RegistroSalida getConAnexosFull(Long id) throws Exception, I18NException {

        RegistroSalida registroSalida = em.find(RegistroSalida.class, id);
        return cargarAnexosFull(registroSalida);
    }


    @Override
    @SuppressWarnings(value = "unchecked")
    public Paginacion getSirRechazadosReenviadosPaginado(Integer pageNumber, Long idOficina) throws Exception {

        Query q;
        Query q2;

        q = em.createQuery("Select rs from RegistroSalida as rs where rs.oficina.id = :idOficina " +
                "and rs.estado = :rechazado or rs.estado = :reenviado order by rs.fecha desc");

        q.setParameter("idOficina", idOficina);
        q.setParameter("rechazado", RegwebConstantes.REGISTRO_RECHAZADO);
        q.setParameter("reenviado", RegwebConstantes.REGISTRO_REENVIADO);

        q2 = em.createQuery("Select count(rs.id) from RegistroSalida as rs where rs.oficina.id = :idOficina " +
                "and rs.estado = :rechazado or rs.estado = :reenviado");

        q2.setParameter("idOficina", idOficina);
        q2.setParameter("rechazado", RegwebConstantes.REGISTRO_RECHAZADO);
        q2.setParameter("reenviado", RegwebConstantes.REGISTRO_REENVIADO);


        Paginacion paginacion;

        if (pageNumber != null) { // Comprobamos si es una busqueda paginada o no
            Long total = (Long) q2.getSingleResult();
            paginacion = new Paginacion(total.intValue(), pageNumber);
            int inicio = (pageNumber - 1) * RESULTADOS_PAGINACION;
            q.setFirstResult(inicio);
            q.setMaxResults(RESULTADOS_PAGINACION);
        } else {
            paginacion = new Paginacion(0, 0);
        }

        paginacion.setListado(q.getResultList());

        return paginacion;

    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<RegistroSalida> getSirRechazadosReenviados(Long idOficina, Integer total) throws Exception {

        Query q;

        q = em.createQuery("Select rs from RegistroSalida as rs where rs.oficina.id = :idOficinaActiva " +
                "and (rs.estado = :rechazado or rs.estado = :reenviado) order by rs.fecha desc");

        q.setMaxResults(total);
        q.setParameter("idOficinaActiva", idOficina);
        q.setParameter("rechazado", RegwebConstantes.REGISTRO_RECHAZADO);
        q.setParameter("reenviado", RegwebConstantes.REGISTRO_REENVIADO);

        return q.getResultList();
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public Long getSirRechazadosReenviadosCount(Long idOficina) throws Exception{

        Query q;

        q = em.createQuery("Select count(rs.id) from RegistroSalida as rs where rs.oficina.id = :idOficinaActiva " +
                "and (rs.estado = :rechazado or rs.estado = :reenviado)");


        q.setParameter("idOficinaActiva", idOficina);
        q.setParameter("rechazado", RegwebConstantes.REGISTRO_RECHAZADO);
        q.setParameter("reenviado", RegwebConstantes.REGISTRO_REENVIADO);

        return (Long) q.getSingleResult();
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<RegistroSalida> getByDocumento(Long idEntidad, String documento) throws Exception{

        Query q;

        q = em.createQuery("Select DISTINCT rs from RegistroSalida as rs left outer join rs.registroDetalle.interesados interessat " +
                "where (UPPER(interessat.documento) LIKE UPPER(:documento)) and rs.usuario.entidad.id = :idEntidad");

        q.setParameter("idEntidad", idEntidad);
        q.setParameter("documento", documento.trim());

        return q.getResultList();
    }

    /**
     * Carga los Anexos Completos al RegistroSalida pasado por parámetro
     * @param registroSalida
     * @return
     * @throws Exception
     * @throws I18NException
     */
    private RegistroSalida cargarAnexosFull(RegistroSalida registroSalida) throws Exception, I18NException {
        Long idEntidad = registroSalida.getOficina().getOrganismoResponsable().getEntidad().getId();

        List<Anexo> anexos = registroSalida.getRegistroDetalle().getAnexos();
        List<AnexoFull> anexosFull = new ArrayList<AnexoFull>();
        for (Anexo anexo : anexos) {
            AnexoFull anexoFull = anexoEjb.getAnexoFull(anexo.getId(), idEntidad);
            anexosFull.add(anexoFull);
        }
        //Asignamos los documentos recuperados de custodia al registro de salida.
        registroSalida.getRegistroDetalle().setAnexosFull(anexosFull);
        return registroSalida;
    }

}