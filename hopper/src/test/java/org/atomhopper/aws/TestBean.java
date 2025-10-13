package org.atomhopper.aws;

/**
 * Simple test bean for property injection testing in Spring integration tests.
 */
public class TestBean {
    private String property;
    private String secondProperty;
    private String username;
    private String password;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getSecondProperty() {
        return secondProperty;
    }

    public void setSecondProperty(String secondProperty) {
        this.secondProperty = secondProperty;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
