package net.jps.atom.hopper.util.uri.template;

import net.jps.atom.hopper.util.uri.template.URITemplateBuilder;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class URITemplateBuilderTest {

    public static class WhenBuildingURITemplates {

        @Test
        public void shouldGenerateCorrectWorkspaceTemplate() {
            final URITemplateBuilder builder = new URITemplateBuilder("domain.com");
            builder.setWorkspaceResource("a");
            builder.setFeedResource("b");

            final String expected = "http://{host=domain.com}{-prefix|:|port}{target_base}/{workspace=a}";

            assertEquals("Should equal", expected, builder.toWorkspaceTemplate().toString());
        }

        @Test
        public void shouldGenerateCorrectFeedTemplate() {
            final URITemplateBuilder builder = new URITemplateBuilder("domain.com");
            builder.setWorkspaceResource("a");
            builder.setFeedResource("b");

            final String expected = "http://{host=domain.com}{-prefix|:|port}{target_base}/{workspace=a}/{feed=b}{-prefix|/entries/|entry}/{-opt|?|lochint,limit}{-join|&|lochint,limit}";

            assertEquals("Should equal", expected, builder.toFeedTemplate().toString());
        }

        @Test
        public void shouldGenerateCorrectCategoriesTemplate() {
            final URITemplateBuilder builder = new URITemplateBuilder("domain.com");
            builder.setWorkspaceResource("a");
            builder.setFeedResource("b");

            final String expected = "http://{host=domain.com}{-prefix|:|port}{target_base}/{workspace=a}/{feed=b}/categories";

            assertEquals("Should equal", expected, builder.toCategoriesTemplate().toString());
        }

        @Test
        public void shouldGenerateCorrectArchivesTemplate() {
            final URITemplateBuilder builder = new URITemplateBuilder("domain.com");
            builder.setWorkspaceResource("a");
            builder.setFeedResource("b");

            final String expected = "http://{host=domain.com}{-prefix|:|port}{target_base}/{workspace=a}/{feed=b}/archives/{-prefix|/|year}{-prefix|/|month}{-prefix|/|day}{-prefix|/|time}";

            assertEquals("Should equal", expected, builder.toArchivesTemplate().toString());
        }
    }
}
