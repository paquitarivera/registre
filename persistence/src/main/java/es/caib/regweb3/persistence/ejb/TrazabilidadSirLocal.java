package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.TrazabilidadSir;

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
public interface TrazabilidadSirLocal extends BaseEjb<TrazabilidadSir, Long> {

    /**
     * Obtiene las TrazabilidadesSir de un RegistroSir
     * @param idRegistroSir
     * @return
     * @throws Exception
     */
    public List<TrazabilidadSir> getByRegistroSir(Long idRegistroSir) throws Exception;

    /**
     * Eimina todas las TrazabilidadesSir de una Entidad
     * @param idEntidad
     * @return
     * @throws Exception
     */
    public Integer eliminarByEntidad(Long idEntidad) throws Exception;

}
