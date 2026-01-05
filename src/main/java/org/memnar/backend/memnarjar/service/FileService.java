package org.memnar.backend.memnarjar.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.StandardOpenOption;


@Service
public class FileService {

    public Path saveFile(String filename, String base64Data, int chunkIndex, int totalChunks) throws IOException {
        
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        
        Path destinationDir = Paths.get(".").toAbsolutePath().normalize();
        Path filePath = destinationDir.resolve(filename);

        if (chunkIndex == 0) {
            System.out.println("--- STARTING NEW UPLOAD: " + filename + " ---");
            Files.write(filePath, decodedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            Files.write(filePath, decodedBytes, StandardOpenOption.APPEND);
        }

        System.out.println("Saved Chunk " + (chunkIndex + 1) + "/" + totalChunks + " (" + decodedBytes.length + " bytes)");
        
        return filePath;
    }

    public void unzip(Path zipFilePath, String destDir) throws IOException {
        System.out.println("--- STARTING UNZIP ---");
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs();

        // use try-with-resources to ensure the ZipInputStream closes
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                
                // Security Check (Zip Slip)
                String destDirPath = dir.getCanonicalPath();
                String destFilePath = newFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator) && !destFilePath.equals(destDirPath)) {
                    throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // THE FIX: Use Files.copy instead of manual buffer loop
                    // This handles large files much more reliably
                    Files.copy(zis, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    System.out.println("Extracted: " + newFile.getName() + " (" + newFile.length() + " bytes)");
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        System.out.println("--- UNZIP FINISHED ---");
    }
}