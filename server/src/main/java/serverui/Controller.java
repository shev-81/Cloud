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
 * Дополнительный Графический интерфейс для управления сервером.
 */
@Data
public class Controller implements Initializable {

    private static ServerApp server = new ServerApp();

    /**
     * Текстовая метка, показывает режим работы сервера.
     */
    @FXML
    Label textLable;

    /**
     * Кнопка выключения сервера.
     */
    @FXML
    Button buttonClose;

    /**
     * Останавливает работу сервера и закрывает сетевое соединение.
     */
    @FXML
    public void closeConnection() {
        server.stop();
        Platform.exit();;
    }

    /**
     * Запускает сервер в паралельном потоке.
     * @param location не используемый параметр.
     * @param resources не используемый параметр.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textLable.setText("Сервер работает");
        new Thread(server).start();
    }
}
