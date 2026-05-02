package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.dto.FileDTO;
import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
// This annotation fixes your CORS error by explicitly allowing your Vite frontend
@CrossOrigin(origins = "http://localhost:5173")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    // This fixes the 404 error on GET requests (fetching config on load)
    @GetMapping
    public ConfigData getConfig() {
        return configService.getConfig();
    }

    @PostMapping
    public void saveConfig(@RequestBody ConfigData newConfig) {
        configService.updateConfig(newConfig);
    }
}
