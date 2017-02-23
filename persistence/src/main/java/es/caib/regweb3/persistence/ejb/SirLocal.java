package es.caib.regweb3.persistence.ejb;

import es.caib.dir3caib.ws.api.oficina.OficinaTF;
import es.caib.regweb3.model.Oficina;
import es.caib.regweb3.model.RegistroEntrada;
import es.caib.regweb3.model.RegistroSalida;
import es.caib.regweb3.model.UsuarioEntidad;
import es.caib.regweb3.model.utils.CamposNTI;
import es.caib.regweb3.sir.core.model.AsientoRegistralSir;
import org.fundaciobit.genapp.common.i18n.I18NException;
import org.fundaciobit.genapp.common.i18n.I18NValidationException;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import java.util.List;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 22/06/16
 */
@Local
@RolesAllowed({"RWE_SUPERADMIN","RWE_ADMIN","RWE_USUARI"})
public interface SirLocal {

  /**
   *
   * @param idRegistroEntrada
   * @param codigoEntidadRegistralDestino
   * @param denominacionEntidadRegistralDestino
   * @param oficinaActiva
   * @param usuario
   * @param idLibro
   * @throws Exception
     * @throws I18NException
     */
  public AsientoRegistralSir enviarFicheroIntercambio(Long idRegistroEntrada, String codigoEntidadRegistralDestino, String denominacionEntidadRegistralDestino, Oficina oficinaActiva, UsuarioEntidad usuario, Long idLibro) throws Exception, I18NException;

  /**
   *
   * @param asientoRegistralSir
   * @param usuario
   * @param oficinaActiva
   * @param idLibro
   * @param idIdioma
   * @param idTipoAsunto
   * @param camposNTIs
     * @return
     */
  public Long aceptarAsientoRegistralSir(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs) throws Exception;

  /**
   * Transforma un {@link es.caib.regweb3.model.RegistroEntrada} en un {@link es.caib.regweb3.sir.core.model.AsientoRegistralSir}
   * @param idRegistroEntrada
   * @param codigoEntidadRegistralDestino
   * @param denominacionEntidadRegistralDestino
   * @return
   * @throws Exception
   * @throws I18NException
   * @throws I18NValidationException
     */
  public AsientoRegistralSir transformarRegistroEntrada(Long idRegistroEntrada, String codigoEntidadRegistralDestino, String denominacionEntidadRegistralDestino)
          throws Exception, I18NException;

  /**
   *
   * @param asientoRegistralSir
   * @param usuario
   * @param oficinaActiva
   * @param idLibro
   * @param idIdioma
   * @param idTipoAsunto
   * @param camposNTIs
   * @return
   * @throws Exception
   * @throws I18NException
   * @throws I18NValidationException
     */
  public RegistroEntrada transformarAsientoRegistralEntrada(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs) throws Exception, I18NException, I18NValidationException;

  /**
   * Transforma un {@link es.caib.regweb3.model.RegistroEntrada} en un {@link es.caib.regweb3.sir.core.model.AsientoRegistralSir}
   * @param registroSalida
   * @param oficinaSir
   * @return
   * @throws Exception
   * @throws I18NException
   * @throws I18NValidationException
   */
  public AsientoRegistralSir transformarRegistroSalida(RegistroSalida registroSalida, OficinaTF oficinaSir, String codigoUnidadTramitacionDestino, String decodificacionUnidadTramitacionDestino)
          throws Exception, I18NException, I18NValidationException;

  /**
   *
   * @param asientoRegistralSir
   * @param usuario
   * @param oficinaActiva
   * @param idLibro
   * @param idIdioma
   * @param idTipoAsunto
   * @param camposNTIs
   * @return
   * @throws Exception
   * @throws I18NException
   * @throws I18NValidationException
   */
  public RegistroSalida transformarAsientoRegistralSalida(AsientoRegistralSir asientoRegistralSir, UsuarioEntidad usuario, Oficina oficinaActiva, Long idLibro, Long idIdioma, Long idTipoAsunto, List<CamposNTI> camposNTIs) throws Exception, I18NException, I18NValidationException;

}

