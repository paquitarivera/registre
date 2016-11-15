
create table RWE_ANEXO (
    ID number(19,0) not null,
    CERTIFICADO raw(255),
    CSV varchar2(255 char),
    CUSTODIAID varchar2(256 char),
    FECHACAPTURA timestamp not null,
    FIRMA raw(255),
    HASH raw(255) not null,
    MODOFIRMA number(10,0),
    OBSERVACIONES varchar2(50 char),
    ORIGEN number(10,0),
    TIMESTAMP raw(255),
    TIPODOC number(19,0),
    TITULO varchar2(200 char) not null,
    VAL_OCSP_CERTIFICADO raw(255),
    TVALDOC number(19,0),
    REGISTRODETALLE number(19,0),
    TDOCUMENTAL number(19,0)
);

create table RWE_ANEXO_SIR (
    ID number(19,0) not null,
    CERTIFICADO raw(255),
    FIRMA raw(255),
    HASH raw(255) not null,
    ID_DOCUMENTO_FIRMADO varchar2(50 char),
    IDENTIFICADOR_FICHERO varchar2(50 char) not null,
    NOMBRE_FICHERO varchar2(80 char) not null,
    OBSERVACIONES varchar2(50 char),
    TIMESTAMP raw(255),
    TIPO_DOCUMENTO varchar2(2 char) not null,
    TIPO_MIME varchar2(20 char),
    VAL_OCSP_CERTIFICADO raw(255),
    VALIDEZ_DOCUMENTO varchar2(2 char),
    ANEXO number(19,0),
    ASIENTO_REGISTRAL number(19,0)
);

create table RWE_ARCHIVO (
    ID number(19,0) not null,
    MIME varchar2(255 char) not null,
    NOMBRE varchar2(255 char) not null,
    TAMANO number(19,0) not null
);

create table RWE_ASIENTO_REGISTRAL_SIR (
    ID number(19,0) not null,
    APLICACION varchar2(4 char),
    COD_ASUNTO varchar2(16 char),
    COD_ENT_REG varchar2(21 char),
    COD_ENT_REG_DEST varchar2(21 char) not null,
    COD_ENT_REG_INI varchar2(21 char) not null,
    COD_ENT_REG_ORI varchar2(21 char) not null,
    COD_UNI_TRA_DEST varchar2(21 char),
    COD_UNI_TRA_ORI varchar2(21 char),
    CONTACTO_USUARIO varchar2(160 char),
    DEC_ENT_REG_DEST varchar2(80 char),
    DEC_ENT_REG_INI varchar2(80 char),
    DEC_ENT_REG_ORI varchar2(80 char),
    DEC_T_ANOTACION varchar2(80 char),
    DEC_UNI_TRA_DEST varchar2(80 char),
    DEC_UNI_TRA_ORI varchar2(80 char),
    DOC_FISICA varchar2(1 char) not null,
    ESTADO number(10,0) not null,
    EXPONE varchar2(4000 char),
    FECHAR_EGISTRO timestamp not null,
    fechaRegistroInicial timestamp,
    ID_INTERCAMBIO varchar2(33 char) not null,
    INDICADOR_PRUEBA number(10,0) not null,
    NOMBRE_USUARIO varchar2(80 char),
    NUM_EXPEDIENTE varchar2(80 char),
    NUMERO_REGISTRO varchar2(20 char) not null,
    numeroRegistroInicial varchar2(255 char),
    NUM_TRANSPORTE varchar2(20 char),
    OBSERVACIONES varchar2(50 char),
    REF_EXTERNA varchar2(16 char),
    RESUMEN varchar2(240 char) not null,
    SOLICITA varchar2(4000 char),
    TIMESTAMP_REGISTRO raw(255),
    timestampRegistroInicial raw(255),
    TIPO_ANOTACION varchar2(2 char) not null,
    TIPO_REGISTRO number(10,0) not null,
    TIPO_TRANSPORTE varchar2(2 char),
    ENTIDAD number(19,0) not null
);

create table RWE_CATCOMUNIDADAUTONOMA (
    ID number(19,0) not null,
    C_CODIGO_DIR2 number(19,0),
    C_COMUNIDAD_RPC varchar2(2 char),
    CODIGOCOMUNIDAD number(19,0) not null,
    DESCRIPCIONCOMUNIDAD varchar2(50 char) not null,
    PAIS number(19,0)
);

create table RWE_CATENTIDADGEOGRAFICA (
    ID number(19,0) not null,
    CODIGOENTIDADGEOGRAFICA varchar2(2 char) not null,
    DESCRIPCIONENTIDADGEOGRAFICA varchar2(50 char) not null
);

create table RWE_CATESTADOENTIDAD (
    ID number(19,0) not null,
    CODIGOESTADOENTIDAD varchar2(2 char) not null,
    DESCRIPCIONESTADOENTIDAD varchar2(50 char) not null
);

create table RWE_CATLOCALIDAD (
    ID number(19,0) not null,
    CODIGOLOCALIDAD number(19,0) not null,
    DESCRIPCIONLOCALIDAD varchar2(50 char) not null,
    ENTIDADGEOGRAFICA number(19,0),
    PROVINCIA number(19,0)
);

create table RWE_CATNIVELADMINISTRACION (
    ID number(19,0) not null,
    CODIGONIVELADMINISTRACION number(19,0) not null,
    DESCRIPCIONNIVELADMINISTRACION varchar2(300 char) not null
);

create table RWE_CATPAIS (
    ID number(19,0) not null,
    ALFA2PAIS varchar2(2 char),
    ALFA3PAIS varchar2(3 char),
    CODIGOPAIS number(19,0) not null,
    DESCRIPCIONPAIS varchar2(100 char) not null
);

create table RWE_CATPROVINCIA (
    ID number(19,0) not null,
    CODIGOPROVINCIA number(19,0) not null,
    DESCRIPCIONPROVINCIA varchar2(50 char) not null,
    COMUNIDADAUTONOMA number(19,0)
);

create table RWE_CATSERVICIO (
    ID number(19,0) not null,
    CODIGOSERVICIO number(19,0) not null,
    DESCRIPCIONSERVICIO varchar2(300 char) not null
);

create table RWE_CATTIPOVIA (
    ID number(19,0) not null,
    CODIGOTIPOVIA number(19,0) not null,
    DESCRIPCIONTIPOVIA varchar2(300 char) not null
);

create table RWE_CODIGOASUNTO (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    CODIGO varchar2(16 char) not null,
    TIPOASUNTO number(19,0)
);

create table RWE_CONFIGURACION (
    ID number(19,0) not null,
    COLORMENU varchar2(255 char),
    TEXTOPIE varchar2(4000 char),
    LOGOMENU number(19,0),
    LOGOPIE number(19,0)
);

create table RWE_CONTADOR (
    ID number(19,0) not null,
    NUMERO number(10,0) not null
);

create table RWE_DESCARGA (
    ID number(19,0) not null,
    FECHAFIN timestamp,
    FECHAIMPORTACION timestamp,
    FECHAINICIO timestamp,
    TIPO varchar2(255 char),
    ENTIDAD number(19,0)
);

create table RWE_ENTIDAD (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    CODIGODIR3 varchar2(255 char) not null,
    COLORMENU varchar2(255 char),
    CONFIGPERSONA number(19,0),
    DESCRIPCION varchar2(4000 char) not null,
    DIASVISADO number(10,0),
    NOMBRE varchar2(255 char) not null,
    NUMREGISTRO varchar2(4000 char),
    POSXSELLO number(10,0),
    POSYSELLO number(10,0),
    SELLO varchar2(4000 char),
    SIR number(1,0) not null,
    TEXTOPIE varchar2(4000 char),
    TIPSCAN varchar2(20 char),
    LOGOMENU number(19,0),
    LOGOPIE number(19,0),
    LOGOSELLO number(19,0),
    PROPIETARIO number(19,0)
);

create table RWE_ENTIDAD_USUENT (
    IDENTIDAD number(19,0) not null,
    IDUSUENT number(19,0) not null
);

create table RWE_HISTORICOUO (
    CODANTERIOR number(19,0) not null,
    CODULTIMA number(19,0) not null
);

