package com.cloud.clientpak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    private FXMLLoader loader;
    private Controller controller;
    public static Stage pStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.pStage = primaryStage;
        loader = new FXMLLoader(getClass().getResource("/view.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Cloud");
        primaryStage.show();
    }
    public static Stage getpStage() {
        return pStage;
    }

    public void stop() {
        try{
            controller = loader.getController();
            controller.getConnection().closeConnection();
        }catch (NullPointerException e){}
    }
    public static void main(String[] args) {
        launch();
    }
}
