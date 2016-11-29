package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.HistoricoRegistroSalida;
import es.caib.regweb3.model.RegistroSalida;
import es.caib.regweb3.model.UsuarioEntidad;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import java.util.List;

/**
 * Created by Fundació BIT.
 *
 * @author jpernia
 * Date: 30/10/14
 */
@Local
@RolesAllowed({"RWE_SUPERADMIN","RWE_ADMIN","RWE_USUARI"})
public interface HistoricoRegistroSalidaLocal extends BaseEjb<HistoricoRegistroSalida, Long> {

    public List<HistoricoRegistroSalida> getByRegistroSalida(Long idRegistro) throws Exception;

    /**
     * Crea un HistoricoRegistroSalida según los parámetros
     * @param registroSalida
     * @param usuarioEntidad
     * @param modificacion
     * @param serializar
     * @return
     * @throws Exception
     */
    public HistoricoRegistroSalida crearHistoricoRegistroSalida(RegistroSalida registroSalida, UsuarioEntidad usuarioEntidad, String modificacion, boolean serializar) throws Exception;


    /**
     * Comprueba si un usuario tiene HistoricoRegistroSalida
     *
     * @param idUsuarioEntidad
     * @return
     * @throws Exception
     */
    public Boolean obtenerPorUsuario(Long idUsuarioEntidad) throws Exception;

    /**
     * Elimina las HistoricoRegistroEntrada de una Entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    public Integer eliminarByEntidad(Long idEntidad) throws Exception;


}
