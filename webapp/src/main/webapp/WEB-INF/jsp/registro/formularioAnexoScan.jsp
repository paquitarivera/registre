<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<!DOCTYPE html>
<html lang="ca" >
<head>
    <title><spring:message code="regweb.titulo"/></title>
    <c:import url="../modulos/imports.jsp"/>
    
    <c:if test="${teScan}">
      ${headerScan}
    </c:if>
</head>

<body style="background-image:none !important;">

<c:if test="${not empty closeAndReload}">

<script type="text/javascript" >

  parent.closeAndReload();

</script>


</c:if>

<c:if test="${empty closeAndReload}">

<c:import url="../modulos/mensajes.jsp"/>

<%-- Formulario que contiene el resto de campos del anexo. --%>
<form:form id="anexoForm" action="${pageContext.request.contextPath}/anexoScan/new" modelAttribute="anexoForm" method="POST"  enctype="multipart/form-data">

            <form:hidden path="anexo.id" />
            <form:hidden path="anexo.registroDetalle.id" />
            <form:hidden path="anexo.custodiaID" />
            <form:hidden path="anexo.fechaCaptura" />

            <form:hidden path="registroID" />
            <form:hidden path="tipoRegistro" />
            <form:hidden path="oficioRemisionSir" />

            <div class="clearfix"></div>
               
            <c:if test="${teScan}">
                <div class="pull-right" style="margin-top: 0px; ">
                    <button id="desaAnnex" type="submit" class="btn btn-warning btn-sm" onclick="$('#reload').show();"><spring:message code="regweb.guardar"/></button>
                </div>
            </c:if>


            <c:if test="${teScan}">
                <div class="tab-pane" id="scan">
                    <iframe src="${urlToPluginWebPage}" style="background-color: white; min-height:200px" frameborder='0' width="100%" height="400px"  id="myiframe" scrolling="auto">
                        <p>NO IFRAME</p>
                    </iframe>
                      <script type="text/javascript">

                        var lastSize = 0;

                        function checkIframeSize() {

                            setTimeout(checkIframeSize, 1000);

                            var iframe = document.getElementById('myiframe');

                            var iframeDocument = iframe.contentDocument || iframe.contentWindow.document;

                            var h1 = $(iframeDocument.body).height();
                            var h2 = iframeDocument.body.scrollHeight;
                            //var h3 = $("#tablefull").height();

                            var h = Math.max(h1,h2);

                            var log = false;

                            var d = new Date();
                            if (log) {
                                console.log("================ " + d + " (H = " + h +" | H1= " + h1 + " | H2= " + h2 + ") ===================");
                            }

                            if (h != lastSize) {
                                h = h + 100;
                                lastSize = h;
                                if (log) {
                                  console.log(" checkIframeSize()::iframeDocument.body.scrollHeight = " + iframeDocument.body.scrollHeight);
                                  console.log(" checkIframeSize()::$(iframeDocument.body).height() = " + $(iframeDocument.body).height());
                                  console.log(" checkIframeSize()::$(TABLE).height() = " + $("#tablefull").height());
                                  console.log(" checkIframeSize():: SET " + h);
                                }
                                document.getElementById('myiframe').style.height=h + "px";
                                lastSize =  Math.max($(iframeDocument.body).height(),iframeDocument.body.scrollHeight); <%--  $("#tablefull").height() --%>
                                if (log) {
                                  console.log(" checkIframeSize():: GET " + lastSize);
                                }
                            }
                        }

                        $(document).ready(function ()  {
                            setTimeout(checkIframeSize, 1000);
                          });

                    </script>

                </div>
            </c:if>
                    

                    

                
            <div class="hide col-xs-12 text-center centrat" id="reload">
                <img src="<c:url value="/img/712.GIF"/>" width="20" height="20"/>
            </div>


</form:form>

</c:if>


<!-- INICI JAVASCRIPT INCLOS DEL PEU -->
<%@ include file="/WEB-INF/jsp/modulos/includes.jsp" %>

<!-- JavaScript -->
<script type="text/javascript" src="<c:url value="/js/bootstrap.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/bootstrap-dropdown-on-hover-plugin.js"/>"></script>
<!-- DateTimePicker -->
<script type="text/javascript" src="<c:url value="/js/datepicker/moment.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/datepicker/bootstrap-datetimepicker.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/datepicker/bootstrap-datetimepicker.ca.js"/>"></script>
<!-- Selects multiple -->
<script type="text/javascript" src="<c:url value="/js/chosen.jquery.js"/>"></script>
<script type="text/javascript" src="<c:url value="/js/regweb.js"/>"></script>
<!-- Upload file jquery -->
<script type="text/javascript" src="<c:url value="/js/jquery.form.js"/>"></script>

<!-- Input File -->
<script>
    $(document)
            .on('change', '.btn-file :file', function() {
                var input = $(this),
                        numFiles = input.get(0).files ? input.get(0).files.length : 1,
                        label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                input.trigger('fileselect', [numFiles, label]);
            });
    $(document).ready( function() {
        $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
            var input = $(this).parents('.input-group').find(':text'),
                    log = numFiles > 1 ? numFiles + ' files selected' : label;
            if( input.length ) {
                input.val(log);
            } else {
                if( log ) alert(log);
            }
        });
    });
</script>

<!-- COLOR MENU -->
<script>
    $(document).ready(function() {
        $(function () {
            if(${entidadActiva != null}){
                $('.navbar-header').css('background-color','#${entidadActiva.colorMenu}');
                $('.navbar-nav > li > a').css('background-color','#${entidadActiva.colorMenu}');
            }
        });
    });
</script>

<!-- FI JAVASCRIPT INCLOS DEL PEU -->


</body>

</html>
