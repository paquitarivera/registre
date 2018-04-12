package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.Integracion;
import es.caib.regweb3.persistence.utils.Paginacion;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import java.util.List;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 16/01/14
 */
@Local
@RolesAllowed({"RWE_SUPERADMIN","RWE_ADMIN","RWE_USUARI"})
public interface IntegracionLocal extends BaseEjb<Integracion, Long> {

    /**
     * Obtiene las integraciones de una entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    List<Integracion> getByEntidad(Long idEntidad) throws Exception;

    /**
     * Búsqueda de integraciones
     * @param pageNumber
     * @param idEntidad
     * @param tipo
     * @return
     * @throws Exception
     */
    Paginacion busqueda(Integer pageNumber, Long idEntidad, Long tipo) throws Exception;

    /**
     *
     * @param tipo
     * @param descripcion
     * @param peticion
     * @param tiempo
     * @param idEntidad
     * @throws Exception
     */
    void addIntegracionOk(Long tipo, String descripcion, String peticion, Long tiempo, Long idEntidad) throws Exception;

    /**
     *
     * @param tipo
     * @param descripcion
     * @param peticion
     * @param th
     * @param tiempo
     * @param idEntidad
     * @throws Exception
     */
    void addIntegracionError(Long tipo, String descripcion, String peticion, Throwable th, Long tiempo, Long idEntidad) throws Exception;

    /**
     * Elimina las Integraciones con una antigüedad de 7 días
     * @param idEntidad
     * @return
     * @throws Exception
     */
    Integer purgarIntegraciones(Long idEntidad) throws Exception;

    /**
     * Elimina las integraciones de una Entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    Integer eliminarByEntidad(Long idEntidad) throws Exception;

}