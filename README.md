<pre>
    ___   __                     __  __                            
   /   | / /_____  ____ ___     / / / /____  ____  ____  ___  _____
  / /| |/ __/ __ \/ __ `__ \   / /_/ // __ \/ __ \/ __ \/ _ \/ ___/
 / ___ / /_/ /_/ / / / / / /  / __  // /_/ / /_/ / /_/ /  __/ /    
/_/  |_\__/\____/_/ /_/ /_/  /_/ /_/ \____/ .___/ .___/\___/_/     
                                         /_/   /_/                 
</pre>

#ATOM Hopper - A Java ATOMPub Server#

[Atom Hopper](http://atomhopper.org) is a framework for accessing, processing, aggregating and indexing Atom formatted events. Atom Hopper was designed to make it easy to build both generalized and specialized persistence mechanisms for Atom XML data, based on the Atom Syndication Format and the Atom Publishing Protocol.

Benefits:

* Simple. Atom Hopper is easy to use. It can be used out-of-the-box as an executable JAR (running within an embedded Jetty Server). For more flexibility, it can be deployed as a WAR file into any Servlet container (ie: Tomcat, Jetty, etc.). Most applications can use Atom Hopper with minimal configuration to specify the Atom Workspaces and the Content storage.
* Scalable. Atom Hopper is very scalable because it is designed to be stateless, allowing state to be distributed across the web.
* Layered. Atom Hopper allows any number of intermediaries, such as proxies, gateways, and firewalls so one can easily layer aspects such as Security, Compression, etc. on an as needed basis.
* Built on a strong foundation. It is built on top of several open source projects such as [Apache Abdera](http://abdera.apache.org/) (a Java-based Atom Publishing framework), [Hibernate](http://www.hibernate.org/), and [MongoDB](http://www.mongodb.org/).
* Flexible. Atom Hopper currently supports the following relational databases: [H2](http://www.h2database.com/), [PostgresSQL](http://www.postgresql.org/), and [MySQL](http://www.mysql.com/) (plus others that work with Hibernate) as well as the NoSQL database [MongoDB](http://www.mongodb.org/).
* High performance. Atom Hopper can handle high loads with high accuracy.
* Improving. Atom Hopper is under development and actively being worked on.
* Atom Hopper is currently being used at [Rackspace](http://www.rackspace.com/) in conjunction with [OpenStack](http://openstack.org).

Atom Hopper works well with [Repose](http://openrepose.org/) especially if you need:
* Authentication and Authorization
* Rate Limiting
* Versioning
* HTTP Logging

###Notes Regarding Data Adapter###
The current status of the Atom Hopper Data Adapters is as follows:

* JDBC Data Adapter - ongoing development

* Hibernate Data Adapter - not currently adding features/fixing defects

* MongoDB Data Adapter - not currently adding features/fixing defects

* Migration Data Adapter - ongoing development

* Postgres Data Adapter - not currently adding features/fixing defects

To find out how to install and run Atom Hopper please see the [Atom Hopper Wiki](https://github.com/rackerlabs/atom-hopper/wiki)

## AWS Secrets Manager Integration

Atom Hopper supports integration with AWS Secrets Manager for secure management of sensitive configuration data such as database credentials, API keys, and other secrets. This integration is **completely optional** and uses Spring's property placeholder mechanism.

### Features

* **Secure credential management**: Store sensitive data in AWS Secrets Manager instead of configuration files
* **Automatic credential rotation**: Application automatically picks up rotated credentials without restart
* **Flexible configuration**: Works with existing Spring XML configuration
* **Backward compatible**: Existing deployments continue to work without any changes
* **Multiple authentication methods**: Supports static credentials, IAM roles, and default credential provider chain

### Requirements

To use AWS Secrets Manager integration, you need:

1. **AWS SDK v2 Secrets Manager dependency** (already included in hopper module):
   ```xml
   <dependency>
       <groupId>software.amazon.awssdk</groupId>
       <artifactId>secretsmanager</artifactId>
       <version>2.20.26</version>
   </dependency>
   ```

2. **AWS credentials** with `secretsmanager:GetSecretValue` permission

3. **Spring Framework 5.2.25+** (already included)

### Quick Start

#### 1. Configure AWS Secrets Manager Client

Add the following to your `application-context.xml`:

```xml
<!-- Register AWS Secrets Manager PropertySource -->
<bean class="org.atomhopper.aws.AwsSecretsManagerPropertySourceRegistrar"/>

<!-- Configure AWS credentials (Option A: Static credentials) -->
<bean id="awsCredentials" class="software.amazon.awssdk.auth.credentials.AwsBasicCredentials" 
      factory-method="create">
    <constructor-arg value="${aws.access.key.id}"/>
    <constructor-arg value="${aws.secret.access.key}"/>
</bean>

<bean id="awsCredentialsProvider" class="software.amazon.awssdk.auth.credentials.StaticCredentialsProvider"
      factory-method="create">
    <constructor-arg ref="awsCredentials"/>
</bean>

<bean id="awsRegion" class="software.amazon.awssdk.regions.Region" factory-method="of">
    <constructor-arg value="${aws.region:us-east-1}"/>
</bean>

