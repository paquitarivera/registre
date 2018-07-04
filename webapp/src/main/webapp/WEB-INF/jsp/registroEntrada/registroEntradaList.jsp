<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<!DOCTYPE html>
<html lang="ca">
<head>
    <title><spring:message code="registroEntrada.buscador"/></title>
    <c:import url="../modulos/imports.jsp"/>
    <script type="text/javascript" src="<c:url value="/js/busquedaorganismo.js"/>"></script>
</head>

<body>

<c:import url="../modulos/menu.jsp"/>

<div class="row-fluid container main">

    <div class="well well-white">

        <div class="row">
            <div class="col-xs-12">
                <ol class="breadcrumb">
                    <li><a <c:if test="${loginInfo.oficinaActiva.sirEnvio || loginInfo.oficinaActiva.sirRecepcion}">class="azul"</c:if> href="<c:url value="/inici"/>"><i class="fa fa-home"></i> ${loginInfo.oficinaActiva.denominacion}</a></li>
                    <li class="active"><i class="fa fa-list-ul"></i> <strong><spring:message code="registroEntrada.buscador"/></strong></li>
                    <%--Importamos el menú de avisos--%>
                    <c:import url="/avisos"/>
                </ol>
            </div>
        </div><!-- /.row -->

        <c:import url="../modulos/mensajes.jsp"/>

        <!-- BUSCADOR -->

        <div class="row">

            <div class="col-xs-12">

                <div class="panel panel-info">

                    <div class="panel-heading">
                        <a class="btn btn-info btn-xs pull-right" href="<c:url value="/registroEntrada/new"/>" role="button"><span class="fa fa-plus"></span> <spring:message code="registroEntrada.nuevo"/></a>
                        <h3 class="panel-title">
                        	<i class="fa fa-search"></i><strong>&nbsp;
                        	<spring:message code="registroEntrada.buscador"/></strong>
                        </h3>
                    </div>

                    <c:url value="/registroEntrada/busqueda" var="urlBusqueda" scope="request"/>
                    <!--  con esta opcion tambien funciona  pero depende de  javascript onsubmit="document.charset = 'ISO-8859-1'"-->
                     <form:form modelAttribute="registroEntradaBusqueda" action="${urlBusqueda}"  method="get" cssClass="form-horizontal">

                        <form:hidden path="pageNumber"/>

                        <div class="panel-body">
                        
                        <div class="col-xs-12">
                        
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="registroEntrada.libro.id" rel="ayuda" data-content="<spring:message code="registro.ayuda.libro.busqueda"/>" data-toggle="popover"><span class="text-danger">*</span> <spring:message code="registroEntrada.libro"/></label>
                                </div>
                                <div class="col-xs-8">
                                    <form:select path="registroEntrada.libro.id" items="${librosConsulta}" itemLabel="nombreCompleto" itemValue="id" cssClass="chosen-select"/>
                                </div>
                            </div>
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="registroEntrada.estado" rel="ayuda" data-content="<spring:message code="registro.ayuda.estado.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.estado"/></label>
                                </div>
                                <div class="col-xs-8">
                                    <form:select path="registroEntrada.estado" cssClass="chosen-select">
                                        <form:option value="" label="..."/>
                                        <c:forEach var="estado" items="${estados}">
                                            <form:option value="${estado}"><spring:message code="registro.estado.${estado}"/></form:option>
                                        </c:forEach>
                                    </form:select>
                                </div>
                            </div>
                            
						</div>
						<div class="col-xs-12">
                            
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="registroEntrada.numeroRegistroFormateado" rel="ayuda" data-content="<spring:message code="registro.ayuda.numero.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.numeroRegistro"/></label>
                                </div>
                                <div class="col-xs-8">
                                    <form:input path="registroEntrada.numeroRegistroFormateado" cssClass="form-control"/> <form:errors path="registroEntrada.numeroRegistroFormateado" cssClass="help-block" element="span"/>
                                </div>
                            </div>
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="registroEntrada.registroDetalle.extracto" rel="ayuda" data-content="<spring:message code="registro.ayuda.extracto.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.extracto"/></label>
                                </div>
                                <div class="col-xs-8">
                                    <form:input path="registroEntrada.registroDetalle.extracto" cssClass="form-control" maxlength="200" /> <form:errors path="registroEntrada.registroDetalle.extracto" cssClass="help-block" element="span"/>
                                </div>
                            </div>
                            
						</div>
						<div class="col-xs-12">
                            
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="fechaInicio" rel="ayuda" data-content="<spring:message code="registro.ayuda.inicio.busqueda"/>" data-toggle="popover"><span class="text-danger">*</span> <spring:message code="informe.fechaInicio"/></label>
                                </div>
                                <div class="col-xs-8" id="fechaInicio">
                                    <div class="input-group date no-pad-right">
                                        <form:input path="fechaInicio" type="text" cssClass="form-control"  maxlength="10" placeholder="dd/mm/yyyy" name="fechaInicio"/>
                                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                    </div>
                                    <form:errors path="fechaInicio" cssClass="help-block" element="span"/>

                                </div>
                            </div>
                            <div class="col-xs-6 espaiLinies">
                                <div class="col-xs-4 pull-left etiqueta_regweb">
                                    <label for="fechaFin" rel="ayuda" data-content="<spring:message code="registro.ayuda.fin.busqueda"/>" data-toggle="popover"><span class="text-danger">*</span> <spring:message code="informe.fechaFin"/></label>
                                </div>
                                <div class="col-xs-8" id="fechaFin">
                                    <div class="input-group date no-pad-right">
                                        <form:input type="text" cssClass="form-control" path="fechaFin" maxlength="10" placeholder="dd/mm/yyyy" name="fechaFin"/>
                                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                    </div>
                                    <form:errors path="fechaFin" cssClass="help-block" element="span"/>

                                </div>
                            </div>
                            
                         </div>

                        <%--Comprueba si debe mostrar las opciones desplegadas o no--%>
                        <c:if test="${empty registroEntradaBusqueda.registroEntrada.oficina.id &&
                        empty registroEntradaBusqueda.interessatDoc && empty registroEntradaBusqueda.interessatNom &&
                        empty registroEntradaBusqueda.interessatLli1 && empty registroEntradaBusqueda.interessatLli2 &&
                        empty registroEntradaBusqueda.organDestinatari && empty registroEntradaBusqueda.observaciones &&
                        empty registroEntradaBusqueda.usuario && !registroEntradaBusqueda.anexos}">
                            <div id="demo" class="collapse">
                        </c:if>
                        <c:if test="${not empty registroEntradaBusqueda.registroEntrada.oficina.id ||
                        not empty registroEntradaBusqueda.interessatDoc || not empty registroEntradaBusqueda.interessatNom ||
                        not empty registroEntradaBusqueda.interessatLli1 || not empty registroEntradaBusqueda.interessatLli2 ||
                        not empty registroEntradaBusqueda.organDestinatari || not empty registroEntradaBusqueda.observaciones ||
                        not empty registroEntradaBusqueda.usuario || registroEntradaBusqueda.anexos}">
                            <div id="demo" class="collapse in">
                        </c:if>

                            <div class="col-xs-12">
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="interessatNom" rel="ayuda" data-content="<spring:message code="registro.ayuda.nombre.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.nombreInteresado"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:input  path="interessatNom" cssClass="form-control" maxlength="255"/>
                                        <form:errors path="interessatNom" cssClass="help-block" element="span"/>
                                    </div>
                                </div>
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="interessatLli1" rel="ayuda" data-content="<spring:message code="registro.ayuda.apellido1.busqueda"/>" data-toggle="popover"><spring:message code="interesado.apellido1"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:input path="interessatLli1" cssClass="form-control" maxlength="255"/>
                                        <form:errors path="interessatLli1" cssClass="help-block" element="span"/>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-12">
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="interessatLli2" rel="ayuda" data-content="<spring:message code="registro.ayuda.apellido2.busqueda"/>" data-toggle="popover"><spring:message code="interesado.apellido2"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:input path="interessatLli2" cssClass="form-control" maxlength="255"/>
                                        <form:errors path="interessatLli2" cssClass="help-block" element="span"/>
                                    </div>
                                </div>
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="interessatDoc" rel="ayuda" data-content="<spring:message code="registro.ayuda.documento.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.docInteresado"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:input  path="interessatDoc" cssClass="form-control" maxlength="17"/>
                                        <form:errors path="interessatDoc" cssClass="help-block" element="span"/>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-12">
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="registroEntrada.oficina.id" rel="ayuda" data-content="<spring:message code="registro.ayuda.oficina.busqueda"/>" data-toggle="popover"><spring:message code="registro.oficinaRegistro"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:select path="registroEntrada.oficina.id" cssClass="chosen-select">
                                            <form:option value="" label="..."/>
                                            <c:forEach var="oficinaRegistro" items="${oficinasRegistro}">
                                                <form:option value="${oficinaRegistro.id}">${oficinaRegistro.denominacion}</form:option>
                                            </c:forEach>
                                        </form:select>
                                    </div>
                                </div>
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="organDestinatari" rel="ayuda" data-content="<spring:message code="registro.ayuda.destino.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.organDestinatari"/></label>
                                    </div>
                                    <div class="col-xs-6">
                                        <form:select path="organDestinatari" cssClass="chosen-select">
                                            <form:option value="" label="..."/>
                                            <c:forEach items="${organosDestino}" var="organismo">
                                                <option value="${organismo.codigo}" <c:if test="${registroEntradaBusqueda.organDestinatari == organismo.codigo}">selected="selected"</c:if>>${organismo.denominacion}</option>
                                            </c:forEach>
                                        </form:select>
                                        <form:errors path="organDestinatari" cssClass="help-block" element="span"/>
                                        <form:hidden path="organDestinatariNom"/>
                                    </div>
                                    <div class="col-xs-2 boto-mesOpcions">
                                        <a data-toggle="modal" role="button" href="#modalBuscadorlistaRegEntrada"
                                           onclick="inicializarBuscador('#codNivelAdministracionlistaRegEntrada','#codComunidadAutonomalistaRegEntrada','#provincialistaRegEntrada','#localidadlistaRegEntrada','${loginInfo.oficinaActiva.organismoResponsable.nivelAdministracion.codigoNivelAdministracion}', '${loginInfo.oficinaActiva.organismoResponsable.codAmbComunidad.codigoComunidad}', 'listaRegEntrada' );"
                                           class="btn btn-info btn-sm"><spring:message code="regweb.buscar"/></a>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-12">
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="observaciones" rel="ayuda" data-content="<spring:message code="registro.ayuda.observaciones.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.observaciones"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:input path="observaciones" class="form-control" type="text" value=""/>
                                    </div>
                                </div>
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="usuario" rel="ayuda" data-content="<spring:message code="registro.ayuda.usuario.busqueda"/>" data-toggle="popover"><spring:message code="usuario.usuario"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:select path="usuario" class="chosen-select">
                                            <form:option value="">...</form:option>
                                            <c:forEach items="${usuariosEntidad}" var="usuarioEntidad">
                                                <option value="${usuarioEntidad.usuario.identificador}" <c:if test="${registroEntradaBusqueda.usuario == usuarioEntidad.usuario.identificador}">selected="selected"</c:if>>${usuarioEntidad.usuario.identificador}</option>
                                            </c:forEach>
                                        </form:select>
                                    </div>
                                </div>
                            </div>

                            <div class="col-xs-12">
                                <div class="col-xs-6 espaiLinies">
                                    <div class="col-xs-4 pull-left etiqueta_regweb">
                                        <label for="anexos" rel="ayuda" data-content="<spring:message code="registro.ayuda.anexos.busqueda"/>" data-toggle="popover"><spring:message code="registroEntrada.anexos"/></label>
                                    </div>
                                    <div class="col-xs-8">
                                        <form:checkbox path="anexos"/>
                                    </div>
                                </div>
                                <div class="col-xs-6 espaiLinies"><div class="col-xs-12">&nbsp;</div></div>
                            </div>

                        </div>
                        <div class="col-xs-12 pad-bottom15 mesOpcions">
                            <a class="btn btn-info btn-xs pull-right masOpciones-info" data-toggle="collapse" data-target="#demo">
                                <%--Comprueba si debe mostrar mas opciones o menos--%>
                                <c:if test="${empty registroEntradaBusqueda.registroEntrada.oficina.id && empty registroEntradaBusqueda.interessatDoc && empty registroEntradaBusqueda.interessatNom && empty registroEntradaBusqueda.organDestinatari && empty registroEntradaBusqueda.observaciones && empty registroEntradaBusqueda.usuario && !registroEntradaBusqueda.anexos}">
                                    <span class="fa fa-plus"></span> <spring:message code="regweb.busquedaAvanzada"/>
                                </c:if>
                                <c:if test="${not empty registroEntradaBusqueda.registroEntrada.oficina.id || not empty registroEntradaBusqueda.interessatDoc || not empty registroEntradaBusqueda.interessatNom || not empty registroEntradaBusqueda.organDestinatari || not empty registroEntradaBusqueda.observaciones || not empty registroEntradaBusqueda.usuario || registroEntradaBusqueda.anexos}">
                                    <span class="fa fa-minus"></span> <spring:message code="regweb.busquedaAvanzada"/>
                                </c:if>
                            </a>
                        </div>


					 	<div class="row">

                            <div class="form-group col-xs-12">
                                <div class="col-xs-1 boto-panel center">
                                    <button type="submit" class="btn btn-warning btn-sm" style="margin-left: 15px;">
                                        <spring:message code="regweb.buscar"/>
                                    </button>
                                </div>
                            </div>

						</div>

                    </form:form>

                            <c:if test="${paginacion != null}">

                                <div class="form-group col-xs-12">

                                        <c:if test="${empty paginacion.listado}">
                                            <div class="alert alert-grey alert-dismissable">
                                                <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
                                                <spring:message code="regweb.busqueda.vacio"/> <strong><spring:message code="registroEntrada.registroEntrada"/></strong>
                                            </div>
                                        </c:if>

                                        <c:if test="${not empty paginacion.listado}">

                                            <div class="alert-grey">
                                                <c:if test="${paginacion.totalResults == 1}">
                                                    <spring:message code="regweb.resultado"/> <strong>${paginacion.totalResults}</strong> <spring:message code="registroEntrada.registroEntrada"/>
                                                </c:if>
                                                <c:if test="${paginacion.totalResults > 1}">
                                                    <spring:message code="regweb.resultados"/> <strong>${paginacion.totalResults}</strong> <spring:message code="registroEntrada.registroEntradas"/>
                                                </c:if>

                                                <p class="pull-right"><spring:message code="regweb.pagina"/> <strong>${paginacion.currentIndex}</strong> de ${paginacion.totalPages}</p>
                                            </div>


                                        <div class="table-responsive">

                                                <table class="table table-bordered table-hover table-striped tablesorter">
                                                    <colgroup>
                                                        <col width="80">
                                                        <col>
                                                        <col width="80">
                                                        <col>
                                                        <col>
                                                        <col>
                                                        <col>
                                                        <col>
                                                        <col>
                                                        <col width="125">
                                                    </colgroup>
                                                    <thead>
                                                        <tr>
                                                            <th class="center"><spring:message code="regweb.numero"/></th>
                                                            <th class="center"><spring:message code="registroEntrada.fecha"/></th>
                                                            <th class="center"><spring:message code="registroEntrada.usuario"/></th>
                                                            <th class="center"><spring:message code="registroEntrada.oficina"/></th>
                                                            <th class="center"><spring:message code="organismo.destino"/></th>
                                                            <c:if test="${registroEntradaBusqueda.registroEntrada.estado == 2}">
                                                                <th class="center"><spring:message code="registroEntrada.reserva"/></th>
                                                            </c:if>
                                                            <c:if test="${registroEntradaBusqueda.registroEntrada.estado != 2}">
                                                                <th class="center"><spring:message code="registroEntrada.extracto"/></th>
                                                            </c:if>
                                                            <th class="center"><spring:message code="registroEntrada.estado"/></th>
                                                            <th class="center"><spring:message code="registroEntrada.interesados"/></th>
                                                            <th class="center"><spring:message code="registroEntrada.anexos"/></th>
                                                            <th class="center"><spring:message code="regweb.acciones"/></th>
                                                        </tr>
                                                    </thead>

                                                    <tbody>
                                                        <c:forEach var="registro" items="${paginacion.listado}" varStatus="status">
                                                            <tr>
                                                                <td>${registro.numeroRegistroFormateado}</td>
                                                                <td class="center"><fmt:formatDate value="${registro.fecha}" pattern="dd/MM/yyyy"/></td>
                                                                <td class="center">${registro.usuario.usuario.identificador}</td>
                                                                <td class="center"><label class="no-bold" rel="ayuda" data-content="${registro.oficina.denominacion}" data-toggle="popover">${registro.oficina.codigo}</label></td>
                                                                <td>${(empty registro.destino)? registro.destinoExternoDenominacion : registro.destino.denominacion}</td>
                                                                <c:if test="${registro.estado == RegwebConstantes.REGISTRO_RESERVA}">
                                                                <td>
                                                                    <c:if test="${fn:length(registro.registroDetalle.reserva) <= 40}">
                                                                        ${registro.registroDetalle.reserva}
                                                                    </c:if>
                                                                    <c:if test="${fn:length(registro.registroDetalle.reserva) > 40}">
                                                                        <p rel="reserva" data-content="${registro.registroDetalle.reserva}" data-toggle="popover">${registro.registroDetalle.reservaCorto}</p>
                                                                    </c:if>
                                                                </td>
                                                                </c:if>
                                                                <c:if test="${registro.estado != RegwebConstantes.REGISTRO_RESERVA}">
                                                                    <td>
                                                                        <c:if test="${fn:length(registro.registroDetalle.extracto) <= 40}">
                                                                            <c:out value="${registro.registroDetalle.extracto}" escapeXml="true"/>
                                                                        </c:if>
                                                                        <c:if test="${fn:length(registro.registroDetalle.extracto) > 40}">
                                                                            <p rel="extracto" data-content="<c:out value="${registro.registroDetalle.extracto}" escapeXml="true"/>" data-toggle="popover"><c:out value="${registro.registroDetalle.extractoCorto}" escapeXml="true"/></p>
                                                                        </c:if>
                                                                    </td>
                                                                </c:if>
                                                                <td class="center">
                                                                    <c:import url="../registro/estadosRegistro.jsp">
                                                                        <c:param name="estado" value="${registro.estado}"/>
                                                                        <c:param name="decodificacionTipoAnotacion" value="${registro.registroDetalle.decodificacionTipoAnotacion}"/>
                                                                    </c:import>
                                                                </td>
                                                                <c:if test="${registro.registroDetalle.interesados != null}">
                                                                    <td class="center"><label
                                                                            class="no-bold representante" rel="ayuda"
                                                                            data-content="<c:out value="${registro.registroDetalle.nombreInteresadosHtml}" escapeXml="true"/>"
                                                                            data-toggle="popover"><c:out value="${registro.registroDetalle.totalInteresados}" escapeXml="true"/></label>
                                                                    </td>
                                                                </c:if>
                                                                <c:if test="${registro.registroDetalle.interesados == null}">
                                                                    <td class="center">0</td>
                                                                </c:if>
                                                                <c:if test="${registro.registroDetalle.anexos != null}">
                                                                    <c:if test="${registro.registroDetalle.tieneJustificante}"><td class="center">${fn:length(registro.registroDetalle.anexos)-1}</td></c:if>
                                                                    <c:if test="${!registro.registroDetalle.tieneJustificante}"><td class="center">${fn:length(registro.registroDetalle.anexos)}</td></c:if>
                                                                </c:if>
                                                                <c:if test="${registro.registroDetalle.anexos == null}">
                                                                    <td class="center">0</td>
                                                                </c:if>

                                                                <td class="center">
                                                                    <a class="btn btn-info btn-sm" href="<c:url value="/registroEntrada/${registro.id}/detalle"/>" title="<spring:message code="registroEntrada.detalle"/>"><span class="fa fa-eye"></span></a>
                                                                    <%--Acciones según el estado--%>
                                                                        <%--Si no nos encontramos en la misma Oficia en la que se creó el Registro o en su Oficina Responsable, no podemos hacer nada con el--%>
                                                                    <c:if test="${registro.oficina.id == loginInfo.oficinaActiva.id || registro.oficina.oficinaResponsable.id == loginInfo.oficinaActiva.id}">

                                                                        <%--Botón editar--%>
                                                                        <c:if test="${(registro.estado == RegwebConstantes.REGISTRO_VALIDO || registro.estado == RegwebConstantes.REGISTRO_RESERVA) && puedeEditar && !registro.registroDetalle.tieneJustificante}">
                                                                            <a class="btn btn-warning btn-sm" href="<c:url value="/registroEntrada/${registro.id}/edit"/>" title="<spring:message code="regweb.editar"/>"><span class="fa fa-pencil"></span></a>
                                                                        </c:if>

                                                                        <%--Botón anular--%>
                                                                        <c:if test="${(registro.estado == RegwebConstantes.REGISTRO_VALIDO || registro.estado == RegwebConstantes.REGISTRO_RESERVA || registro.estado == RegwebConstantes.REGISTRO_PENDIENTE_VISAR) && puedeEditar}">
                                                                            <a class="btn btn-danger btn-sm" href="javascript:void(0);" onclick='javascript:confirm("<c:url value="/registroEntrada/${registro.id}/anular"/>","<spring:message code="regweb.confirmar.anular" htmlEscape="true"/>")' title="<spring:message code="regweb.anular"/>"><span class="fa fa-thumbs-o-down"></span></a>
                                                                        </c:if>

                                                                        <%--Botón activar--%>
                                                                        <c:if test="${registro.estado == RegwebConstantes.REGISTRO_ANULADO && puedeEditar}">  <%--Anulado--%>
                                                                            <a class="btn btn-primary btn-sm" onclick='javascript:confirm("<c:url value="/registroEntrada/${registro.id}/activar"/>","<spring:message code="regweb.confirmar.activar" htmlEscape="true"/>")' href="javascript:void(0);" title="<spring:message code="regweb.activar"/>"><span class="fa fa-thumbs-o-up"></span></a>
                                                                        </c:if>

                                                                    </c:if>


                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                </table>


                                            <!-- Paginacion -->
                                            <c:import url="../modulos/paginacionBusqueda.jsp">
                                                <c:param name="entidad" value="registroEntrada"/>
                                            </c:import>

                                    </div>

                                    </c:if>

                                </div>

                            </c:if>


                        </div>
                </div>
            </div>
        </div>

        <!-- FIN BUSCADOR -->

        <!-- Importamos el codigo jsp del modal del formulario para realizar la búsqueda de organismos Destino
             Mediante el archivo "busquedaorganismo.js" se implementa dicha búsqueda -->
            <c:import url="../registro/buscadorOrganismosOficinasREPestanas.jsp">
            <c:param name="tipo" value="listaRegEntrada"/>
        </c:import>


    </div>
</div> <!-- /container -->

<c:import url="../modulos/pie.jsp"/>

<!-- Cambia la imagen de la búsqueda avanzada-->
<script type="text/javascript">

    $("[rel='extracto']").popover({ trigger: 'hover',placement: 'top',container:"body", html:true});
    $("[rel='reserva']").popover({ trigger: 'hover',placement: 'top',container:"body", html:true});

    // Posicionamos el ratón en el campo indicado al cargar el modal
    $('#modalBuscadorlistaRegEntrada').on('shown.bs.modal', function () {
        $('#denominacionlistaRegEntrada').focus();
    });

    var traduccion = new Array();
    traduccion['regweb.busquedaAvanzada'] = "<spring:message code='regweb.busquedaAvanzada' javaScriptEscape='true' />";

    $(function(){
        $("#demo").on("hide.bs.collapse", function(){
            $(".masOpciones-info").html('<span class="fa fa-plus"></span> ' + traduccion['regweb.busquedaAvanzada']);
        });
        $("#demo").on("show.bs.collapse", function(){
            $(".masOpciones-info").html('<span class="fa fa-minus"></span> ' + traduccion['regweb.busquedaAvanzada']);
        });
    });
</script>


</body>
</html>