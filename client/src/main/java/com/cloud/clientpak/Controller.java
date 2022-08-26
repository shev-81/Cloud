package com.cloud.clientpak;

import com.cloud.clientpak.services.FileWorker;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Data;
import messages.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;


/**
 *  The application controller class. Binds all application methods that are
 *  executed from user actions in the application UI. Contains a link to the
 *  current network connection to the server.
 */
@Data
public class Controller implements Initializable{

    /**
     * {@link Connection Network connection}
     */
    private static Connection connection;

    /**
     * List of files in the cloud.
     */
    private List<FileInfo> fileList;

    /**
     * Dialog for selecting a file from the current operating system.
     * @see FileChooser
     */
    private FileChooser fileChooser;

    /**
     * Storage capacity.
     */
    public final static long CAPACITY_CLOUD_IN_GB = 10;

    /**
     * Переменная класса {@link FileWorker FileWorker}.
     */
    private FileWorker fileWorker;

    /**
     * The main storage panel.
     */
    @FXML
    VBox cloudPane;

    /**
     * Table view for showing files.
     */
    @FXML
    TableView <FileInfo> tableView;

    /**
     * Registration panel.
     */
    @FXML
    GridPane regPane;

    /**
     * Login input field during registration.
     */
    @FXML
    TextField regLogin;

    /**
     * Password input field during registration.
     */
    @FXML
    PasswordField regPassword;

    /**
     * The password re-entry field during registration.
     */
    @FXML
    PasswordField regPasswordRep;

    /**
     * The field for entering the user name during registration.
     */
    @FXML
    TextField regName;

    /**
     * The label on the registration panel.
     */
    @FXML
    Label regMessage;

    /**
     * Login input field on the authorization panel.
     */
    @FXML
    TextField authLogin;

    /**
     * Password entry field on the authorization panel.
     */
    @FXML
    PasswordField authPassword;

    /**
     * Authorization panel.
     */
    @FXML
    GridPane authPane;

    /**
     * A text label on the authorization panel.
     */
    @FXML
    Label authMessage;

    /**
     *The progress bar of the file upload progress.
     */
    @FXML
    ProgressBar progressBar;

    /**
     * A text label for displaying the downloaded file.
     */
    @FXML
    Label fileNameMessage;

    /**
     * Cloud load bar's.
     */
    @FXML
    VBox load_bar;
    @FXML
    VBox bar;

    /**
     * The text label shows the numerical equivalent of the cloud load.
     */
    @FXML
    Label fileSizeLabel;

    /**
     * When the application starts, it shows the user authorization panel.
     * Creates an empty list of files, creates a dialog for selecting files,
     * creates a file management service, prepares a table model for displaying
     * files.
     * @param location an unused parameter from an inherited interface.
     * @param resources an unused parameter from an inherited interface.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        this.fileList = new ArrayList<>();
        this.fileChooser = new FileChooser();
        this.fileWorker = new FileWorker();
        createTableView();
       }

    /**
     * Called when the user clicks on the add file button. Opens the dialog
     * for selecting the file of the current OS {@link FileChooser FileChooser}.
     * Passes the path to the selected file to the file management service
     * {@link FileWorker FileWorker}.
     */
    @FXML
    public void clickAddFile() {
        File file = fileChooser.showOpenDialog(ClientApp.getStage());
        if (file != null) {
            long sizeStream = getFileList().stream().map(FileInfo::getFilename).filter((p)-> p.equals(file.getName())).count();
            fileWorker.checkAddFile(file, this::setVisibleLoadInfoFile,sizeStream > 0);
        }
    }

    /**
     * Makes visible a label showing the name of the file being transferred.
     * @param check true while the file is being transferred.
     */
    public void setVisibleLoadInfoFile(boolean check){
        fileNameMessage.setText("Загрузка файла.");
        fileNameMessage.setVisible(check);
        progressBar.setVisible(check);
    }

    /**
     * Sets the size of the progress bar.
     * @param percent Percentage of filling.
     */
    public void changeProgressBar (double percent){
        progressBar.setProgress(percent);
    }

