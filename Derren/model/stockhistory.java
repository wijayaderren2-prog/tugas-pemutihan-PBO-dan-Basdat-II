package com.kopdes.kmp.model;

import java.time.LocalDateTime;

public class StockHistory extends BaseEntity {
    private Integer productId;
    private int changeQty;
    private String changeType; // IN / OUT / ADJUST
    private String note;
    private LocalDateTime changedAt;

    public StockHistory() {}

    public StockHistory(Integer id, Integer productId, int changeQty,
                        String changeType, String note, LocalDateTime changedAt) {
        this.id = id;
        this.productId = productId;
        this.changeQty = changeQty;
        this.changeType = changeType;
        this.note = note;
        this.changedAt = changedAt;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public int getChangeQty() { return changeQty; }
    public void setChangeQty(int changeQty) { this.changeQty = changeQty; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    @Override
    public String getDisplayName() { return changeType + " " + changeQty; }
}