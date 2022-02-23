package com.cloud.clientpak;

import messages.AbstractMessage;
import messages.AuthMessage;
import messages.FileMessage;
import messages.RegUserRequest;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;

public class Connection implements Runnable {

    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private Controller controller;

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

        if(obj instanceof AuthMessage) authMess(obj);          // ответ от сервера о состоянни авторизации

        if (obj instanceof RegUserRequest) regUserReq(obj);    // ответ от сервера о состоянии регистрации  regUserReq(obj);

        if (obj instanceof FileMessage) fileMess(obj);         // сервер прислал файл //todo сделать прогресс бар получения файла
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
        System.out.println(fmsg.partNumber + " / " + fmsg.partsCount);
        FileOutputStream fos = new FileOutputStream("client/files/" + fmsg.filename, append);
        fos.write(fmsg.data);
        fos.close();
        if (fmsg.partNumber == fmsg.partsCount) {
            System.out.println("файл полностью получен");
        }
    }

    public void openCloudWindow(AuthMessage msg) throws IOException {
        String listFiles = msg.getPassUser();   // строка с перечнем файлов клиенту возвращается в объекте авторизации в поле пароля
        String [] patrs = listFiles.split("\\s+");
        for(int i = 0; i < patrs.length; i++){
            controller.getFileList().add(patrs[i]);
        }
        controller.reloadFxFilesList();
        controller.changeStageToCloud();
    }

    public void AuthNo(){
        Platform.runLater(() ->{
            controller.authMessage.setVisible(true);
            controller.authMessage.setText("Авторизация не пройдена!");
        });
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
