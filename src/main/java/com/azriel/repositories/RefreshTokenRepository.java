package com.azriel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.azriel.models.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);
}
