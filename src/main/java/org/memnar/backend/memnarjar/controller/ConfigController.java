package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// This annotation is crucial for allowing requests from your frontend (e.g., http://localhost:5173)
// Adjust the port if your React development server uses a different one.

@RestController
@CrossOrigin
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("api/config")
    public ConfigData getConfig() {
        // Fetches the current configuration state directly from the service
        return configService.getConfig();
    }

    @PostMapping("api/config")
    public void saveConfig(@RequestBody ConfigData config) {
        // Updates the in-memory configuration state in the service
        configService.updateConfig(config);
    }
}
