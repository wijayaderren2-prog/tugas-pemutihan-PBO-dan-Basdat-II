package com.kopdes;

public class Produk {
    private String sku;
    private String namaProduk;
    private int idKategori;
    private String deskripsi;
    private double harga;
    private int stok;
    private String satuan;

    public Produk(String sku, String namaProduk, int idKategori, String deskripsi, double harga, int stok, String satuan) {
        this.sku = sku;
        this.namaProduk = namaProduk;
        this.idKategori = idKategori;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.stok = stok;
        this.satuan = satuan;
    }

    public String getSku() { return sku; }
    public String getNamaProduk() { return namaProduk; }
    public int getIdKategori() { return idKategori; }
    public String getDeskripsi() { return deskripsi; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
    public String getSatuan() { return satuan; }
}