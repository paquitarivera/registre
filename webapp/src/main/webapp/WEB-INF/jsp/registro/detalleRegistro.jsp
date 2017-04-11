<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<dt><i class="fa fa-home"></i> <spring:message code="oficina.oficina"/>: </dt> <dd> ${registro.oficina.denominacion}</dd>
<dt><i class="fa fa-clock-o"></i> <spring:message code="regweb.fecha"/>: </dt> <dd> <fmt:formatDate value="${registro.fecha}" pattern="dd/MM/yyyy HH:mm:ss"/></dd>
<dt><i class="fa fa-book"></i> <spring:message code="libro.libro"/>: </dt> <dd> ${registro.libro.nombre}</dd>
<c:if test="${param.tipoRegistro == 'entrada'}">
    <c:if test="${not empty registro.destino}"> <dt><i class="fa fa-institution"></i> <spring:message code="registroEntrada.organismoDestino"/>: </dt> <dd>${registro.destino.denominacion} <c:if test="${registro.destino.estado.codigoEstadoEntidad != RegwebConstantes.ESTADO_ENTIDAD_VIGENTE}"><span class="label label-danger"><spring:message code="unidad.estado.${registro.destino.estado.codigoEstadoEntidad}" /></span></c:if> </dd></c:if>
    <c:if test="${not empty registro.destinoExternoCodigo}"> <dt><i class="fa fa-institution"></i> <spring:message code="registroEntrada.organismoDestino"/>: </dt> <dd>${registro.destinoExternoDenominacion}</dd></c:if>
</c:if>
<c:if test="${param.tipoRegistro == 'salida'}">
    <dt><i class="fa fa-institution"></i> <spring:message code="registroSalida.origen"/>: </dt> <dd>${registro.origen.denominacion} <c:if test="${registro.origen.estado.codigoEstadoEntidad != RegwebConstantes.ESTADO_ENTIDAD_VIGENTE}"><span class="label label-danger"><spring:message code="unidad.estado.${registro.origen.estado.codigoEstadoEntidad}" /></span></c:if></dd>
</c:if>
<c:if test="${not empty registro.registroDetalle.extracto}"><dt><i class="fa fa-file-text-o"></i> <spring:message code="registroEntrada.extracto"/>: </dt> <dd> ${registro.registroDetalle.extracto}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.reserva}"><dt><i class="fa fa-file-text-o"></i> <spring:message code="registroEntrada.reserva"/>: </dt> <dd> ${registro.registroDetalle.reserva}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.tipoDocumentacionFisica}"><dt><i class="fa fa-file-text-o"></i> <spring:message code="registroEntrada.tipoDocumentacionFisica"/>: </dt> <dd> <spring:message code="tipoDocumentacionFisica.${registro.registroDetalle.tipoDocumentacionFisica}" /></dd></c:if>
<c:if test="${not empty registro.registroDetalle.tipoAsunto}"><dt><i class="fa fa-thumb-tack"></i> <spring:message code="tipoAsunto.tipoAsunto"/>: </dt> <dd> <i:trad value="${registro.registroDetalle.tipoAsunto}" property="nombre"/></dd></c:if>
<c:if test="${not empty registro.registroDetalle.idioma}"><dt><i class="fa fa-bullhorn"></i><spring:message code="registroEntrada.idioma"/>:</dt> <dd> <spring:message code="idioma.${registro.registroDetalle.idioma}"/></dd></c:if>
<c:if test="${not empty registro.registroDetalle.codigoAsunto}"> <dt><i class="fa fa-thumb-tack"></i> <spring:message code="codigoAsunto.codigoAsunto"/>: </dt> <dd> <i:trad value="${registro.registroDetalle.codigoAsunto}" property="nombre"/></dd></c:if>
<c:if test="${not empty registro.registroDetalle.referenciaExterna}"> <dt><i class="fa fa-thumb-tack"></i> <spring:message code="registroEntrada.referenciaExterna"/>: </dt> <dd> ${registro.registroDetalle.referenciaExterna}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.expediente}"> <dt><i class="fa fa-newspaper-o"></i> <spring:message code="registroEntrada.expediente"/>: </dt> <dd> ${registro.registroDetalle.expediente}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.transporte || not empty registro.registroDetalle.numeroTransporte}"> <dt><i class="fa fa-bus"></i> <spring:message code="registroEntrada.transporte"/>: </dt></c:if>  <c:if test="${not empty registro.registroDetalle.transporte}"><dd> <spring:message code="transporte.0${registro.registroDetalle.transporte}"/></c:if>  <c:if test="${not empty registro.registroDetalle.numeroTransporte}">${registro.registroDetalle.numeroTransporte}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.oficinaOrigen}"> <dt><i class="fa fa-home"></i> <spring:message code="registroEntrada.oficinaOrigen"/>: </dt> <dd> ${registro.registroDetalle.oficinaOrigen.denominacion} </dd></c:if>
<c:if test="${not empty registro.registroDetalle.oficinaOrigenExternoCodigo}"> <dt><i class="fa fa-home"></i> <spring:message code="registroEntrada.oficinaOrigen"/>: </dt> <dd> ${registro.registroDetalle.oficinaOrigenExternoDenominacion}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.numeroRegistroOrigen}"> <dt><i class="fa fa-barcode"></i> <spring:message code="registroEntrada.numeroRegistroOrigen"/>: </dt> <dd> ${registro.registroDetalle.numeroRegistroOrigen}</dd></c:if>
<c:if test="${not empty registro.registroDetalle.fechaOrigen}"> <dt><i class="fa fa-clock-o"></i> <spring:message code="registroEntrada.fechaOrigen"/>: </dt> <dd> <fmt:formatDate value="${registro.registroDetalle.fechaOrigen}" pattern="dd/MM/yyyy HH:mm:ss"/></dd></c:if>
<c:if test="${not empty registro.registroDetalle.observaciones}"> <dt><i class="fa fa-file-text-o"></i> <spring:message code="registroEntrada.observaciones"/>: </dt> <dd> ${registro.registroDetalle.observaciones}</dd></c:if>
<dt><i class="fa fa-gears"></i> <spring:message code="registroEntrada.aplicacion"/>: </dt> <dd> ${registro.registroDetalle.aplicacion} ${registro.registroDetalle.version}</dd>
<dt><i class="fa fa-bookmark"></i> <spring:message code="registroEntrada.estado"/>: </dt>
<dd>
    <c:import url="../registro/estadosRegistro.jsp">
        <c:param name="estado" value="${registro.estado}"/>
    </c:import>
</dd>
