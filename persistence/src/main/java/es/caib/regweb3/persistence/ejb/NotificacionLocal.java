package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.Notificacion;
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
public interface NotificacionLocal extends BaseEjb<Notificacion, Long> {

    /**
     * Obtiene el total de notificaciones de un usuario y estado
     * @param idUsuarioEntidad
     * @param idEstado
     * @return
     * @throws Exception
     */
    Long getByEstadoCount(Long idUsuarioEntidad, Long idEstado) throws Exception;

    /**
     * Obtiene las notificaciones de una entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    List<Notificacion> getByEntidad(Long idEntidad) throws Exception;

    /**
     * Búsqueda de notificaciones
     * @param notificacion
     * @param idUsuarioEntidad
     * @return
     * @throws Exception
     */
    Paginacion busqueda(Notificacion notificacion, Long idUsuarioEntidad) throws Exception;

    /**
     * Marca como leída una notificación
     * @param idNotificacion
     * @throws Exception
     */
    void leerNotificacion(Long idNotificacion) throws Exception;

    /**
     * Número de notificaciones nuevas
     * @param idUsuarioEntidad
     * @return
     * @throws Exception
     */
    public Long notificacionesPendientes(Long idUsuarioEntidad) throws Exception;

    /**
     * Elimina las notificaciones de una Entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    Integer eliminarByEntidad(Long idEntidad) throws Exception;

}