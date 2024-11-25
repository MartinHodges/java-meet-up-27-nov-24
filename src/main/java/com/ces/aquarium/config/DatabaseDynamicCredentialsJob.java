package com.ces.aquarium.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="application", name="dynamic-db-config.enabled", havingValue = "true")
public class DatabaseDynamicCredentialsJob {

    @Value("${application.dynamic-db-config.enabled:false}")
    boolean dynamicDbEnabled;

    @Value("${application.dynamic-db-config.filename}")
    String dynamicDbCredentialsFilename;

    private final HikariDataSource hikariDataSource;

    @Scheduled(
            fixedDelayString = "${application.dynamic-db-config.refresh:5}",
            timeUnit = TimeUnit.MINUTES
    )
    public void checkForRefreshedCredentials() throws IOException {
        if (dynamicDbEnabled) {
            log.info("Checking for refreshed database credentials");
            Map<String,String> credentials = readCredentialsFromFile(dynamicDbCredentialsFilename);
            String username = credentials.get("username");
            String password = credentials.get("password");

            boolean refreshed = !StringUtils.equals(username, hikariDataSource.getUsername()) ||
                                !StringUtils.equals(password, hikariDataSource.getPassword());

            if (refreshed) {
                log.info("Updating database credentials");
                hikariDataSource.setUsername(username);
                hikariDataSource.setPassword(password);
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();
            }
        }
    }

    public static Map<String, String> readCredentialsFromFile(String filename) throws IOException {
        Map<String, String> values = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(filename));

        if (lines.size() != 2) {
            log.error("Invalid dynamic database file contents: Expected 2 lines, got {}", lines.size());
            return values;
        }

        lines.forEach(line ->
            {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    values.put(parts[0].trim(), parts[1].trim());
                } else {
                    log.error("Invalid dynamic database file contents: Incorrect number of parts in line");
                }
            });

        return values;
    }



}
