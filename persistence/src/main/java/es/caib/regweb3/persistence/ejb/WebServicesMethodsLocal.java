package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.model.PreRegistro;

import javax.ejb.Local;

/**
 * Created by Fundacio Bit
 *
 * @author earrivi
 */
@Local
public interface WebServicesMethodsLocal {

    public PreRegistro crearPreRegistro(PreRegistro preRegistro) throws Exception;
}
