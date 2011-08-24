package org.atomhopper.util.config.resource;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigurationResource {

    InputStream getInputStream() throws IOException;
}
