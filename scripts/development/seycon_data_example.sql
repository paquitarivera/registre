--USUARIOS
INSERT INTO SC_WL_USUARI (USU_CODI,USU_PASS,USU_NOM) VALUES ('superadmin','superadmin','Usuario rwe_superadmin');
INSERT INTO SC_WL_USUARI (USU_CODI,USU_PASS,USU_NOM) VALUES ('admin','admin','Usuario rwe_admin');
INSERT INTO SC_WL_USUARI (USU_CODI,USU_PASS,USU_NOM) VALUES ('usuari','usuari','Usuario rwe_usuari');

--USUARIOS-GRUPOS
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('superadmin','RWE_SUPERADMIN');
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('superadmin','RWE_ADMIN');
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('superadmin','RWE_USUARI');
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('admin','RWE_ADMIN');
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('admin','RWE_USUARI');
INSERT INTO SC_WL_USUGRU (UGR_CODUSU,UGR_CODGRU) VALUES ('usuari','RWE_USUARI');