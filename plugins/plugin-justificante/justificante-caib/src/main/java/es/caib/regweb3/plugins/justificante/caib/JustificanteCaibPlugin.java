package es.caib.regweb3.plugins.justificante.caib;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import es.caib.regweb3.model.Interesado;
import es.caib.regweb3.model.RegistroEntrada;
import es.caib.regweb3.model.RegistroSalida;
import es.caib.regweb3.model.utils.AnexoFull;
import es.caib.regweb3.plugins.justificante.IJustificantePlugin;
import es.caib.regweb3.utils.RegwebConstantes;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.fundaciobit.plugins.utils.AbstractPluginProperties;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


/**
 * Created by Fundació BIT.
 * @author jpernia
 * Date: 01/03/2017
 *
 */
public class JustificanteCaibPlugin extends AbstractPluginProperties implements IJustificantePlugin {

    protected final Logger log = Logger.getLogger(getClass());

    /**
     *
     */
    public JustificanteCaibPlugin() {
        super();
    }


    /**
     * @param propertyKeyBase
     * @param properties
     */
    public JustificanteCaibPlugin(String propertyKeyBase, Properties properties) {
        super(propertyKeyBase, properties);
    }

    /**
     * @param propertyKeyBase
     */
    public JustificanteCaibPlugin(String propertyKeyBase) {
        super(propertyKeyBase);
    }


    @Override
    public ByteArrayOutputStream generarJustificante(RegistroEntrada registroEntrada) throws Exception{

        Long idEntidadActiva = registroEntrada.getUsuario().getEntidad().getId();

        Locale locale;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

        // Apply preferences and build metadata.
        Document document = new Document(PageSize.A4);
        FileOutputStream ficheroPdf = new FileOutputStream("fichero.pdf");
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setViewerPreferences(PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage);
        PdfWriter.getInstance(document,ficheroPdf).setInitialLeading(20);

        // Build PDF document.
        document.open();

        //CONFIGURACIONES GENERALES FORMATO PDF
        document.setPageSize(PageSize.A4);
        document.addAuthor("REGWEB3");
        document.addCreationDate();
        document.addCreator("iText library");
        document.newPage();

        // LOGOS
        PdfPTable logos = new PdfPTable(2);
        logos.setWidthPercentage(100);
        // Regweb3
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream fileRW = classLoader.getResourceAsStream("img/logo-regweb3.jpg");
        PdfContentByte cb = writer.getDirectContent();
        Image logoRW = Image.getInstance(cb, ImageIO.read(fileRW), 1);
        logoRW.setAlignment(Element.ALIGN_LEFT);
        logoRW.scaleToFit(100, 110);
        logoRW.setAbsolutePosition(35f, 790f);
        Paragraph parrafo;
        parrafo = new Paragraph("");
        parrafo.setAlignment(Element.ALIGN_LEFT);
        document.add(parrafo);
        document.add(logoRW);
        // Sir
        InputStream fileSIR = classLoader.getResourceAsStream("img/SIR_petit.jpg");
        Image logoSIR = Image.getInstance(cb, ImageIO.read(fileSIR), 1);
        logoSIR.setAlignment(Element.ALIGN_RIGHT);
        logoSIR.scaleToFit(100, 100);
        logoSIR.setAbsolutePosition(460f, 790f);
        parrafo = new Paragraph("");
        parrafo.setAlignment(Element.ALIGN_RIGHT);
        document.add(parrafo);
        document.add(logoSIR);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Agafam idioma pels missatges del justificant. Si no és "ca" o "es", agafa "es" per defecte
        Long idiomaRegistre = registroEntrada.getRegistroDetalle().getIdioma();
        log.info("idioma: " + idiomaRegistre);
        if(idiomaRegistre.equals(RegwebConstantes.IDIOMA_CATALAN_ID) || idiomaRegistre.equals(RegwebConstantes.IDIOMA_CASTELLANO_ID)) {
            locale = new Locale(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idiomaRegistre));
            log.info("locale: " + locale);
            log.info("entra_1");
        } else{
            locale = new Locale("es");
            log.info("entra_2");
        }

