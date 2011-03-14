package net.jps.atom.hopper.util.uri;

import java.util.Map;
import java.util.HashMap;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class TemplateTargetBuilderTest {

    public static final Object ARCHIVE = new Object(), COLLECTION = new Object();

    public static class WhenBuildingTemplates {

        private TemplateTargetBuilder targetBuilder;
        private RequestContext requestContext;

        @Before
        public void standUp() {
            requestContext = mock(RequestContext.class);
            when(requestContext.getTargetBasePath()).thenReturn("/root/context");

            targetBuilder = new TemplateTargetBuilder();
            targetBuilder.setTemplate(ARCHIVE, "{target_base}/{workspace}/{feed}{-prefix|/|entry}/{-opt|?|categories,marker,limit}{-join|&|categories,marker,limit}");
        }

        @Test
        public void shouldGenerateExpectedURL() {
            final Map<String, String> parameterMap = new HashMap<String, String>();
            parameterMap.put("workspace", "a");
            parameterMap.put("feed", "b");
            parameterMap.put("marker", "12345");
            parameterMap.put("entry", "c");
            parameterMap.put("limit", "5");

            final String expected = "/root/context/a/b/c/?marker=12345&limit=5";

            assertEquals("URL built from template should match expected", expected, targetBuilder.urlFor(requestContext, ARCHIVE, parameterMap));
        }
    }
}
