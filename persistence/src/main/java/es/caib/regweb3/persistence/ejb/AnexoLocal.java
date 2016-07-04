package es.caib.regweb3.persistence.ejb;


import es.caib.regweb3.model.Anexo;
import es.caib.regweb3.model.UsuarioEntidad;
import es.caib.regweb3.model.utils.AnexoFull;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;
import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;
import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import java.util.List;

/**
 * Created by Fundacio Bit
 *
 * @author earrivi
 * @author anadal
 * Date: 6/03/13
 */
@Local
@RolesAllowed({"RWE_SUPERADMIN","RWE_ADMIN","RWE_USUARI"})
public interface AnexoLocal extends BaseEjb<Anexo, Long> {

  
  public AnexoFull crearAnexo(AnexoFull anexoFull, UsuarioEntidad usuarioEntidad,
      Long registroID, String tipoRegistro) throws I18NException, I18NValidationException;
    

  
  public AnexoFull getAnexoFull(Long anexoID) throws I18NException;

    /**
     * Método que levanta todos los anexos completo con el archivo de custodia.
     *
     * @param anexoID
     * @return
     * @throws I18NException
     */
    public AnexoFull getAnexoFullCompleto(Long anexoID) throws I18NException;
  
  
  
  public AnexoFull actualizarAnexo(AnexoFull anexoFull, UsuarioEntidad usuarioEntidad,
      Long registroID, String tipoRegistro) throws I18NException, I18NValidationException;
    


    /**
     *
     * @param idRegistro
     * @return
     * @throws Exception
     */
    public List<Anexo> getByRegistroEntrada(Long idRegistro) throws Exception;

    /**
     *
     * @param idRegistro
     * @return
     * @throws Exception
     */
    public List<Anexo> getByRegistroSalida(Long idRegistro) throws Exception;

    /**
     *  Obtiene los anexos de un registroDetalle
     * @param idRegistroDetalle
     * @return
     * @throws Exception
     */
    public List<Anexo> getByRegistroDetalle(Long idRegistroDetalle) throws Exception;

    
    public byte[] getArchivoContent(String custodiaID) throws Exception;
    

    public byte[] getFirmaContent(String custodiaID) throws Exception;
    

    /**
     * Obtiene el fichero existente en el sistema de archivos
     *
     * @param custodiaID
     * @return
     */

    public DocumentCustody getArchivo(String custodiaID) throws Exception;

    /**
     * Obtiene la firma existente en el sistema de archivos
     * @param custodiaID
     * @return
     */

    public SignatureCustody getFirma(String custodiaID) throws Exception;

    /**
     * Elimina completamente una custodia ( = elimicion completa de Anexo)
     *
     * @param custodiaID
     * @return true si l'arxiu no existeix o s'ha borrat. false en els altres
     * casos.
     */
    public boolean eliminarCustodia(String custodiaID) throws Exception;


    /**
     * Crea o actualiza un anexos en el sistema de custodia
     * TODO borrar no se emplea
     *
     * @param name
     * @param file
     * @param signatureName
     * @param signature
     * @param signatureMode
     * @param custodyID         Si vale null significa que creamos el archivo. Otherwise actualizamos el fichero.
     * @param custodyParameters JSON del registre
     * @return Identificador de custodia
     * @throws Exception
     */
    public String crearArchivo(String name, byte[] file, String signatureName,
                               byte[] signature, int signatureMode, String custodyID, String custodyParameters) throws Exception;


}