package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ConfigController {

    private final ConfigService configService;

    @Autowired
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @MessageMapping("/config/get")
    @SendTo("/memnarjar/config")
    public ConfigData getConfig() {
        // Fetches the current configuration state directly from the service
        return configService.getConfig();
    }

    @MessageMapping("/config/save")
    @SendTo("/memnarjar/config")
    public ConfigData saveConfig(ConfigData config) {
        System.out.println("[DEBUG - ConfigController] Received WebSocket message from frontend to update config. Received DatasetName: " + config.getDatasetName());
        // Updates the in-memory configuration state in the service
        configService.updateConfig(config);
        return configService.getConfig();
    }
}
