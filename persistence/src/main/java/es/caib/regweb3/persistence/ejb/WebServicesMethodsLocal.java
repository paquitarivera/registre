package es.caib.regweb3.persistence.ejb;

import es.caib.regweb3.sir.core.utils.FicheroIntercambio;
import es.caib.regweb3.sir.core.utils.Mensaje;

import javax.ejb.Local;

/**
 * Created by Fundacio Bit
 *
 * @author earrivi
 */
@Local
public interface WebServicesMethodsLocal {

    /**
     * Recibe un mensaje de control en formato SICRES3 desde un nodo distribuido
     * @param mensaje
     * @throws Exception
     */
    public void recibirMensajeDatosControl(Mensaje mensaje) throws Exception;

    /**
     * Recibe un fichero de intercambio en formato SICRES3 desde un nodo distribuido
     * @param ficheroIntercambio
     * @throws Exception
     */
    public void recibirFicheroIntercambio(FicheroIntercambio ficheroIntercambio) throws Exception;
}
