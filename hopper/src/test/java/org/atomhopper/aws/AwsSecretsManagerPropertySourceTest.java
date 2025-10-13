package org.atomhopper.aws;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AwsSecretsManagerPropertySource.
 * Tests placeholder parsing, resolution logic, caching behavior, and error handling.
 */
public class AwsSecretsManagerPropertySourceTest {

    @Mock
    private SecretsManagerClient mockSecretsManagerClient;

    private AwsSecretsManagerPropertySource propertySource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        propertySource = new AwsSecretsManagerPropertySource(mockSecretsManagerClient);
    }

    // ========== Placeholder Parsing Tests ==========

    @Test
    public void testGetProperty_WithNullName_ReturnsNull() {
        Object result = propertySource.getProperty(null);
        assertNull("Null property name should return null", result);
        verifyZeroInteractions(mockSecretsManagerClient);
    }

    @Test
    public void testGetProperty_WithoutPrefix_ReturnsNull() {
        Object result = propertySource.getProperty("regular-property");
        assertNull("Property without aws-secretsmanager prefix should return null", result);
        verifyZeroInteractions(mockSecretsManagerClient);
    }

    @Test
    public void testGetProperty_WithEmptyString_ReturnsNull() {
        Object result = propertySource.getProperty("");
        assertNull("Empty property name should return null", result);
        verifyZeroInteractions(mockSecretsManagerClient);
    }

    @Test
    public void testGetProperty_WithPrefixOnly_CallsAwsWithEmptySecretName() {
        // Setup mock response
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("secret-value")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:");
        
        assertEquals("Should return secret value", "secret-value", result);
        verify(mockSecretsManagerClient, times(1)).getSecretValue(any(GetSecretValueRequest.class));
    }

    // ========== Secret Resolution Tests ==========

    @Test
    public void testGetProperty_WithSimpleSecretName_ResolvesSuccessfully() {
        // Setup mock response
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("my-secret-value")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:my-secret");
        
        assertEquals("Should return secret value", "my-secret-value", result);
        verify(mockSecretsManagerClient, times(1)).getSecretValue(any(GetSecretValueRequest.class));
    }

    @Test
    public void testGetProperty_WithJsonSecret_ResolvesEntireJson() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:db-credentials");
        
        assertEquals("Should return entire JSON string", jsonSecret, result);
    }

    @Test
    public void testGetProperty_WithJsonSecretAndKey_ExtractsSpecificValue() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:db-credentials/username");
        
        assertEquals("Should extract username from JSON", "admin", result);
    }

    @Test
    public void testGetProperty_WithJsonSecretAndPasswordKey_ExtractsPassword() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:db-credentials/password");
        
        assertEquals("Should extract password from JSON", "secret123", result);
    }

    @Test
    public void testGetProperty_WithComplexJsonPath_ExtractsCorrectValue() {
        String jsonSecret = "{\"host\":\"localhost\",\"port\":\"5432\",\"database\":\"atomhopper\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:db-config/database");
        
        assertEquals("Should extract database from JSON", "atomhopper", result);
    }

    // ========== Caching Behavior Tests ==========

    @Test
    public void testGetProperty_CalledTwice_UsesCacheOnSecondCall() {
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("cached-value")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        // First call
        Object result1 = propertySource.getProperty("aws-secretsmanager:my-secret");
        assertEquals("First call should return value", "cached-value", result1);

        // Second call
        Object result2 = propertySource.getProperty("aws-secretsmanager:my-secret");
        assertEquals("Second call should return cached value", "cached-value", result2);

        // Verify AWS was only called once
        verify(mockSecretsManagerClient, times(1)).getSecretValue(any(GetSecretValueRequest.class));
    }

    @Test
    public void testGetProperty_DifferentSecrets_CachesIndependently() {
        GetSecretValueResponse response1 = GetSecretValueResponse.builder()
                .secretString("value1")
                .build();
        GetSecretValueResponse response2 = GetSecretValueResponse.builder()
                .secretString("value2")
                .build();
        
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response1, response2);

        // Call with different secrets
        Object result1 = propertySource.getProperty("aws-secretsmanager:secret1");
        Object result2 = propertySource.getProperty("aws-secretsmanager:secret2");
        
        assertEquals("First secret should return value1", "value1", result1);
        assertEquals("Second secret should return value2", "value2", result2);

        // Verify AWS was called twice (once for each secret)
        verify(mockSecretsManagerClient, times(2)).getSecretValue(any(GetSecretValueRequest.class));
    }

    @Test
    public void testGetProperty_JsonKeyExtraction_CachesWithKey() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        // First call with key
        Object result1 = propertySource.getProperty("aws-secretsmanager:db-credentials/username");
        assertEquals("Should extract username", "admin", result1);

        // Second call with same key
        Object result2 = propertySource.getProperty("aws-secretsmanager:db-credentials/username");
        assertEquals("Should return cached username", "admin", result2);

        // Verify AWS was only called once
        verify(mockSecretsManagerClient, times(1)).getSecretValue(any(GetSecretValueRequest.class));
    }

    @Test
    public void testGetProperty_SameSecretDifferentKeys_CallsAwsOnce() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        // Call with different keys from same secret
        Object username = propertySource.getProperty("aws-secretsmanager:db-credentials/username");
        Object password = propertySource.getProperty("aws-secretsmanager:db-credentials/password");
        
        assertEquals("Should extract username", "admin", username);
        assertEquals("Should extract password", "secret123", password);

        // Verify AWS was called twice (once for each key path)
        verify(mockSecretsManagerClient, times(2)).getSecretValue(any(GetSecretValueRequest.class));
    }

    // ========== Error Handling Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testGetProperty_SecretNotFound_ThrowsIllegalArgumentException() {
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Secret not found").build());

        propertySource.getProperty("aws-secretsmanager:non-existent-secret");
    }

    @Test
    public void testGetProperty_SecretNotFound_ContainsSecretNameInException() {
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().message("Secret not found").build());

        try {
            propertySource.getProperty("aws-secretsmanager:missing-secret");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should contain secret name", 
                    e.getMessage().contains("missing-secret"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetProperty_AwsServiceError_ThrowsIllegalStateException() {
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(new RuntimeException("AWS service error"));

        propertySource.getProperty("aws-secretsmanager:my-secret");
    }

    @Test
    public void testGetProperty_AwsServiceError_ContainsSecretNameInException() {
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenThrow(new RuntimeException("AWS service error"));

        try {
            propertySource.getProperty("aws-secretsmanager:error-secret");
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue("Exception message should contain secret name", 
                    e.getMessage().contains("error-secret"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetProperty_JsonKeyNotFound_ThrowsIllegalStateException() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        propertySource.getProperty("aws-secretsmanager:db-credentials/nonexistent");
    }

    @Test
    public void testGetProperty_JsonKeyNotFound_ContainsKeyNameInException() {
        String jsonSecret = "{\"username\":\"admin\",\"password\":\"secret123\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        try {
            propertySource.getProperty("aws-secretsmanager:db-credentials/missing-key");
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue("Exception message should contain secret reference", 
                    e.getMessage().contains("db-credentials/missing-key"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testGetProperty_InvalidJsonFormat_ThrowsIllegalStateException() {
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("not-valid-json")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        propertySource.getProperty("aws-secretsmanager:bad-json/key");
    }

    // ========== Edge Cases ==========

    @Test
    public void testGetProperty_SecretWithSlashInName_ParsesCorrectly() {
        // When a secret reference contains slashes, it splits on first slash
        // "prod/db/credentials" becomes secret="prod" and key="db/credentials"
        String jsonSecret = "{\"db/credentials\":\"value-with-slash\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:prod/db/credentials");
        
        // This should treat "prod" as secret name and "db/credentials" as key path
        assertNotNull("Should handle secret names with slashes", result);
        assertEquals("Should extract value for key with slash", "value-with-slash", result);
    }

    @Test
    public void testGetProperty_EmptySecretValue_ReturnsEmptyString() {
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:empty-secret");
        
        assertEquals("Should return empty string", "", result);
    }

    @Test
    public void testGetProperty_SecretWithSpecialCharacters_ResolvesSuccessfully() {
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("p@ssw0rd!#$%")
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:special-chars-secret");
        
        assertEquals("Should handle special characters", "p@ssw0rd!#$%", result);
    }

    @Test
    public void testGetProperty_JsonWithNestedQuotes_ExtractsCorrectly() {
        String jsonSecret = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString(jsonSecret)
                .build();
        when(mockSecretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);

        Object result = propertySource.getProperty("aws-secretsmanager:json-secret/key2");
        
        assertEquals("Should extract second key correctly", "value2", result);
    }
}
