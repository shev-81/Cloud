package config;

import java.io.IOException;
import java.util.Properties;

public class ConfigFromFile implements Config {

    private final String address;

    private final int port;

    private final String packageHandlers;

    public ConfigFromFile(String fileName) {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.address = prop.getProperty("server.address");
        this.port = Integer.parseInt(prop.getProperty("port"));
        this.packageHandlers = prop.getProperty("handlers.package");
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getPackageHandlers() {
        return this.packageHandlers;
    }
}
