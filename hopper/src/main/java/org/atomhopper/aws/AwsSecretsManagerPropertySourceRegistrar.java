package org.atomhopper.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * BeanFactoryPostProcessor that registers AwsSecretsManagerPropertySource with Spring Environment.
 * This allows AWS Secrets Manager placeholders to be resolved in Spring configuration files.
 * 
 * Configuration is optional - if no SecretsManagerClient bean is configured, this registrar
 * will not add the PropertySource, maintaining backward compatibility.
 */
public class AwsSecretsManagerPropertySourceRegistrar implements BeanFactoryPostProcessor, EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(AwsSecretsManagerPropertySourceRegistrar.class);
    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            this.environment = (ConfigurableEnvironment) environment;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (environment == null) {
            LOG.warn("Environment is not configurable, skipping AWS Secrets Manager PropertySource registration");
            return;
        }

        try {
            // Check if SecretsManagerClient bean is configured
            if (!beanFactory.containsBean("awsSecretsManagerClient")) {
                LOG.debug("No awsSecretsManagerClient bean found, skipping AWS Secrets Manager integration");
                return;
            }

            SecretsManagerClient secretsManagerClient = beanFactory.getBean("awsSecretsManagerClient", SecretsManagerClient.class);
            AwsSecretsManagerPropertySource propertySource = new AwsSecretsManagerPropertySource(secretsManagerClient);
            
            environment.getPropertySources().addFirst(propertySource);
            LOG.info("AWS Secrets Manager PropertySource registered successfully");
            
        } catch (Exception e) {
            LOG.warn("Failed to register AWS Secrets Manager PropertySource: {}", e.getMessage());
            LOG.debug("AWS Secrets Manager PropertySource registration error details", e);
            // Don't throw exception to maintain backward compatibility
        }
    }
}
