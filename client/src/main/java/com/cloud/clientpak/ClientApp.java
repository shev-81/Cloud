package com.cloud.clientpak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Data;

import java.io.IOException;

/**
 * The FX class of the application, Launches the UI of the application
 * according to the description of the application scene described in
 * view.xml file.
 */
@Data
public class ClientApp extends Application {

    /**
     * The loader of the XML file describing the UI.
     */
    private FXMLLoader loader;

    /**
     * Variable {@link Controller Controller}
     */
    private static Controller controller;

    /**
     * The main Stage of the FX application.
     */
    private static Stage pStage;

    /**
     * Executed at application startup, saves references to in class variables
     * Primary Stage and Controller.
     * @param primaryStage The main Stage of the FX application.
     * @throws Exception It may occur when working with the loader.
     */
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

    public static Stage getStage() {
        return pStage;
    }

    /**
     * Выполняется перед закрытием приложения, останавливаетсервис работы
     * с файлами и закрывает сетевое соединение.
     */
    @Override
    public void stop() {
        try{
            controller = loader.getController();
            controller.getFileWorker().stop();
            Controller.getConnection().close();
        }catch (NullPointerException ignored){
            ignored.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
