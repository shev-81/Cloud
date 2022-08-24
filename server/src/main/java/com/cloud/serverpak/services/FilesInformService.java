package com.cloud.serverpak.services;

import messages.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FilesInformService {

    public List<FileInfo> getListFiles(String nameUser) throws IOException {
        if (nameUser != null) {
            Path path = Paths.get("server/files/" + nameUser);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            return Files.list(path).map(FileInfo::new).collect(Collectors.toList());
        }
        return null;
    }

    public long getFilesSize(String nameUser) throws IOException {
        if (nameUser != null) {
            Path path = Paths.get("server/files/" + nameUser);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            long size = Files.list(path).map((p) -> p.toFile().length()).reduce((s1, s2) -> s1 + s2).orElse(Long.valueOf(0));
            return size;
        }
        return 0;
    }
}
