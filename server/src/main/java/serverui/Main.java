package serverui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private FXMLLoader loader;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        loader = new  FXMLLoader(getClass().getResource("/sample.fxml"));
        controller = loader.getController();
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Server");
        primaryStage.show();
    }

    @Override
    public void stop(){
        controller.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
