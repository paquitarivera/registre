<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<!DOCTYPE html>
<html lang="ca">
<head>
    <title><spring:message code="regweb.titulo"/></title>
    <c:import url="modulos/imports.jsp"/>
</head>

    <body>

        <c:import url="modulos/menu.jsp"/>

        <div class="row-fluid container main">

            <div class="well well-white">

            <c:import url="modulos/mensajes.jsp"/>

                <div class="row">

                    <div class="col-xs-12">

                        <ol class="breadcrumb">
                            <c:import url="modulos/migadepan.jsp">
                                <c:param name="avisos" value="false"/>
                            </c:import>
                        </ol>

                        <c:if test="${rolAutenticado.nombre == 'RWE_SUPERADMIN' || rolAutenticado.nombre == 'RWE_ADMIN'}">
                            <c:if test="${catalogo == null}">
                                <div class="alert alert-danger">
                                    <strong><spring:message code="regweb.aviso"/>: </strong> <spring:message code="catalogoDir3.catalogo.vacio"/>
                                </div>
                            </c:if>
                        </c:if>

                    </div>

                    <%--REGISTROS DE ENTRADA PENDIENTES (RESERVA)--%>
                    <c:if test="${not empty pendientes}">
                        <div class="col-xs-6">
                            <div class="panel panel-warning">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="registroEntrada.pendientes"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col width="80">
                                                <col>
                                                <col>
                                                <col>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                            <tr>
                                                <th><spring:message code="registroEntrada.numeroRegistro"/></th>
                                                <th><spring:message code="registroEntrada.fecha"/></th>
                                                <th><spring:message code="registroEntrada.libro.corto"/></th>
                                                <th><spring:message code="registroEntrada.usuario"/></th>
                                                <th><spring:message code="registroEntrada.reserva"/></th>
                                                <th class="center"><spring:message code="regweb.acciones"/></th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="registroEntrada" items="${pendientes}" varStatus="status">
                                                <tr>
                                                    <td>${registroEntrada.numeroRegistroFormateado}</td>
                                                    <td><fmt:formatDate value="${registroEntrada.fecha}" pattern="dd/MM/yyyy"/></td>
                                                    <td>${registroEntrada.libro}</td>
                                                    <td>${registroEntrada.usuario}</td>
                                                    <td>${registroEntrada.extracto}</td>
                                                    <td class="center">
                                                        <a class="btn btn-info btn-sm" href="<c:url value="/registroEntrada/${registroEntrada.id}/detalle"/>" title="<spring:message code="registroEntrada.detalle"/>"><span class="fa fa-eye"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <%--REGISTROS DE ENTRADA PENDIENTES DE VISAR--%>
                    <%--<c:if test="${not empty pendientesVisar}">
                        <div class="col-xs-6">
                            <div class="panel panel-primary">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="registroEntrada.pendientesVisar"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col width="80">
                                                <col>
                                                <col>
                                                <col>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                            <tr>
                                                <th><spring:message code="registroEntrada.numeroRegistro"/></th>
                                                <th><spring:message code="registroEntrada.fecha"/></th>
                                                <th><spring:message code="registroEntrada.libro.corto"/></th>
                                                <th><spring:message code="registroEntrada.usuario"/></th>
                                                <th><spring:message code="registroEntrada.extracto"/></th>
                                                <th class="center"><spring:message code="regweb3.acciones"/></th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="registroEntrada" items="${pendientesVisar}" varStatus="status">
                                                <tr>
                                                    <td>${registroEntrada.numeroRegistroFormateado}</td>
                                                    <td><fmt:formatDate value="${registroEntrada.fecha}" pattern="dd/MM/yyyy"/></td>
                                                    <td>${registroEntrada.libro}</td>
                                                    <td>${registroEntrada.usuario}</td>
                                                    <td>${registroEntrada.extracto}</td>
                                                    <td class="center">
                                                        <a class="btn btn-info btn-sm" href="<c:url value="/registroEntrada/${registroEntrada.id}/detalle"/>" title="<spring:message code="registroEntrada.detalle"/>"><span class="fa fa-eye"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>--%>

                    <%--OFICIONS PENDIENTES DE LLEGADA--%>
                    <c:if test="${not empty oficiosPendientesLlegada}">
                        <div class="col-xs-6">

                            <div class="panel panel-success">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="oficioRemision.pendientesLlegada.ultimos"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col>
                                                <col>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                                <tr>
                                                    <th><spring:message code="oficioRemision.fecha"/></th>
                                                    <th><spring:message code="oficioRemision.oficina"/></th>
                                                    <th><spring:message code="oficioRemision.organismoDestino"/></th>
                                                    <th class="center"><spring:message code="regweb.acciones"/></th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="oficioRemision" items="${oficiosPendientesLlegada}" end="5">
                                                <tr>
                                                    <td><fmt:formatDate value="${oficioRemision.fecha}" pattern="dd/MM/yyyy"/></td>
                                                    <td>${oficioRemision.oficina.denominacion}</td>
                                                    <td>${(empty oficioRemision.organismoDestinatario)? oficioRemision.destinoExternoDenominacion : oficioRemision.organismoDestinatario.denominacion}</td>
                                                    <td class="center">
                                                        <a class="btn btn-success btn-sm" href="<c:url value="/oficioRemision/${oficioRemision.id}/procesar"/>" title="<spring:message code="oficioRemision.procesar"/>"><span class="fa fa-check"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>

                                            </tbody>
                                        </table>

                                    </div>

                                </div>
                            </div>

                        </div>
                    </c:if>

                    <%--OFICIONS PENDIENTES DE REMISIÓN INTERNA--%>
                    <c:if test="${not empty organismosOficioRemisionInterna}">
                        <div class="col-xs-6">

                            <div class="panel panel-success">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="oficioRemision.organimosInterno.inicio"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                            <tr>
                                                <th><spring:message code="organismo.organismo"/></th>
                                                <th class="center"><spring:message code="regweb.acciones"/></th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="organismo" items="${organismosOficioRemisionInterna}">
                                                <tr>
                                                    <td>${organismo}</td>
                                                    <td class="center">
                                                        <a class="btn btn-success btn-sm" href="<c:url value="/oficioRemision/pendientesRemisionInterna"/>" title="<spring:message code="oficioRemision.buscador"/>"><span class="fa fa-search"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>

                                            </tbody>
                                        </table>

                                    </div>

                                </div>
                            </div>

                        </div>
                    </c:if>

                    <%--OFICIONS PENDIENTES DE REMISIÓN EXTERNA--%>
                    <c:if test="${not empty organismosOficioRemisionExterna}">
                        <div class="col-xs-6">

                            <div class="panel panel-success">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="oficioRemision.organimosExterna.inicio"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                            <tr>
                                                <th><spring:message code="organismo.organismo"/></th>
                                                <th class="center"><spring:message code="regweb.acciones"/></th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="organismo" items="${organismosOficioRemisionExterna}">
                                                <tr>
                                                    <td>${organismo}</td>
                                                    <td class="center">
                                                        <a class="btn btn-success btn-sm" href="<c:url value="/oficioRemision/pendientesRemisionExterna"/>" title="<spring:message code="oficioRemision.buscador"/>"><span class="fa fa-search"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>

                                            </tbody>
                                        </table>

                                    </div>

                                </div>
                            </div>

                        </div>
                    </c:if>

                    <%--PREREGISTROS PENDIENTES DE PROCESAR--%>
                    <c:if test="${not empty asientosRegistralesSir}">
                        <div class="col-xs-6">

                            <div class="panel panel-warning">
                                <div class="panel-heading">
                                    <h3 class="panel-title"><i class="fa fa-search"></i> <strong><spring:message code="asientoRegistralSir.pendientesProcesar"/></strong> </h3>
                                </div>

                                <div class="panel-body">

                                    <div class="table-responsive-inici">

                                        <table class="table1 table-bordered table-hover table-striped tablesorter">
                                            <colgroup>
                                                <col>
                                                <col>
                                                <col>
                                                <col>
                                                <col width="51">
                                            </colgroup>
                                            <thead>
                                            <tr>
                                                <th><spring:message code="asientoRegistralSir.numero"/></th>
                                                <th><spring:message code="asientoRegistralSir.fecha"/></th>
                                                <th><spring:message code="asientoRegistralSir.oficinaDestino"/></th>
                                                <th><spring:message code="asientoRegistralSir.extracto"/></th>
                                                <th class="center"><spring:message code="regweb.acciones"/></th>
                                            </tr>
                                            </thead>

                                            <tbody>
                                            <c:forEach var="asientoRegistralSir" items="${asientosRegistralesSir}">
                                                <tr>
                                                    <td>${asientoRegistralSir.numeroRegistro}</td>
                                                    <td><fmt:formatDate value="${asientoRegistralSir.fechaRegistro}" pattern="dd/MM/yyyy"/></td>
                                                    <td>${asientoRegistralSir.decodificacionEntidadRegistralDestino}</td>
                                                    <td>${asientoRegistralSir.resumen}</td>
                                                    <td class="center">
                                                        <a class="btn btn-info btn-sm" href="<c:url value="/asientoRegistralSir/${asientoRegistralSir.id}/detalle"/>" title="<spring:message code="asientoRegistralSir.detalle"/>"><span class="fa fa-eye"></span></a>
                                                    </td>
                                                </tr>
                                            </c:forEach>

                                            </tbody>
                                        </table>

                                    </div>

                                </div>
                            </div>

                        </div>
                    </c:if>




                </div><!-- /.row -->



            </div><!-- /.well-white -->
        </div> <!-- /container -->

        <c:import url="modulos/pie.jsp"/>

    </body>
</html>