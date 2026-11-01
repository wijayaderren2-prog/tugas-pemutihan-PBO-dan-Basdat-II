
import com.kopdes.Produk;
import java.util.ArrayList;
import java.util.List;

public class ProdukDAO {
    private static List<Produk> listProduk = new ArrayList<>();

    static {
        // Sample data awal
        listProduk.add(new Produk("PRD-001", "Beras Organik 5kg", 1, "Beras kualitas super", 65000, 45, "kg"));
        listProduk.add(new Produk("PRD-002", "Minyak Goreng 1L", 2, "Minyak kelapa sawit", 18000, 30, "pck"));
    }

    public List<Produk> getAllProduk() {
        return new ArrayList<>(listProduk);
    }

    public void insertProduk(Produk p) {
        listProduk.add(p);
    }

    public void updateProduk(Produk p) {
        for (int i = 0; i < listProduk.size(); i++) {
            if (listProduk.get(i).getSku().equals(p.getSku())) {
                listProduk.set(i, p);
                break;
            }
        }
    }

    public void deleteProduk(String sku) {
        listProduk.removeIf(p -> p.getSku().equals(sku));
    }
}