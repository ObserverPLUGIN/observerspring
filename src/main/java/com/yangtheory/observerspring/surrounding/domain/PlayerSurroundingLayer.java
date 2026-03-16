package com.yangtheory.observerspring.surrounding.domain;

import com.yangtheory.observerspring.common.persistence.BaseTimeEntity;
import jakarta.persistence.Basic;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "player_surrounding_layers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_player_surrounding_layers_snapshot_type",
                        columnNames = {"snapshot_id", "layer_type"})
        },
        indexes = {
                @Index(name = "idx_psl_snapshot_id", columnList = "snapshot_id")
        })
public class PlayerSurroundingLayer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "player_layer_seq_gen")
    @SequenceGenerator(name = "player_layer_seq_gen", sequenceName = "player_layer_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psl_snapshot"))
    private PlayerSurroundingSnapshot snapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "layer_type", nullable = false, length = 10)
    private LayerType layerType;

    @Column(name = "relative_y", nullable = false)
    private int relativeY;

    @Column(name = "absolute_y", nullable = false)
    private int absoluteY;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "cells_json", nullable = false, columnDefinition = "CLOB")
    private String cellsJson;

    @Column(name = "non_air_block_count", nullable = false)
    private int nonAirBlockCount;

    @Column(name = "unique_material_count", nullable = false)
    private int uniqueMaterialCount;

    @Column(name = "highlight_block_count", nullable = false)
    private int highlightBlockCount;

    protected PlayerSurroundingLayer() {
    }

    public PlayerSurroundingLayer(
            LayerType layerType,
            int relativeY,
            int absoluteY,
            String cellsJson,
            int nonAirBlockCount,
            int uniqueMaterialCount,
            int highlightBlockCount) {
        this.layerType = layerType;
        this.relativeY = relativeY;
        this.absoluteY = absoluteY;
        this.cellsJson = cellsJson;
        this.nonAirBlockCount = nonAirBlockCount;
        this.uniqueMaterialCount = uniqueMaterialCount;
        this.highlightBlockCount = highlightBlockCount;
    }

    void attachTo(PlayerSurroundingSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Long getId() {
        return id;
    }

    public PlayerSurroundingSnapshot getSnapshot() {
        return snapshot;
    }

    public LayerType getLayerType() {
        return layerType;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }

    public String getCellsJson() {
        return cellsJson;
    }

    public int getNonAirBlockCount() {
        return nonAirBlockCount;
    }

    public int getUniqueMaterialCount() {
        return uniqueMaterialCount;
    }

    public int getHighlightBlockCount() {
        return highlightBlockCount;
    }
}
