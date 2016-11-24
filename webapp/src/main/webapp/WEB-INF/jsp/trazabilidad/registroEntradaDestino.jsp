<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<div class="timeline-badge info"><i class="fa fa-file-o"></i></div>
<div class="timeline-panel">
    <div class="timeline-heading">
        <h4 class="timeline-title"><spring:message code="registroEntrada.registroEntrada"/> ${registroEntradaDestino.numeroRegistroFormateado}</h4>
        <p><small class="text-muted"><i class="fa fa-clock-o"></i> <fmt:formatDate value="${registroEntradaDestino.fecha}" pattern="dd/MM/yyyy HH:mm:ss"/></small></p>
    </div>
    <div class="timeline-body">
        <p><small><i class="fa fa-exchange"></i> <strong><spring:message code="registroEntrada.oficina"/>:</strong> ${registroEntradaDestino.oficina.denominacion}</small></p>
    </div>
</div>