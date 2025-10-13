package org.atomhopper.aws;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

/**
 * Spring context integration tests for AWS Secrets Manager integration.
 * Tests application context loading, placeholder resolution, and backward compatibility.
 * 
 * Requirements tested:
 * - 2.1: AWS Secrets Manager dependencies are optional
 * - 2.2: System works without AWS Secrets Manager placeholders
 * - 2.3: Existing users don't need to modify configurations
 */
@RunWith(Enclosed.class)
public class AwsSecretsManagerSpringIntegrationTest {

    /**
     * Tests for application context loading with AWS configuration.
     */
    public static class ContextLoadingTests {

        @Test
        public void testContextLoads_WithoutAwsConfiguration() {
            // Test that context loads without AWS configuration (backward compatibility)
            ClassPathXmlApplicationContext context = null;
            try {
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-without-aws.xml");

                assertNotNull("Context should load successfully without AWS", context);
                assertTrue("Context should be active", context.isActive());
                
                // Verify no AWS beans are present
                assertFalse("Should not contain AWS client", 
                        context.containsBean("awsSecretsManagerClient"));
                
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testContextLoads_WithAwsRegistrarButNoClient() {
            // Test graceful handling when registrar is present but client is not configured
            ClassPathXmlApplicationContext context = null;
            try {
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-registrar-only.xml");

                assertNotNull("Context should load even without AWS client", context);
                assertTrue("Context should be active", context.isActive());
                
                // Verify registrar exists but didn't fail
                assertTrue("Should contain PropertySource registrar", 
                        context.containsBean("awsSecretsManagerPropertySourceRegistrar"));
                assertFalse("Should not contain AWS client", 
                        context.containsBean("awsSecretsManagerClient"));
                
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testContextLoads_WithMockAwsClient() {
            // Test that context loads with AWS configuration using mock client factory
            ClassPathXmlApplicationContext context = null;
            try {
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-with-mock-aws.xml");

                assertNotNull("Context should load successfully with AWS", context);
                assertTrue("Context should be active", context.isActive());
                
                // Verify AWS beans are present
                assertTrue("Should contain AWS client", 
                        context.containsBean("awsSecretsManagerClient"));
                assertTrue("Should contain PropertySource registrar", 
                        context.containsBean("awsSecretsManagerPropertySourceRegistrar"));
                
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    /**
     * Tests for backward compatibility with existing configurations.
     */
    public static class BackwardCompatibilityTests {

        @Test
        public void testBackwardCompatibility_StandardPlaceholders() {
            // Test that standard Spring placeholders still work
            ClassPathXmlApplicationContext context = null;
            try {
                // Set system property for standard placeholder resolution
                System.setProperty("test.property", "standard-value");
                
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-standard-placeholders.xml");

                TestBean testBean = context.getBean("testBean", TestBean.class);
                assertNotNull("Test bean should exist", testBean);
                assertEquals("Standard placeholder should resolve", "standard-value", testBean.getProperty());
                
            } finally {
                System.clearProperty("test.property");
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testBackwardCompatibility_NoAwsDependencies() {
            // Test that applications without AWS dependencies continue to work
            ClassPathXmlApplicationContext context = null;
            try {
                System.setProperty("db.username", "testuser");
                System.setProperty("db.password", "testpass");
                
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-no-aws-dependencies.xml");

                assertNotNull("Context should load without AWS dependencies", context);
                assertTrue("Context should be active", context.isActive());
                
                TestBean testBean = context.getBean("testBean", TestBean.class);
                assertEquals("Should use standard placeholders", "testuser", testBean.getUsername());
                assertEquals("Should use standard placeholders", "testpass", testBean.getPassword());
                
            } finally {
                System.clearProperty("db.username");
                System.clearProperty("db.password");
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testBackwardCompatibility_ExistingConfigUnchanged() {
            // Test that existing configurations work without modification
            ClassPathXmlApplicationContext context = null;
            try {
                System.setProperty("existing.property", "existing-value");
                
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-existing-config.xml");

                assertNotNull("Existing config should load", context);
                assertTrue("Context should be active", context.isActive());
                
                TestBean testBean = context.getBean("testBean", TestBean.class);
                assertEquals("Existing property should resolve", "existing-value", testBean.getProperty());
                
            } finally {
                System.clearProperty("existing.property");
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    /**
     * Tests for PropertySource registration mechanism.
     */
    public static class PropertySourceRegistrationTests {

        @Test
        public void testPropertySourceRegistrar_RegistersSuccessfully() {
            // Test that the registrar bean can be instantiated
            ClassPathXmlApplicationContext context = null;
            try {
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-registrar-only.xml");

                AwsSecretsManagerPropertySourceRegistrar registrar = 
                        context.getBean(AwsSecretsManagerPropertySourceRegistrar.class);
                
                assertNotNull("Registrar should be instantiated", registrar);
                
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testPropertySourceRegistrar_GracefulDegradation() {
            // Test that registrar doesn't fail when AWS client is not present
            ClassPathXmlApplicationContext context = null;
            try {
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-registrar-only.xml");

                // Context should load successfully even without AWS client
                assertNotNull("Context should load", context);
                assertTrue("Context should be active", context.isActive());
                
                // Verify test bean can still be created
                TestBean testBean = context.getBean("testBean", TestBean.class);
                assertNotNull("Test bean should exist", testBean);
                assertEquals("Property should have default value", "test-value", testBean.getProperty());
                
            } finally {
                if (context != null) {
                    context.close();
                }
            }
        }
    }

    /**
     * Tests for configuration validation.
     */
    public static class ConfigurationValidationTests {

        @Test
        public void testConfiguration_MultipleBeansWithStandardPlaceholders() {
            // Test that multiple beans can use standard placeholders
            ClassPathXmlApplicationContext context = null;
            try {
                System.setProperty("bean1.property", "value1");
                System.setProperty("bean2.property", "value2");
                
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-multiple-beans.xml");

                TestBean bean1 = context.getBean("testBean1", TestBean.class);
                TestBean bean2 = context.getBean("testBean2", TestBean.class);
                
                assertNotNull("Bean 1 should exist", bean1);
                assertNotNull("Bean 2 should exist", bean2);
                assertEquals("Bean 1 property should resolve", "value1", bean1.getProperty());
                assertEquals("Bean 2 property should resolve", "value2", bean2.getProperty());
                
            } finally {
                System.clearProperty("bean1.property");
                System.clearProperty("bean2.property");
                if (context != null) {
                    context.close();
                }
            }
        }

        @Test
        public void testConfiguration_BeanWithMultipleProperties() {
            // Test that a single bean can have multiple properties resolved
            ClassPathXmlApplicationContext context = null;
            try {
                System.setProperty("prop1", "value1");
                System.setProperty("prop2", "value2");
                
                context = new ClassPathXmlApplicationContext(
                        "classpath:org/atomhopper/aws/test-context-bean-multiple-properties.xml");

                TestBean testBean = context.getBean("testBean", TestBean.class);
                
                assertNotNull("Test bean should exist", testBean);
                assertEquals("First property should resolve", "value1", testBean.getProperty());
                assertEquals("Second property should resolve", "value2", testBean.getSecondProperty());
                
            } finally {
                System.clearProperty("prop1");
                System.clearProperty("prop2");
                if (context != null) {
                    context.close();
                }
            }
        }
    }
}
