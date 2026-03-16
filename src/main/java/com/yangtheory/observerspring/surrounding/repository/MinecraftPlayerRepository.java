package com.yangtheory.observerspring.surrounding.repository;

import com.yangtheory.observerspring.surrounding.domain.MinecraftPlayer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinecraftPlayerRepository extends JpaRepository<MinecraftPlayer, Long> {

    Optional<MinecraftPlayer> findByPlayerUuid(String playerUuid);
}
