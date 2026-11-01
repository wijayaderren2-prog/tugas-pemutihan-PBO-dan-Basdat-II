package com.kopdes;




import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProdukDAO {

    // Menampilkan seluruh produk
    public List<Produk> getAllProduk() throws Exception {

        List<Produk> listProduk = new ArrayList<>();

        String sql = "SELECT sku, nama_produk, id_kategori, " +
                     "deskripsi, harga, stok, satuan FROM produk";

        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Produk p = new Produk(
                    rs.getString("sku"),
                    rs.getString("nama_produk"),
                    rs.getInt("id_kategori"),
                    rs.getString("deskripsi"),
                    rs.getDouble("harga"),
                    rs.getInt("stok"),
                    rs.getString("satuan")
                );

                listProduk.add(p);
            }
        }

        return listProduk;
    }

    // Menambahkan produk
    public void insertProduk(Produk p) throws Exception {

        String sql = "INSERT INTO produk " +
                     "(sku, nama_produk, id_kategori, deskripsi, harga, stok, satuan) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getSku());
            ps.setString(2, p.getNamaProduk());
            ps.setInt(3, p.getIdKategori());
            ps.setString(4, p.getDeskripsi());
            ps.setDouble(5, p.getHarga());
            ps.setInt(6, p.getStok());
            ps.setString(7, p.getSatuan());

            ps.executeUpdate();
        }
    }

    // Memperbarui produk
    public void updateProduk(Produk p) throws Exception {

        String sql = "UPDATE produk SET " +
                     "nama_produk = ?, " +
                     "id_kategori = ?, " +
                     "deskripsi = ?, " +
                     "harga = ?, " +
                     "stok = ?, " +
                     "satuan = ? " +
                     "WHERE sku = ?";

        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNamaProduk());
            ps.setInt(2, p.getIdKategori());
            ps.setString(3, p.getDeskripsi());
            ps.setDouble(4, p.getHarga());
            ps.setInt(5, p.getStok());
            ps.setString(6, p.getSatuan());
            ps.setString(7, p.getSku());

            int result = ps.executeUpdate();

            if (result == 0) {
                throw new Exception("Produk dengan SKU tersebut tidak ditemukan.");
            }
        }
    }

    // Menghapus produk
    public void deleteProduk(String sku) throws Exception {

        String sql = "DELETE FROM produk WHERE sku = ?";

        try (Connection conn = KoneksiDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sku);

            int result = ps.executeUpdate();

            if (result == 0) {
                throw new Exception("Produk dengan SKU tersebut tidak ditemukan.");
            }
        }
    }
}
