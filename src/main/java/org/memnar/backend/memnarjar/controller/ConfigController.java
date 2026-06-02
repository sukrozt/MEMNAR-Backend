package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.dto.FileDTO;
import org.memnar.backend.memnarjar.model.ConfigData;
import org.memnar.backend.memnarjar.service.ConfigService;
import org.memnar.backend.security.model.UserConfig;
import org.memnar.backend.security.repository.UserConfigRepository;
import org.memnar.backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
// This annotation fixes your CORS error by explicitly allowing your Vite frontend
@CrossOrigin(origins = "http://localhost:5173")
public class ConfigController {

    private final ConfigService configService;
    private final UserConfigRepository userConfigRepository;
    private final UserRepository userRepository;

    @Autowired
    public ConfigController(ConfigService configService, UserConfigRepository userConfigRepository, UserRepository userRepository) {
        this.configService = configService;
        this.userConfigRepository = userConfigRepository;
        this.userRepository = userRepository;
    }

    // This fixes the 404 error on GET requests (fetching config on load)
    @GetMapping
    public ConfigData getConfig() {
        return configService.getConfig();
    }

    @PostMapping
    public void saveConfig(@RequestBody ConfigData newConfig) {
        // 1. O an giriş yapmış kullanıcıyı Spring Security'den alıyoruz
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                try {
                    // 2. Veritabanına kaydet
                    UserConfig userConfig = new UserConfig();
                    userConfig.setUser(user);
                    userConfig.setDatasetName(newConfig.getDatasetName());
                    userConfig.setMinSupp(newConfig.getMinSupp());
                    userConfig.setMinConf(newConfig.getMinConf());
                    userConfig.setMinZScore(newConfig.getMinZScore());
                    userConfig.setMaxSetSize(newConfig.getMaxSetSize());
                    userConfig.setPValueCutoff(newConfig.getPValueCutoff());
                    userConfig.setUnformatted(newConfig.isUnformatted());
                    userConfigRepository.save(userConfig);
                } catch (Exception e) {
                    System.err.println("Config kaydedilirken hata: " + e.getMessage());
                }
            });
        }

        configService.updateConfig(newConfig);
    }
}
