package com.kopdes.kmp.model;

public enum StockStatus {
    TERSEDIA("Tersedia"),
    STOK_RENDAH("Stok Rendah"),
    HABIS("Habis");

    private final String label;
    StockStatus(String label) { this.label = label; }
    public String getLabel() { return label; }

    public static StockStatus fromStock(int stock) {
        if (stock <= 0) return HABIS;
        if (stock < 10) return STOK_RENDAH;
        return TERSEDIA;
    }
}