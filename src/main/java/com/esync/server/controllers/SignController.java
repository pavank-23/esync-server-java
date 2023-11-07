package com.esync.server.controllers;

import com.esync.server.models.FileMetadata;
import com.esync.server.repository.FileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.*;
import java.security.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
@RequestMapping("/api/file")
public class SignController {
    private final FileRepository fileRepository;
    @Autowired
    public SignController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }
    @SuppressWarnings("StatementWithEmptyBody")
    private static @NotNull String checksum(String filepath, MessageDigest md) throws IOException {
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1)
                ;
            md = dis.getMessageDigest();
        }
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }
//    @Value("${esync.app.dropboxToken}")
//    private static String ACCESS_TOKEN;
    @PostMapping("/sign")
    @PreAuthorize("hasRole('ADMIN')")
    public String signFile(@RequestParam String fileName) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String localFilePath = String.format("./tempStorage/%s", fileName);
        String res = checksum(localFilePath, md);
        fileRepository.save(new FileMetadata(fileName, localFilePath, res));
        return res;
    }}

