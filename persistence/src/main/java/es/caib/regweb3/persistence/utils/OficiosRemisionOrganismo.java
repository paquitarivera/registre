package es.caib.regweb3.persistence.utils;

import es.caib.dir3caib.ws.api.oficina.OficinaTF;
import es.caib.regweb3.model.Organismo;

import java.util.List;

/**
 * Bean para almacenar Registros, de un mismo Organismo,
 * pendientes de realizar un Oficio de Remmision.
 * Created by earrivi on 5/09/14.
 */
public class OficiosRemisionOrganismo {

    Organismo organismo;
    Boolean externo = false;
    Boolean oficinas = false;
    Boolean vigente = false;
    Paginacion paginacion;
    Boolean sir = false;
    List<OficinaTF> oficinasSIR;

    public OficiosRemisionOrganismo() {
    }

    public Organismo getOrganismo() {
        return organismo;
    }

    public void setOrganismo(Organismo organismo) {
        this.organismo = organismo;
    }

    public Boolean getExterno() {
        return externo;
    }

    public void setExterno(Boolean externo) {
        this.externo = externo;
    }

    public Boolean getVigente() {
        return vigente;
    }

    public void setVigente(Boolean vigente) {
        this.vigente = vigente;
    }

    public Paginacion getPaginacion() {
        return paginacion;
    }

    public void setPaginacion(Paginacion paginacion) {
        this.paginacion = paginacion;
    }

    public List<OficinaTF> getOficinasSIR() {
        return oficinasSIR;
    }

    public void setOficinasSIR(List<OficinaTF> oficinasSIR) {
        this.oficinasSIR = oficinasSIR;
    }

    public Boolean getSir() {
        return sir;
    }

    public void setSir(Boolean sir) {
        this.sir = sir;
    }

    public Boolean getOficinas() {
        return oficinas;
    }

    public void setOficinas(Boolean oficinas) {
        this.oficinas = oficinas;
    }
}