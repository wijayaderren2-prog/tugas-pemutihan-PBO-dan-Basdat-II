package com.kopdes.kmp.model;

import java.math.BigDecimal;

/**
 * Mirror dari composite type `product_info` di PostgreSQL.
 * Menerapkan ORDBMS: nilai objek kompleks disimpan sebagai satu kolom.
 */
public class ProductInfo {
    private String nama;
    private String kategori;
    private BigDecimal harga;
    private int stok;

    public ProductInfo() {}
    public ProductInfo(String nama, String kategori, BigDecimal harga, int stok) {
        this.nama = nama; this.kategori = kategori; this.harga = harga; this.stok = stok;
    }

    public String getNama() { return nama; }
    public String getKategori() { return kategori; }
    public BigDecimal getHarga() { return harga; }
    public int getStok() { return stok; }

    public void setNama(String nama) { this.nama = nama; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setHarga(BigDecimal harga) { this.harga = harga; }
    public void setStok(int stok) { this.stok = stok; }

    @Override
    public String toString() {
        return nama + " | " + kategori + " | " + harga + " | " + stok;
    }
}