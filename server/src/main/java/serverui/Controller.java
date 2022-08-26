package serverui;

import com.cloud.serverpak.ServerApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Data;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Additional Graphical interface for server management.
 */
@Data
public class Controller implements Initializable {

    private static ServerApp server = new ServerApp();

    /**
     * Text label, shows the mode of operation of the server.
     */
    @FXML
    Label textLable;

    /**
     * Server shutdown button.
     */
    @FXML
    Button buttonClose;

    /**
     * Stops the server and closes the network connection.
     */
    @FXML
    public void closeConnection() {
        server.stop();
        Platform.exit();
    }

    /**
     * Starts the server in a parallel thread.
     * @param location an unused parameter.
     * @param resources an unused parameter.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textLable.setText("Сервер работает");
        new Thread(server).start();
    }
}
