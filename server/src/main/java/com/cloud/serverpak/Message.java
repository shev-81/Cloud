package com.cloud.serverpak;

import java.io.File;

public class Message {

    private int command;
    private String metaInfo;
    private long sizeFile;
    private File file;

    public Message(int command) {
        this.command = command;
    }

    public Message(int command, String metaInfo) {
        this.command = command;
        this.metaInfo = metaInfo;
    }

    public Message(int command, File file) {
        this.command = command;
        this.metaInfo = file.getName();
        this.file = file;
        this.sizeFile = file.length();
    }

    public int getCommand() {
        return command;
    }

    public String getMetaInfo() {
        return metaInfo;
    }

    public long getSizeFile() {
        return sizeFile;
    }

    public File getFile() {
        return file;
    }
}