        String denominacionOficina = registroEntrada.getOficina().getDenominacion();
        String codigoOficina = registroEntrada.getOficina().getCodigo();
        String numeroRegistroFormateado = registroEntrada.getNumeroRegistroFormateado();
        Long tipoDocumentacionFisica = registroEntrada.getRegistroDetalle().getTipoDocumentacionFisica();
        String extracte = registroEntrada.getRegistroDetalle().getExtracto();
        String nomDesti = registroEntrada.getDestino().getNombreCompleto();
        String expedient = registroEntrada.getRegistroDetalle().getExpediente();
        Date fechaRegistro = registroEntrada.getFecha();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataRegistre = formatDate.format(fechaRegistro);

        // TITULO Y REGISTRO
        informacioRegistre(locale, document, idEntidadActiva, denominacionOficina, idiomaRegistre, codigoOficina, dataRegistre,
                numeroRegistroFormateado, tipoDocumentacionFisica);

        // INTERESADOS
        List<Interesado> interesados = registroEntrada.getRegistroDetalle().getInteresados();
        llistarInteressats(interesados, locale, document);

        // INFORMACION REGISTRO
        adicionalRegistre(locale, document, extracte, nomDesti, expedient, registroEntrada.getClass().getSimpleName());

        // ADJUNTOS
        List<AnexoFull> anexos = registroEntrada.getRegistroDetalle().getAnexosFull();
        llistarAnnexes(anexos, idiomaRegistre, locale, document, idEntidadActiva, denominacionOficina);

        // CSV Y TEXTO VERTICAL
//            csvRegistre(locale, document, dataRegistre, idEntidadActiva, numeroRegistroFormateado, idiomaRegistre, writer, missatges);

        document.close();

