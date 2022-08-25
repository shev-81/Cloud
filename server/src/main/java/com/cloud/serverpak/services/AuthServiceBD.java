package com.cloud.serverpak.services;

import com.cloud.serverpak.MainHandler;
import com.cloud.serverpak.interfaces.AuthService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A class that works with a user Database. Designed to
 * perform CRUD operations on user records.
 */
public class AuthServiceBD implements AuthService {

    /**
     * The logger variable.
     */
    private static final Logger LOGGER = LogManager.getLogger(AuthServiceBD.class);

    /**
     * A list that stores a list of all users from the database.
     */
    private List<User> listUser;

    private static List<MainHandler> mainHandlerList;

    /**
     * Connection to the database.
     */
    private static Connection connection;

    /**
     * A variable for working with the database.
     */
    private static Statement stmt;

    /**
     * Алгоритм шифрования данных.
     */
    private Base64.Encoder encoder;

    /**
     * User class.
     */
    private class User {

        /**
         * The user name variable.
         */
        private String name;

        /**
         * The user login variable.
         */
        private String login;

        /**
         * The user's password variable.
         */
        private String pass;

        /**
         * Parameterized constructor for creating a user object with
         * @param name Username.
         * @param login User login.
         * @param pass User password.
         */
        public User(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }
    }

    /**
     * The constructor starts a connection to the Database and
     * loads the list of users.
     * Uses {@link #start start()} and {@link #loadUsers loadUsers()}  methods.
     */
    public AuthServiceBD() {
        listUser = new ArrayList<>();
        mainHandlerList = new ArrayList<>();
        encoder = Base64.getEncoder();
        try {
            start();
            loadUsers();
            LOGGER.info("Загрузили пользователей из БД AuthServiceBD");
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        } catch (Exception e) {
            LOGGER.throwing(Level.FATAL, e);
        }
    }

    /**
     * Registers a new user, and throws an exception if it is impossible.
     * @param nickName Username..
     * @param login User login.
     * @param pass The user's password.
     * @return true if the registration was successful.
     */
    public boolean registerNewUser(String nickName, String login, String pass) {
        int result = 0;
        try {
            String passCode = new String(encoder.encode(pass.getBytes(StandardCharsets.UTF_8)));
            result = stmt.executeUpdate("INSERT INTO users (NickName, login, pass) VALUES ('" + nickName + "','" + login + "','" + passCode + "');");
            listUser.add(new User(nickName, login, passCode));
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
        return result > 0;
    }

    /**
     * Loads all users from the Database to the list of users on the
     * server.
     * @throws SQLException при ошибке запроса.
     */
    public void loadUsers() throws SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT * FROM users;")) {
            while (rs.next()) {
                listUser.add(new User(
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                ));
            }
        }
    }

    /**
     * Connects the connection to the Database, and creates a statement
     * object.
     */
    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:server/BD/users.db");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
            throw new RuntimeException("Не возможно подключиться к БД.");
        }
    }

    /**
     * Closes the Database connection.
     */
    @Override
    public void stop() {
        try {
            if (stmt != null)
                stmt.close();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
    }

    /**
     * Returns the user's Name by his Username and Password.
     * @param login User login.
     * @param pass The user's password.
     * @return Username.
     */
    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (User user : listUser) {
            if (user.login.equals(login) && user.pass.equals(new String(encoder.encode(pass.getBytes(StandardCharsets.UTF_8))))){
                return user.name;
            }
        }
        return null;
    }

    public static List<MainHandler> getMainHandlerList() {
        return mainHandlerList;
    }
}
