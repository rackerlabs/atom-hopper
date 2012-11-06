package org.atomhopper.util.config.resource.file;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(Enclosed.class)
public class FileConfigurationResourceTest {

    public static class WhenUsingConfigFiles {

        String someFakeFileLocation;

        @Before
        public void setUp() throws Exception {
            someFakeFileLocation = "/noSuchLocation";
        }

        @Rule
        public ExpectedException exception = ExpectedException.none();

        @Test
        public void shouldThrowIOException() throws Exception {
            exception.expect(IOException.class);
            new FileConfigurationResource(someFakeFileLocation).getInputStream();
        }
    }
}