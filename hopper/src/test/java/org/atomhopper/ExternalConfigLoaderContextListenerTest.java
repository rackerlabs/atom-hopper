package org.atomhopper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class ExternalConfigLoaderContextListenerTest {

    public static class WhenInitializingContext {

        ExternalConfigLoaderContextListener externalConfigLoaderContextListener;

        @Rule
        public ExpectedException exception = ExpectedException.none();

        @Test
        public void shouldThrowErrorOnInitialization() throws Exception {
            exception.expect(ClassFormatError.class);
            externalConfigLoaderContextListener = new ExternalConfigLoaderContextListener();
            externalConfigLoaderContextListener.contextInitialized(mock(ServletContextEvent.class));
        }
    }
}
