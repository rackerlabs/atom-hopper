package org.atomhopper.util.uri;

import org.atomhopper.util.uri.CustomSchemeResolver;
import org.atomhopper.util.uri.URISchemeMapper;
import org.atomhopper.util.uri.UriToUrlResolver;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class CustomSchemeResolverTest {

    public static final String TESTING_URI_SCHEME = "testing";

    public static class WhenResolvingCustomURLSchemeMappings {

        @Test
        public void shouldUseCustomMapperWhenURISchemeMatches() throws Exception {
            final CustomSchemeResolver resolver = new CustomSchemeResolver();
            resolver.addMapper(new TestingSchemeMapper());

            final URI uriWithKnownScheme = new URI(TESTING_URI_SCHEME + ":localhost");
            assertNotNull("Resolver should match against custom schemes and produce the corresponding URL", resolver.toURL(uriWithKnownScheme));
        }

        @Test(expected = MalformedURLException.class)
        public void shouldRejectURIWithUnresolvableScheme() throws Exception {
            final CustomSchemeResolver resolver = new CustomSchemeResolver();

            final URI uriWithUnknownScheme = new URI("scheme:localhost");
            resolver.toURL(uriWithUnknownScheme);
        }
    }

    public static class WhenUsingTheDefaultCustomResolverInstance {

        @Test
        public void shouldMapClasspathUri() throws Exception {
            final UriToUrlResolver resolver = CustomSchemeResolver.newDefaultInstance();

            final URI classpathUri = new URI("classpath:/META-INF/schema/config/bindings.xjb");
            assertNotNull("Resolver should match against custom schemes and produce the corresponding URL", resolver.toURL(classpathUri));
        }
    }

    @Ignore
    public static class TestingSchemeMapper implements URISchemeMapper {

        @Override
        public boolean canMap(URI uriToMatch) {
            return uriToMatch.getScheme().equals(TESTING_URI_SCHEME);
        }

        @Override
        public URL toURL(URI uriToMap) throws MalformedURLException {
            return new URL("http://" + uriToMap.getHost());
        }
    }
}
