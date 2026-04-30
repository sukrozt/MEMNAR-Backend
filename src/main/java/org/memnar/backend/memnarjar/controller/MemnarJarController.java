package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.memnar.memnar.pnarpp.algorithm.PNARpp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;

@RestController
@CrossOrigin
public class MemnarJarController {

    private final ConfigService configService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MemnarJarController(ConfigService configService, SimpMessagingTemplate messagingTemplate) {
        this.configService = configService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/memnarjar/start")
    @SendTo("/memnarjar/status")
    public MemnarJarStatus runJar() throws Exception {
        
        System.out.println("\n ----- PREPARING TO RUN ----- \n");

        // Alert the client immediately that the algorithm has started running
        messagingTemplate.convertAndSend("/memnarjar/status", new MemnarJarStatus("Running", "The MEMNAR algorithm is currently executing..."));

        System.out.println("[DEBUG - MemnarJarController] About to write config file. DatasetName currently is: " + configService.getConfig().getDatasetName());

        configService.writeConfigFile();
        long startTime = System.currentTimeMillis();
        
        // Ensure this method is thread-safe if multiple users connect!
        PNARpp.runAlgorithm();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("\n -----FINISHED----- \n ELAPSED TIME : " + totalTime + " ms");

        // Find the most recently created output directory and HTML file
        File outputDir = new File("output");
        File latestOutputFile = null;
        long latestTime = 0;

        if (outputDir.exists() && outputDir.isDirectory()) {
            // 1. Check for files directly in the 'output' directory
            File[] directFiles = outputDir.listFiles((dir, name) -> name.endsWith(".html"));
            if (directFiles != null) {
                for (File file : directFiles) {
                    if (file.lastModified() > latestTime) {
                        latestOutputFile = file;
                        latestTime = file.lastModified();
                    }
                }
            }

            // 2. Check for files in subdirectories
            File[] subDirs = outputDir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    File htmlFile = new File(subDir, "MutualExclusiveSets.html");
                    if (htmlFile.exists() && htmlFile.lastModified() > latestTime) {
                        latestOutputFile = htmlFile;
                        latestTime = htmlFile.lastModified();
                    }
                }
            }
        }

        if (latestOutputFile != null) {
            try (FileInputStream fis = new FileInputStream(latestOutputFile)) {
                String output = new String(fis.readAllBytes());
                return new MemnarJarStatus("FINISHED in " + totalTime + " ms", output);
            } catch (Exception e) {
                return new MemnarJarStatus("Error", "Algorithm finished, but failed to read the output file.");
            }
        } else {
            return new MemnarJarStatus("Error", "Algorithm finished, but output file not found in 'output' directory.");
        }

    }
}