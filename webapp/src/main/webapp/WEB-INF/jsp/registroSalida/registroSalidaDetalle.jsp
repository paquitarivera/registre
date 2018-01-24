<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<!DOCTYPE html>
<html lang="ca">
<head>
    <title><spring:message code="regweb.titulo"/></title>
    <c:import url="../modulos/imports.jsp"/>
</head>

<body>

<c:import url="../modulos/menu.jsp"/>

<div class="row-fluid container main">

    <div class="well well-white">
    
        <!-- Miga de pan -->
        <div class="row">
            <div class="col-xs-12">
                <ol class="breadcrumb">
                    <li><a <c:if test="${oficinaActiva.sirEnvio || oficinaActiva.sirRecepcion}">class="azul"</c:if> href="<c:url value="/inici"/>"><i class="fa fa-home"></i> ${oficinaActiva.denominacion}</a></li>
                    <li class="active"><i class="fa fa-pencil-square-o"></i> <spring:message code="registroSalida.registroSalida"/> ${registro.numeroRegistroFormateado}</li>
                    <%--Importamos el menú de avisos--%>
                    <c:import url="/avisos"/>
                </ol>
            </div>
        </div><!-- Fin miga de pan -->

        <div id="mensajes"></div>
    
        <div class="row">

            <div class="col-xs-12">
                <c:import url="../modulos/mensajes.jsp"/>
            </div>

            <!-- Panel Lateral -->
            <div class="col-xs-4">
            
                <div class="panel panel-danger">
                    <div class="panel-heading">
                        <h3 class="panel-title"><i class="fa fa-file-o"></i>
                            <strong> <spring:message code="registroSalida.registroSalida"/> ${registro.numeroRegistroFormateado}</strong>
                        </h3>
                    </div>
                    <div class="panel-body">

                        <%--DETALLE REGISTRO--%>
                        <dl class="detalle_registro">
                            <c:import url="../registro/detalleRegistro.jsp">
                                <c:param name="tipoRegistro" value="salida"/>
                            </c:import>
                        </dl>
            
                    </div>

                    <%--BOTONERA--%>

                    <%--Si no nos encontramos en la misma Oficia en la que se creó el Registro o en su Oficina Responsable, no podemos hacer nada con el--%>
                    <c:if test="${oficinaRegistral}">

                        <%--Botones Justificante y Sello--%>
                        <c:if test="${registro.estado != RegwebConstantes.REGISTRO_PENDIENTE_VISAR && registro.estado != RegwebConstantes.REGISTRO_ANULADO}">
                            <div class="panel-footer center">

                                <%--Si no se ha generado el justificante y el registro es VÁLIDO, muestra el boton para generarlo --%>
                                <c:if test="${idJustificante == null && registro.estado != RegwebConstantes.REGISTRO_ANULADO && puedeEditar}">
                                    <div class="btn-group">
                                        <button type="button" class="btn btn-warning btn-sm dropdown-toggle" data-toggle="dropdown">
                                            <spring:message code="justificante.boton"/> <span class="caret"></span>
                                        </button>
                                        <ul class="dropdown-menu">
                                            <li class="submenu-complet"><a onclick="crearJustificante('<c:url value="/registroSalida/${idRegistro}/justificante/ca"/>')" onmouseover="this.style.cursor='pointer';"><spring:message code="regweb.catalan"/></a></li>
                                            <li class="submenu-complet"><a onclick="crearJustificante('<c:url value="/registroSalida/${idRegistro}/justificante/es"/>')" onmouseover="this.style.cursor='pointer';"><spring:message code="regweb.castellano"/></a></li>
                                        </ul>
                                    </div>
                                </c:if>

                                <%--Si se ha generado el justificante, muestra el boton paras descargarlo --%>
                                <c:if test="${idJustificante != null}">
                                    <div class="btn-group"><button type="button" class="btn btn-success btn-sm" onclick="goTo('<c:url value="/anexo/descargarFirma/${idJustificante}"/>')"><span class="fa fa-download"></span> <spring:message code="justificante.boton"/></button></div>
                                </c:if>


                                <%-- Botón de sello --%>
                                <div class="btn-group"><button type="button" data-toggle="modal" data-target="#selloModal" class="btn btn-warning btn-sm"><spring:message code="sello.imprimir"/></button></div>

                            </div>
                        </c:if>

                    <%--Botón Oficio Remision--%>
                    <c:if test="${registro.estado == RegwebConstantes.REGISTRO_VALIDO}">

                        <c:if test="${oficio.oficioRemision}">
                            <div class="panel-footer center">

                                <c:if test="${oficio.interno}">
                                    <button type="button" onclick="goTo('<c:url value="/oficioRemision/salidasPendientesRemision"/>')" class="btn btn-success btn-sm btn-block">
                                        <spring:message code="oficioRemision.boton.crear.interno"/>
                                    </button>
                                </c:if>
                                <c:if test="${oficio.externo}">
                                    <button type="button" onclick="goTo('<c:url value="/oficioRemision/salidasPendientesRemision"/>')" class="btn btn-success btn-sm btn-block">
                                        <spring:message code="oficioRemision.boton.crear.externo"/>
                                    </button>
                                </c:if>
                                <c:if test="${oficio.sir && oficinaActiva.sirEnvio}">
                                    <c:if test="${empty erroresAnexosSir}">
                                        <c:if test="${empty anexos}">
                                            <button type="button" onclick='javascript:confirmEnvioSinAnexos("<c:url value="/registroSalida/${registro.id}/enviarSir"/>","<spring:message code="regweb.confirmar.envioSIR" htmlEscape="true"/>", "<spring:message code="regweb.anexos.vacio" htmlEscape="true"/>","<spring:message code="regweb.enviar" htmlEscape="true"/>","<spring:message code="regweb.anexos.añadir" htmlEscape="true"/>")' href="javascript:void(0);" class="btn btn-success btn-sm btn-block">
                                                <spring:message code="registroEntrada.enviar.sir"/>
                                            </button>
                                        </c:if>
                                        <c:if test="${not empty anexos}">
                                            <button type="button" onclick="goTo('<c:url value="/registroSalida/${registro.id}/enviarSir"/>')" class="btn btn-success btn-sm btn-block">
                                                <spring:message code="registroEntrada.enviar.sir"/>
                                            </button>
                                        </c:if>
                                    </c:if>

                                    <c:if test="${not empty erroresAnexosSir}">
                                        <button type="button" class="btn btn-success btn-sm btn-block disabled">
                                            <spring:message code="registroEntrada.enviar.sir"/>
                                        </button>
                                    </c:if>
                                </c:if>
                                <c:if test="${oficio.sir && !oficinaActiva.sirEnvio}">
                                    <p class="text-danger">El <strong>destinatario</strong> dispone de una Oficina integrada en SIR, pero la ${oficinaActiva.denominacion} no está integrada en SIR y no se podrá realizar el intercambio.</p>
                                    <button type="button" onclick="goTo('<c:url value="/oficioRemision/salidasPendientesRemision"/>')" class="btn btn-success btn-sm btn-block">
                                        <spring:message code="oficioRemision.boton.crear.externo"/>
                                    </button>
                                </c:if>


                            </div>
                        </c:if>
                    </c:if>

                    </c:if>

                    <div class="panel-footer center">

                        <%--Botón Nueva Salida--%>
                        <div class="btn-group"><button type="button" onclick="goTo('<c:url value="/registroSalida/new"/>')" class="btn btn-danger btn-sm"><spring:message code="registro.boton.nuevo"/></button></div>

                        <%--Si estamos en la Oficina donde se hizo el registro, podemos operar con el--%>
                        <c:if test="${oficinaRegistral}">

                            <%--Botón Editar Registro--%>
                            <c:if test="${registro.estado == RegwebConstantes.REGISTRO_VALIDO && puedeEditar && !tieneJustificante}">
                                <div class="btn-group"><button type="button" onclick="goTo('<c:url value="/registroSalida/${registro.id}/edit"/>')" class="btn btn-warning btn-sm"><spring:message code="registro.boton.editar"/></button></div>
                            </c:if>

                            <%--Botón Activar--%>
                            <c:if test="${registro.estado == RegwebConstantes.REGISTRO_ANULADO && puedeEditar}">
                                <div class="btn-group"><button type="button" onclick='javascript:confirm("<c:url value="/registroSalida/${registro.id}/activar"/>","<spring:message code="regweb.confirmar.activar" htmlEscape="true"/>")' class="btn btn-primary btn-sm"><spring:message code="regweb.activar"/></button></div>
                            </c:if>

                            <%--Botón Visar--%>
                            <c:if test="${registro.estado == RegwebConstantes.REGISTRO_PENDIENTE_VISAR && isAdministradorLibro}">
                                <div class="btn-group"><button type="button" onclick='javascript:confirm("<c:url value="/registroSalida/${registro.id}/visar"/>","<spring:message code="regweb.confirmar.visar" htmlEscape="true"/>")' class="btn btn-success btn-sm"><spring:message code="regweb.visar"/></button></div>
                            </c:if>

                            <%--Botón Anular--%>
                            <c:if test="${(registro.estado == RegwebConstantes.REGISTRO_VALIDO || registro.estado == RegwebConstantes.REGISTRO_PENDIENTE_VISAR) && puedeEditar}">
                                <div class="btn-group"><button type="button" onclick='javascript:confirm("<c:url value="/registroSalida/${registro.id}/anular"/>","<spring:message code="regweb.confirmar.anular" htmlEscape="true"/>")' class="btn btn-danger btn-sm"><spring:message code="regweb.anular"/></button></div>
                            </c:if>

                            <%--Botón reenviar--%>
                            <c:if test="${registro.estado == RegwebConstantes.REGISTRO_RECHAZADO || registro.estado == RegwebConstantes.REGISTRO_REENVIADO}">
                                <div class="btn-group"><button type="button" onclick='javascript:goTo("<c:url value="/registroSalida/${registro.id}/reenviar"/>")' class="btn btn-success btn-sm"><spring:message code="registro.boton.reenviar"/></button></div>
                            </c:if>

                            <%--Botón rectificar--%>
                            <c:if test="${registro.estado == RegwebConstantes.REGISTRO_ANULADO || registro.estado == RegwebConstantes.REGISTRO_RECHAZADO}">
                                <div class="btn-group"><button type="button" onclick='javascript:confirm("<c:url value="/registroSalida/${registro.id}/rectificar"/>","<spring:message code="regweb.confirmar.rectificar" htmlEscape="true"/>")' class="btn btn-danger btn-sm"><spring:message code="registro.boton.rectificar"/></button></div>
                            </c:if>

                        </c:if>

                    </div>
            
                </div>
            
            </div>
            <!-- Fin Panel Lateral -->


            <div class="col-xs-8">

                <ul class="navDanger navDanger-tabs" id="myTab">

                    <li><a href="#general" data-toggle="tab"><i class="fa fa-file-o"></i> General</a></li>
                    <c:if test="${not empty trazabilidades}">
                        <li><a href="#trazabilidad" data-toggle="tab"><i class="fa fa-clock-o fa-fw"></i> <spring:message code="registroEntrada.trazabilidad"/></a></li>
                    </c:if>
                    <c:if test="${not empty historicos && registro.estado != RegwebConstantes.REGISTRO_RESERVA}">
                        <li><a href="#modificaciones" data-toggle="tab"><i class="fa fa-pencil-square-o"></i> <spring:message code="regweb.modificaciones"/></a></li>
                    </c:if>

                </ul>

                <div id="contenido" class="tab-content contentDanger">

                    <div class="tab-pane" id="general">

                        <c:if test="${registro.registroDetalle.tipoDocumentacionFisica != RegwebConstantes.TIPO_DOCFISICA_ACOMPANYA_DOC_REQUERIDA || registro.registroDetalle.tieneAnexos}">
                            <!-- ANEXOS COMPLETO-->
                            <c:if test="${anexosCompleto}">
                                <c:import url="../registro/anexos.jsp">
                                    <c:param name="tipoRegistro" value="salida"/>
                                </c:import>
                            </c:if>

                                <%--ANEXOS SOLO LECTURA--%>
                            <c:if test="${not anexosCompleto}">
                                <c:import url="../registro/anexosLectura.jsp">
                                    <c:param name="tipoRegistro" value="salida"/>
                                </c:import>
                            </c:if>
                        </c:if>

                            <%--INTERESADOS--%>
                        <c:if test="${registro.estado == RegwebConstantes.REGISTRO_VALIDO && oficinaRegistral && puedeEditar && !tieneJustificante}">
                            <c:import url="../registro/interesados.jsp">
                                <c:param name="tipoRegistro" value="salida"/>
                                <c:param name="comunidad" value="${comunidad.codigoComunidad}"/>
                                <c:param name="idRegistroDetalle" value="${registro.registroDetalle.id}"/>
                            </c:import>
                        </c:if>

                            <%--INTERESADOS SOLO LECTURA--%>
                        <c:if test="${registro.estado != RegwebConstantes.REGISTRO_VALIDO || !oficinaRegistral || !puedeEditar || tieneJustificante}">
                            <c:import url="../registro/interesadosLectura.jsp">
                                <c:param name="tipoRegistro" value="salida"/>
                            </c:import>
                        </c:if>

                            <%--EXPONE - SOLICITA--%>
                        <c:if test="${not empty registro.registroDetalle.expone || not empty registro.registroDetalle.solicita}">
                            <c:import url="../registro/exponeSolicita.jsp">
                                <c:param name="tipoRegistro" value="salida"/>
                            </c:import>
                        </c:if>

                    </div>

                        <%--TRAZABILIDAD--%>
                    <c:if test="${not empty trazabilidades}">

                        <div class="tab-pane" id="trazabilidad">

                            <c:import url="../trazabilidad/trazabilidadSalida.jsp"/>

                        </div>
                    </c:if>
                    <!-- MODIFICACIONES REGISTRO -->
                    <c:if test="${not empty historicos && registro.estado != RegwebConstantes.REGISTRO_RESERVA}">
                        <div class="tab-pane" id="modificaciones">
                            <c:import url="../registro/modificaciones.jsp">
                                <c:param name="tipoRegistro" value="salida"/>
                            </c:import>
                        </div>
                    </c:if>

                </div>

            </div>


        </div>
    
    </div>

    <%--SELLO --%>
    <c:import url="../registro/sello.jsp">
        <c:param name="tipoRegistro" value="registroSalida"/>
    </c:import>

