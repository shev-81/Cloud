package com.cloud.serverpak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthServiceBD implements AuthService {

    private static final Logger LOGGER = LogManager.getLogger(AuthServiceBD.class);
    private List<User> listUser;
    private static Connection connection;
    private static Statement stmt;

    private class User {
        private String name;
        private String login;
        private String pass;

        public User(String name, String login, String pass) {
            this.name = name;
            this.login = login;
            this.pass = pass;
        }
    }

    AuthServiceBD() {
        listUser = new ArrayList<>();
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

    public boolean registerNewUser(String nickName, String login, String pass) {
        int result = 0;
        try {
            int loginHash  = login.hashCode();
            int passHash = pass.hashCode();
            result = stmt.executeUpdate("INSERT INTO users (NickName, login, pass) VALUES ('" + nickName + "','" + loginHash + "','" + passHash + "');");
            listUser.add(new User(nickName, String.valueOf(loginHash), String.valueOf(passHash)));
        } catch (SQLException e) {
            LOGGER.throwing(Level.ERROR, e);
        }
        return result > 0;
    }

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

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (User user : listUser) {
            if (user.login.equals(String.valueOf(login.hashCode())) && user.pass.equals(String.valueOf(pass.hashCode())))
                return user.name;
        }
        return null;
    }
}
