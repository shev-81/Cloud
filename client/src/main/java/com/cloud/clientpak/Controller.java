package com.cloud.clientpak;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import messages.*;
import javafx.application.Platform;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable{

    private Connection connection;
    private List<FileInfo> fileList;
    private FileChooser fileChooser;
    private boolean reWriteFileCheck;
    private final static long CAPACITY_CLOUD_IN_GB = 10;
    private ExecutorService executorService;

    @FXML
    VBox cloudPane;

    @FXML
    TableView <FileInfo> tableView;

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

    @FXML
    VBox load_bar;
    @FXML
    VBox bar;

    @FXML
    Label fileSizeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        fileList = new ArrayList<>();
        this.fileChooser = new FileChooser();
        reWriteFileCheck = false;
        executorService = Executors.newSingleThreadExecutor(); // 1 поток на выполнение последовательных операций посылки файлов
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        tableView.getColumns().addAll(fileTypeColumn, filenameColumn, fileSizeColumn, fileDateColumn);
        tableView.getSortOrder().add(fileTypeColumn);
       }

    @FXML
    public void clickAddFile(){
        File file = fileChooser.showOpenDialog(ClientApp.getpStage());
        if (file != null) {
            long sizeStream = fileList.stream().map((p) -> p.getFilename()).filter((p)-> p.equals(file.getName())).count();
            if(sizeStream > 0){
                Alert allert = new Alert(Alert.AlertType.CONFIRMATION,"Файл будет перезаписан.");
                Optional<ButtonType> option = allert.showAndWait();
                if (option.get() == null) {
                    return;
                } else if (option.get() == ButtonType.OK) {
                    reWriteFileCheck = true;                // чек на перезапись если отключен то файл не перезаписывается
                    addFile(file);
                    return;
                } else if (option.get() == ButtonType.CANCEL) {
                    return;
                }
            }
            if (connection.getLastLoadSizeFiles() + file.length() > CAPACITY_CLOUD_IN_GB * 1024 * 1024 * 1024) {
                Alert allert = new Alert(Alert.AlertType.INFORMATION, "Нехватает места в облаке для сохранения файла.");
                allert.show();
                return;
            }
            reWriteFileCheck = false;
            addFile(file);
        }
    }

    public void addFile(File file){
        executorService.execute(()->{
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
                reWriteFileCheck = false;
                connection.send(new FilesSizeRequest(1));  // запрос на новый размер файлов и список файлов
                in.close();
                Platform.runLater(() -> {
                    fileNameMessage.setVisible(false);
                    progressBar.setVisible(false);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void clickDeleteButton() {
        String delFileName = tableView.getSelectionModel().getSelectedItem().getFilename();
        if(delFileName != null){
            fileList.remove(delFileName);
            reloadFxFilesList(fileList);
            connection.send(new DellFileRequest(delFileName));
        }
    }

    @FXML
    public void clickGetButton() {
        String getFileName = tableView.getSelectionModel().getSelectedItem().getFilename();
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

    public void reloadFxFilesList(List <FileInfo> fileList ){
        tableView.getItems().clear();
        tableView.getItems().addAll(fileList);
        tableView.sort();
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

    @FXML
    public void changeUser() {
        changeStageToAuth();
        connection.closeConnection();
        connection = null;
    }

    // длина в лоад-баре 1 % в зависимости от размера бара в окне просмотра в пикселях
    // расчет к примре получил метод 4 гб  -> (10 gb / 4 gb) = 2,5 ( это 1/4 от 10 gb)
    // теперь расчитываем сколько в % от 100 будет ( 1/4 ) 100 / 2,5 = 40 %  должен
    // быть заполнен БарЛоад далее берем посчитанный 1 % в пикселях
    // длина БарЛоадера и * на колличество % на которые он должен быть заполнен
    public void changeLoadBar(double sizeFiles) {
        double onePercentLoadBar = load_bar.getHeight()/100;
        double PercenAllFiles = 100 / (CAPACITY_CLOUD_IN_GB / sizeFiles);
        bar.setPrefHeight(PercenAllFiles * onePercentLoadBar);
    }

    public boolean isReWriteFileCheck() {
        return reWriteFileCheck;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setFileList(List<FileInfo> fileList) {
        this.fileList = fileList;
    }
}