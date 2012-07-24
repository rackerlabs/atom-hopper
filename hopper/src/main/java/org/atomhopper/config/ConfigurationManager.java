package org.atomhopper.config;

/**
 *
 * @author zinic
 */
public interface ConfigurationManager<T> {
   
   boolean isConfigured();

   T getConfiguration();
}
