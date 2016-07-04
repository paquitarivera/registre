
--ROL
INSERT INTO RWE_ROL (id,nombre,descripcion,orden) VALUES (1,'RWE_SUPERADMIN','Administrador',1);
INSERT INTO RWE_ROL (id,nombre,descripcion,orden) VALUES (2,'RWE_ADMIN','Admin. Entitat',2);
INSERT INTO RWE_ROL (id,nombre,descripcion,orden) VALUES (3,'RWE_USUARI','Operador',3);

--PROPIEDADES GLOBALES
INSERT INTO RWE_PROPIEDADGLOBAL (id,clave,valor,descripcion,entidad) VALUES (RWE_ALL_SEQ.nextVal,'es.caib.regweb3.defaultlanguage','ca','Idioma por defecto',null);
INSERT INTO RWE_PROPIEDADGLOBAL (id,clave,valor,descripcion,entidad) VALUES (RWE_ALL_SEQ.nextVal,'es.caib.regweb3.archivos.path','ca','Directorio base para los archivos generales',null);
INSERT INTO RWE_PROPIEDADGLOBAL (id,clave,valor,descripcion,entidad) VALUES (RWE_ALL_SEQ.nextVal,'es.caib.regweb3.showtimestamp','false','Muestra la fecha/hora de compilación de la aplicación',null);
INSERT INTO RWE_PROPIEDADGLOBAL (id,clave,valor,descripcion,entidad) VALUES (RWE_ALL_SEQ.nextVal,'es.caib.regweb3.iscaib','false','Indica si es una instalación en la CAIB',null);