create table RWE_HISTORICO_REGISTRO_ENTRADA (
    ID number(19,0) not null,
    ESTADO number(19,0),
    FECHA timestamp not null,
    MODIFICACION varchar2(255 char) not null,
    RE_ORIGINAL clob,
    REGISTRO_ENTRADA number(19,0),
    USUARIO number(19,0)
) TABLESPACE REGWEB_DADES;

create table RWE_HISTORICO_REGISTRO_SALIDA (
    ID number(19,0) not null,
    ESTADO number(19,0),
    FECHA timestamp not null,
    MODIFICACION varchar2(255 char) not null,
    RS_ORIGINAL clob,
    REGISTRO_SALIDA number(19,0),
    USUARIO number(19,0)
) TABLESPACE REGWEB_DADES;

create table RWE_INTERESADO (
    ID number(19,0) not null,
    APELLIDO1 varchar2(255 char),
    APELLIDO2 varchar2(255 char),
    CANALNOTIF number(19,0),
    CODIGODIR3 varchar2(255 char),
    CP varchar2(5 char),
    DIRECCION varchar2(160 char),
    DIRELECTRONICA varchar2(160 char),
    DOCUMENTO varchar2(17 char),
    EMAIL varchar2(160 char),
    ISREPRESENTANTE number(1,0),
    NOMBRE varchar2(255 char),
    OBSERVACIONES varchar2(160 char),
    RAZONSOCIAL varchar2(2000 char),
    TELEFONO varchar2(20 char),
    TIPOINTERESADO number(19,0),
    TIPODOCIDENT number(19,0),
    LOCALIDAD number(19,0),
    PAIS number(19,0),
    PROVINCIA number(19,0),
    REGISTRODETALLE number(19,0),
    REPRESENTADO number(19,0),
    REPRESENTANTE number(19,0)
);

create table RWE_INTERESADO_SIR (
    ID number(19,0) not null,
    CANAL_NOTIF_INTERESADO varchar2(2 char),
    CANAL_NOTIF_REPRESENTANTE varchar2(2 char),
    COD_MUNICIPIO_INTERESADO varchar2(5 char),
    COD_MUNICIPIO_REPRESENTANTE varchar2(5 char),
    COD_PAIS_INTERESADO varchar2(4 char),
    COD_PAIS_REPRESENTANTE varchar2(4 char),
    CP_INTERESADO varchar2(5 char),
    CP_REPRESENTANTE varchar2(5 char),
    COD_PROVINCIA_INTERESADO varchar2(2 char),
    COD_PROVINCIA_REPRESENTANTE varchar2(2 char),
    EMAIL_INTERESADO varchar2(160 char),
    EMAIL_REPRESENTANTE varchar2(160 char),
    DIR_ELECTRONICA_INTERESADO varchar2(160 char),
    DIR_ELECTRONICA_REPRESENTANTE varchar2(160 char),
    DIRECCION_INTERESADO varchar2(160 char),
    DIRECCION_REPRESENTANTE varchar2(160 char),
    DOCUMENTO_INTERESADO varchar2(17 char),
    DOCUMENTO_REPRESENTANTE varchar2(17 char),
    NOMBRE_INTERESADO varchar2(30 char),
    NOMBRE_REPRESENTANTE varchar2(30 char),
    OBSERVACIONES varchar2(160 char),
    APELLIDO1_INTERESADO varchar2(30 char),
    APELLIDO1_REPRESENTANTE varchar2(30 char),
    RAZON_SOCIAL_INTERESADO varchar2(80 char),
    RAZON_SOCIAL_REPRESENTANTE varchar2(80 char),
    APELLIDO2_INTERESADO varchar2(30 char),
    APELLIDO2_REPRESENTANTE varchar2(30 char),
    TELEFONO_INTERESADO varchar2(20 char),
    TELEFONO_REPRESENTANTE varchar2(20 char),
    T_DOCUMENTO_INTERESADO varchar2(1 char),
    T_DOCUMENTO_REPRESENTANTE varchar2(1 char),
    ASIENTO_REGISTRAL number(19,0)
);

create table RWE_LIBRO (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    CODIGO varchar2(4 char) not null,
    NOMBRE varchar2(255 char) not null,
    CONTADOR_ENTRADA number(19,0),
    CONTADOR_OFICIO_REMISION number(19,0),
    CONTADOR_SALIDA number(19,0),
    ORGANISMO number(19,0)
);

create table RWE_LOPD (
    ID number(19,0) not null,
    ACCION number(19,0) not null,
    ANYOREGISTRO varchar2(255 char) not null,
    FECHA timestamp not null,
    NUMREGISTRO number(10,0) not null,
    TIPOREGISTRO number(19,0) not null,
    LIBRO number(19,0) not null,
    USUARIO number(19,0) not null
) PARTITION BY RANGE(FECHA)
(
    PARTITION RWE_LOPD_2015 VALUES LESS THAN (TO_DATE('01-01-2015','DD-MM-YYYY')),
    PARTITION RWE_LOPD_2016 VALUES LESS THAN (TO_DATE('01-01-2016','DD-MM-YYYY')),
    PARTITION RWE_LOPD_2017 VALUES LESS THAN (TO_DATE('01-01-2017','DD-MM-YYYY')),
    PARTITION RWE_LOPD_2018 VALUES LESS THAN (TO_DATE('01-01-2018','DD-MM-YYYY')),
    PARTITION RWE_LOPD_2019 VALUES LESS THAN (TO_DATE('01-01-2019','DD-MM-YYYY')),
    PARTITION RWE_LOPD_2020 VALUES LESS THAN (TO_DATE('01-01-2020','DD-MM-YYYY'))
) TABLESPACE REGWEB_DADES;

create table RWE_MODELO_OFICIO_REMISION (
    ID number(19,0) not null,
    NOMBRE varchar2(255 char) not null,
    ENTIDAD number(19,0),
    MODELO number(19,0)
);

create table RWE_MODELO_RECIBO (
    ID number(19,0) not null,
    NOMBRE varchar2(255 char) not null,
    ENTIDAD number(19,0),
    MODELO number(19,0)
);

create table RWE_MODIFICACIONLOPD_MIGRADO (
    ID number(19,0) not null,
    FECHA timestamp not null,
    FECHAMOD timestamp not null,
    TIPOACCESO varchar2(10 char) not null,
    USUARIO varchar2(10 char) not null,
    REGMIG number(19,0)
) TABLESPACE REGWEB_HIST;

create table RWE_OFICINA (
    ID number(19,0) not null,
    CODPOSTAL varchar2(14 char),
    CODIGO varchar2(9 char) not null,
    DENOMINACION varchar2(300 char) not null,
    NOMBREVIA varchar2(300 char),
    NUMVIA varchar2(20 char),
    COMUNIDAD number(19,0),
    PAIS number(19,0),
    ESTADO number(19,0),
    LOCALIDAD number(19,0),
    OFICINARESPONSABLE number(19,0),
    ORGANISMORESPONSABLE number(19,0),
    TIPOVIA number(19,0)
);

create table RWE_OFICINA_SERVICIO (
    IDOFICINA number(19,0) not null,
    IDSERVICIO number(19,0) not null
);

create table RWE_OFICIO_REMISION (
    ID number(19,0) not null,
    DESTINOEXTERNOCODIGO varchar2(9 char),
    DESTINOEXTERNODENOMINA varchar2(300 char),
    ESTADO number(10,0) not null,
    FECHA timestamp not null,
    FECHA_ESTADO timestamp,
    IDINTERCAMBIOSIR varchar2(300 char),
    NUMREGISTRO number(10,0) not null,
    TIPO_OFICIO number(19,0) not null,
    LIBRO number(19,0) not null,
    OFICINA number(19,0) not null,
    ORGANISMODEST number(19,0),
    USUARIO number(19,0) not null
);

create table RWE_OFIREM_REGENT (
    IDOFIREM number(19,0) not null,
    IDREGENT number(19,0) not null
);

create table RWE_OFIREM_REGSAL (
    IDOFIREM number(19,0) not null,
    IDREGSAL number(19,0) not null
);

