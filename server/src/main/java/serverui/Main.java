package serverui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The FX class of the application, Launches the UI of the application
 * according to the description of the application scene described in
 * sample.xml file.
 */
public class Main extends Application {

    /**
     * The loader of the XML file describing the UI.
     */
    private FXMLLoader loader;

    /**
     * Executed at application startup, saves references to in class variables
     * Primary Stage and Controller.
     * @param primaryStage The main Stage of the FX application.
     * @throws Exception It may occur when working with the loader.
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        loader = new  FXMLLoader(getClass().getResource("/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Server");
        primaryStage.show();
    }

    /**
     * Launches the application.
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
