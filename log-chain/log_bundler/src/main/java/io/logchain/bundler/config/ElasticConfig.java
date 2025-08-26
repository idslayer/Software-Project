package io.logchain.bundler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elastic")
public class ElasticConfig {
    private String address;
    private String user;
    private String password;
    private String normLogIndex;
    private String anchorIndex;

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNormLogIndex() {
        return normLogIndex;
    }
    public void setNormLogIndex(String normLogIndex) {
        this.normLogIndex = normLogIndex;
    }

    public String getAnchorIndex() {
        return anchorIndex;
    }

    public void setAnchorIndex(String anchorIndex) {
        this.anchorIndex = anchorIndex;
    }
}