create table RWE_ORGANISMO (
    ID number(19,0) not null,
    CODPOSTAL varchar2(14 char),
    CODIGO varchar2(9 char) not null,
    DENOMINACION varchar2(300 char) not null,
    EDP number(1,0),
    NIVELJERARQUICO number(19,0),
    NOMBREVIA varchar2(300 char),
    NUMVIA varchar2(20 char),
    CODAMBCOMUNIDAD number(19,0),
    CODAMBPROVINCIA number(19,0),
    PAIS number(19,0),
    EDPRINCIPAL number(19,0),
    ENTIDAD number(19,0),
    ESTADO number(19,0),
    LOCALIDAD number(19,0),
    NIVELADMINISTRACION number(19,0),
    ORGANISMORAIZ number(19,0),
    ORGANISMOSUPERIOR number(19,0),
    TIPOVIA number(19,0)
);

create table RWE_PENDIENTE (
    ID number(19,0) not null,
    ESTADO varchar2(255 char),
    FECHA timestamp,
    idOrganismo number(19,0),
    PROCESADO number(1,0)
);

create table RWE_PERMLIBUSU (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    PERMISO number(19,0),
    LIBRO number(19,0),
    USUARIO number(19,0)
);

create table RWE_PERSONA (
    ID number(19,0) not null,
    APELLIDO1 varchar2(30 char),
    APELLIDO2 varchar2(30 char),
    CANALNOTIF number(19,0),
    CP varchar2(5 char),
    DIRECCION varchar2(160 char),
    DIRELECTRONICA varchar2(160 char),
    DOCUMENTO varchar2(17 char),
    EMAIL varchar2(160 char),
    NOMBRE varchar2(30 char),
    OBSERVACIONES varchar2(160 char),
    RAZONSOCIAL varchar2(80 char),
    TELEFONO varchar2(20 char),
    TIPOPERSONA number(19,0) not null,
    TIPODOCIDENT number(19,0),
    ENTIDAD number(19,0) not null,
    LOCALIDAD number(19,0),
    PAIS number(19,0),
    PROVINCIA number(19,0)
);

create table RWE_PROPIEDADGLOBAL (
    ID number(19,0) not null,
    CLAVE varchar2(255 char) not null,
    DESCRIPCION varchar2(255 char),
    ENTIDAD number(19,0),
    TIPO number(19,0),
    VALOR varchar2(255 char)
);

create table RWE_REGISTROLOPD_MIGRADO (
    ID number(19,0) not null,
    FECHA timestamp not null,
    TIPOACCESO varchar2(10 char) not null,
    USUARIO varchar2(10 char) not null,
    REGMIG number(19,0)
) TABLESPACE REGWEB_HIST;

create table RWE_REGISTRO_DETALLE (
    ID number(19,0) not null,
    APLICACION varchar2(255 char),
    EXPEDIENTE varchar2(80 char),
    EXPONE varchar2(4000 char),
    EXTRACTO varchar2(240 char),
    FECHAORIGEN timestamp,
    IDIOMA number(19,0),
    NUMREG_ORIGEN varchar2(20 char),
    NUMTRANSPORTE varchar2(20 char),
    OBSERVACIONES varchar2(50 char),
    OFICINAEXTERNO varchar2(9 char),
    DENOMOFIORIGEXT varchar2(300 char),
    REFEXT varchar2(16 char),
    RESERVA varchar2(4000 char),
    SOLICITA varchar2(4000 char),
    TIPODOCFISICA number(19,0),
    TRANSPORTE number(19,0),
    VERSION varchar2(255 char),
    CODASUNTO number(19,0),
    OFICINAORIG number(19,0),
    TIPOASUNTO number(19,0)
);

