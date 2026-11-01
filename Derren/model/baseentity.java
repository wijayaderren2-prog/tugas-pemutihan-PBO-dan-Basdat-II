package com.kopdes.kmp.model;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    protected Integer id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public abstract String getDisplayName();
}