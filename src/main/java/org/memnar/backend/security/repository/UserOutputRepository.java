package org.memnar.backend.security.repository;

import org.memnar.backend.security.model.UserOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserOutputRepository extends JpaRepository<UserOutput, Long> {
    // Bir kullanıcının eski sonuç dosyalarını tarihe göre sıralı getirmek için
    List<UserOutput> findByUserIdOrderByCreatedAtDesc(Long userId);
}