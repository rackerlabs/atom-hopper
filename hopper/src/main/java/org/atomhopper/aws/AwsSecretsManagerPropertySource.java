package org.atomhopper.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring PropertySource implementation that resolves properties from AWS Secrets Manager.
 * Supports placeholder syntax: ${aws-secretsmanager:secret-name} or ${aws-secretsmanager:secret-name/key}
 */
public class AwsSecretsManagerPropertySource extends PropertySource<SecretsManagerClient> {

    private static final Logger LOG = LoggerFactory.getLogger(AwsSecretsManagerPropertySource.class);
    private static final String PROPERTY_SOURCE_NAME = "awsSecretsManager";
    private static final String PREFIX = "aws-secretsmanager:";
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public AwsSecretsManagerPropertySource(SecretsManagerClient secretsManagerClient) {
        super(PROPERTY_SOURCE_NAME, secretsManagerClient);
    }

    @Override
    public Object getProperty(String name) {
        if (name == null || !name.startsWith(PREFIX)) {
            return null;
        }

        String secretReference = name.substring(PREFIX.length());
        
        if (cache.containsKey(secretReference)) {
            LOG.trace("Returning cached value for secret reference: {}", secretReference);
            return cache.get(secretReference);
        }

        try {
            String value = resolveSecret(secretReference);
            cache.put(secretReference, value);
            LOG.debug("Successfully resolved secret reference: {}", secretReference);
            return value;
        } catch (ResourceNotFoundException e) {
            LOG.error("Secret not found in AWS Secrets Manager: {}", secretReference);
            throw new IllegalArgumentException("Secret not found: " + secretReference, e);
        } catch (Exception e) {
            LOG.error("Failed to resolve secret from AWS Secrets Manager: {}", secretReference, e);
            throw new IllegalStateException("Failed to resolve secret: " + secretReference, e);
        }
    }

    private String resolveSecret(String secretReference) {
        String[] parts = secretReference.split("/", 2);
        String secretName = parts[0];
        String key = parts.length > 1 ? parts[1] : null;

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = source.getSecretValue(request);
        String secretString = response.secretString();

        if (key == null) {
            return secretString;
        }

        // Parse JSON to extract specific key
        return extractJsonKey(secretString, key);
    }

    private String extractJsonKey(String json, String key) {
        // Simple JSON parsing for key extraction
        // Format expected: {"key":"value",...}
        String searchPattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchPattern);
        
        if (keyIndex == -1) {
            throw new IllegalArgumentException("Key '" + key + "' not found in secret JSON");
        }

        int colonIndex = json.indexOf(":", keyIndex);
        int valueStart = json.indexOf("\"", colonIndex) + 1;
        int valueEnd = json.indexOf("\"", valueStart);

        if (valueStart == 0 || valueEnd == -1) {
            throw new IllegalArgumentException("Invalid JSON format for key: " + key);
        }

        return json.substring(valueStart, valueEnd);
    }
}
