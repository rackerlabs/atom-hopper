# Implementation Plan

- [x] 1. Add AWS SDK v2 dependency to hopper module
  - Add AWS Secrets Manager dependency to hopper/pom.xml
  - Verify dependency compatibility with existing Spring Framework 5.2.25
  - _Requirements: 1.1, 3.1, 3.3_

- [x] 2. Research Spring Cloud AWS compatibility with Spring Framework 5
  - [x] 2.1 Investigate Spring Cloud AWS 2.x compatibility with Spring Framework 5.2.25
    - Verify if Spring Cloud AWS can work without Spring Boot
    - Check XML namespace support for AWS configuration
    - _Requirements: 3.1, 3.3_
  
  - [x] 2.2 Evaluate AWS SDK v1 vs v2 for Spring integration
    - Compare Spring Cloud AWS (SDK v1) vs custom implementation (SDK v2)
    - Assess if existing Spring mechanisms can handle AWS property resolution
    - _Requirements: 3.3_

- [x] 3. Implement minimal AWS integration (if custom code is required)
  - [x] 3.1 Create AwsSecretsManagerPropertySource only if no existing solution works
    - Implement minimal PropertySource extending Spring's PropertySource<String>
    - Handle aws-secretsmanager: placeholder parsing and AWS SDK integration
    - _Requirements: 1.1, 1.3, 3.3_
  
  - [x] 3.2 Register PropertySource through Spring configuration
    - Use Spring XML configuration to register PropertySource bean
    - Avoid ApplicationContextInitializer if possible
    - _Requirements: 2.1, 2.2, 2.3_

- [x] 4. Update application-context.xml examples
  - [x] 4.1 Create example AWS configuration in server module
    - Add commented AWS client configuration to server/src/main/resources/META-INF/application-context.xml
    - Show proper bean configuration for SecretsManagerClient
    - _Requirements: 4.1, 4.2, 4.3_
  
  - [x] 4.2 Document placeholder usage patterns
    - Add comments showing aws-secretsmanager: placeholder syntax
    - Provide examples for database credentials and other common use cases
    - _Requirements: 1.1, 4.4_

- [x] 5. Create integration tests
  - [x] 5.1 Write PropertySource unit tests
    - Test placeholder parsing and resolution logic
    - Mock AWS SDK client for isolated testing
    - Test caching behavior and TTL expiration
    - _Requirements: 1.1, 1.3, 6.3_
  
  - [x] 5.2 Write Spring context integration tests
    - Test application context loading with AWS configuration
    - Test placeholder resolution in Spring bean properties
    - Test backward compatibility with existing configurations
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [ ]* 5.3 Create LocalStack integration tests
    - Set up LocalStack for AWS Secrets Manager testing
    - Test end-to-end secret retrieval and placeholder resolution
    - Test error scenarios with invalid secrets and configurations
    - _Requirements: 1.2, 5.1, 5.2_

- [x] 6. Add documentation
  - [x] 6.1 Update README with AWS Secrets Manager configuration
    - Document dependency requirements and configuration steps
    - Provide complete application-context.xml examples
    - _Requirements: 4.1, 4.2, 4.3, 4.4_