    /**
     * It is called when clicking on the "Delete" button, selects an entry
     * with the current focus from the table model and takes out the file
     * name from it that needs to be deleted. Deletes the entry with focus
     * from the table. Generates and sends a message to the server about
     * the need to delete the file.
     */
    @FXML
    public void clickDeleteButton() {
        String delFileName;
        delFileName = tableView.getSelectionModel().getSelectedItem().getFilename();
        if(delFileName != null){
            fileList.remove(delFileName);
            reloadFxFilesList(fileList);
            connection.send(new DelFileRequest(delFileName));
        }
    }

    /**
     * Sends a request to the server to receive the selected file.
     */
    @FXML
    public void clickGetButton() {
        String getFileName = tableView.getSelectionModel().getSelectedItem().getFilename();
        if(getFileName != null){
            connection.send(new FileRequest(getFileName));
        }
    }

    /**
     * Makes the authorization panel visible, hides the rest of the panels.
     */
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

    /**
     * Makes the main panel of the cloud visible and hides the rest.
     */
    public void changeStageToCloud() {
        cloudPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
    }

    /**
     * Makes the registration panel visible, puts the rest of the panels
     * in an invisible state.
     */
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

    /**
     * It is called by clicking on the "Exit the program" item in the upper menu.
     */
    @FXML
    public void exitApp() {
        fileWorker.stop();
        connection.close();
        Platform.exit();
    }

    /**
     * Reloads the table view based on the list of files.
     * @param fileList List of files.
     */
    public void reloadFxFilesList(List <FileInfo> fileList ){
        tableView.getItems().clear();
        tableView.getItems().addAll(fileList);
        tableView.sort();
    }

    /**
     * It is called by clicking the log in to the cloud button.
     * Before logging in, it is checked whether the user has entered
     * the data - login and password. If no data is entered, it
     * displays a message in the text label. Starts a network connection
     * to the server.
     * @throws InterruptedException it may occur if another thread tries to
     * interrupt the waiting for a connection to the server, but in this
     * program this is unlikely.
     */
    @FXML
    public void enterCloud() throws InterruptedException {
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
            return;
        }
        if (connection == null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            connection = new Connection(this, countDownLatch);
            new Thread(connection).start();
            countDownLatch.await();
            fileWorker.setConnection(connection);
        }
        connection.send(new AuthMessage(authLogin.getText(), authPassword.getText()));
    }

    /**
     * It is called when the user clicks on the register button. Checks whether
     * all data is entered for registration. If everything is in order, it opens
     * a network connection with the server and sends a service message about
     * registering a new user.
     * @throws InterruptedException it may occur if another thread tries to
     * interrupt the waiting for a connection to the server, but in this
     * program this is unlikely.
     */
    @FXML
    public void register() throws InterruptedException {
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
            return;
        }
        if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
            return;
        }
        if (connection == null) {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            connection = new Connection(this, countDownLatch);
            new Thread(connection).start();
            countDownLatch.await();
        }
        AbstractMessage message = new RegUserRequest(regName.getText(),  regLogin.getText(), regPassword.getText());
        connection.send(message);
    }

    /**
     * Changes the panel to the Authorization panel and closes the network connection.
     */
    @FXML
    public void changeUser() {
        changeStageToAuth();
        connection.close();
        connection = null;
    }

    /**
     * Prepares the presentation of the table, for its subsequent filling with data.
     */
    public void createTableView(){
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

    /**
     * Changes the size of the load bar of the cloud file storage. The length in
     * the load bar is 1% depending on the size of the bar in the viewport in
     * pixels calculation example: I got the method 4 gb -> (10 gb / 4 gb) = 2.5
     * (this is 1/4 of 10 gb) we calculate how much in % of 100 will be
     * ( 1/4 ) 100 / 2.5 = 40 % should be The Bar is filled. We take the calculated
     * 1% in pixels of the length of the Bar and multiply by the % by which it
     * should be filled.
     * @param sizeFiles File size in bytes.
     */
    public void changeLoadBar(double sizeFiles) {
        double onePercentLoadBar = load_bar.getHeight()/100;
        double PercenAllFiles = 100 / (CAPACITY_CLOUD_IN_GB / sizeFiles);
        bar.setPrefHeight(PercenAllFiles * onePercentLoadBar);
    }

    public static Connection getConnection() {
        return connection;
    }
}