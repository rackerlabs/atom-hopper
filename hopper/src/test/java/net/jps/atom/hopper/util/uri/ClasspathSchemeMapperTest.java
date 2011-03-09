package net.jps.atom.hopper.util.uri;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class ClasspathSchemeMapperTest {

    public static class WhenMappingClasspathURIs {

        private URISchemeMapper classpathSchemeMapper;
        private URI classpathURI, httpURI;

        @Before
        public void standUp() throws Exception {
            classpathSchemeMapper = new ClasspathSchemeMapper();

            classpathURI = new URI("classpath:/META-INF/schema/config/bindings.xjb");
            httpURI = new URI("http://localhost");
        }

        @Test
        public void shouldMatchClasspathURIs() {
            assertTrue("ClasspathSchemeMapper should match on valid classpath URI", classpathSchemeMapper.canMap(classpathURI));
        }

        @Test
        public void shouldNotMatchNonClasspathURIs() {
            assertFalse("ClasspathSchemeMapper should not match on invalid classpath URI", classpathSchemeMapper.canMap(httpURI));
        }
    }
}
