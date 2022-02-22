package com.cloud.serverpak;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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

public class ClientHandler {

    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nameUser;
    private ServerApp server;

    public ClientHandler(ServerApp serverApp, Socket socket){
        try {
            this.server = serverApp;
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            this.nameUser = "";
            while (true){
                read();
            }
        } catch (IOException e) {
            LOGGER.info("[Server]: Соединение c клиентом разорванно.");
            closeConnection();
        }
    }

    public void read() throws IOException {
        int command = in.readInt();
        switch (command) {
            case 0: comZeroFromClient(); break;         // команда на  удаление файла с сервера
            case 1: comOneFromClient(); break;          // команда на запись полученного файла (от клиента на серевер)
            case 2: comTwoFromClient(); break;          // команда на копирование файла к клиенту
            case 3: comThreeFromClient(); break;        // команда на запрос списка файлов с сервера клиенту  //todo пока сервер посылает список только после авторизации
            case 4: comFourFromClient(); break;         // команда на запрос авторизации пользователя
            case 5: comFiveFromClient(); break;         // команда на регистрацию нового пользователя
        }
    }

    public void sendUser(Message message) {
        int command = message.getCommand();
        try {
            switch (command) {
                case 2: comTwoToClient(message); break;     // копирование файла клиенту
                case 3: comThreeToClient(message); break;   // посылаем список файлов (в каталоге клиента) с сервера
                case 4: comFourToClient(); break;           // авторизация не пройдена
                case 5: comFiveToClient(); break;           // регистрация успешно пройдена
                case 50: comFiftyToClient(); break;         // регистрация успешно пройдена
            }
        } catch (Exception e) {
            LOGGER.throwing(Level.ERROR, e);
            closeConnection();
        }
    }

    public void comZeroFromClient() {
        try {
            String nameDelFile = in.readUTF();
            Path path = Paths.get("server/files/" + nameUser + "/" + nameDelFile);
            Files.delete(path);
            LOGGER.info("Пользователь " + nameUser + " удалил файл "+nameDelFile);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("Пользователь " + nameUser + " не удалил файл");
        }
    }

    public void comOneFromClient() throws IOException {
        LOGGER.info("[Server]: Получаем файл.");
        Path directory1 = Paths.get("server/files/" + nameUser);
        if(!Files.exists(directory1)){
            Files.createDirectory(directory1);
        }
        String nameFile = in.readUTF();
        long sizeFile = in.readLong();
        try (BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream("server/files/" + nameUser + "/" + nameFile))) {
            byte [] byteArr = new byte[1024];
            while (sizeFile > 0) {
                int rez = in.read(byteArr);
                fout.write(byteArr,0, rez);
                sizeFile -= rez;
            }
            LOGGER.info("[Server]: Файл "+nameFile+" записан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void comTwoFromClient() throws IOException {
        String getNameFile = in.readUTF();
        sendUser(new Message(2, getNameFile));
    }

    public void comThreeFromClient(){}  //todo возможно будет реализация функции обновления списка файлов на сервере

    public void comFourFromClient() throws IOException {
        String loginPass = in.readUTF();
        String[] partsLoginPass = loginPass.split("\\s+");
        String nickName = server.getAuthService().getNickByLoginPass(partsLoginPass[0], partsLoginPass[1]);
        if (nickName != null) {
            if (!server.isNickBusy(nickName)) {
                LOGGER.info("Пользователь " + nickName + " авторизовался на сервере!");
                File directory = new File("server/files/" + nickName);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                Path path = Path.of("server/files/" + nickName);
                nameUser = nickName;
                server.subscribe(this);
                // формируем лист файлов находящихся у сервера для клиента
                String listFiles = Files.list(path).map((p) -> p.getFileName().toString()).collect(Collectors.joining(" "));
                sendUser(new Message(3, listFiles));  // т.к. авторизация успешна - высылаем лист файлов
            } else {
                sendUser(new Message(4));   // авторизация не пройдена возвращаем эту же команду клиенту
            }
        } else {
            sendUser(new Message(4));     // авторизация не пройдена возвращаем эту же команду клиенту
        }
    }

    public void comFiveFromClient() throws IOException {
        String nameUserLoginPass = in.readUTF();
        String[] partsUsLogPass = nameUserLoginPass.split("\\s+");
        if(server.getAuthService().registerNewUser(partsUsLogPass[0], partsUsLogPass[1], partsUsLogPass[2])){ //если регистрацция успешно пройдена
            sendUser(new Message(5));
        }else { // если не пройдена
            sendUser(new Message(50));
        }
    }

    public void comTwoToClient(Message message) throws IOException {
        String getNameFile = message.getMetaInfo();
        File fileCopy = new File("server/files/" + nameUser + "/" + getNameFile);
        long sizeFile = fileCopy.length();
        out.writeInt(1);   // клиент согласно описания протокола должен принять по команде - " 1 "
        out.writeUTF(getNameFile);
        out.writeLong(sizeFile);
        Files.copy(Paths.get("server/files/" + nameUser + "/" + getNameFile), out);
//        try (BufferedInputStream fin = new BufferedInputStream(new FileInputStream(fileCopy))){
//            byte [] byteArr = new byte[1024];   // посылаем файл используя буфер массив
//            int in = 0;
//            while((in = fin.read(byteArr)) != -1){
//                out.write(byteArr, 0 ,in);
//            }
//            LOGGER.info("[Server]: Файл "+getNameFile +" выслан пользователю.");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void comThreeToClient(Message message)throws IOException {
        String listFiles = message.getMetaInfo();
        out.writeInt(3);
        out.writeUTF(listFiles);
    }

    public void comFourToClient() throws IOException {
        out.writeInt(4);
    }

    public void comFiveToClient() throws IOException {
        out.writeInt(5);
    }

    public void comFiftyToClient() throws IOException {
        out.writeInt(50);
    }

    public void closeConnection() {
        try {
            server.unSubscribe(this);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.throwing(Level.WARN, e);
        }
    }

    public String getNameUser() {
        return nameUser;
    }
}
