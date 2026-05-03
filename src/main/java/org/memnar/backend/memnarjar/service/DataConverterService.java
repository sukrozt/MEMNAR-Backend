package org.memnar.backend.memnarjar.service;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataConverterService {

    private final ConfigService configService;
    private final FileService fileService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public DataConverterService(ConfigService configService, FileService fileService, SimpMessagingTemplate messagingTemplate) {
        this.configService = configService;
        this.fileService = fileService;
        this.messagingTemplate = messagingTemplate;
    }

    public void processFormatting() throws Exception {
        ConfigData config = configService.getConfig();
        
        System.out.println("[DEBUG - DataConverterService] Checking if formatting is needed. isUnformatted: " + config.isUnformatted());
        
        if (config.isUnformatted()) {
            messagingTemplate.convertAndSend("/memnarjar/status", new MemnarJarStatus("Formatting", "Formatting raw data..."));
            String rawInputPath = config.getDatasetName();
            
            if (rawInputPath == null || rawInputPath.trim().isEmpty()) {
                throw new IllegalStateException("Dataset path is empty. Please upload a file first.");
            }

            // IMPORTANT: Write the configuration file AND setup resources (res folder) 
            // BEFORE running the DataConverter, because the DataConverter requires config.properties to exist!
            System.out.println("[DEBUG - DataConverterService] Writing config file for DataConverter. Raw input: " + rawInputPath);
            configService.writeConfigFile();
            
            System.out.println("[DEBUG - DataConverterService] Detected unformatted data. Running DataConverter on: " + rawInputPath);
            fileService.runDataConverter(rawInputPath);
            
            // Calculate the automatically generated output path
            String formattedOutputPath = rawInputPath.endsWith("_filtered.txt") ? rawInputPath : (rawInputPath.endsWith(".txt") 
                ? rawInputPath.substring(0, rawInputPath.length() - 4) + "_filtered.txt" 
                : rawInputPath + "_filtered.txt");

            config.setDatasetName(formattedOutputPath);
            // Set to false so the main algorithm config doesn't re-append "_filtered" on the second write
            config.setUnformatted(false);
            configService.updateConfig(config);
        } else {
            // Even if we skip formatting, we must write the config file for the main algorithm to run!
            configService.writeConfigFile();
        }
    }
}
