package es.caib.regweb3.utils;

import org.apache.log4j.Logger;


/**
 * 
 * @author anadal
 * 
 */
public class Configuracio implements RegwebConstantes {

	protected static final Logger log = Logger.getLogger(Configuracio.class);

    public static boolean isCAIB() {
      return Boolean.getBoolean(REGWEB3_PROPERTY_BASE + "iscaib");
    }

    public static String getUrlPreregistre() {
        return System.getProperty(REGWEB3_PROPERTY_BASE + "preregistre");
    }
  
  
    public static boolean showTimeStamp() {
    return Boolean.getBoolean(REGWEB3_PROPERTY_BASE + "showtimestamp");
  }

  
    public static boolean isDevelopment() {
    return Boolean.getBoolean(REGWEB3_PROPERTY_BASE + "development");
  }


    public static String getHibernateDialect() {
      return  System.getProperty(RegwebConstantes.REGWEB3_PROPERTY_BASE + "hibernate.dialect");
    }

    public static String getDefaultLanguage() {
      String def = System.getProperty(REGWEB3_PROPERTY_BASE + "defaultlanguage");
      return (def == null)? RegwebConstantes.IDIOMA_CATALAN_CODIGO : def;
    }

    public static String getSirServerBase() {
      return  System.getProperty(RegwebConstantes.REGWEB3_PROPERTY_BASE + "sir.serverbase");
    }

    public static String getConsvUsername() {
      return System.getProperty(REGWEB3_PROPERTY_BASE + "concsv.username");
    }

    public static String getConsvPassword() {
      return System.getProperty(REGWEB3_PROPERTY_BASE + "concsv.password");
    }

}