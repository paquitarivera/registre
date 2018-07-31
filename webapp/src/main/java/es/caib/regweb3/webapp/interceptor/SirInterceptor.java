package es.caib.regweb3.webapp.interceptor;

import es.caib.regweb3.model.Rol;
import es.caib.regweb3.utils.RegwebConstantes;
import es.caib.regweb3.webapp.security.LoginInfo;
import es.caib.regweb3.webapp.utils.Mensaje;
import org.apache.log4j.Logger;
import org.fundaciobit.genapp.common.web.i18n.I18NUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by Fundació BIT.
 *
 * Interceptor para el RegistroSir
 *
 * @author jpernia
 * Date: 5/12/14
 */
public class SirInterceptor extends HandlerInterceptorAdapter {

    protected final Logger log = Logger.getLogger(getClass());


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String url = request.getServletPath();
        HttpSession session = request.getSession();
        LoginInfo loginInfo = (LoginInfo) session.getAttribute(RegwebConstantes.SESSION_LOGIN_INFO);
        Rol rolActivo = loginInfo.getRolActivo();

        // Comprobamos que el usuario dispone del Rol RWE_USUARI
        if (!rolActivo.getNombre().equals(RegwebConstantes.ROL_ADMIN)) {
            log.info("Error de rol");
            Mensaje.saveMessageAviso(request, I18NUtils.tradueix("aviso.rol"));
            response.sendRedirect("/regweb3/aviso");
            return false;
        }

        return true;
    }

}