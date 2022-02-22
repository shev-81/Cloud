package com.cloud.clientpak;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Описание протокола передачи данных  Protocol ->  |  int - coommand  |  UTF - metaInfo  |  Long - file size  |   byte - bytes file  |
 *    команды передаваемые в заголовке:
 *        -= int - coommand =-
 *    0 - Команда на  удаление файла (посылается клиентом).
 *    1 - Команда на запись полученного файла (сервер -> клиент, клиент -> сервер, в зависимости кто передает файл).
 *    2 - Команда серверу от клиента на передачу файла к клиенту.
 *    3 - Команда на запрос списка файлов с сервера клиенту (используется при успешной авторизации.
 *        Клиент ее не посылает, только сервер при условии прохождения клиентом авторизации).
 *    4 - Команда на запрос авторизации пользователя (сервер получив команду проверяет логин и пароль
 *        и высылает команду клиенту - "3" и список файлов при успешном прохождении авторизации или же
 *        возвращает эту же команду если авторизация не пройдена).
 *    5 - Команда запрос на регистрацию нового пользователя (при условии что регистрация успешна
 *        сервер вернет клиенту эту же команду если нет то команду " 50 ").
 *        -= UTF - metaInfo =-
 *    Содержит строку в зависимости от используемой команды - int - в ней может находится или одно имя файла
 *    или список файлов в виде строки имен файлов разделенных пробелами.
 *        -= Long - file size =-
 *    Содержит размер передаваемого файла в байтах.
 *        -= Byte - bytes file =-
 *    Содержимое передаваемого файла (передается в соответствии с командой в заголовке).
 */

public class Connection implements Runnable {

    private final String SERVER_ADDR = "localhost";  //192.168.1.205";
    private final int SERVER_PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
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
        } catch (IOException e) {
            closeConnection();
            controller.changeStageToAuth();
        }
    }

    public void openConnection() {
        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void readMsg() throws IOException {
        int command = in.readInt();
        switch (command) {
            case 1: comOneFrServ();  break;         // запись файла полученного с сервера по запросу командой " 2 ".
            case 3: comThreeFrServ(); break;        // получение списка файлов с сервера и открытие доступа к окну файлов на сервере
            case 4: comFourFrServ(); break;         // авторизация не пройдена
            case 5: comFiveFrServ(); break;         // регистрация нового пользователя прошла на сервере
            case 50: comFiftyFrServ(); break;       // регистрация нового пользователя НЕ прошла на сервере
        }
    }

    public boolean send(Message message) {
        int command = message.getCommand();
        try {
            switch (command) {
                case 0:  comZeroToServ(message); break;     // запрос от клиента на удаление файла
                case 1: return comOneToServ(message);       // посылаем в Cloud файл
                case 2: comTwoToServ(message); break;       // запрос на копирование файла с сервера
                case 3: break; //todo запрос списка фалов с сервера для отображения клиенту
                case 4: comFourToServ(message); break;      // посылаем запрос на авторизацию мета информация содержит строку с логином и паролем
                case 5: comFiveToServ(message); break;      // запрос на регистрацию нового пользователя
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void comOneFrServ() throws IOException {
        String nameFile = in.readUTF();
        Path path = Paths.get("client/files/" + nameFile);
        Files.deleteIfExists(path);
        long sizeFile = in.readLong();
        try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream("client/files/" + nameFile))) {
            byte [] byteArr = new byte[1024];
            while (sizeFile > 0) {
                int rez = in.read(byteArr);
                fout.write(byteArr,0, rez);
                sizeFile -= rez;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void comThreeFrServ() throws IOException {
        String listFiles = in.readUTF();
        String [] patrs = listFiles.split("\\s+");
        for(int i = 0; i < patrs.length; i++){
            controller.getFileList().add(patrs[i]);
        }
        controller.reloadFxFilesList();
        controller.changeStageToCloud();
    }

    public void comFourFrServ(){
        Platform.runLater(() ->{
            controller.authMessage.setVisible(true);
            controller.authMessage.setText("Авторизация не пройдена!");
        });
    }

    public void comFiveFrServ(){
        Platform.runLater(() ->{
            controller.changeStageToAuth();
            controller.authMessage.setText("Регистрация пройдена!");
            controller.authMessage.setVisible(true);
        });
    }

    public void comFiftyFrServ(){
        Platform.runLater(() ->{
            controller.regMessage.setText("Регистрация не пройдена!");
            controller.regMessage.setVisible(true);
        });
    }

    public void comZeroToServ(Message message) throws IOException {
        String nameDelFile = message.getMetaInfo();
        out.writeInt(0);
        out.writeUTF(nameDelFile);
    }

    public boolean comOneToServ(Message message) throws IOException {
        File file = message.getFile();
        String nameFile = message.getMetaInfo();
        long sizeFile = message.getSizeFile();
        try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file))) {
            out.writeInt(1);
            out.writeUTF(nameFile);
            out.writeLong(sizeFile);
            byte[] byteArr = new byte[1024];   // посылаем файл используя буфер массив
            int in = 0;
            while ((in = fin.read(byteArr)) != -1) {
                out.write(byteArr, 0, in);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void comTwoToServ(Message message) throws IOException{
        out.writeInt(2);
        out.writeUTF(message.getMetaInfo());
    }

    public void comFourToServ(Message message) throws IOException{
        String loginPass = message.getMetaInfo();
        out.writeInt(4);
        out.writeUTF(loginPass);
    }

    public void comFiveToServ(Message message) throws IOException{
        String userNameLoginPass = message.getMetaInfo();
        out.writeInt(5);
        out.writeUTF(userNameLoginPass);
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
