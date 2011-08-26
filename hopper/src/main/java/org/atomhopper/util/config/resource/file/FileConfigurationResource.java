package org.atomhopper.util.config.resource.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.atomhopper.util.config.resource.ConfigurationResource;

public class FileConfigurationResource implements ConfigurationResource {

    private final File configurationFile;

    public FileConfigurationResource(String resourcePath) {
        configurationFile = new File(resourcePath);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(configurationFile);
    }
}
