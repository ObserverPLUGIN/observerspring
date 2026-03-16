package com.yangtheory.observerspring.surrounding.domain;

import com.yangtheory.observerspring.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "mc_worlds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mc_worlds_server_world",
                        columnNames = {"server_name", "world_name"})
        })
public class MinecraftWorld extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mc_world_seq_gen")
    @SequenceGenerator(name = "mc_world_seq_gen", sequenceName = "mc_world_seq", allocationSize = 1)
    private Long id;

    @Column(name = "server_name", nullable = false, length = 60)
    private String serverName;

    @Column(name = "world_name", nullable = false, length = 60)
    private String worldName;

    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_type", nullable = false, length = 20)
    private DimensionType dimensionType;

    protected MinecraftWorld() {
    }

    public MinecraftWorld(String serverName, String worldName, DimensionType dimensionType) {
        this.serverName = serverName;
        this.worldName = worldName;
        this.dimensionType = dimensionType;
    }

    public Long getId() {
        return id;
    }

    public String getServerName() {
        return serverName;
    }

    public String getWorldName() {
        return worldName;
    }

    public DimensionType getDimensionType() {
        return dimensionType;
    }

    public void updateDimensionType(DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }
}
