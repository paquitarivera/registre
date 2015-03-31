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

        <div class="row">
            <div class="col-xs-12">
                <ol class="breadcrumb">
                    <c:import url="../modulos/migadepan.jsp"/>
                    <li class="active"><i class="fa fa-list-ul"></i> <spring:message code="informe.libroRegistro"/></li>
                </ol>
            </div>
        </div><!-- /.row -->


        <!-- BUSCADOR -->
            <div class="row">

                <div class="col-xs-12">

                    <div class="panel panel-success">
                        <div class="panel-heading">
                            <h3 class="panel-title"><i class="fa fa-search"></i><spring:message code="informe.libroRegistro"/> </h3>
                        </div>
                        <div class="panel-body">
                            <form:form modelAttribute="informeLibroBusquedaForm" method="post" cssClass="form-horizontal" name="informeLibroBusquedaForm" onsubmit="return validaFormulario(this)">
                                <div class="row">
                                    <div class="form-group col-xs-6 pad-left">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="tipo"><span class="text-danger">*</span> <spring:message code="informe.tipoLibro"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right">
                                            <form:select path="tipo" cssClass="chosen-select" onchange="actualizarLibros(this)">
                                                <form:option value="1" default="default"><spring:message code="informe.entrada"/></form:option>
                                                <form:option value="2"><spring:message code="informe.salida"/></form:option>
                                            </form:select>
                                        </div>
                                    </div>
                                    <div class="form-group col-xs-6  pad-left">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="formato"><span class="text-danger">*</span> <spring:message code="regweb.formato"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right">
                                            <form:select path="formato" cssClass="chosen-select">
                                                <form:option value="pdf" default="default"><spring:message code="regweb.formato.pdf" /></form:option>
                                                <form:option value="excel"><spring:message code="regweb.formato.excel"/></form:option>
                                            </form:select>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-xs-6 pad-left libros1">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="libros"><span class="text-danger">*</span> <spring:message code="registroEntrada.libro"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right" id="libr">
                                            <c:if test="${fn:length(libros) eq 1}">
                                                <form:select path="libros" items="${libros}" itemValue="id" itemLabel="libroOrganismo" cssClass="chosen-select" multiple="false"/>
                                            </c:if>
                                                <c:if test="${fn:length(libros) gt 1}">
                                                <spring:message code="informe.libros" var="varLibros"/>
                                                <form:select data-placeholder="${varLibros}" path="libros" items="${libros}" itemValue="id" itemLabel="libroOrganismo" cssClass="chosen-select" multiple="true"/>
                                                </c:if>
                                            <span id="librosErrors"></span>
                                        </div>
                                    </div>
                                    <div class="form-group col-xs-6 pad-left campos1">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="campos"><span class="text-danger">*</span> <spring:message code="regweb.campos"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right" id="campos">
                                            <spring:message code="informe.campos" var="varCampos"/>
                                            <form:select data-placeholder="${varCampos}" multiple="true" cssClass="chosen-select" id="campos" path="campos" name="campos">
                                                <form:option value="llibr" selected="selected"><spring:message code="registroEntrada.libro"/></form:option>
                                                <form:option value="ofici" selected="selected"><spring:message code="registroEntrada.oficina"/></form:option>
                                                <form:option value="anyRe" selected="selected"><spring:message code="registroEntrada.anyRegistro"/></form:option>
                                                <form:option value="data" selected="selected"><spring:message code="registroEntrada.dataRegistre"/></form:option>
                                                <form:option value="numRe" selected="selected"><spring:message code="registroEntrada.numeroRegistro"/></form:option>
                                                <form:option value="extra" selected="selected"><spring:message code="registroEntrada.extracto"/></form:option>
                                                <form:option value="tipAs" selected="selected"><spring:message code="registroEntrada.tipoAsunto"/></form:option>
                                                <form:option value="nomIn" selected="selected"><spring:message code="registroEntrada.interesados"/></form:option>
                                                <form:option value="orgOr" selected="selected"><spring:message code="registroEntrada.oficinaOrigen"/></form:option>
                                                <form:option value="numOr" selected="selected"><spring:message code="registroEntrada.numeroRegistroOrigen"/></form:option>
                                                <form:option value="datOr" selected="selected"><spring:message code="registroEntrada.dataOrigen"/></form:option>
                                                <form:option value="orgDe" selected="selected"><spring:message code="registroEntrada.destinoOrigen"/></form:option>
                                                <form:option value="docFi" selected="selected"><spring:message code="registroEntrada.documentacionFisica"/></form:option>
                                                <form:option value="idiom" selected="selected"><spring:message code="registroEntrada.idioma"/></form:option>
                                                <form:option value="obser" selected="selected"><spring:message code="registroEntrada.observaciones"/></form:option>
                                                <form:option value="estat" selected="selected"><spring:message code="registroEntrada.estado"/></form:option>
                                                <form:option value="exped" selected="selected"><spring:message code="registroEntrada.expediente"/></form:option>
                                                <form:option value="codAs"><spring:message code="registroEntrada.codigoAsunto"/></form:option>
                                                <form:option value="refEx"><spring:message code="registroEntrada.referenciaExterna"/></form:option>
                                                <form:option value="trans"><spring:message code="registroEntrada.transporte"/></form:option>
                                                <form:option value="numTr"><spring:message code="registroEntrada.numTransporte"/></form:option>
                                            </form:select>
                                            <span id="camposErrors"></span>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="form-group col-xs-6  pad-left">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="fechaInicio"><span class="text-danger">*</span> <spring:message code="informe.fechaInicio"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right" id="fechaInicio">
                                            <div class="input-group date no-pad-right">
                                                <form:input type="text" cssClass="form-control" path="fechaInicio" maxlength="10" placeholder="dd/mm/yyyy" name="fechaInicio"/>
                                                <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                            </div>
                                            <span class="errors"></span>
                                        </div>
                                    </div>
                                    <div class="form-group col-xs-6  pad-left">
                                        <div class="col-xs-3 pull-left etiqueta_regweb control-label">
                                            <form:label path="fechaFin"><span class="text-danger">*</span> <spring:message code="informe.fechaFin"/></form:label>
                                        </div>
                                        <div class="col-xs-9 no-pad-right" id="fechaFin">
                                            <div class="input-group date no-pad-right">
                                                <form:input type="text" cssClass="form-control" path="fechaFin" maxlength="10" placeholder="dd/mm/yyyy" name="fechaFin"/>
                                                <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                            </div>
                                            <span class="errors"></span>
                                        </div>
                                    </div>
                                </div>
                                <div class="form-group col-xs-12">
                                    <button type="submit" class="btn btn-warning"><spring:message code="regweb.buscar"/></button>
                                </div>

                                <c:set var="errorInicio"><spring:message code="error.fechaInicio.posterior"/></c:set>
                                <input id="error1" type="hidden" value="${errorInicio}"/>
                                <c:set var="errorFin"><spring:message code="error.fechaFin.posterior"/></c:set>
                                <input id="error2" type="hidden" value="${errorFin}"/>
                                <c:set var="errorInicioFin"><spring:message code="error.fechaInicioFin.posterior"/></c:set>
                                <input id="error3" type="hidden" value="${errorInicioFin}"/>

                            </form:form>
                        </div>
                    </div>
                </div>
            </div>
        <!-- FIN BUSCADOR-->


    </div>
