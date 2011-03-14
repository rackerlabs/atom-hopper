package net.jps.atom.hopper.util.uri;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class URITemplateBuilderTest {

    public static class WhenBuildingURITemplates {

        @Test
        public void shouldGenerateCorrectFeedTemplate() {
            final URITemplateBuilder builder = new URITemplateBuilder("domain.com");
            builder.setWorkspaceResource("a");
            builder.setFeedResource("b");

            final String expected = "http://{host=domain.com}{-prefix|:|port}/{target_base}/{workspace=a}/{feed=b}{-prefix|/entries/|entry}/{-opt|?|categories,marker,limit}{-join|&|categories,marker,limit}";

            assertEquals("Should equal", expected, builder.toFeedTemplate().toString());
        }
    }
}
