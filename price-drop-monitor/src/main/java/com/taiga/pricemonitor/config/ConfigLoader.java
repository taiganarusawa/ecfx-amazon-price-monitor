package com.taiga.pricemonitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.IOException;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    public static AppConfig load(String filePath) {
        Yaml yaml = new Yaml(new Constructor(AppConfig.class, new LoaderOptions()));
        try (FileInputStream fis = new FileInputStream(filePath)) {
            AppConfig config = yaml.load(fis);
            logger.info("Configuration loaded successfully from {}", filePath);
            return config;
        } catch (IOException e) {
            logger.error("Failed to load configuration from {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}
