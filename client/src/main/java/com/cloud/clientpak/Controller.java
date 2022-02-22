package com.cloud.clientpak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private Connection connection;
    private List<String> fileList;
    private ObservableList<String> listFilesModel;
    private FileChooser fileChooser;

    @FXML
    VBox cloudPane;

    @FXML
    ListView <String> listView;

    @FXML
    GridPane regPane;
    @FXML
    TextField regLogin;
    @FXML
    PasswordField regPassword;
    @FXML
    PasswordField regPasswordRep;
    @FXML
    TextField regName;
    @FXML
    Label regMessage;

    @FXML
    TextField authLogin;
    @FXML
    PasswordField authPassword;
    @FXML
    GridPane authPane;
    @FXML
    Label authMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        fileList = new ArrayList<>();
        this.fileChooser = new FileChooser();
    }

    @FXML
    public void clickAddFile() {
        File file = fileChooser.showOpenDialog(ClientApp.getpStage());
        if (file != null) {
            Message message = new Message(1, file);
            if(fileList.contains(file.getName())){
                Alert allert = new Alert(Alert.AlertType.CONFIRMATION,"Файл будет перезаписан.");
                Optional<ButtonType> option = allert.showAndWait();
                if (option.get() == null) {
                    return;
                } else if (option.get() == ButtonType.OK) {
                    new Thread(() -> {
                        if(!connection.send(message)){
                            Alert allert1 = new Alert(Alert.AlertType.ERROR,"Ошибка при отправке файла!");
                            allert1.show();
                        }
                    }).start();
                    return;
                } else if (option.get() == ButtonType.CANCEL) {
                    return;
                }
            }
            new Thread(() -> {
                if(!connection.send(message)){
                    Alert allert = new Alert(Alert.AlertType.ERROR,"Ошибка при отправке файла!");
                    allert.show();
                }
            }).start();
            fileList.add(file.getName());
            reloadFxFilesList();
        }
    }

    @FXML
    public void clickDeleteButton() {
        String delFileName = listView.getSelectionModel().getSelectedItem();
        if(delFileName != null){
            fileList.remove(delFileName);
            reloadFxFilesList();
            connection.send(new Message(0, delFileName));
        }
    }

    @FXML
    public void clickGetButton() {
        String getFileName = listView.getSelectionModel().getSelectedItem();
        if(getFileName != null){
            connection.send(new Message(2, getFileName));
        }
    }

    @FXML
    public void changeStageToAuth() {
        Platform.runLater(() -> {
            authLogin.clear();
            authPassword.clear();
            fileList.clear();
        });
        authPane.setVisible(true);
        authMessage.setVisible(false);
        regPane.setVisible(false);
        cloudPane.setVisible(false);
    }

    public void changeStageToCloud() {
        cloudPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
    }

    @FXML
    public void changeStageToReg() {
        Platform.runLater(() -> {
            regLogin.clear();
            regPassword.clear();
            regPasswordRep.clear();
            regName.clear();
        });
        regPane.setVisible(true);
        regMessage.setVisible(false);
        authPane.setVisible(false);
        cloudPane.setVisible(false);
    }

    @FXML
    public void exitApp() {
        Platform.exit();
    }

    public void reloadFxFilesList(){
        listFilesModel = FXCollections.observableArrayList(fileList);
        Platform.runLater(()->{
            listView.setItems(listFilesModel);
        });
    }

    public Connection getConnection() {
        return connection;
    }

    @FXML
    public void enterCloud() {
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        }else{
            String login = authLogin.getText();
            String pass = authPassword.getText();
            Message message = new Message(4, login+" "+pass);
            connection.send(message);
        }

    }

    @FXML
    public void register() {            // command - 5 регистрация нового пользователя
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
        } else if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
        } else {
            String userNameLoginPass = regName.getText()+" "+ regLogin.getText() +" "+ regPassword.getText();
            Message message = new Message(5, userNameLoginPass);
            connection.send(message);
        }
    }

    public List<String> getFileList() {
        return fileList;
    }

    @FXML
    public void changeUser() {
        changeStageToAuth();
        connection.closeConnection();
        connection = null;
    }
}