</div> <!-- /container -->

<c:import url="../modulos/pie.jsp"/>


<!-- Modifica los Libros según el tipo de Registro elegido -->
<script type="text/javascript">

    function actualizarLibros(){
        <c:url var="obtenerLibros" value="/informe/obtenerLibros" />
        actualizarLibrosTodos('${obtenerLibros}','#libros',$('#tipo option:selected').val(),$('#libros option:selected').val(),true);
    }

</script>

<!-- VALIDADOR DE FORMULARI -->
<script type="text/javascript">

//Valida los libros seleccionados (libros, nombre del libro)
function librosSeleccionados(select, camp) {
    var variable = '';
    var htmlBuit = '';
    // Valor de los libros
    var value = $(select).val();
    var numLibros = 0;
    if (value!=null && value!=""){
        // Número de los libros en el select
        numLibros = value.length;
    }
    // Si hay menos de un libro seleccionado, retorna error
    if (numLibros<1){
        variable = "#" + camp + " span#librosErrors";
        htmlBuit = "<span id='librosErrors' class='help-block'>És obligatori elegir almanco 1 llibre</span>";
        $(variable).html(htmlBuit);
        $(variable).parents(".libros1").addClass("has-error");
        $('ul.chosen-choices').css('border-color','#a94442');
        return false;
    }else{
        variable = "#" + camp + " span:contains('elegir')";
        $(variable).removeClass("help-block");
        $(variable).parents(".libros1").removeClass("has-error");
        htmlBuit = "<span id='librosErrors'></span>";
        $(variable).html(htmlBuit);
        $('ul.chosen-choices').css('border-color','#aaa');
        return true;
    }
}

