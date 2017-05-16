<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<div class="timeline-badge success"><i class="fa fa-envelope-o"></i></div>

<div class="timeline-panel">
    <div class="timeline-heading">
        <h4 class="timeline-title">
            <a href="<c:url value="/oficioRemision/${oficioRemision.id}/detalle"/>">
                <c:if test="${oficioRemision.sir == false}">
                    <spring:message code="oficioRemision.oficioRemision"/>
                </c:if>
                <c:if test="${oficioRemision.sir == true}">
                    <spring:message code="oficioRemision.oficioRemision.sir"/>
                </c:if>
                <fmt:formatDate value="${oficioRemision.fecha}" pattern="yyyy"/> / ${oficioRemision.numeroOficio}
            </a>
        </h4>
        <p><small class="text-muted"><i class="fa fa-clock-o"></i> <fmt:formatDate value="${oficioRemision.fecha}" pattern="dd/MM/yyyy HH:mm:ss"/></small></p>
    </div>
    <div class="timeline-body">

        <p> <%--Oficina origen--%>
            <small><i class="fa fa-exchange"></i> <strong><spring:message code="oficioRemision.oficina"/>:</strong>
                ${oficioRemision.oficina.denominacion}
            </small>
        </p>
        <p> <%--Organismo destino--%>
            <small><i class="fa fa-exchange"></i> <strong><spring:message code="oficioRemision.organismoDestino"/>:</strong>
                <c:if test="${not empty oficioRemision.organismoDestinatario}">${oficioRemision.organismoDestinatario.denominacion}</c:if>
                <c:if test="${empty oficioRemision.organismoDestinatario}">${oficioRemision.destinoExternoDenominacion}</c:if>
            </small>
        </p>
        <p> <%--Tipo oficio--%>
            <small><i class="fa fa-sign-in"></i> <strong><spring:message code="oficioRemision.tipo"/>:</strong>
                <c:if test="${oficioRemision.tipoOficioRemision == RegwebConstantes.TIPO_OFICIO_REMISION_ENTRADA}">
                    <span class="label label-info">
                        <spring:message code="oficioRemision.tipo.1"/> - <c:if test="${not empty oficioRemision.organismoDestinatario}"><spring:message code="oficioRemision.interno"/></c:if>
                    <c:if test="${empty oficioRemision.organismoDestinatario}"><spring:message code="oficioRemision.externo"/></c:if>
                    </span>
                </c:if>

                <c:if test="${oficioRemision.tipoOficioRemision == RegwebConstantes.TIPO_OFICIO_REMISION_SALIDA}">
                    <span class="label label-danger">
                        <spring:message code="oficioRemision.tipo.2"/> - <c:if test="${not empty oficioRemision.organismoDestinatario}"><spring:message code="oficioRemision.interno"/></c:if>
                    <c:if test="${empty oficioRemision.organismoDestinatario}"><spring:message code="oficioRemision.externo"/></c:if>
                    </span>
                </c:if>

            </small>
        </p>
        <p> <%--Estado oficio--%>
            <small><i class="fa fa-bookmark"></i> <strong><spring:message code="oficioRemision.estado"/>:</strong>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_INTERNO_ENVIADO}"><span class="label label-success"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_EXTERNO_ENVIADO}"><span class="label label-success"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_ACEPTADO}"><span class="label label-success"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_ENVIADO}"><span class="label label-warning"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_ENVIADO_ACK}"><span class="label label-success"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_ENVIADO_ERROR}"><span class="label label-danger"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_REENVIADO}"><span class="label label-warning"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_REENVIADO_ACK}"><span class="label label-warning"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_REENVIADO_ERROR}"><span class="label label-danger"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_RECHAZADO}"><span class="label label-warning"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_RECHAZADO_ACK}"><span class="label label-success"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_RECHAZADO_ERROR}"><span class="label label-danger"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_DEVUELTO}"><span class="label label-danger"></c:if>
                <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_ANULADO}"><span class="label label-danger"></c:if>

                <spring:message code="oficioRemision.estado.${oficioRemision.estado}"/> - <fmt:formatDate value="${oficioRemision.fechaEstado}" pattern="dd/MM/yyyy HH:mm:ss"/>

                </span>
            </small>
        </p>
        <%--OficioRemision SIR--%>
        <c:if test="${oficioRemision.sir == true}">
            <%--Código y descripción error--%>
            <c:if test="${oficioRemision.estado == RegwebConstantes.OFICIO_SIR_ENVIADO_ERROR ||
                          oficioRemision.estado == RegwebConstantes.OFICIO_SIR_REENVIADO_ERROR ||
                          oficioRemision.estado == RegwebConstantes.OFICIO_SIR_RECHAZADO_ERROR}">
                <p>
                    <small><i class="fa fa-comment"></i> <strong><spring:message code="registroSir.descripcionError"/>:</strong> ${oficioRemision.descripcionError}</small>
                </p>

            </c:if>
            <%--Reintentos--%>
            <p>
                <c:if test="${oficioRemision.numeroReintentos > 0}">
                    <small><i class="fa fa-retweet"></i> <strong><spring:message code="oficioRemision.reintentos"/>:</strong> ${oficioRemision.numeroReintentos}</small>
                </c:if>
            </p>
            <%--Identificador intercambio--%>
            <p>
                <small><i class="fa fa-qrcode"></i> <strong><spring:message code="registroSir.identificadorIntercambio"/>:</strong> ${oficioRemision.identificadorIntercambio}</small>
            </p>
        </c:if>

    </div>
</div>