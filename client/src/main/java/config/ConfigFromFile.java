package config;

import java.io.IOException;
import java.util.Properties;

public class ConfigFromFile implements Config {

    private final String address;

    private final int port;

    public ConfigFromFile(String fileName) {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.address = prop.getProperty("server.address");
        this.port = Integer.parseInt(prop.getProperty("port"));
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public int getPort() {
        return this.port;
    }
}
