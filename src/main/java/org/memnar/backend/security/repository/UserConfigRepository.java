package org.memnar.backend.security.repository;

import org.memnar.backend.security.model.UserConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {
    // Bir kullanıcının eski konfigürasyonlarını tarihe göre sıralı getirmek için
    List<UserConfig> findByUserIdOrderByCreatedAtDesc(Long userId);
}