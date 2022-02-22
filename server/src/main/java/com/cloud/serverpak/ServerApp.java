package com.cloud.serverpak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerApp {
    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class); // Trace < Debug < Info < Warn < Error < Fatal
    private ArrayList<ClientHandler> clients;
    private Socket socket = null;
    private static AuthService authService = new AuthServiceBD();

    ServerApp() {
        this.clients = new ArrayList<>();
        ExecutorService service = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                LOGGER.info("Server wait connected User.");
                socket = serverSocket.accept();
                LOGGER.info("User connected.");
                service.execute(() -> {
                    new ClientHandler(this, socket);
                });
            }
        } catch (IOException e) {
            LOGGER.throwing(Level.FATAL, e);
        } finally {
            LOGGER.info("Server is offline.");
            service.shutdown();
        }
    }

    public boolean isNickBusy(String nickName) {
        for (ClientHandler client : clients) {
            if (client.getNameUser().equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

    public synchronized void unSubscribe(ClientHandler o) {
        clients.remove(o);
    }

    public AuthService getAuthService() {
        return authService;
    }
}