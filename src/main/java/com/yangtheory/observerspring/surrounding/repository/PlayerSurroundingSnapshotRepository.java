package com.yangtheory.observerspring.surrounding.repository;

import com.yangtheory.observerspring.surrounding.domain.PlayerSurroundingSnapshot;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerSurroundingSnapshotRepository extends JpaRepository<PlayerSurroundingSnapshot, Long> {

    @EntityGraph(attributePaths = {"player", "world", "layers"})
    Optional<PlayerSurroundingSnapshot> findFirstByPlayer_PlayerUuidOrderByCapturedAtDesc(String playerUuid);

    List<PlayerSurroundingSnapshot> findTop20ByPlayer_PlayerUuidOrderByCapturedAtDesc(String playerUuid);
}
