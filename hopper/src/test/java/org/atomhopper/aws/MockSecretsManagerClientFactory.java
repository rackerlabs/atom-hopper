package org.atomhopper.aws;

import org.mockito.Mockito;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Factory for creating mock SecretsManagerClient instances for testing.
 * This allows Spring XML configuration to create mock clients without programmatic registration.
 */
public class MockSecretsManagerClientFactory {

    /**
     * Creates a mock SecretsManagerClient that returns test values.
     * This method is called by Spring as a factory method.
     */
    public static SecretsManagerClient createMockClient() {
        SecretsManagerClient mockClient = Mockito.mock(SecretsManagerClient.class);
        
        // Setup default mock response
        GetSecretValueResponse response = GetSecretValueResponse.builder()
                .secretString("mock-secret-value")
                .build();
        
        when(mockClient.getSecretValue(any(GetSecretValueRequest.class)))
                .thenReturn(response);
        
        return mockClient;
    }
}
