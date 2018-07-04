package es.caib.regweb3.webapp.utils;

import es.caib.regweb3.model.Entidad;
import es.caib.regweb3.persistence.utils.PropiedadGlobalUtil;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.webapp.controller.registro.ScanRequestServlet;
import es.caib.regweb3.webapp.security.LoginInfo;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.security.RunAs;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author anadal
 * 
 */
@Component
@RunAs("RWE_USUARI")
public class CommonsMultipartResolver extends
    org.springframework.web.multipart.commons.CommonsMultipartResolver {

  protected final Logger log = Logger.getLogger(getClass());

  public CommonsMultipartResolver() {
    super();
  }

  @Override
  public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request)
      throws MultipartException {
    
    
    if (log.isDebugEnabled()) {
      log.debug("------------ resolveMultipart() -------\n" 
          + ScanRequestServlet.servletRequestInfoToStr(request));
    }
    

    Long maxUploadSize;
    String msgCode;

    // Pujada d'un fitxer
    // Es fa una mescla entre el màxim global i màxim per entitat
    maxUploadSize = getMaxUploadSize(request);

    msgCode = "tamanyfitxerpujatsuperat";

    if (maxUploadSize == null) {
      this.setMaxUploadSize(-1); // -1 = No Limit
    } else {
      this.setMaxUploadSize(maxUploadSize);
    }
    if (log.isDebugEnabled()) {
      log.debug("Tamany de pujada de Fitxers: "
        + (maxUploadSize == null ? "Sense limit" : maxUploadSize));
    }

    try {
      return super.resolveMultipart(request);
    } catch (MaxUploadSizeExceededException musee) {
      throw new RegWebMaxUploadSizeExceededException(musee.getCause(), musee.getMaxUploadSize(), msgCode);
    }

  }

  private Long getMaxUploadSize(HttpServletRequest request) {
    LoginInfo loginInfo = (LoginInfo) request.getSession().getAttribute(RegwebConstantes.SESSION_LOGIN_INFO);
    Entidad entidad = loginInfo.getEntidadActiva();
    Long maxUploadSizeGlobal;
    if(entidad != null) {
       maxUploadSizeGlobal = PropiedadGlobalUtil.getMaxUploadSizeInBytes(entidad.getId());
    }else {
       maxUploadSizeGlobal = PropiedadGlobalUtil.getMaxUploadSizeInBytes();
    }
    if (log.isDebugEnabled()) {
      if (maxUploadSizeGlobal == null) {
        log.debug("No s'ha definit limit de tamany global en la pujada de Fitxers");
      } else {
        log.info("S'ha definit un tamany màxim de pujada global de Fitxers a "
            + maxUploadSizeGlobal + " bytes");
      }
    }
    
    return maxUploadSizeGlobal;
    
    /*  Si algun dia 
    Long maxUploadSizeEntitat;
    try {
      maxUploadSizeEntitat = LoginInfo.getInstance().getEntitat().getMaxUploadSize();
    } catch (Throwable th) {
      maxUploadSizeEntitat = null;
    }

    Long maxUploadSize = PdfUtils.selectMin(maxUploadSizeGlobal, maxUploadSizeEntitat);
    return maxUploadSize;
    */
  }

  

}
