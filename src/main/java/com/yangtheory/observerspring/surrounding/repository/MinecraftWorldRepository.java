package com.yangtheory.observerspring.surrounding.repository;

import com.yangtheory.observerspring.surrounding.domain.MinecraftWorld;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MinecraftWorldRepository extends JpaRepository<MinecraftWorld, Long> {

    Optional<MinecraftWorld> findByServerNameAndWorldName(String serverName, String worldName);
}
