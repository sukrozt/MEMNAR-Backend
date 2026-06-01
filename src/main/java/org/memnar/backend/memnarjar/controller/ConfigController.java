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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
// This annotation fixes your CORS error by explicitly allowing your Vite frontend
@CrossOrigin(origins = "http://localhost:5173")
public class ConfigController {

    private final ConfigService configService;
    private final UserConfigRepository userConfigRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ConfigController(ConfigService configService, UserConfigRepository userConfigRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.configService = configService;
        this.userConfigRepository = userConfigRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
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
                    // 2. Gelen Config nesnesini JSON formatına çevir
                    String configJson = objectMapper.writeValueAsString(newConfig);
                    
                    // 3. Veritabanına kaydet
                    UserConfig userConfig = new UserConfig();
                    userConfig.setUser(user);
                    userConfig.setConfigJson(configJson);
                    userConfigRepository.save(userConfig);
                } catch (Exception e) {
                    System.err.println("Config JSON'a çevrilirken hata: " + e.getMessage());
                }
            });
        }

        configService.updateConfig(newConfig);
    }
}
