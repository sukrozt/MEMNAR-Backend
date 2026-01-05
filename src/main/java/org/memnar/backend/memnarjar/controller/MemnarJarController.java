package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import pnarpp.algorithm.PNARpp;

import java.io.FileInputStream;

@RestController
@CrossOrigin
public class MemnarJarController {

    private final ConfigService configService;
    // Note: FileService is no longer needed here if only runJar remains!
    // Unless you plan to use it to read the output file later.

    @Autowired
    public MemnarJarController(ConfigService configService) {
        this.configService = configService;
    }

    @MessageMapping("/memnarjar/start")
    @SendTo("/memnarjar/status")
    public MemnarJarStatus runJar() throws Exception {
        
        System.out.println("\n ----- PREPARING TO RUN ----- \n");

        configService.writeConfigFile();
        long startTime = System.currentTimeMillis();
        
        // Ensure this method is thread-safe if multiple users connect!
        PNARpp.runAlgorithm(); 

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("\n -----FINISHED----- \n ELAPSED TIME : " + totalTime + " ms");

        // Ideally, move this file reading logic to FileService as well eventually
        try (FileInputStream fis = new FileInputStream("output/braingenesupp01minconf05/MutualExclusiveSets.html")) {
            String output = new String(fis.readAllBytes());
            return new MemnarJarStatus("FINISHED in " + totalTime + " ms", output);
        } catch (Exception e) {
            return new MemnarJarStatus("Error", "Algorithm finished, but output file not found.");
        }
        //asdasdasd
    }
}