//Valida los campos seleccionados (campos, nombre del campo)
function camposSeleccionados(select, camp) {
    var variable = '';
    var htmlBuit = '';
    // Valor de los campos
    var value = $(select).val();
    var numCampos = 0;
    if (value!=null && value!=""){
        // Número de los campos en el select
        numCampos = value.length;
    }
    // Si hay menos de dos campos seleccionados, retorna error
    if (numCampos<2){
        variable = "#" + camp + " span#camposErrors";
        htmlBuit = "<span id='camposErrors' class='help-block'>És obligatori elegir almanco 2 camps</span>";
        $(variable).html(htmlBuit);
        $(variable).parents(".campos1").addClass("has-error");
        $('ul.chosen-choices').css('border-color','#a94442');
        return false;
    }else{
        variable = "#" + camp + " span:contains('elegir')";
        $(variable).removeClass("help-block");
        $(variable).parents(".campos1").removeClass("has-error");
        htmlBuit = "<span id='camposErrors'></span>";
        $(variable).html(htmlBuit);
        $('ul.chosen-choices').css('border-color','#aaa');
        return true;
    }
}

// Valida el formuario si las fechas Inicio y Fin son correctas, hay almenos 2 campos seleccionados, hay un Libro seleccionado
function validaFormulario(form) {
    var fechaInicio = true;
    var fechaFin = true;
    var libros = true;
    var campos = true;
    var fechas = true;
    // Valida el formato de Fecha de Inicio
    if (!validaFecha(form.fechaInicio, 'fechaInicio')) {
        fechaInicio = false;
    }
    // Valida el formato de Fecha de Fin
    if (!validaFecha(form.fechaFin, 'fechaFin')) {
        fechaFin = false;
    }
    // Si las Fechas son correctas, Valida el Fecha Inicio y Fecha Fin menor o igual que fecha actual, Fecha Inicio menor o igual que Fecha Fin
    if((fechaInicio)&&(fechaFin)){
        if (!validaFechasConjuntas(form.fechaInicio, form.fechaFin, 'fechaInicio', 'fechaFin')) {
            fechas = false;
        }
    }
    // Valida los libros seleccionados
    if (!librosSeleccionados(form.libros, 'libr')){
        libros = false;
    }
    // Valida los campos seleccionados
    if (!camposSeleccionados(form.campos, 'campos')){
        campos = false;
    }
    // Si todos los campos son correctos, hace el submit
    if((fechaInicio)&&(fechaFin)&&(libros)&&(campos)&&(fechas)){
        return true;
    } else{
        return false;
    }
}

</script>

<script type="text/javascript">
    function actualizarLibrosTodos(url, idSelect, seleccion, valorSelected, todos){
        var html = '';
        if(seleccion != '-1'){
            jQuery.ajax({
                url: url,
                type: 'GET',
                dataType: 'json',
                data: { id: seleccion },
                contentType: 'application/json',
                success: function(result) {
                    if(todos){html = '';}
                    var len = result.length;
                    var selected='';
                    for ( var i = 0; i < len; i++) {
                        selected='';
                        if(result.length == 1){
                            selected = 'selected="selected"';
                        }
                        html += '<option '+selected+' value="' + result[i].id + '">'
                        + result[i].libroOrganismo + '</option>';
                    }
                    html += '</option>';

                    if(len != 0){
                        $(idSelect).html(html);
                        $(idSelect).attr("disabled",false).trigger("chosen:updated");
                    }else if(len==0){
                        var html='';
                        $(idSelect).html(html);
                        $(idSelect).attr("disabled",true).trigger("chosen:updated");
                    }
                }
            });

        }
    }
</script>

</body>
</html>