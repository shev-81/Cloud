package com.cloud.clientpak;

import messages.*;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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

    @FXML
    ProgressBar progressBar;
    @FXML
    Label fileNameMessage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        fileList = new ArrayList<>();
        this.fileChooser = new FileChooser();
    }

    @FXML
    public void clickAddFile() throws IOException {
        File file = fileChooser.showOpenDialog(ClientApp.getpStage());
        if (file != null) {
            if(fileList.contains(file.getName())){
                Alert allert = new Alert(Alert.AlertType.CONFIRMATION,"Файл будет перезаписан.");
                Optional<ButtonType> option = allert.showAndWait();
                if (option.get() == null) {
                    return;
                } else if (option.get() == ButtonType.OK) {
                    addFile(file);
                    return;
                } else if (option.get() == ButtonType.CANCEL) {
                    return;
                }
            }
            addFile(file);
            fileList.add(file.getName());
            reloadFxFilesList();
        }
    }

    public void addFile(File file) throws IOException {
        new Thread(() ->{
            try {
                int bufSize = 1024 * 1024 * 10;
                int partsCount = (int)(file.length() / bufSize);
                double percentProgressBar = (double) 1 / partsCount;
                if (file.length() % bufSize != 0) {
                    partsCount++;
                }
                Platform.runLater(() -> {
                    fileNameMessage.setText("Загрузка файла - "+ file.getName()+" в облако.");
                    fileNameMessage.setVisible(true);
                    progressBar.setVisible(true);
                });
                FileMessage fmOut = new FileMessage(file.getName(), -1, partsCount, new byte[bufSize]);
                FileInputStream in = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readedBytes = in.read(fmOut.data);
                    fmOut.partNumber = i + 1;
                    Platform.runLater(() -> {
                        progressBar.setProgress((double) fmOut.partNumber * percentProgressBar);
                    });
                    if (readedBytes < bufSize) {
                        fmOut.data = Arrays.copyOfRange(fmOut.data, 0, readedBytes);
                    }
                    connection.send(fmOut);
                    System.out.println("Отправлена часть #" + (i + 1));
                }
                in.close();
                Platform.runLater(() -> {
                    fileNameMessage.setVisible(false);
                    progressBar.setVisible(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void clickDeleteButton() {
        String delFileName = listView.getSelectionModel().getSelectedItem();
        if(delFileName != null){
            fileList.remove(delFileName);
            reloadFxFilesList();
            connection.send(new DellFileRequest(delFileName));
        }
    }

    @FXML
    public void clickGetButton() {
        String getFileName = listView.getSelectionModel().getSelectedItem();
        if(getFileName != null){
            connection.send(new FileRequest(getFileName));
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
            AbstractMessage message = new AuthMessage(login, pass);
            connection.send(message);
        }
    }

    @FXML
    public void register() {
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
            AbstractMessage message = new RegUserRequest(regName.getText(), regLogin.getText(), regPassword.getText());
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