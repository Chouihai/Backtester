package Backtester.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigurationService {
    private static final String CONFIG_FILE = "backtester-config.properties";
    private static final String DEFAULT_DATA_SOURCE = "file";
    private static final String DEFAULT_FILE_PATH = "AAPL.JSON";
    private static final String DEFAULT_API_KEY = "";

    private final String apiKey;
    private final String dataSource;
    private final String filePath;

    public ConfigurationService() {
        Properties props = new Properties();
        boolean loaded = false;
        // Try to load from classpath
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
                loaded = true;
            }
        } catch (IOException ignored) {}
        // Try to load from working directory if not found in classpath
        if (!loaded) {
            try (InputStream in = Files.newInputStream(Paths.get(CONFIG_FILE))) {
                props.load(in);
            } catch (IOException ignored) {}
        }
        this.apiKey = trimOrDefault(props.getProperty("apiKey"), DEFAULT_API_KEY);
        this.dataSource = trimOrDefault(props.getProperty("dataSource"), DEFAULT_DATA_SOURCE);
        this.filePath = trimOrDefault(props.getProperty("filePath"), DEFAULT_FILE_PATH);
    }

    private String trimOrDefault(String value, String def) {
        if (value == null) return def;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? def : trimmed;
    }

    public String getApiKey() {
        return apiKey;
    }

    public enum DataSource {
        API, FILE
    }

    public DataSource getDataSource() {
        if (dataSource.equalsIgnoreCase("api")) {
            return DataSource.API;
        } else {
            return DataSource.FILE;
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isApiKeyValid() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
} 