package com.yangtheory.observerspring.surrounding.domain;

import com.yangtheory.observerspring.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "mc_players",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mc_players_uuid", columnNames = "player_uuid")
        },
        indexes = {
                @Index(name = "idx_mc_players_last_snapshot_at", columnList = "last_snapshot_captured_at")
        })
public class MinecraftPlayer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mc_player_seq_gen")
    @SequenceGenerator(name = "mc_player_seq_gen", sequenceName = "mc_player_seq", allocationSize = 1)
    private Long id;

    @Column(name = "player_uuid", nullable = false, length = 36)
    private String playerUuid;

    @Column(name = "last_known_name", nullable = false, length = 16)
    private String lastKnownName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_world_id", foreignKey = @ForeignKey(name = "fk_mc_players_last_world"))
    private MinecraftWorld lastWorld;

    @Column(name = "last_block_x")
    private Integer lastBlockX;

    @Column(name = "last_block_y")
    private Integer lastBlockY;

    @Column(name = "last_block_z")
    private Integer lastBlockZ;

    @Column(name = "last_yaw")
    private Double lastYaw;

    @Column(name = "last_pitch")
    private Double lastPitch;

    @Column(name = "last_snapshot_captured_at")
    private OffsetDateTime lastSnapshotCapturedAt;

    protected MinecraftPlayer() {
    }

    public MinecraftPlayer(String playerUuid, String lastKnownName) {
        this.playerUuid = playerUuid;
        this.lastKnownName = lastKnownName;
    }

    public Long getId() {
        return id;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public MinecraftWorld getLastWorld() {
        return lastWorld;
    }

    public Integer getLastBlockX() {
        return lastBlockX;
    }

    public Integer getLastBlockY() {
        return lastBlockY;
    }

    public Integer getLastBlockZ() {
        return lastBlockZ;
    }

    public Double getLastYaw() {
        return lastYaw;
    }

    public Double getLastPitch() {
        return lastPitch;
    }

    public OffsetDateTime getLastSnapshotCapturedAt() {
        return lastSnapshotCapturedAt;
    }

    public void refreshLiveState(
            String playerName,
            MinecraftWorld world,
            int blockX,
            int blockY,
            int blockZ,
            double yaw,
            double pitch,
            OffsetDateTime capturedAt) {
        this.lastKnownName = playerName;
        this.lastWorld = world;
        this.lastBlockX = blockX;
        this.lastBlockY = blockY;
        this.lastBlockZ = blockZ;
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        this.lastSnapshotCapturedAt = capturedAt;
    }
}
