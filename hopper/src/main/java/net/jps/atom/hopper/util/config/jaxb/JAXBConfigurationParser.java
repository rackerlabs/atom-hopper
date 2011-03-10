package net.jps.atom.hopper.util.config.jaxb;

import com.rackspace.cloud.commons.config.ConfigurationParserException;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import net.jps.atom.hopper.util.config.AbstractConfigurationParser;

/**
 *
 *
 */
public final class JAXBConfigurationParser<T> extends AbstractConfigurationParser<T> {

    private static final Logger LOG = new RCLogger(JAXBConfigurationParser.class);
    
    private final JAXBContext jaxbContext;

    public JAXBConfigurationParser(Class<T> configClass, Class<?>... objectFactories) {
        super(configClass);

        try {
            jaxbContext = JAXBContext.newInstance(objectFactories);
        } catch (JAXBException jaxbex) {
            throw LOG.newException("Failed to create the JAXB context required for configuration marshalling", jaxbex, ConfigurationParserException.class);
        }
    }

    @Override
    protected T readConfiguration() {
        T rootConfigElement;

        try {
            final Object unmarshaledObj = jaxbContext.createUnmarshaller().unmarshal(getConfigurationResource().getInputStream());

            if (getConfigurationClass().isInstance(unmarshaledObj)) {
                rootConfigElement = (T) unmarshaledObj;
            } else if (unmarshaledObj instanceof JAXBElement) {
                rootConfigElement = ((JAXBElement<T>) unmarshaledObj).getValue();
            } else {
                throw LOG.newException("Failed to read config", ConfigurationParserException.class);
            }
        } catch (Exception ex) {
            throw LOG.wrapError(ex, ConfigurationParserException.class);
        }

        return rootConfigElement;
    }
}
