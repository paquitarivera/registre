package es.caib.regweb3.webapp.scheduler;


import es.caib.regweb3.persistence.ejb.SchedulerLocal;
import es.caib.regweb3.utils.Configuracio;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.ejb.EJB;

/**
 * Created by Fundació BIT.
 *
 * @author earrivi
 * Date: 20/03/2018
 */
@Service
public class Regweb3Scheduler {

    protected final Logger log = Logger.getLogger(getClass());

    @EJB(mappedName = "regweb3/SchedulerEJB/local")
    private SchedulerLocal schedulerEjb;


    /**
     * Qué hace: Averigua y asigna el próximo evento de los registros que no lo tienen definido
     * Cuando lo hace: cada 4 minutos
     */
    @Scheduled(cron = "0 0/4 * * * *")
    public void actualizarProximoEventoRegistrosEntrada(){

        try {

            schedulerEjb.actualizarProximoEventoRegistrosEntrada();

        } catch (Exception e) {
            log.info("-- Error Scheduler: actualizarProximoEventoRegistrosEntrada --");
            e.printStackTrace();
        }

    }

    /**
     * Qué hace: Averigua y asigna el próximo evento de los registros que no lo tienen definido
     * Cuando lo hace: cada 6 minutos
     */
    @Scheduled(cron = "0 0/6 * * * *")
    public void actualizarProximoEventoRegistrosSalida(){

        try {

            schedulerEjb.actualizarProximoEventoRegistrosSalida();

        } catch (Exception e) {
            log.info("-- Error Scheduler: actualizarProximoEventoRegistrosSalida --");
            e.printStackTrace();
        }

    }


    /**
     * Qué hace: Realiza tareas administrativas generales de la aplicación
     * Cuando lo hace: Todos días, a las 01:00 h.
     */
    @Scheduled(cron = "0 0 1 * * *") // 0 0 1 * * * Cada día a las 01:00h
    public void tareasAdministrativas(){
        try {
            schedulerEjb.purgarIntegraciones();

        } catch (Exception e) {
            log.info("-- Error Scheduler: purgando integraciones --");
            e.printStackTrace();
        }

        try {
            schedulerEjb.purgarAnexosSir();

        } catch (Exception e) {
            log.info("-- Error Scheduler: purgando AnexosSir --");
            e.printStackTrace();
        }

        try {
            schedulerEjb.purgarAnexosDistribuidos();

        } catch (Exception e) {
            log.info("-- Error Scheduler: purgando Anexos Distribuidos --");
            e.printStackTrace();
        }

        try {
            schedulerEjb.purgarAnexosRegistrosConfirmados();

        } catch (Exception e) {
            log.info("-- Error Scheduler: purgando Anexos de registros enviados por Sir y que han sido confirmados --");
            e.printStackTrace();
        }
    }

    /**
     * Qué hace: Genera las comunicaciones automáticas a los usuarios
     * Cuando lo hace: cada Domingo a las 01:00h
     */
    @Scheduled(cron = "0 0 1 * * SUN")
    public void generarComunicaciones(){

        try {
            schedulerEjb.generarComunicaciones();

        } catch (Exception e) {
            log.info("-- Error Scheduler: Generando comunicaciones --");
            e.printStackTrace();
        }

    }

    /**
     * Qué hace: Inicializa a 0 los contadores de todos los libros de todas las entidades
     * Cuando lo hace: Todos los 1 de Enero a las 00:00:00 h.
     */
    @Scheduled(cron = "0 0 0 1 1 ?")
    public void inicializarContadores(){

        try {
            schedulerEjb.reiniciarContadoresEntidad();

        } catch (Exception e) {
            log.info("-- Error Scheduler: reiniciarContadoresEntidad --");
            e.printStackTrace();
        }

    }

    /**
     * Qué hace: Reintenta enviar los Registros sin confirmación o con error
     * Cuando lo hace: en cada hora, se ejecuta a los 15 minutos despues de iniciada la hora y cada 45 minutos
     * que al coincidir con la hora en punto y tener un desplazamiento de 15 minutos, conseguimos que se ejecute a y 15 en cada hora).
     */
    @Scheduled(cron = "0 15/45 * * * *") // {0 0 * * * * Cada hora, cada día} -  {*/60 * * * * * cada 60 secs }
    public void reintentarEnvioSir(){

        try {
            schedulerEjb.reintentarEnviosSinConfirmacion();

        } catch (Exception e) {
            log.info("-- Error Scheduler: reintentarEnviosSinConfirmacion --");
            e.printStackTrace();
        }

        try {
            schedulerEjb.reintentarEnviosConError();

        } catch (Exception e) {
            log.info("-- Error Scheduler: reintentarEnviosConError --");
            e.printStackTrace();
        }

    }


    /**
     * Qué hace: Distribuye los registros que hay en la cola
     * Cuando lo hace: cada 30 minutos
     */
     @Scheduled(cron = "0 0/30 * * * *") // {0 0 * * * * Cada hora, cada día} -  {*/60 * * * * * cada 60 secs }
    public void distribuirRegistrosEnCola(){

        try {

            schedulerEjb.distribuirRegistrosEnCola();

        } catch (Exception e) {
            log.info("-- Error Scheduler: distribuirRegistrosEnCola --");
            e.printStackTrace();
        }

    }

    /**
     * Qué hace: Cierra los expedientes que están en DM del Arxiu del GOIB
     * Cuando lo hace: Desde las 00:00 hasta las 07:00 y desde las 15:00 hasta las 00:00 cada 15 minutos
     */
    @Scheduled(cron = "0 0/15 0,1,2,3,4,5,6,7,15,16,17,18,19,20,21,22,23 * * *") // 0 0/30 15-7 * * *   0 0/30 * * * *
    public void cerrarExpedientes(){
        try {

            if(Configuracio.isCAIB()){ // Solo si es una instalación GOIB
                schedulerEjb.cerrarExpedientes();
            }

        } catch (Exception e) {
            log.info("-- Error Scheduler: cerrarExpedientes --");
            e.printStackTrace();
        }
    }

    /**
     * Scheduler para realizar pruebas que se ejecutará cada 60 segundos
     */
    //@Scheduled(cron = "*/60 * * * * *") // **60 * * * * * cada 60 secs
    public void pruebas(){
        try {

        } catch (Exception e) {
            log.info("-- Error pruebas --");
            e.printStackTrace();
        }
    }

}