</div>

<c:import url="../modulos/pie.jsp"/>

<%--<script type="text/javascript" src="<c:url value="/js/busquedaorganismo.js"/>"></script>--%>
<script type="text/javascript" src="<c:url value="/js/sello.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/repro.js"/>"></script>

<script type="text/javascript">

    // Descarga el justificante si se ha generado manualmente
    window.onload = function descargaJustificante(){
        <c:if test="${param.justificante==true}">
            mensajeSuccess('#mensajes', '<spring:message code="justificante.generando.success" javaScriptEscape='true'/>');
            goTo('<c:url value="/anexo/descargarFirma/${idJustificante}"/>');
        </c:if>
    };

    // Muestra los datos del hitórico seleccionado y oculta el resto
    function comparaRegistros(idHistorico){

        <c:forEach var="historico" items="${historicos}">
        $('#'+${historico.id}).hide();
        </c:forEach>
        var elemento = '#'+idHistorico;
        $(elemento).show();
    }

    function actualizarLocalidad(){
        <c:url var="obtenerLocalidades" value="/rest/obtenerLocalidades" />
        actualizarSelect('${obtenerLocalidades}','#localidad\\.id',$('#provincia\\.id option:selected').val(),$('#localidad\\.id option:selected').val(),false,false);
    }

    /**
     * Genera el Justificante del Registro
     * @param url
     */
    function crearJustificante(url){

        $.ajax({
            url:url,
            type:'POST',
            beforeSend: function(objeto){
                waitingDialog.show('<spring:message code="justificante.generando" javaScriptEscape='true'/>', {dialogSize: 'm', progressType: 'danger'});
            },
            success:function(respuesta){

                if(respuesta.status == 'SUCCESS'){
                    goTo('<c:url value="/registroSalida/${idRegistro}/detalle?justificante=true"/>');

                }else if(respuesta.status == 'FAIL') {
                    mensajeError('#mensajes', respuesta.error);
                    waitingDialog.hide();
                }

            }
        });

    }

</script>

</body>
</html>