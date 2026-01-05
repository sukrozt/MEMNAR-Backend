package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ConfigData getConfig() {
        return configService.getConfig();
    }

    @PostMapping
    public String updateConfig(@RequestBody ConfigData newConfig) {
        configService.updateConfig(newConfig);
        return "Settings Saved Successfully";
    }
}