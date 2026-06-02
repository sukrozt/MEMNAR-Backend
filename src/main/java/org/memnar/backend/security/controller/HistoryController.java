package org.memnar.backend.security.controller;

import org.memnar.backend.security.model.UserConfig;
import org.memnar.backend.security.repository.UserConfigRepository;
import org.memnar.backend.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "http://localhost:5173")
public class HistoryController {

    private final UserConfigRepository configRepository;
    private final UserRepository userRepository;

    @Autowired
    public HistoryController(UserConfigRepository configRepository, UserRepository userRepository) {
        this.configRepository = configRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/configs")
    public ResponseEntity<List<UserConfig>> getUserConfigs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName())
                    .map(user -> ResponseEntity.ok(configRepository.findByUserIdOrderByCreatedAtDesc(user.getId())))
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(401).build();
    }

    // Hesap (kullanıcı) verilerini Frontend'e göndermek için yeni endpoint
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return userRepository.findByUsername(auth.getName())
                    .map(user -> {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("id", user.getId());
                        profile.put("username", user.getUsername());
                        // Şifreyi GÖNDERMİYORUZ!
                        return ResponseEntity.ok(profile);
                    })
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(401).build();
    }
}