package serverui;

import com.cloud.serverpak.ServerApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;

@Data
public class Controller implements Initializable {

    private ServerApp server;

    @FXML
    Label textLable;

    @FXML
    Button buttonClose;

    @FXML
    public void closeConnection() {
        server.stop();
        Platform.exit();;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    textLable.setText("Сервер работает");
                });
                server = new ServerApp();
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
