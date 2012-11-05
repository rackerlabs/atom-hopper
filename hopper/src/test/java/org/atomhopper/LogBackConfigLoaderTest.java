package org.atomhopper;

import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(Enclosed.class)
public class LogBackConfigLoaderTest {

    public static class WhenLoadingConfig {

        String someFakeFileLocation;

        @Before
        public void setUp() throws Exception {
            someFakeFileLocation = "/noSuchLocation";
        }

        @Rule
        public ExpectedException exception = ExpectedException.none();

        @Test
        public void shouldThrowIOException() throws IOException, JoranException {
            exception.expect(IOException.class);
            new LogBackConfigLoader(someFakeFileLocation);
        }
    }
}