create table RWE_REGISTRO_ENTRADA (
    ID number(19,0) not null,
    DESTEXTCOD varchar2(9 char),
    DESTEXTDEN varchar2(300 char),
    ESTADO number(19,0) not null,
    FECHA timestamp not null,
    NUMREGISTRO number(10,0) not null,
    NUMREGFORMAT varchar2(255 char) not null,
    DESTINO number(19,0),
    LIBRO number(19,0) not null,
    OFICINA number(19,0) not null,
    REGISTRO_DETALLE number(19,0),
    USUARIO number(19,0) not null
) PARTITION BY RANGE(FECHA)
(
    PARTITION RWE_REGISTRO_ENTRADA_2015 VALUES LESS THAN (TO_DATE('01-01-2015','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_ENTRADA_2016 VALUES LESS THAN (TO_DATE('01-01-2016','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_ENTRADA_2017 VALUES LESS THAN (TO_DATE('01-01-2017','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_ENTRADA_2018 VALUES LESS THAN (TO_DATE('01-01-2018','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_ENTRADA_2019 VALUES LESS THAN (TO_DATE('01-01-2019','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_ENTRADA_2020 VALUES LESS THAN (TO_DATE('01-01-2020','DD-MM-YYYY'))
) TABLESPACE REGWEB_DADES;

create table RWE_REGISTRO_MIGRADO (
    ID number(19,0) not null,
    ANO number(10,0) not null,
    ANOENTSAL number(10,0) not null,
    ANULADO number(1,0),
    CODIDIDOC number(10,0) not null,
    CODIDIEXT number(10,0) not null,
    CODOFICINA number(10,0) not null,
    CODOFIFIS number(10,0) not null,
    CODORGDESEMI number(10,0) not null,
    DENOFICINA varchar2(255 char) not null,
    DENOFIFIS varchar2(60 char) not null,
    DESCDOC varchar2(60 char) not null,
    DESIDIDOC varchar2(15 char) not null,
    DESORGDESEMI varchar2(60 char),
    DESREMDES varchar2(160 char) not null,
    MAILREMITENTE varchar2(50 char),
    EXTRACTO varchar2(2000 char) not null,
    FECHADOC timestamp not null,
    FECHAREG timestamp not null,
    FECHAVIS timestamp,
    infoAdicional varchar2(255 char),
    DESIDIEXT varchar2(15 char) not null,
    NUMERO number(10,0) not null,
    NUMCORREO varchar2(8 char),
    NUMDISQUET number(10,0),
    NUMENTSAL number(10,0) not null,
    OTROS varchar2(255 char),
    PRODESGEO varchar2(50 char) not null,
    PRODESGEOBAL number(10,0) not null,
    PRODESGEOFUE varchar2(50 char),
    TIPODOC varchar2(2 char) not null,
    TREGISTRO number(1,0),
    IDENTIDAD number(19,0)
) TABLESPACE REGWEB_HIST;

create table RWE_REGISTRO_SALIDA (
    ID number(19,0) not null,
    ESTADO number(19,0) not null,
    FECHA timestamp not null,
    NUMREGISTRO number(10,0) not null,
    NUMREGFORMAT varchar2(255 char) not null,
    DESTEXTCOD varchar2(9 char),
    DESTEXTDEN varchar2(300 char),
    LIBRO number(19,0) not null,
    OFICINA number(19,0) not null,
    ORIGEN number(19,0),
    REGISTRO_DETALLE number(19,0),
    USUARIO number(19,0) not null
) PARTITION BY RANGE(FECHA)
(
    PARTITION RWE_REGISTRO_SALIDA_2015 VALUES LESS THAN (TO_DATE('01-01-2015','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_SALIDA_2016 VALUES LESS THAN (TO_DATE('01-01-2016','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_SALIDA_2017 VALUES LESS THAN (TO_DATE('01-01-2017','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_SALIDA_2018 VALUES LESS THAN (TO_DATE('01-01-2018','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_SALIDA_2019 VALUES LESS THAN (TO_DATE('01-01-2019','DD-MM-YYYY')),
    PARTITION RWE_REGISTRO_SALIDA_2020 VALUES LESS THAN (TO_DATE('01-01-2020','DD-MM-YYYY'))
) TABLESPACE REGWEB_DADES;

create table RWE_RELORGOFI (
    IDORGANISMO number(19,0),
    IDOFICINA number(19,0),
    ESTADO number(19,0) not null
);

create table RWE_RELSIROFI (
    IDORGANISMO number(19,0),
    IDOFICINA number(19,0),
    ESTADO number(19,0) not null
);

create table RWE_REPRO (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    NOMBRE varchar2(255 char) not null,
    ORDEN number(10,0) not null,
    REPRO clob,
    TIPOREGISTRO number(19,0) not null,
    USUARIOENTIDAD number(19,0)
) TABLESPACE REGWEB_DADES;

create table RWE_ROL (
    ID number(19,0) not null,
    DESCRIPCION varchar2(255 char) not null,
    NOMBRE varchar2(255 char) not null,
    ORDEN number(10,0) not null
);

create table RWE_TIPOASUNTO (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    CODIGO varchar2(16 char) not null,
    ENTIDAD number(19,0) not null
);

create table RWE_TIPODOCUMENTAL (
    ID number(19,0) not null,
    CODIGONTI varchar2(255 char),
    ENTIDAD number(19,0)
);

create table RWE_TRAZABILIDAD (
    ID number(19,0) not null,
    FECHA timestamp not null,
    OFICIO_REMISION number(19,0) not null,
    REGENT_DESTINO number(19,0),
    REGENT_ORIGEN number(19,0),
    REGISTRO_SALIDA number(19,0) not null
);

create table RWE_TRA_CODIGOASUNTO (
    IDCODIGOASUNTO number(19,0) not null,
    NOMBRE varchar2(255 char) not null,
    LANG varchar2(2 char)
);

create table RWE_TRA_TDOCUMENTAL (
    IDTDOCUMENTAL number(19,0) not null,
    NOMBRE varchar2(255 char) not null,
    LANG varchar2(2 char)
);

create table RWE_TRA_TIPOASUNTO (
    IDTIPOASUNTO number(19,0) not null,
    NOMBRE varchar2(255 char) not null,
    LANG varchar2(2 char)
);

create table RWE_USUARIO (
    ID number(19,0) not null,
    APELLIDO1 varchar2(255 char),
    APELLIDO2 varchar2(255 char),
    DOCUMENTO varchar2(255 char),
    EMAIL varchar2(255 char) not null,
    IDENTIFICADOR varchar2(255 char) not null,
    IDIOMA number(19,0),
    NOMBRE varchar2(255 char) not null,
    RWE_ADMIN number(1,0) not null,
    RWE_SUPERADMIN number(1,0) not null,
    RWE_USUARI number(1,0) not null,
    TIPOUSUARIO number(19,0)
);

create table RWE_USUARIO_ENTIDAD (
    ID number(19,0) not null,
    ACTIVO number(1,0) not null,
    ENTIDAD number(19,0),
    ULTIMAOFICINA number(19,0),
    USUARIO number(19,0)
);

create sequence RWE_ALL_SEQ;


-- INICI Indexes
create index RWE_ANEXO_REGDET_FK_I on RWE_ANEXO (REGISTRODETALLE) TABLESPACE REGWEB_INDEX;
create index RWE_ANEXO_TDOCAL_FK_I on RWE_ANEXO (TDOCUMENTAL) TABLESPACE REGWEB_INDEX;
create index RWE_CATCOM_CATPAI_FK_I on RWE_CATCOMUNIDADAUTONOMA (PAIS) TABLESPACE REGWEB_INDEX;
create index RWE_CATLOC_CATENG_FK_I on RWE_CATLOCALIDAD (ENTIDADGEOGRAFICA) TABLESPACE REGWEB_INDEX;
create index RWE_CATLOC_CATPRO_FK_I on RWE_CATLOCALIDAD (PROVINCIA) TABLESPACE REGWEB_INDEX;
create index RWE_CATPRO_CATCAU_FK_I on RWE_CATPROVINCIA (COMUNIDADAUTONOMA) TABLESPACE REGWEB_INDEX;
create index RWE_CODASU_TASUN_FK_I on RWE_CODIGOASUNTO (TIPOASUNTO) TABLESPACE REGWEB_INDEX;
create index RWE_DESCAR_ENTIDA_FK_I on RWE_DESCARGA (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_ENTIDA_PRO_FK_I on RWE_ENTIDAD (PROPIETARIO) TABLESPACE REGWEB_INDEX;
create index RWE_HRE_REGENT_FK_I on RWE_HISTORICO_REGISTRO_ENTRADA (REGISTRO_ENTRADA) TABLESPACE REGWEB_INDEX;
create index RWE_HRE_USUENT_FK_I on RWE_HISTORICO_REGISTRO_ENTRADA (USUARIO) TABLESPACE REGWEB_INDEX;
create index RWE_INTERES_CATPAI_FK_I on RWE_INTERESADO (PAIS) TABLESPACE REGWEB_INDEX;
create index RWE_INTERES_CATLOC_FK_I on RWE_INTERESADO (LOCALIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_INTERES_REPADO_FK_I on RWE_INTERESADO (REPRESENTADO) TABLESPACE REGWEB_INDEX;
create index RWE_INTERES_REPANT_FK_I on RWE_INTERESADO (REPRESENTANTE) TABLESPACE REGWEB_INDEX;
create index RWE_LIBRO_CONSAL_FK_I on RWE_LIBRO (CONTADOR_SALIDA) TABLESPACE REGWEB_INDEX;
create index RWE_LIBRO_ORGANI_FK_I on RWE_LIBRO (ORGANISMO) TABLESPACE REGWEB_INDEX;
create index RWE_LIBRO_CONOFI_FK_I on RWE_LIBRO (CONTADOR_OFICIO_REMISION) TABLESPACE REGWEB_INDEX;
create index RWE_LIBRO_CONENT_FK_I on RWE_LIBRO (CONTADOR_ENTRADA) TABLESPACE REGWEB_INDEX;
create index RWE_LOPD_USUENT_FK_I on RWE_LOPD (USUARIO) TABLESPACE REGWEB_INDEX;
create index RWE_LOPD_LIBRO_FK_I on RWE_LOPD (LIBRO) TABLESPACE REGWEB_INDEX;
create index RWE_MODOFI_ENTIDA_FK_I on RWE_MODELO_OFICIO_REMISION (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_MODOFI_ARCHIV_FK_I on RWE_MODELO_OFICIO_REMISION (MODELO) TABLESPACE REGWEB_INDEX;
create index RWE_MODREB_ENTIDA_FK_I on RWE_MODELO_RECIBO (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_MODREB_ARCHIV_FK_I on RWE_MODELO_RECIBO (MODELO) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_PAIS_FK_I on RWE_OFICINA (PAIS) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_ESTENT_FK_I on RWE_OFICINA (ESTADO) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_COMUNI_FK_I on RWE_OFICINA (COMUNIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_TVIA_FK_I on RWE_OFICINA (TIPOVIA) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_LOCALI_FK_I on RWE_OFICINA (LOCALIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_OFICIN_FK_I on RWE_OFICINA (OFICINARESPONSABLE) TABLESPACE REGWEB_INDEX;
create index RWE_OFICIN_ORGANI_FK_I on RWE_OFICINA (ORGANISMORESPONSABLE) TABLESPACE REGWEB_INDEX;
create index RWE_OFIREM_ORGANI_FK_I on RWE_OFICIO_REMISION (ORGANISMODEST) TABLESPACE REGWEB_INDEX;
create index RWE_OFIREM_OFICIN_FK_I on RWE_OFICIO_REMISION (OFICINA) TABLESPACE REGWEB_INDEX;
create index RWE_OFIREM_LIBRO_FK_I on RWE_OFICIO_REMISION (LIBRO) TABLESPACE REGWEB_INDEX;
create index RWE_OFIREM_USUARI_FK_I on RWE_OFICIO_REMISION (USUARIO) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_SUPERI_FK_I on RWE_ORGANISMO (ORGANISMOSUPERIOR) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_TVIA_FK_I on RWE_ORGANISMO (TIPOVIA) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_LOCALI_FK_I on RWE_ORGANISMO (LOCALIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_EDP_FK_I on RWE_ORGANISMO (EDPRINCIPAL) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_PROVIN_FK_I on RWE_ORGANISMO (CODAMBPROVINCIA) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_ESTADO_FK_I on RWE_ORGANISMO (ESTADO) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_ENTIDA_FK_I on RWE_ORGANISMO (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_RAIZ_FK_I on RWE_ORGANISMO (ORGANISMORAIZ) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_CAUTON_FK_I on RWE_ORGANISMO (CODAMBCOMUNIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_ORGANI_PAIS_FK_I on RWE_ORGANISMO (PAIS) TABLESPACE REGWEB_INDEX;
create index RWE_PELIUS_USUARI_FK_I on RWE_PERMLIBUSU (USUARIO) TABLESPACE REGWEB_INDEX;
create index RWE_PELIUS_LIBRO_FK_I on RWE_PERMLIBUSU (LIBRO) TABLESPACE REGWEB_INDEX;
create index RWE_PERSONA_ENTIDAD_FK_I on RWE_PERSONA (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_PROPIE_ENTIDA_FK_I on RWE_PROPIEDADGLOBAL (ENTIDAD) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_FECREG_I on RWE_REGISTRO_MIGRADO (FECHAREG) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_CODOF_I on RWE_REGISTRO_MIGRADO (CODOFICINA) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_EXTR_I on RWE_REGISTRO_MIGRADO (EXTRACTO) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_NUM_I on RWE_REGISTRO_MIGRADO (NUMERO) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_REMDES_I on RWE_REGISTRO_MIGRADO (DESREMDES) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_ANO_I on RWE_REGISTRO_MIGRADO (ANO) TABLESPACE REGWEB_INDEX;
create index RWE_REGMIG_TREG_I on RWE_REGISTRO_MIGRADO (TREGISTRO) TABLESPACE REGWEB_INDEX;
-- FINAL Indexes

-- INICI PK's
alter table RWE_ANEXO add constraint RWE_ANEXO_pk primary key (ID);

alter table RWE_ANEXO_SIR add constraint RWE_ANEXO_SIR_pk primary key (ID);

alter table RWE_ARCHIVO add constraint RWE_ARCHIVO_pk primary key (ID);

alter table RWE_ASIENTO_REGISTRAL_SIR add constraint RWE_ASIENTO_REGISTRAL_SIR_pk primary key (ID);

alter table RWE_CATCOMUNIDADAUTONOMA add constraint RWE_CATCOMUNIDADAUTONOMA_pk primary key (ID);

alter table RWE_CATENTIDADGEOGRAFICA add constraint RWE_CATENTIDADGEOGRAFICA_pk primary key (ID);

alter table RWE_CATESTADOENTIDAD add constraint RWE_CATESTADOENTIDAD_pk primary key (ID);

alter table RWE_CATLOCALIDAD add constraint RWE_CATLOCALIDAD_pk primary key (ID);

alter table RWE_CATNIVELADMINISTRACION add constraint RWE_CATNIVELADMINISTRACION_pk primary key (ID);

alter table RWE_CATPAIS add constraint RWE_CATPAIS_pk primary key (ID);

alter table RWE_CATPROVINCIA add constraint RWE_CATPROVINCIA_pk primary key (ID);

alter table RWE_CATSERVICIO add constraint RWE_CATSERVICIO_pk primary key (ID);

alter table RWE_CATTIPOVIA add constraint RWE_CATTIPOVIA_pk primary key (ID);

alter table RWE_CODIGOASUNTO add constraint RWE_CODIGOASUNTO_pk primary key (ID);

alter table RWE_CONFIGURACION add constraint RWE_CONFIGURACION_pk primary key (ID);

alter table RWE_CONTADOR add constraint RWE_CONTADOR_pk primary key (ID);

alter table RWE_DESCARGA add constraint RWE_DESCARGA_pk primary key (ID);

alter table RWE_ENTIDAD add constraint RWE_ENTIDAD_pk primary key (ID);

alter table RWE_ENTIDAD_USUENT add constraint RWE_ENTIDAD_USUENT_pk primary key (IDENTIDAD, IDUSUENT);

alter table RWE_HISTORICOUO add constraint RWE_HISTORICOUO_pk primary key (CODANTERIOR, CODULTIMA);

alter table RWE_HISTORICO_REGISTRO_ENTRADA add constraint RWE_HIST_REGISTRO_ENTRADA_PK primary key (ID);

alter table RWE_HISTORICO_REGISTRO_SALIDA add constraint RWE_HIST_REGISTRO_SALIDA_PK primary key (ID);

alter table RWE_INTERESADO add constraint RWE_INTERESADO_pk primary key (ID);

alter table RWE_INTERESADO_SIR add constraint RWE_INTERESADO_SIR_pk primary key (ID);

alter table RWE_LIBRO add constraint RWE_LIBRO_pk primary key (ID);

alter table RWE_LOPD add constraint RWE_LOPD_pk primary key (ID);

alter table RWE_MODELO_OFICIO_REMISION add constraint RWE_MODELO_OFICIO_REMISION_pk primary key (ID);

alter table RWE_MODELO_RECIBO add constraint RWE_MODELO_RECIBO_pk primary key (ID);

alter table RWE_MODIFICACIONLOPD_MIGRADO add constraint RWE_MODIFLOPD_MIGRADO_PK primary key (ID);

alter table RWE_OFICINA add constraint RWE_OFICINA_pk primary key (ID);

alter table RWE_OFICINA_SERVICIO add constraint RWE_OFICINA_SERVICIO_pk primary key (IDOFICINA, IDSERVICIO);

alter table RWE_OFICIO_REMISION add constraint RWE_OFICIO_REMISION_pk primary key (ID);

alter table RWE_ORGANISMO add constraint RWE_ORGANISMO_pk primary key (ID);

alter table RWE_PENDIENTE add constraint RWE_PENDIENTE_pk primary key (ID);

alter table RWE_PERMLIBUSU add constraint RWE_PERMLIBUSU_pk primary key (ID);

alter table RWE_PERSONA add constraint RWE_PERSONA_pk primary key (ID);

alter table RWE_PROPIEDADGLOBAL add constraint RWE_PROPIEDADGLOBAL_pk primary key (ID);

alter table RWE_REGISTROLOPD_MIGRADO add constraint RWE_REGISTROLOPD_MIGRADO_pk primary key (ID);

alter table RWE_REGISTRO_DETALLE add constraint RWE_REGISTRO_DETALLE_pk primary key (ID);

alter table RWE_REGISTRO_ENTRADA add constraint RWE_REGISTRO_ENTRADA_pk primary key (ID);

alter table RWE_REGISTRO_MIGRADO add constraint RWE_REGISTRO_MIGRADO_pk primary key (ID);

alter table RWE_REGISTRO_SALIDA add constraint RWE_REGISTRO_SALIDA_pk primary key (ID);

alter table RWE_RELORGOFI add constraint RWE_RELORGOFI_pk primary key (IDORGANISMO, IDOFICINA);

alter table RWE_RELSIROFI add constraint RWE_RELSIROFI_pk primary key (IDORGANISMO, IDOFICINA);

alter table RWE_REPRO add constraint RWE_REPRO_pk primary key (ID);

alter table RWE_ROL add constraint RWE_ROL_pk primary key (ID);

alter table RWE_TIPOASUNTO add constraint RWE_TIPOASUNTO_pk primary key (ID);

alter table RWE_TIPODOCUMENTAL add constraint RWE_TIPODOCUMENTAL_pk primary key (ID);

alter table RWE_TRAZABILIDAD add constraint RWE_TRAZABILIDAD_pk primary key (ID);

alter table RWE_TRA_CODIGOASUNTO add constraint RWE_TRA_CODIGOASUNTO_pk primary key (IDCODIGOASUNTO, LANG);

alter table RWE_TRA_TDOCUMENTAL add constraint RWE_TRA_TDOCUMENTAL_pk primary key (IDTDOCUMENTAL, LANG);

alter table RWE_TRA_TIPOASUNTO add constraint RWE_TRA_TIPOASUNTO_pk primary key (IDTIPOASUNTO, LANG);

alter table RWE_USUARIO add constraint RWE_USUARIO_pk primary key (ID);

alter table RWE_USUARIO_ENTIDAD add constraint RWE_USUARIO_ENTIDAD_pk primary key (ID);

-- FINAL PK's

-- INICI FK's

alter table RWE_ANEXO
add constraint RWE_ANEXO_REGDET_FK
foreign key (REGISTRODETALLE)
references RWE_REGISTRO_DETALLE;

alter table RWE_ANEXO
add constraint RWE_ANEXO_TDOCAL_FK
foreign key (TDOCUMENTAL)
references RWE_TIPODOCUMENTAL;

alter table RWE_ANEXO_SIR
add constraint RWE_ANEXOSIR_ASIREG_FK
foreign key (ASIENTO_REGISTRAL)
references RWE_ASIENTO_REGISTRAL_SIR;

alter table RWE_ANEXO_SIR
add constraint RWE_ANEXOSIR_ANEXO_FK
foreign key (ANEXO)
references RWE_ARCHIVO;

alter table RWE_ASIENTO_REGISTRAL_SIR
add constraint RWE_ARS_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_CATCOMUNIDADAUTONOMA
add constraint RWE_CATCOMUNAUT_CATPAIS_FK
foreign key (PAIS)
references RWE_CATPAIS;

alter table RWE_CATLOCALIDAD
add constraint RWE_CATLOCAL_CATPROVIN_FK
foreign key (PROVINCIA)
references RWE_CATPROVINCIA;

alter table RWE_CATLOCALIDAD
add constraint RWE_CATLOCAL_CATENT_FK
foreign key (ENTIDADGEOGRAFICA)
references RWE_CATENTIDADGEOGRAFICA;

alter table RWE_CATPROVINCIA
add constraint RWE_CATPROVINC_CATCOMUNAUTO_FK
foreign key (COMUNIDADAUTONOMA)
references RWE_CATCOMUNIDADAUTONOMA;

alter table RWE_CODIGOASUNTO
add constraint RWE_CODASUNTO_TIPOASUNTO_FK
foreign key (TIPOASUNTO)
references RWE_TIPOASUNTO;

alter table RWE_CONFIGURACION
add constraint RWE_CONFIGURACION_LOGOMENU_FK
foreign key (LOGOMENU)
references RWE_ARCHIVO;

alter table RWE_CONFIGURACION
add constraint RWE_CONFIGURACION_LOGOPIE_FK
foreign key (LOGOPIE)
references RWE_ARCHIVO;

alter table RWE_DESCARGA
add constraint RWE_DESCARGA_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_ENTIDAD
add constraint RWE_ENTIDAD_LOGOSELLO_FK
foreign key (LOGOSELLO)
references RWE_ARCHIVO;

alter table RWE_ENTIDAD
add constraint RWE_ENTIDAD_LOGOMENU_FK
foreign key (LOGOMENU)
references RWE_ARCHIVO;

alter table RWE_ENTIDAD
add constraint RWE_ENTIDAD_LOGOPIE_FK
foreign key (LOGOPIE)
references RWE_ARCHIVO;

alter table RWE_ENTIDAD
add constraint RWE_ENTIDAD_USU_PROP_FK
foreign key (PROPIETARIO)
references RWE_USUARIO;

alter table RWE_ENTIDAD_USUENT
add constraint RWE_ENTIDAD_USU_ADM_FK
foreign key (IDUSUENT)
references RWE_USUARIO_ENTIDAD;

alter table RWE_ENTIDAD_USUENT
add constraint RWE_USU_ADM_ENTIDAD_FK
foreign key (IDENTIDAD)
references RWE_ENTIDAD;

alter table RWE_HISTORICOUO
add constraint RWE_ORG_ORG_HISTANTE_FK
foreign key (CODANTERIOR)
references RWE_ORGANISMO;

alter table RWE_HISTORICOUO
add constraint RWE_ORG_ORG_HISTULTI_FK
foreign key (CODULTIMA)
references RWE_ORGANISMO;

alter table RWE_HISTORICO_REGISTRO_ENTRADA
add constraint RWE_HISTORICO_USUARIO_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_HISTORICO_REGISTRO_ENTRADA
add constraint RWE_HITORICO_RE_FK
foreign key (REGISTRO_ENTRADA)
references RWE_REGISTRO_ENTRADA;

alter table RWE_HISTORICO_REGISTRO_SALIDA
add constraint RWE_HITORICO_RS_FK
foreign key (REGISTRO_SALIDA)
references RWE_REGISTRO_SALIDA;

alter table RWE_HISTORICO_REGISTRO_SALIDA
add constraint RWE_HISTORICO_USUARIO_RS_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_REPREANTE_FK
foreign key (REPRESENTANTE)
references RWE_INTERESADO;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_REPRESENT_FK
foreign key (REPRESENTADO)
references RWE_INTERESADO;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_PAIS_FK
foreign key (PAIS)
references RWE_CATPAIS;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_PROVINCIA_FK
foreign key (PROVINCIA)
references RWE_CATPROVINCIA;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_REGDET_FK
foreign key (REGISTRODETALLE)
references RWE_REGISTRO_DETALLE;

alter table RWE_INTERESADO
add constraint RWE_INTERESADO_LOCALIDAD_FK
foreign key (LOCALIDAD)
references RWE_CATLOCALIDAD;

alter table RWE_INTERESADO_SIR
add constraint RWE_INTERESADOSIR_ASIREG_FK
foreign key (ASIENTO_REGISTRAL)
references RWE_ASIENTO_REGISTRAL_SIR;

alter table RWE_LIBRO
add constraint RWE_LIBRO_CONT_SAL_FK
foreign key (CONTADOR_SALIDA)
references RWE_CONTADOR;

alter table RWE_LIBRO
add constraint RWE_LIBRO_CONT_ENT_FK
foreign key (CONTADOR_ENTRADA)
references RWE_CONTADOR;

alter table RWE_LIBRO
add constraint RWE_LIBRO_ORGANISMO_FK
foreign key (ORGANISMO)
references RWE_ORGANISMO;

alter table RWE_LIBRO
add constraint RWE_LIBRO_CONT_ORM_FK
foreign key (CONTADOR_OFICIO_REMISION)
references RWE_CONTADOR;

alter table RWE_LOPD
add constraint RWE_LOPD_USUENT_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_LOPD
add constraint RWE_LOPD_LIBRO_FK
foreign key (LIBRO)
references RWE_LIBRO;

alter table RWE_MODELO_OFICIO_REMISION
add constraint RWE_MODELOFREMISION_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_MODELO_OFICIO_REMISION
add constraint RWE_MODELOFREMISION_MODELO_FK
foreign key (MODELO)
references RWE_ARCHIVO;

alter table RWE_MODELO_RECIBO
add constraint RWE_MODELRECIBO_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_MODELO_RECIBO
add constraint RWE_MODELRECIBO_ARCHIVO_FK
foreign key (MODELO)
references RWE_ARCHIVO;

alter table RWE_MODIFICACIONLOPD_MIGRADO
add constraint RWE_MODLOPDMIG_REGMIG_FK
foreign key (REGMIG)
references RWE_REGISTRO_MIGRADO;

alter table RWE_OFICINA
add constraint RWE_OFICINA_TIPOVIA_FK
foreign key (TIPOVIA)
references RWE_CATTIPOVIA;

alter table RWE_OFICINA
add constraint RWE_OFICINA_PAIS_FK
foreign key (PAIS)
references RWE_CATPAIS;

alter table RWE_OFICINA
add constraint RWE_OFICINA_ESTADO_FK
foreign key (ESTADO)
references RWE_CATESTADOENTIDAD;

alter table RWE_OFICINA
add constraint RWE_OFICINA_LOCALIDAD_FK
foreign key (LOCALIDAD)
references RWE_CATLOCALIDAD;

alter table RWE_OFICINA
add constraint RWE_OFICINA_ORGANISMO_FK
foreign key (ORGANISMORESPONSABLE)
references RWE_ORGANISMO;

alter table RWE_OFICINA
add constraint RWE_OFICINA_OFICINA_FK
foreign key (OFICINARESPONSABLE)
references RWE_OFICINA;

alter table RWE_OFICINA
add constraint RWE_OFICINA_COMUNIDAD_FK
foreign key (COMUNIDAD)
references RWE_CATCOMUNIDADAUTONOMA;

alter table RWE_OFICINA_SERVICIO
add constraint RWE_SERVICIO_OFICINA_FK
foreign key (IDOFICINA)
references RWE_OFICINA;

alter table RWE_OFICINA_SERVICIO
add constraint RWE_OFICINA_SERVICIO_FK
foreign key (IDSERVICIO)
references RWE_CATSERVICIO;

alter table RWE_OFICIO_REMISION
add constraint RWE_OFIREM_USUORM_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_OFICIO_REMISION
add constraint RWE_OFIREM_ORGANISMODEST_FK
foreign key (ORGANISMODEST)
references RWE_ORGANISMO;

alter table RWE_OFICIO_REMISION
add constraint RWE_OFIREM_OFICINA_FK
foreign key (OFICINA)
references RWE_OFICINA;

alter table RWE_OFICIO_REMISION
add constraint RWE_OFIREM_LIBRO_FK
foreign key (LIBRO)
references RWE_LIBRO;

alter table RWE_OFIREM_REGENT
add constraint RWE_OFIREM_REGENT_FK
foreign key (IDREGENT)
references RWE_REGISTRO_ENTRADA;

alter table RWE_OFIREM_REGENT
add constraint RWE_REGENT_OFIREM_FK
foreign key (IDOFIREM)
references RWE_OFICIO_REMISION;

alter table RWE_OFIREM_REGSAL
add constraint RWE_REGSAL_OFIREM_FK
foreign key (IDOFIREM)
references RWE_OFICIO_REMISION;

alter table RWE_OFIREM_REGSAL
add constraint RWE_OFIREM_REGSAL_FK
foreign key (IDREGSAL)
references RWE_REGISTRO_SALIDA;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_ORGRAIZ_FK
foreign key (ORGANISMORAIZ)
references RWE_ORGANISMO;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_TIPOVIA_FK
foreign key (TIPOVIA)
references RWE_CATTIPOVIA;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_CATPROVINCIA_FK
foreign key (CODAMBPROVINCIA)
references RWE_CATPROVINCIA;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_PAIS_FK
foreign key (PAIS)
references RWE_CATPAIS;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_CATAMBCOMAUTO_FK
foreign key (CODAMBCOMUNIDAD)
references RWE_CATCOMUNIDADAUTONOMA;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_ORG_SUPERIOR_FK
foreign key (ORGANISMOSUPERIOR)
references RWE_ORGANISMO;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_CATNIVELADMIN_FK
foreign key (NIVELADMINISTRACION)
references RWE_CATNIVELADMINISTRACION;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_EDPRIN_FK
foreign key (EDPRINCIPAL)
references RWE_ORGANISMO;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_ESTADO_FK
foreign key (ESTADO)
references RWE_CATESTADOENTIDAD;

alter table RWE_ORGANISMO
add constraint RWE_ORGANISMO_LOCALIDAD_FK
foreign key (LOCALIDAD)
references RWE_CATLOCALIDAD;

alter table RWE_PERMLIBUSU
add constraint RWE_PERMLIBUSU_USUENT_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_PERMLIBUSU
add constraint RWE_PERMLIBUSU_LIBRO_FK
foreign key (LIBRO)
references RWE_LIBRO;

alter table RWE_PERSONA
add constraint RWE_PERSONA_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_PERSONA
add constraint RWE_PERSONA_PAIS_FK
foreign key (PAIS)
references RWE_CATPAIS;

alter table RWE_PERSONA
add constraint RWE_PERSONA_PROVINCIA_FK
foreign key (PROVINCIA)
references RWE_CATPROVINCIA;

alter table RWE_PERSONA
add constraint RWE_PERSONA_LOCALIDAD_FK
foreign key (LOCALIDAD)
references RWE_CATLOCALIDAD;

alter table RWE_REGISTROLOPD_MIGRADO
add constraint RWE_REGLOPDMIG_REGMIG_FK
foreign key (REGMIG)
references RWE_REGISTRO_MIGRADO;

alter table RWE_REGISTRO_DETALLE
add constraint RWE_REGDET_OFICINAORIG_FK
foreign key (OFICINAORIG)
references RWE_OFICINA;

alter table RWE_REGISTRO_DETALLE
add constraint RWE_REGDET_CODASUNTO_FK
foreign key (CODASUNTO)
references RWE_CODIGOASUNTO;

alter table RWE_REGISTRO_DETALLE
add constraint RWE_REGDET_TIPOASUNTO_FK
foreign key (TIPOASUNTO)
references RWE_TIPOASUNTO;

alter table RWE_REGISTRO_ENTRADA
add constraint RWE_REGENT_DESTINO_FK
foreign key (DESTINO)
references RWE_ORGANISMO;

alter table RWE_REGISTRO_ENTRADA
add constraint RWE_REGENT_USUENT_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_REGISTRO_ENTRADA
add constraint RWE_REGENT_REGDET_FK
foreign key (REGISTRO_DETALLE)
references RWE_REGISTRO_DETALLE;

alter table RWE_REGISTRO_ENTRADA
add constraint RWE_REGENT_OFICINA_FK
foreign key (OFICINA)
references RWE_OFICINA;

alter table RWE_REGISTRO_ENTRADA
add constraint RWE_REGENT_LIBRO_FK
foreign key (LIBRO)
references RWE_LIBRO;

alter table RWE_REGISTRO_MIGRADO
add constraint RWE_REGMIG_ENTIDAD_FK
foreign key (IDENTIDAD)
references RWE_ENTIDAD;

alter table RWE_REGISTRO_SALIDA
add constraint RWE_REGSAL_USUSAL_FK
foreign key (USUARIO)
references RWE_USUARIO_ENTIDAD;

alter table RWE_REGISTRO_SALIDA
add constraint RWE_REGSAL_REGDET_FK
foreign key (REGISTRO_DETALLE)
references RWE_REGISTRO_DETALLE;

alter table RWE_REGISTRO_SALIDA
add constraint RWE_REGSAL_ORIGEN_FK
foreign key (ORIGEN)
references RWE_ORGANISMO;

alter table RWE_REGISTRO_SALIDA
add constraint RWE_REGSAL_OFICINA_FK
foreign key (OFICINA)
references RWE_OFICINA;

alter table RWE_REGISTRO_SALIDA
add constraint RWE_REGSAL_LIBRO_FK
foreign key (LIBRO)
references RWE_LIBRO;

alter table RWE_RELORGOFI
add constraint RWE_RELORGOFI_ORGANISMO_FK
foreign key (IDORGANISMO)
references RWE_ORGANISMO;

alter table RWE_RELORGOFI
add constraint RWE_OFICINA_RELORGOFI_FK
foreign key (IDOFICINA)
references RWE_OFICINA;

alter table RWE_RELORGOFI
add constraint RWE_RELORGANOFI_CATESTENT_FK
foreign key (ESTADO)
references RWE_CATESTADOENTIDAD;

alter table RWE_RELSIROFI
add constraint RWE_RELSIROFI_ORGANISMO_FK
foreign key (IDORGANISMO)
references RWE_ORGANISMO;

alter table RWE_RELSIROFI
add constraint RWE_RELSIROFI_OFICINA_FK
foreign key (IDOFICINA)
references RWE_OFICINA;

alter table RWE_RELSIROFI
add constraint RWE_RELSIROFI_CATESTENTI_FK
foreign key (ESTADO)
references RWE_CATESTADOENTIDAD;

alter table RWE_REPRO
add constraint RWE_REPRO_USUARIO_FK
foreign key (USUARIOENTIDAD)
references RWE_USUARIO_ENTIDAD;

alter table RWE_TIPOASUNTO
add constraint RWE_TIPOASUNTO_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_TIPODOCUMENTAL
add constraint RWE_TIPODOCUMENTAL_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_TRAZABILIDAD
add constraint RWE_TRAZAB_REGSAL_FK
foreign key (REGISTRO_SALIDA)
references RWE_REGISTRO_SALIDA;

alter table RWE_TRAZABILIDAD
add constraint RWE_TRAZAB_REGENTO_FK
foreign key (REGENT_ORIGEN)
references RWE_REGISTRO_ENTRADA;

alter table RWE_TRAZABILIDAD
add constraint RWE_TRAZAB_OFIREM_FK
foreign key (OFICIO_REMISION)
references RWE_OFICIO_REMISION;

alter table RWE_TRAZABILIDAD
add constraint RWE_TRAZAB_REGENTD_FK
foreign key (REGENT_DESTINO)
references RWE_REGISTRO_ENTRADA;

alter table RWE_TRA_CODIGOASUNTO
add constraint RWE_CODASUNTO_TRACODASUNTO_FK
foreign key (IDCODIGOASUNTO)
references RWE_CODIGOASUNTO;

alter table RWE_TRA_TDOCUMENTAL
add constraint RWE_TIPODOC_TRATIPODOC_FK
foreign key (IDTDOCUMENTAL)
references RWE_TIPODOCUMENTAL;

alter table RWE_TRA_TIPOASUNTO
add constraint RWE_TASUNTO_TRATASUNTO_FK
foreign key (IDTIPOASUNTO)
references RWE_TIPOASUNTO;

alter table RWE_USUARIO_ENTIDAD
add constraint RWE_USUENT_ENTIDAD_FK
foreign key (ENTIDAD)
references RWE_ENTIDAD;

alter table RWE_USUARIO_ENTIDAD
add constraint RWE_USUENT_USUARIO_FK
foreign key (USUARIO)
references RWE_USUARIO;

alter table RWE_USUARIO_ENTIDAD
add constraint RWE_USUENT_OFICINA_FK
foreign key (ULTIMAOFICINA)
references RWE_OFICINA;
-- FINAL FK's

-- INICI UNIQUES
alter table RWE_CATESTADOENTIDAD add constraint RWE_CATESTENT_CODESTENT_UK unique (CODIGOESTADOENTIDAD);
alter table RWE_CODIGOASUNTO add constraint RWE_CODASUN_CODIGO_TIPASUN_UK unique (CODIGO, TIPOASUNTO);
alter table RWE_ENTIDAD add constraint RWE_ENTIDAD_CODIGODIR3_uk unique (CODIGODIR3);
alter table RWE_PROPIEDADGLOBAL add constraint RWE_propiedad_clave_entidad_uk unique (CLAVE, ENTIDAD);
alter table RWE_REGISTRO_MIGRADO add constraint RWE_REGMIGRADO_AN_NUM_OF_UK unique (ANO, NUMERO, CODOFICINA, TREGISTRO, IDENTIDAD);
alter table RWE_ROL add constraint RWE_ROL_NOMBRE_uk unique (NOMBRE);
alter table RWE_USUARIO add constraint RWE_USUARIO_IDENTIFICADOR_uk unique (IDENTIFICADOR);
-- FINAL UNIQUES

-- INICI GRANTS
grant select,insert,delete,update on RWE_ANEXO to www_regweb3;
grant select,insert,delete,update on RWE_ANEXO_SIR to www_regweb3;
grant select,insert,delete,update on RWE_ARCHIVO to www_regweb3;
grant select,insert,delete,update on RWE_ASIENTO_REGISTRAL_SIR to www_regweb3;
grant select,insert,delete,update on RWE_CATCOMUNIDADAUTONOMA to www_regweb3;
grant select,insert,delete,update on RWE_CATENTIDADGEOGRAFICA to www_regweb3;
grant select,insert,delete,update on RWE_CATESTADOENTIDAD to www_regweb3;
grant select,insert,delete,update on RWE_CATLOCALIDAD to www_regweb3;
grant select,insert,delete,update on RWE_CATNIVELADMINISTRACION to www_regweb3;
grant select,insert,delete,update on RWE_CATPAIS to www_regweb3;
grant select,insert,delete,update on RWE_CATPROVINCIA to www_regweb3;
grant select,insert,delete,update on RWE_CATSERVICIO to www_regweb3;
grant select,insert,delete,update on RWE_CATTIPOVIA to www_regweb3;
grant select,insert,delete,update on RWE_CODIGOASUNTO to www_regweb3;
grant select,insert,delete,update on RWE_CONFIGURACION to www_regweb3;
grant select,insert,delete,update on RWE_CONTADOR to www_regweb3;
grant select,insert,delete,update on RWE_DESCARGA to www_regweb3;
grant select,insert,delete,update on RWE_ENTIDAD to www_regweb3;
grant select,insert,delete,update on RWE_ENTIDAD_USUENT to www_regweb3;
grant select,insert,delete,update on RWE_HISTORICOUO to www_regweb3;
grant select,insert,delete,update on RWE_HISTORICO_REGISTRO_ENTRADA to www_regweb3;
grant select,insert,delete,update on RWE_HISTORICO_REGISTRO_SALIDA to www_regweb3;
grant select,insert,delete,update on RWE_INTERESADO to www_regweb3;
grant select,insert,delete,update on RWE_INTERESADO_SIR to www_regweb3;
grant select,insert,delete,update on RWE_LIBRO to www_regweb3;
grant select,insert,delete,update on RWE_LOPD to www_regweb3;
grant select,insert,delete,update on RWE_MODELO_OFICIO_REMISION to www_regweb3;
grant select,insert,delete,update on RWE_MODELO_RECIBO to www_regweb3;
grant select,insert,delete,update on RWE_MODIFICACIONLOPD_MIGRADO to www_regweb3;
grant select,insert,delete,update on RWE_OFICINA to www_regweb3;
grant select,insert,delete,update on RWE_OFICINA_SERVICIO to www_regweb3;
grant select,insert,delete,update on RWE_OFICIO_REMISION to www_regweb3;
grant select,insert,delete,update on RWE_OFIREM_REGENT to www_regweb3;
grant select,insert,delete,update on RWE_OFIREM_REGSAL to www_regweb3;
grant select,insert,delete,update on RWE_ORGANISMO to www_regweb3;
grant select,insert,delete,update on RWE_PENDIENTE to www_regweb3;
grant select,insert,delete,update on RWE_PERMLIBUSU to www_regweb3;
grant select,insert,delete,update on RWE_PERSONA to www_regweb3;
grant select,insert,delete,update on RWE_PROPIEDADGLOBAL to www_regweb3;
grant select,insert,delete,update on RWE_REGISTROLOPD_MIGRADO to www_regweb3;
grant select,insert,delete,update on RWE_REGISTRO_DETALLE to www_regweb3;
grant select,insert,delete,update on RWE_REGISTRO_ENTRADA to www_regweb3;
grant select,insert,delete,update on RWE_REGISTRO_MIGRADO to www_regweb3;
grant select,insert,delete,update on RWE_REGISTRO_SALIDA to www_regweb3;
grant select,insert,delete,update on RWE_RELORGOFI to www_regweb3;
grant select,insert,delete,update on RWE_RELSIROFI to www_regweb3;
grant select,insert,delete,update on RWE_REPRO to www_regweb3;
grant select,insert,delete,update on RWE_ROL to www_regweb3;
grant select,insert,delete,update on RWE_TIPOASUNTO to www_regweb3;
grant select,insert,delete,update on RWE_TIPODOCUMENTAL to www_regweb3;
grant select,insert,delete,update on RWE_TRAZABILIDAD to www_regweb3;
grant select,insert,delete,update on RWE_TRA_CODIGOASUNTO to www_regweb3;
grant select,insert,delete,update on RWE_TRA_TDOCUMENTAL to www_regweb3;
grant select,insert,delete,update on RWE_TRA_TIPOASUNTO to www_regweb3;
grant select,insert,delete,update on RWE_USUARIO to www_regweb3;
grant select,insert,delete,update on RWE_USUARIO_ENTIDAD to www_regweb3;
grant select on RWE_ALL_SEQ to www_regweb3;
-- FINAL GRANTS

-- INICI LOBS
alter table RWE_HISTORICO_REGISTRO_ENTRADA move lob (RE_ORIGINAL) store as RWE_HIST_REG_ENT_RE_ORI_LOB (tablespace regweb3_lob index RWE_HIST_REG_ENT_RE_ORI_LOB_i);
alter table RWE_HISTORICO_REGISTRO_SALIDA move lob (RS_ORIGINAL) store as RWE_HIST_REG_SAL_LOB (tablespace regweb3_lob index RWE_HIST_REG_SAL_LOB_i);
alter table RWE_REPRO move lob (REPRO) store as RWE_REPRO_REPRO_LOB (tablespace regweb3_lob index RWE_REPRO_REPRO_LOB_i);
-- FINAL LOBS

