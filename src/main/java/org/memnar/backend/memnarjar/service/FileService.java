package org.memnar.backend.memnarjar.service;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.memnar.memnar.pnarpp.algorithm.PNARpp;
import org.memnar.memnar.pnarpp.datasetmgr.TCGADataset;
import org.memnar.memnar.RunConverter;

@Service
public class FileService {

    public Path saveFile(String filename, String base64Data, int chunkIndex, int totalChunks) throws IOException {
        
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        
        Path destinationDir = Paths.get("mutation_data").toAbsolutePath().normalize();
        if (!Files.exists(destinationDir)) {
            System.out.println("mutation_data folder hasn't been found.");
        }
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

    public void runDataConverter(String rawInputPath) throws Exception {
        try {
            System.out.println("⚙️ Starting DataConverter...");
            
            String[] args = { rawInputPath };
            RunConverter.main(args);
            
            System.out.println("✅ DataConverter finished successfully.");
        } catch (Exception e) {
            System.err.println("❌ Error executing converter: " + e.getMessage());
            throw e;
    }
    }

    public void enforceMutationDataFolder(String zipFileName) throws IOException {
        String folderName = zipFileName;
        if (folderName.toLowerCase().endsWith(".zip")) {
            folderName = folderName.substring(0, folderName.length() - 4);
        }
        
        File extractedDir = new File(folderName);
        File targetDir = new File("mutation_data");
        
        if (!folderName.equalsIgnoreCase("mutation_data") && extractedDir.exists() && extractedDir.isDirectory()) {
            System.out.println("Renaming unzipped folder '" + folderName + "' to 'mutation_data'");
            boolean renamed = extractedDir.renameTo(targetDir);
            if (!renamed) {
                System.out.println("Rename failed. Creating mutation_data and copying exact context...");
                FileSystemUtils.copyRecursively(extractedDir, targetDir);
                FileSystemUtils.deleteRecursively(extractedDir);
            }
        }
    }

    public String findValidRawDataFile(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    // Simple heuristic: find the first file that isn't a hidden/system file.
                    if (file.isFile() && !fileName.startsWith(".") && !fileName.equalsIgnoreCase("Thumbs.db")) {
                        return file.getPath();
                    }
                }
            }
        }
        throw new IOException("Could not find a suitable raw data file inside the '" + dirPath + "' directory.");
    }
}