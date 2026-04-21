package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.dto.FileDTO; // Or your specific DTO
import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.memnar.backend.memnarjar.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller; // Use @Controller for WebSocket
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Path;

@Controller
public class FileController {

    private final FileService fileService;
    private final ConfigService configService;

    @Autowired
    public FileController(FileService fileService, ConfigService configService) {
        this.fileService = fileService;
        this.configService = configService;
    }

    @MessageMapping("/memnarjar/datainput") //listens from
    @SendTo("/memnarjar/status") //send to 
    public MemnarJarStatus handleFileUpload(FileDTO data) {
        System.out.println("\nUPLOAD RECEIVED");

        try {
            // 1. Save chunk
            Path savedZip = fileService.saveFile(data.getName(), data.getBase64(), data.getChunkIndex(), data.getTotalChunks());
            
            // Only process after the final chunk is received
            if (data.getChunkIndex() < data.getTotalChunks() - 1) {
                return new MemnarJarStatus("Uploading", "Chunk " + (data.getChunkIndex() + 1) + " of " + data.getTotalChunks() + " received.");
            }
            
            // ALWAYS unzip (since input is always a zip now)
            fileService.unzip(savedZip, "."); 

            // 2. Enforce folder name to be "mutation_data"
            String folderName = data.getName();
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

            // Call FileService to ensure the background file exists (and check KeggPathway)
            fileService.ensureKeggPathwayExists();

            // 3. Handle the DataConverter logic
            if (data.isUnformatted()) {
                System.out.println(" User marked data as unformatted. Running DataConverter...");
                
                // Because they uploaded a zip, we assume the raw file inside is named raw_dataset.txt
                // (Change this if your raw text file inside the zip has a specific name!)
                String rawInputPath = "mutation_data/raw_dataset.txt"; 
                String formattedOutputPath = "mutation_data/formatted_dataset.txt";
                
                fileService.runDataConverter(rawInputPath, formattedOutputPath);

                ConfigData config = configService.getConfig();
                config.setDatasetName(formattedOutputPath);
                configService.updateConfig(config);

                return new MemnarJarStatus("Success", "Zip uploaded, unzipped, formatted, and background files secured.");
            } else {
                System.out.println("✅ Data is already formatted. Skipping DataConverter.");
                
                ConfigData config = configService.getConfig();
                config.setDatasetName("mutation_data");
                configService.updateConfig(config);

                return new MemnarJarStatus("Success", "Zip uploaded, unzipped, and ready for MEMNAR.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new MemnarJarStatus("Error", "Upload failed: " + e.getMessage());
        }
    }
}