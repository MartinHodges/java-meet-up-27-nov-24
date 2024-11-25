package com.ces.aquarium.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Configuration
@Slf4j
@ConditionalOnProperty(prefix="application", name="dynamic-db-config.enabled", havingValue = "true")
public class PreloadDatabaseCredentials {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(Environment environment) throws IOException {
        String dynamicDbCredentialsFilename = environment.getProperty("application.dynamic-db-config.filename");
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setProperties(getProperties(dynamicDbCredentialsFilename));
        return propertySourcesPlaceholderConfigurer;
    }

    public Properties getProperties(String filename) throws IOException {
        log.info("Preloading database credentials from {}", filename);
        Map<String, String> stringStringMap = DatabaseDynamicCredentialsJob.readCredentialsFromFile(filename);
        log.info("Found username {} and password {}", stringStringMap.get("username"), stringStringMap.get("password"));
        return new Properties() {
            {
                setProperty("spring.datasource.username", stringStringMap.get("username"));
                setProperty("spring.datasource.password", stringStringMap.get("password"));
            }
        };
    }
}