        return baos;
    }


    @Override
    public ByteArrayOutputStream generarJustificante(RegistroSalida registroSalida) throws Exception{

        Long idEntidadActiva = registroSalida.getUsuario().getEntidad().getId();

        Locale locale;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

        // Apply preferences and build metadata.
        Document document = new Document(PageSize.A4);
        FileOutputStream ficheroPdf = new FileOutputStream("fichero.pdf");
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setViewerPreferences(PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage);
        PdfWriter.getInstance(document,ficheroPdf).setInitialLeading(20);

        // Build PDF document.
        document.open();

        //CONFIGURACIONES GENERALES FORMATO PDF
        document.setPageSize(PageSize.A4);
        document.addAuthor("REGWEB3");
        document.addCreationDate();
        document.addCreator("iText library");
        document.newPage();

        // LOGOS
        PdfPTable logos = new PdfPTable(2);
        logos.setWidthPercentage(100);
        // Regweb3
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream fileRW = classLoader.getResourceAsStream("img/logo-regweb3.jpg");
        PdfContentByte cb = writer.getDirectContent();
        Image logoRW = Image.getInstance(cb, ImageIO.read(fileRW), 1);
        logoRW.setAlignment(Element.ALIGN_LEFT);
        logoRW.scaleToFit(100, 110);
        logoRW.setAbsolutePosition(35f, 790f);
        Paragraph parrafo;
        parrafo = new Paragraph("");
        parrafo.setAlignment(Element.ALIGN_LEFT);
        document.add(parrafo);
        document.add(logoRW);
        // Sir
        InputStream fileSIR = classLoader.getResourceAsStream("img/SIR_petit.jpg");
        Image logoSIR = Image.getInstance(cb, ImageIO.read(fileSIR), 1);
        logoSIR.setAlignment(Element.ALIGN_RIGHT);
        logoSIR.scaleToFit(100, 100);
        logoSIR.setAbsolutePosition(460f, 790f);
        parrafo = new Paragraph("");
        parrafo.setAlignment(Element.ALIGN_RIGHT);
        document.add(parrafo);
        document.add(logoSIR);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Agafam idioma pels missatges del justificant. Si no és "ca" o "es", agafa "es" per defecte
        Long idiomaRegistre = registroSalida.getRegistroDetalle().getIdioma();
        if(idiomaRegistre.equals(RegwebConstantes.IDIOMA_CATALAN_ID) || idiomaRegistre.equals(RegwebConstantes.IDIOMA_CASTELLANO_ID)) {
            locale = new Locale(RegwebConstantes.CODIGO_BY_IDIOMA_ID.get(idiomaRegistre));
        } else{
            locale = new Locale("es");
        }

        String denominacionOficina = registroSalida.getOficina().getDenominacion();
        String codigoOficina = registroSalida.getOficina().getCodigo();
        String numeroRegistroFormateado = registroSalida.getNumeroRegistroFormateado();
        Long tipoDocumentacionFisica = registroSalida.getRegistroDetalle().getTipoDocumentacionFisica();
        String extracte = registroSalida.getRegistroDetalle().getExtracto();
        String nomOrigen = registroSalida.getOrigen().getNombreCompleto();
        String expedient = registroSalida.getRegistroDetalle().getExpediente();
        Date fechaRegistro = registroSalida.getFecha();
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dataRegistre = formatDate.format(fechaRegistro);


        // TITULO Y REGISTRO
        informacioRegistre(locale, document, idEntidadActiva, denominacionOficina, idiomaRegistre, codigoOficina, dataRegistre,
                numeroRegistroFormateado, tipoDocumentacionFisica);

        // INTERESADOS
        List<Interesado> interesados = registroSalida.getRegistroDetalle().getInteresados();
        llistarInteressats(interesados, locale, document);

        // INFORMACION REGISTRO
        adicionalRegistre(locale, document, extracte, nomOrigen, expedient, registroSalida.getClass().getSimpleName());

        // ADJUNTOS
//            List<Anexo> anexos = anexoEjb.getByRegistroSalida(registroSalida);
        List<AnexoFull> anexos = registroSalida.getRegistroDetalle().getAnexosFull();
        llistarAnnexes(anexos, idiomaRegistre, locale, document, idEntidadActiva, denominacionOficina);

        // CSV Y TEXTO VERTICAL
//            csvRegistre(locale, document, dataRegistre, idEntidadActiva, numeroRegistroFormateado, idiomaRegistre, writer, missatges);


        document.close();

        return baos;
    }


    // Lista los anexos tanto para el registro de entrada como el de salida
    protected void llistarAnnexes(List<AnexoFull> anexos, Long idiomaRegistre, Locale locale, Document document,
                                  Long idEntidadActiva, String denominacio) throws Exception {

        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

        if(anexos.size()>0) {
            // Creamos estilo para el título Adjuntos
            PdfPTable titolAnnexe = new PdfPTable(1);
            titolAnnexe.setWidthPercentage(100);
            PdfPCell cellAnnexe = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.adjuntos"), font8Bold));
            cellAnnexe.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cellAnnexe.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellAnnexe.setBorder(Rectangle.BOTTOM);
            cellAnnexe.setBorderColor(BaseColor.BLACK);
            cellAnnexe.setBorderWidth(2f);
            titolAnnexe.addCell(cellAnnexe);
            document.add(titolAnnexe);
            document.add(new Paragraph(" "));

            // Añadimos los campos de la Información
            PdfPTable taulaAnnexe = new PdfPTable(new float[]{20, 10, 15, 15, 25, 15});
            taulaAnnexe.setWidthPercentage(100);
            PdfPCell cellInfoAnnexe = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.nombreAdjunto"), font8));
            cellInfoAnnexe.setBackgroundColor(BaseColor.WHITE);
            cellInfoAnnexe.setBorderColor(BaseColor.BLACK);
            cellInfoAnnexe.setBorderWidth(1f);
            cellInfoAnnexe.setHorizontalAlignment(Element.ALIGN_MIDDLE);
            taulaAnnexe.addCell(cellInfoAnnexe);
            taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.tamaño"), font8)));
            taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.validez"), font8)));
            taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.tipoAdjunto"), font8)));
            taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.hash"), font8)));
            taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.observacionesAdjunto"), font8)));

            PdfPCell cellInfoAnnexe2 = new PdfPCell(new Paragraph("", font8));
            cellInfoAnnexe2.setBackgroundColor(BaseColor.WHITE);
            cellInfoAnnexe2.setBorderColor(BaseColor.BLACK);
            cellInfoAnnexe2.setBorderWidth(1f);
            cellInfoAnnexe2.setHorizontalAlignment(Element.ALIGN_LEFT);
            for(AnexoFull anexo : anexos) {
                cellInfoAnnexe2 = new PdfPCell(new Paragraph(anexo.getAnexo().getTitulo(), font8));
                taulaAnnexe.addCell(cellInfoAnnexe2);
                if(anexo.getAnexo().getCustodiaID()!=null) {
                    taulaAnnexe.addCell(new PdfPCell(new Paragraph(String.valueOf(anexo.getAnexo().getCustodiaID().getBytes().length), font8)));
                } else{
                    taulaAnnexe.addCell(new PdfPCell(new Paragraph(String.valueOf(anexo.getAnexo().getFirma().length), font8)));
                }
                taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"tipoValidezDocumento." + anexo.getAnexo().getValidezDocumento()), font8)));
                taulaAnnexe.addCell(new PdfPCell(new Paragraph(tradueixMissatge(locale,"tipoDocumento.0" + anexo.getAnexo().getTipoDocumento()), font8)));
                taulaAnnexe.addCell(new PdfPCell(new Paragraph(new String(Base64.encodeBase64(anexo.getAnexo().getHash()),"UTF-8"), font8)));
                taulaAnnexe.addCell(new PdfPCell(new Paragraph(anexo.getAnexo().getObservaciones(), font8)));
            }
            document.add(taulaAnnexe);

            // Pie de anexo
            PdfPTable peuAnnexe = new PdfPTable(1);
            peuAnnexe.setWidthPercentage(100);
