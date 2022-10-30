package config;

import java.io.IOException;
import java.util.Properties;

public class ConfigFromFile implements Config {

    private final String packageHandlers;

    private final int port;

    public ConfigFromFile(String fileName) {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.packageHandlers = prop.getProperty("handlers.package");
        this.port = Integer.parseInt(prop.getProperty("port"));
    }

    @Override
    public String getPackageHandlers() {
        return this.packageHandlers;
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
