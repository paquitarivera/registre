package es.caib.regweb3.utils.test;

import es.caib.regweb3.utils.StringUtils;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void capitalizeWords() throws Exception {

        System.out.println(StringUtils.capitailizeWord("TALLERES HNOS. J Y J. TORRES, CB", true));
        System.out.println(StringUtils.capitailizeWord("Dr. juan carLos De lA martínez", false));
    }
}