package org.atomhopper.servlet;

/**
 * Enumeration for any initialization parameters that the servlet may expect.
 */
public enum ServletInitParameter {
    
    CONTEXT_ADAPTER_CLASS("context-adapter-class"),
    CONFIGURATION_LOCATION("config-location");
    
    private final String value;
    
    private ServletInitParameter(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