//            PdfPCell cellPeuAnnexe = new PdfPCell(new Paragraph(denominacio + " " + PropiedadGlobalUtil.getDeclaracioJustificant(idEntidadActiva, idiomaRegistre), font8));
            PdfPCell cellPeuAnnexe = new PdfPCell(new Paragraph(denominacio + " " + tradueixMissatge(locale,"justificante.mensaje.declaracion"), font8));
            cellPeuAnnexe.setBackgroundColor(BaseColor.WHITE);
            cellPeuAnnexe.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellPeuAnnexe.setBorder(Rectangle.TOP);
            cellPeuAnnexe.setBorderColor(BaseColor.BLACK);
            cellPeuAnnexe.setBorderWidth(1f);
            peuAnnexe.addCell(cellPeuAnnexe);
            document.add(peuAnnexe);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            PdfPTable titolLlei = new PdfPTable(1);
            titolLlei.setWidthPercentage(100);
//            PdfPCell cellLlei = new PdfPCell(new Paragraph(PropiedadGlobalUtil.getLleiJustificant(idEntidadActiva, idiomaRegistre), font8));
            PdfPCell cellLlei = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.mensaje.ley"), font8));
            cellLlei.setBackgroundColor(BaseColor.WHITE);
            cellLlei.setHorizontalAlignment(Element.ALIGN_LEFT);
            cellLlei.setBorderColor(BaseColor.WHITE);
            cellLlei.setBorderWidth(0f);
            titolLlei.addCell(cellLlei);
            document.add(titolLlei);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
        }
    }

    // Lista los interesados y representantes tanto para el registro de entrada como el de salida
    protected void llistarInteressats(List<Interesado> interesados, Locale locale, Document document) throws Exception {

        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

        // Creamos estilo para el título Interesado
        PdfPTable titolInteressat = new PdfPTable(1);
        titolInteressat.setWidthPercentage(100);
        PdfPCell cellInteressat = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.interesado"), font8Bold));
        cellInteressat.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellInteressat.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellInteressat.setBorder(Rectangle.BOTTOM);
        cellInteressat.setBorderColor(BaseColor.BLACK);
        cellInteressat.setBorderWidth(2f);
        titolInteressat.addCell(cellInteressat);

        // Creamos estilo para el título Representante
        PdfPTable titolRepresentant = new PdfPTable(1);
        titolRepresentant.setWidthPercentage(90);

        PdfPTable taulaInteresado = new PdfPTable(new float[] { 15, 30, 15, 30 });
        // Añadimos una entrada para cada interesado
        for(Interesado interesado : interesados) {
            // Añadimos título
            if(!interesado.getIsRepresentante()) {
                document.add(titolInteressat);
                // Añadimos campos del interesado
                taulaInteresado.setWidthPercentage(100);
                taulaInteresado.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
                taulaInteresado.getDefaultCell().setBorder(0);
                taulaInteresado.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.identificacion"), font8));
                taulaInteresado.addCell(new Paragraph(interesado.getDocumento(), font8));
                if(interesado.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_PERSONA_FISICA)) {
                    taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.nombre"), font8));
                    taulaInteresado.addCell(new Paragraph(interesado.getNombreCompleto(), font8));
                } else if(interesado.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_PERSONA_JURIDICA)) {
                    taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.razonSocial"), font8));
                    taulaInteresado.addCell(new Paragraph(interesado.getRazonSocial(), font8));
                } else if(interesado.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_ADMINISTRACION)) {
                    taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.organismo"), font8));
                    taulaInteresado.addCell(new Paragraph(interesado.getRazonSocial(), font8));
                }
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.pais"), font8));
                if(interesado.getPais() != null) {
                    taulaInteresado.addCell(new Paragraph(interesado.getPais().getDescripcionPais(), font8));
                } else{ taulaInteresado.addCell(""); }
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.municipio"), font8));
                if(interesado.getLocalidad() != null && interesado.getCp() != null) {
                    taulaInteresado.addCell(new Paragraph(interesado.getCp() + " - " + interesado.getLocalidad().getNombre(), font8));
                } else{ taulaInteresado.addCell(""); }
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.provincia"), font8));
                if(interesado.getProvincia() != null) {
                    taulaInteresado.addCell(new Paragraph(interesado.getProvincia().getDescripcionProvincia(), font8));
                } else{ taulaInteresado.addCell(""); }
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.direccion"), font8));
                taulaInteresado.addCell(new Paragraph(interesado.getDireccion(), font8));
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.telefono"), font8));
                taulaInteresado.addCell(new Paragraph(interesado.getTelefono(), font8));
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.canalNot"), font8));
                if(interesado.getCanal() != null) {
                    taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"canalNotificacion." + interesado.getCanal()), font8));
                } else{ taulaInteresado.addCell(""); }
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.correo"), font8));
                taulaInteresado.addCell(new Paragraph(interesado.getEmail(), font8));
                taulaInteresado.addCell(new Paragraph(tradueixMissatge(locale,"justificante.observaciones"), font8));
                taulaInteresado.addCell(new Paragraph(interesado.getObservaciones(), font8));

                document.add(taulaInteresado);
                // Vaciamos el contenido del interesado para rellenarlo con uno nuevo
                taulaInteresado.deleteBodyRows();
                document.add(new Paragraph(" "));
                // Si el interesado tiene representante
                if(interesado.getRepresentante() != null) {
                    // Recorremos todos los interesados para buscar el reresentante
                    for(Interesado representante : interesados) {
                        // Encuentra su representante y lo muestra
                        if (interesado.getRepresentante().getId().equals(representante.getId())) {
                            PdfPCell cellRepresentant = new PdfPCell(new Paragraph(tradueixMissatge(locale, "justificante.representante") + " de " + interesado.getNombreCompleto(), font8Bold));
                            cellRepresentant.setBackgroundColor(BaseColor.WHITE);
                            cellRepresentant.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cellRepresentant.setBorder(Rectangle.BOTTOM);
                            cellRepresentant.setBorderColor(BaseColor.BLACK);
                            cellRepresentant.setBorderWidth(2f);
                            titolRepresentant.addCell(cellRepresentant);
                            document.add(titolRepresentant);
                            PdfPTable taulaRepresentant = new PdfPTable(new float[] { 13, 28, 13, 28 });
                            // Añadimos campos del Representante
                            taulaRepresentant.setWidthPercentage(90);
                            taulaRepresentant.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
                            taulaRepresentant.getDefaultCell().setBorder(0);
                            taulaRepresentant.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.identificacion"), font8));
                            taulaRepresentant.addCell(new Paragraph(representante.getDocumento(), font8));
                            if(representante.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_PERSONA_FISICA)) {
                                taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.nombre"), font8));
                                taulaRepresentant.addCell(new Paragraph(representante.getNombreCompleto(), font8));
                            } else if(representante.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_PERSONA_JURIDICA)) {
                                taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.razonSocial"), font8));
                                taulaRepresentant.addCell(new Paragraph(representante.getRazonSocial(), font8));
                            } else if(representante.getTipo().equals(RegwebConstantes.TIPO_INTERESADO_ADMINISTRACION)) {
                                taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.organismo"), font8));
                                taulaRepresentant.addCell(new Paragraph(representante.getRazonSocial(), font8));
                            }
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.pais"), font8));
                            if(representante.getPais() != null) {
                                taulaRepresentant.addCell(new Paragraph(representante.getPais().getDescripcionPais(), font8));
                            } else{ taulaRepresentant.addCell(""); }
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.municipio"), font8));
                            if(representante.getLocalidad() != null && representante.getCp() != null) {
                                taulaRepresentant.addCell(new Paragraph(representante.getCp() + " - " + representante.getLocalidad().getNombre(), font8));
                            } else{ taulaRepresentant.addCell(""); }
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.provincia"), font8));
                            if(representante.getProvincia() != null) {
                                taulaRepresentant.addCell(new Paragraph(representante.getProvincia().getDescripcionProvincia(), font8));
                            } else{ taulaRepresentant.addCell(""); }
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.direccion"), font8));
                            taulaRepresentant.addCell(new Paragraph(representante.getDireccion(), font8));
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.telefono"), font8));
                            taulaRepresentant.addCell(new Paragraph(representante.getTelefono(), font8));
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.canalNot"), font8));
                            if(representante.getCanal() != null) {
                                taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"canalNotificacion." + interesado.getCanal()), font8));
                            } else{ taulaRepresentant.addCell(""); }
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.correo"), font8));
                            taulaRepresentant.addCell(new Paragraph(representante.getEmail(), font8));
                            taulaRepresentant.addCell(new Paragraph(tradueixMissatge(locale,"justificante.observaciones"), font8));
                            taulaRepresentant.addCell(new Paragraph(representante.getObservaciones(), font8));
                            document.add(taulaRepresentant);
                            // Vaciamos el contenido del representante para rellenarlo con uno nuevo
                            taulaRepresentant.deleteBodyRows();
                            document.add(new Paragraph(" "));
                        }
                    }
                }
            }
        }
        document.add(new Paragraph(" "));
    }

    // Añade el título y la información de registro
    protected void informacioRegistre(Locale locale, Document document, Long idEntidadActiva, String denominacionOficina,
                                      Long idiomaRegistre, String codigoOficina, String dataRegistre, String numeroRegistroFormateado,
                                      Long tipoDocumentacionFisica) throws Exception {

        Font font16Bold = FontFactory.getFont(FontFactory.HELVETICA, 16, Font.BOLD);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

        document.addTitle(tradueixMissatge(locale,"justificante.anexo.titulo"));
        PdfPTable titulo = new PdfPTable(1);
        titulo.setWidthPercentage(100);
//        PdfPCell cellTitulo= new PdfPCell(new Paragraph(PropiedadGlobalUtil.getTitolJustificant(idEntidadActiva, idiomaRegistre), font16Bold));
        PdfPCell cellTitulo= new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.mensaje.titulo"), font16Bold));
        cellTitulo.setBackgroundColor(BaseColor.WHITE);
        cellTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTitulo.setBorder(Rectangle.BOTTOM);
        cellTitulo.setBorderColor(BaseColor.BLACK);
        cellTitulo.setBorderWidth(2f);
        titulo.addCell(cellTitulo);
        document.add(titulo);

        // REGISTRO
        PdfPTable taulaRegistre = new PdfPTable(new float[] { 1, 3 });
        taulaRegistre.setWidthPercentage(100);
        taulaRegistre.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        taulaRegistre.getDefaultCell().setBorder(0);
        taulaRegistre.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        taulaRegistre.addCell(new Paragraph(tradueixMissatge(locale,"justificante.oficina"), font8));
        String oficina = denominacionOficina + " - " + codigoOficina;
        taulaRegistre.addCell(new Paragraph(oficina, font8));
        taulaRegistre.addCell(new Paragraph(tradueixMissatge(locale,"justificante.fechaPresentacion"), font8));
        taulaRegistre.addCell(new Paragraph(dataRegistre, font8));
        taulaRegistre.addCell(new Paragraph(tradueixMissatge(locale,"justificante.numRegistro"), font8));
        taulaRegistre.addCell(new Paragraph(numeroRegistroFormateado, font8));
        taulaRegistre.addCell(new Paragraph(tradueixMissatge(locale,"justificante.docFisica"), font8));
        String docFisica = tradueixMissatge(locale,"tipoDocumentacionFisica." + tipoDocumentacionFisica);
        taulaRegistre.addCell(new Paragraph(docFisica, font8));
        document.add(taulaRegistre);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

    }

    // Añade más información al registro
    protected void adicionalRegistre(Locale locale, Document document, String extracte, String nomDesti, String expedient,
                                     String tipoRegistro) throws Exception {

        Font font8Bold = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);

        // Creamos estilo para el título Información
        PdfPTable titolInformacio = new PdfPTable(1);
        titolInformacio.setWidthPercentage(100);
        PdfPCell cellInformacio = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.informacion"), font8Bold));
        cellInformacio.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cellInformacio.setHorizontalAlignment(Element.ALIGN_LEFT);
        cellInformacio.setBorder(Rectangle.BOTTOM);
        cellInformacio.setBorderColor(BaseColor.BLACK);
        cellInformacio.setBorderWidth(2f);
        titolInformacio.addCell(cellInformacio);
        document.add(titolInformacio);

        // Añadimos los campos de la Información
        PdfPTable taulaInformacio = new PdfPTable(new float[] { 25, 75 });
        taulaInformacio.setWidthPercentage(100);
        taulaInformacio.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
        taulaInformacio.getDefaultCell().setBorder(0);
        taulaInformacio.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.tipoAsiento"), font8));
        if(tipoRegistro.equals("RegistroEntrada")) {
            taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.entrada"), font8));
        }else{
            taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.salida"), font8));
        }
        taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.resumen"), font8));
        taulaInformacio.addCell(new Paragraph(extracte, font8));
        taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.unidad"), font8));
        taulaInformacio.addCell(new Paragraph(nomDesti, font8));
        taulaInformacio.addCell(new Paragraph(tradueixMissatge(locale,"justificante.expediente"), font8));
        taulaInformacio.addCell(new Paragraph(expedient, font8));
        document.add(taulaInformacio);
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

    }

    // Añade la información del pie, CSV, etc
    protected void csvRegistre(Locale locale, Document document, String dataRegistre, Long idEntidadActiva,
                               String numeroRegistroFormateado, Long idiomaRegistre, PdfWriter writer) throws Exception {

        Font font9Underline = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.UNDERLINE);
        Font font9 = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font font8 = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font font7 = FontFactory.getFont(FontFactory.HELVETICA, 7);

        // Añadimos la separación
        PdfPTable csv = new PdfPTable(1);
        csv.setWidthPercentage(100);
        PdfPCell cellCsv= new PdfPCell(new Paragraph("", font8));
        cellCsv.setBackgroundColor(BaseColor.WHITE);
        cellCsv.setBorder(Rectangle.BOTTOM);
        cellCsv.setBorderColor(BaseColor.BLACK);
        cellCsv.setBorderWidth(2f);
        csv.addCell(cellCsv);
        document.add(csv);
        document.add(new Paragraph(" "));
        // Añadimos los campos
        PdfPTable taulaCsv = new PdfPTable(new float[]{25, 45, 30 });
        taulaCsv.setWidthPercentage(100);
        PdfPCell cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.ambito"), font9Underline));
        cellInfoCsv.setBackgroundColor(BaseColor.WHITE);
        cellInfoCsv.setBorderWidth(0f);
        cellInfoCsv.setBorderColor(BaseColor.WHITE);
        cellInfoCsv.setHorizontalAlignment(Element.ALIGN_LEFT);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.csv"), font9Underline));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.fechaDocumento"), font9Underline));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.aplicacion"), font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph("REGWEB·XXXXXXXXXX", font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(dataRegistre, font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.numRegistro"), font9Underline));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.validacion"), font9Underline));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.cif"), font9Underline));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph(numeroRegistroFormateado, font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
//        cellInfoCsv = new PdfPCell(new Paragraph(PropiedadGlobalUtil.getSedeJustificant(idEntidadActiva, idiomaRegistre), font9));
        cellInfoCsv = new PdfPCell(new Paragraph(tradueixMissatge(locale,"justificante.mensaje.sede"), font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        cellInfoCsv = new PdfPCell(new Paragraph("CIF interessat?", font9));
        cellInfoCsv.setBorderWidth(0f);
        taulaCsv.addCell(cellInfoCsv);
        document.add(taulaCsv);

        // Código de barras
        PdfContentByte cb = writer.getDirectContent();
        Barcode128 code128 = new Barcode128();
        code128.setCode("REGWEBXXXXXXXXXX");
        code128.setCodeType(Barcode128.CODE128);
        Image code128Image = code128.createImageWithBarcode(cb, null, null);
        code128Image.scalePercent(125);
        code128Image.setAlignment(Element.ALIGN_MIDDLE);
        document.add(code128Image);

        // Texto Vertical
//        Phrase p = new Phrase(PropiedadGlobalUtil.getTextVerticalJustificant(idEntidadActiva, idiomaRegistre) + PropiedadGlobalUtil.getSedeJustificant(idEntidadActiva, idiomaRegistre), font7);
        Phrase p = new Phrase(tradueixMissatge(locale,"justificante.mensaje.textovertical") + tradueixMissatge(locale,"justificante.mensaje.sede"), font7);
        ColumnText.showTextAligned(cb, Element.ALIGN_MIDDLE, p, 20, 30, 90);

    }


    protected String tradueixMissatge(Locale locale, String missatge) throws Exception {

        try {
            ResourceBundle justificantemissatges = ResourceBundle.getBundle("justificantemissatges", locale);
            return new String(justificantemissatges.getString(missatge).getBytes("ISO-8859-1"), "UTF-8");
        }catch (Exception e) {
            try{
                ResourceBundle logicmissatges = ResourceBundle.getBundle("logicmissatges", locale);
                return new String(logicmissatges.getString(missatge).getBytes("ISO-8859-1"), "UTF-8");
            }catch (Exception e2){
                return "{"+locale+"_"+missatge+"}";
            }
        }

    }


}