<bean id="awsSecretsManagerClient" class="software.amazon.awssdk.services.secretsmanager.SecretsManagerClient"
      factory-method="builder" destroy-method="close">
    <property name="region" ref="awsRegion"/>
    <property name="credentialsProvider" ref="awsCredentialsProvider"/>
</bean>
```

#### 2. Use AWS Secrets Manager Placeholders

Reference secrets in your bean properties using the `aws-secretsmanager:` prefix:

```xml
<!-- Example: PostgreSQL DataSource with AWS Secrets Manager -->
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="org.postgresql.Driver" />
    <property name="url" value="jdbc:postgresql://localhost:5432/atomhopper" />
    <property name="username" value="${aws-secretsmanager:db-credentials/username}" />
    <property name="password" value="${aws-secretsmanager:db-credentials/password}" />
</bean>
```

#### 3. Create Secrets in AWS Secrets Manager

Create a JSON secret in AWS Secrets Manager:

```json
{
  "username": "atomhopper_user",
  "password": "secure_password_here"
}
```

### Placeholder Syntax

* **Entire secret as string**: `${aws-secretsmanager:secret-name}`
* **Specific key from JSON secret**: `${aws-secretsmanager:secret-name/key}`
* **With default value**: `${aws-secretsmanager:secret-name:default-value}`

### Authentication Options

#### Option A: Static Credentials (shown above)
Use explicit AWS access key and secret key configured in application-context.xml.

#### Option B: Default Credential Provider Chain
Omit the `credentialsProvider` property to use AWS default credential chain:

```xml
<bean id="awsSecretsManagerClient" class="software.amazon.awssdk.services.secretsmanager.SecretsManagerClient"
      factory-method="builder" destroy-method="close">
    <property name="region" ref="awsRegion"/>
    <!-- No credentialsProvider - uses default chain -->
</bean>
```

#### Option C: IAM Role (EC2/ECS)
When running on EC2 or ECS, use the instance/task IAM role:

```xml
<bean id="awsCredentialsProvider" class="software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider"
      factory-method="create"/>

<bean id="awsSecretsManagerClient" class="software.amazon.awssdk.services.secretsmanager.SecretsManagerClient"
      factory-method="builder" destroy-method="close">
    <property name="region" ref="awsRegion"/>
    <property name="credentialsProvider" ref="awsCredentialsProvider"/>
</bean>
```

### Common Usage Patterns

#### Database Credentials
```xml
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="url" value="jdbc:postgresql://db.example.com:5432/atomhopper" />
    <property name="username" value="${aws-secretsmanager:atomhopper/db/username}" />
    <property name="password" value="${aws-secretsmanager:atomhopper/db/password}" />
</bean>
```

#### Hibernate Configuration
```xml
<bean name="feed-repository-bean" class="org.atomhopper.hibernate.HibernateFeedRepository">
    <constructor-arg>
        <map>
            <entry key="hibernate.connection.username" value="${aws-secretsmanager:db-credentials/username}" />
            <entry key="hibernate.connection.password" value="${aws-secretsmanager:db-credentials/password}" />
        </map>
    </constructor-arg>
</bean>
```

#### Mixing with Environment Variables
```xml
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="url" value="${DB_URL:jdbc:postgresql://localhost:5432/atomhopper}" />
    <property name="username" value="${aws-secretsmanager:db-credentials/username}" />
    <property name="password" value="${aws-secretsmanager:db-credentials/password}" />
    <property name="maxActive" value="${DB_POOL_SIZE:50}" />
</bean>
```

### Complete Example

See `server/src/main/resources/META-INF/application-context-aws-example.xml` for a comprehensive example with:
* Multiple authentication methods
* Various placeholder usage patterns
* Best practices and troubleshooting tips
* Common use cases and configurations

### Best Practices

1. **Secret Naming**: Use hierarchical naming like `atomhopper/prod/db/credentials`
2. **JSON Structure**: Group related credentials in a single JSON secret
3. **Environment-Specific**: Use different secret names per environment (dev/staging/prod)
4. **Least Privilege**: Grant IAM permissions only for required secrets
5. **Monitoring**: Enable AWS CloudTrail for secret access logging
6. **Local Development**: Use default values for local development: `${aws-secretsmanager:secret:local-default}`

### Troubleshooting

| Issue | Solution |
|-------|----------|
| "Unable to load AWS credentials" | Verify AWS credentials are configured and have `secretsmanager:GetSecretValue` permission |
| "Secret not found" | Verify secret name matches exactly (case-sensitive) and exists in the configured region |
| "Access denied to secret" | Check IAM permissions and resource-based policies on the secret |
| "Placeholder not resolved" | Ensure `AwsSecretsManagerPropertySourceRegistrar` bean is registered before other beans |

### Security Considerations

* Secret values are **never logged** to prevent credential exposure
* Secrets are cached in memory to reduce AWS API calls
* Use AWS KMS encryption for secrets at rest
* Rotate credentials regularly using AWS Secrets Manager rotation features
* Use IAM policies to restrict access to specific secret name patterns

### Performance

* Secrets are fetched during application startup
* In-memory caching reduces AWS API calls
* Connection pooling is enabled by default in AWS SDK v2
* Minimal overhead compared to traditional property files

###Notes Regarding licensing###

*All files contained with this distribution of Atom Hopper are licenced 
under the [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0).
You must agree to the terms of this license and abide by them before
viewing, utilizing or distributing the source code contained within this distribution.*
