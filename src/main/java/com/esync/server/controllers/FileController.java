package com.esync.server.controllers;

import com.dropbox.core.v2.DbxDownloadStyleBuilder;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/file")
public class FileController {
    @Value("${esync.app.dropboxToken}")
    private static String ACCESS_TOKEN;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @SuppressWarnings("StatementWithEmptyBody")
    private static String checksum(String filepath, MessageDigest md) throws IOException {
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

    private static void DownloadFile(String filePathInDropbox, String fileName) throws FileNotFoundException {
//        String url="https://dl.dropboxusercontent.com/u/73386806/Prune%20Juice/Prune%20Juice.exe";
//        String filename="PruneJuice.exe";

        try {
            URL download = new URL(filePathInDropbox);
            ReadableByteChannel rbc = Channels.newChannel(download.openStream());
            FileOutputStream fileOut = new FileOutputStream(fileName);
            fileOut.getChannel().transferFrom(rbc, 0, 1 << 24);
            fileOut.flush();
            fileOut.close();
            rbc.close();
        } catch (Exception e) {
            logger.info("Error at Download");
        }


    }

    @PostMapping("/sign")
    @PreAuthorize("hasRole('ADMIN')")
    public String signFile(@PathVariable String dropbox, @PathVariable String fileName) throws IOException, NoSuchAlgorithmException {
        DownloadFile(dropbox, fileName);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String localFilePath = String.format("./temp/%s", fileName);
        return checksum(localFilePath, md);
    }

    @GetMapping("/files")
    @PreAuthorize("hasRole('DEV') or hasRole('ADMIN')")
    public String CloudFiles() throws DbxException {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("esync-package").build();
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        System.out.println(ACCESS_TOKEN);
        StringBuilder res = new StringBuilder();
        ListFolderResult result = client.files().listFolder("");
        while (true) {
            for (Metadata metadata : result.getEntries()) {
                res.append(metadata.getPathLower());
            }

            if (!result.getHasMore()) {
                break;
            }

            result = client.files().listFolderContinue(result.getCursor());
        }
        return res.toString();
    }

    public FileController() {
    }
}
