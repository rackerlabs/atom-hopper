package net.jps.atom.hopper.util.config;

import net.jps.atom.hopper.util.config.AbstractConfigurationParser;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class AbstractConfigurationParserTest {

    public static final String EXPECTED_CONFIGURATION = "some config thing";

    public static class WhenReadingConfigurations {

        @Test(expected = IllegalStateException.class)
        public void shouldRejectReadingFromNullConfigurationResources() {
            new TestableConfigurationParser().read();
        }
    }

    @Ignore
    public static class TestableConfigurationParser extends AbstractConfigurationParser<String> {

        public TestableConfigurationParser() {
            super(String.class);
        }

        @Override
        protected String readConfiguration() {
            return EXPECTED_CONFIGURATION;
        }
    }
}
