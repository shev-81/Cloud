package com.cloud.clientpak;

import messages.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class Connection implements Runnable {

    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private Controller controller;
    private long lastLoadSizeFiles;

    public Connection(Controller controller) {
        this.controller = controller;
        openConnection();
    }

    @Override
    public void run() {
        try {
            while(true){
                readMsg();
            }
        } catch (IOException | ClassNotFoundException e) {
            closeConnection();
            controller.changeStageToAuth();
        }
    }

    public void readMsg() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();

        if(obj instanceof AuthMessage) authMess(obj);               // ответ от сервера о состоянни авторизации

        if (obj instanceof RegUserRequest) regUserReq(obj);         // ответ от сервера о состоянии регистрации  regUserReq(obj);

        if (obj instanceof FileMessage) fileMess(obj);              // сервер прислал файл

        if (obj instanceof FilesSizeRequest) filesSizeMess(obj);    // сервер прислал размер фалов
    }

    public void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    public void send(AbstractMessage msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authMess(Object obj) throws IOException {
        AuthMessage msg = (AuthMessage) obj;
        if(!msg.getLoginUser().equals("none")){
            openCloudWindow(msg);
        }else{
            AuthNo();
        }
    }

    public void regUserReq(Object obj){
        RegUserRequest regUserRequest = (RegUserRequest) obj;
        // если регистрация НЕ пройдена
        if (regUserRequest.getNameUser().equals("none")) {
            Platform.runLater(() -> {
                controller.regMessage.setText("Регистрация не пройдена!");
                controller.regMessage.setVisible(true);
            });
        } else {
            // если регистрация пройдена
            Platform.runLater(() -> {
                controller.changeStageToAuth();
                controller.authMessage.setText("Регистрация пройдена!");
                controller.authMessage.setVisible(true);
            });
        }
    }

    public void fileMess(Object obj) throws IOException {
        FileMessage fmsg = (FileMessage) obj;
        boolean append = true;
        if (fmsg.partsCount == 1) {
            append = false;
        }
        double percentProgressBar = (double) 1 / fmsg.partsCount;
        Platform.runLater(() -> {
            controller.fileNameMessage.setText("Копируем файл - "+ fmsg.filename + ".");
            controller.fileNameMessage.setVisible(true);
            controller.progressBar.setVisible(true);
            controller.progressBar.setProgress((double) fmsg.partNumber * percentProgressBar);
        });
        System.out.println(fmsg.partNumber + " / " + fmsg.partsCount);
        FileOutputStream fos = new FileOutputStream("client/files/" + fmsg.filename, append);
        fos.write(fmsg.data);
        fos.close();
        if (fmsg.partNumber == fmsg.partsCount) {
            System.out.println("файл полностью получен");
            send(new FilesSizeRequest(1));
            Platform.runLater(()->{
                controller.progressBar.setVisible(false);
                controller.fileNameMessage.setVisible(false);
            });
        }
    }

    public void openCloudWindow(AuthMessage msg) throws IOException {
        controller.setFileList(msg.getListFiles());
        controller.reloadFxFilesList(msg.getListFiles());
        controller.changeStageToCloud();
    }

    public void AuthNo(){
        Platform.runLater(() ->{
            controller.authMessage.setVisible(true);
            controller.authMessage.setText("Авторизация не пройдена!");
        });
    }

    public void filesSizeMess(Object obj){
        if(controller.isReWriteFileCheck()){
            return;
        }
        FilesSizeRequest filesObj = (FilesSizeRequest) obj;
        lastLoadSizeFiles = filesObj.getFilesSize();                         // сохраняем последний запрос размера в байтах
        double filesSize = (double) filesObj.getFilesSize()/1024/1024/1024;  // расчет байтов единицы измерения гигабайты
        List<FileInfo> listFiles = filesObj.getListFiles();
        controller.setFileList(listFiles);
        Platform.runLater(() -> {
            controller.changeLoadBar(filesSize);
            controller.fileSizeLabel.setText(String.valueOf(filesSize).substring(0, 3));
            controller.reloadFxFilesList(listFiles);                             // обновляем лист файлов в нашей таблице
        });
    }

    public long getLastLoadSizeFiles() {
        return lastLoadSizeFiles;
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
