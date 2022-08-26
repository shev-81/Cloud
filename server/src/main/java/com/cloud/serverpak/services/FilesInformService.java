package com.cloud.serverpak.services;

import messages.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class is intended as a service that returns in its methods: how much space
 * the cloud is busy for a specific user, a list of the user's files.
 */
public class FilesInformService {

    /**
     * Returns a list of objects describing files for a specific user.
     * @param nameUser username.
     * @return A list of objects describing the files. it can return "Null" if there are no files.
     * @throws IOException when working with files.
     */
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

    /**
     * Returns the size of files in bytes.
     * @param nameUser username.
     * @return file size in bytes.
     * @throws IOException when working with files.
     */
    public long getFilesSize(String nameUser) throws IOException {
        if (nameUser != null) {
            Path path = Paths.get("server/files/" + nameUser);
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            return Files.list(path).map((p) -> p.toFile().length()).reduce(Long::sum).orElse(0L);
        }
        return 0;
    }
}
