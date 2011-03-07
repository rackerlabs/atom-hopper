/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.config;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import net.jps.atom.hopper.config.v1_0.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author jhopper
 */
@RunWith(Enclosed.class)
public class SchemaTest {

    public static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    public static class WhenValidating {

        private JAXBContext jaxbContext;
        private Unmarshaller jaxbUnmarshaller;

        @Before
        public void standUp() throws Exception {
            jaxbContext = JAXBContext.newInstance(
                    net.jps.atom.hopper.config.v1_0.ObjectFactory.class);

            jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            jaxbUnmarshaller.setSchema(SCHEMA_FACTORY.newSchema(
                    new StreamSource[]{
                        new StreamSource(SchemaTest.class.getResourceAsStream("/META-INF/schema/config/atom-hopper-config.xsd"))
                    }));
        }

        @Test
        public void staticExampleShouldMatchSchema() throws Exception {
            final Configuration cfg = jaxbUnmarshaller.unmarshal(
                    new StreamSource(SchemaTest.class.getResourceAsStream("/META-INF/schema/examples/config/feed-server-config.xml")), Configuration.class).getValue();

            assertFalse("Configured workspace list should have at least one element", cfg.getWorkspace().isEmpty());
        }
    }
}
