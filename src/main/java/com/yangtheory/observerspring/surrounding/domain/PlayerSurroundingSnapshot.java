package com.yangtheory.observerspring.surrounding.domain;

import com.yangtheory.observerspring.common.persistence.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "player_surrounding_snapshots",
        indexes = {
                @Index(name = "idx_pss_player_captured_at", columnList = "player_id, captured_at"),
                @Index(name = "idx_pss_world_captured_at", columnList = "world_id, captured_at")
        })
public class PlayerSurroundingSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_snapshot_seq_gen")
    @SequenceGenerator(
            name = "player_snapshot_seq_gen",
            sequenceName = "player_snapshot_seq",
            allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pss_player"))
    private MinecraftPlayer player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "world_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pss_world"))
    private MinecraftWorld world;

    @Column(name = "captured_at", nullable = false)
    private OffsetDateTime capturedAt;

    @Column(name = "center_block_x", nullable = false)
    private int centerBlockX;

    @Column(name = "center_block_y", nullable = false)
    private int centerBlockY;

    @Column(name = "center_block_z", nullable = false)
    private int centerBlockZ;

    @Column(name = "yaw", nullable = false)
    private double yaw;

    @Column(name = "pitch", nullable = false)
    private double pitch;

    @Column(name = "radius", nullable = false)
    private int radius;

    @Column(name = "slice_size", nullable = false)
    private int sliceSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private SnapshotTriggerType triggerType;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("relativeY asc")
    private Set<PlayerSurroundingLayer> layers = new LinkedHashSet<>();

    protected PlayerSurroundingSnapshot() {
    }

    public PlayerSurroundingSnapshot(
            MinecraftPlayer player,
            MinecraftWorld world,
            OffsetDateTime capturedAt,
            int centerBlockX,
            int centerBlockY,
            int centerBlockZ,
            double yaw,
            double pitch,
            int radius,
            SnapshotTriggerType triggerType) {
        this.player = player;
        this.world = world;
        this.capturedAt = capturedAt;
        this.centerBlockX = centerBlockX;
        this.centerBlockY = centerBlockY;
        this.centerBlockZ = centerBlockZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.radius = radius;
        this.sliceSize = radius * 2 + 1;
        this.triggerType = triggerType;
    }

    public Long getId() {
        return id;
    }

    public MinecraftPlayer getPlayer() {
        return player;
    }

    public MinecraftWorld getWorld() {
        return world;
    }

    public OffsetDateTime getCapturedAt() {
        return capturedAt;
    }

    public int getCenterBlockX() {
        return centerBlockX;
    }

    public int getCenterBlockY() {
        return centerBlockY;
    }

    public int getCenterBlockZ() {
        return centerBlockZ;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public int getRadius() {
        return radius;
    }

    public int getSliceSize() {
        return sliceSize;
    }

    public SnapshotTriggerType getTriggerType() {
        return triggerType;
    }

    public Set<PlayerSurroundingLayer> getLayers() {
        return layers;
    }

    public void addLayer(PlayerSurroundingLayer layer) {
        layers.add(layer);
        layer.attachTo(this);
    }
}
