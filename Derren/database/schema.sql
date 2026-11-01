-- =========================================================
-- KOPDES KMP — Skema Database
-- Menerapkan ORDBMS: COMPOSITE TYPE + USER-DEFINED DOMAIN
-- =========================================================

DROP TABLE IF EXISTS stock_history CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TYPE IF EXISTS product_info CASCADE;
DROP DOMAIN IF EXISTS price_type CASCADE;
DROP DOMAIN IF EXISTS stock_qty CASCADE;

-- ---------- USER-DEFINED DOMAIN (ORDBMS) ----------
-- Domain untuk harga: harus > 0
CREATE DOMAIN price_type AS NUMERIC(15,2)
    CHECK (VALUE > 0);

-- Domain untuk stok: tidak boleh negatif
CREATE DOMAIN stock_qty AS INTEGER
    CHECK (VALUE >= 0);

-- ---------- COMPOSITE TYPE (ORDBMS) ----------
-- product_info: merepresentasikan informasi ringkas produk
-- (nama, kategori, harga, stok) sebagai satu nilai objek.
CREATE TYPE product_info AS (
    nama        VARCHAR(150),
    kategori    VARCHAR(80),
    harga       NUMERIC(15,2),
    stok        INTEGER
);

-- ---------- TABEL KATEGORI ----------
CREATE TABLE category (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    description TEXT
);

-- ---------- TABEL PRODUK ----------
CREATE TABLE product (
    id          SERIAL PRIMARY KEY,
    sku         VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE RESTRICT,
    description TEXT,
    price       price_type NOT NULL,
    stock       stock_qty NOT NULL DEFAULT 0,
    unit        VARCHAR(20) NOT NULL DEFAULT 'pcs',
    -- Kolom composite type (ORDBMS) — menyimpan snapshot info produk
    info        product_info,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- TABEL RIWAYAT STOK ----------
CREATE TABLE stock_history (
    id          SERIAL PRIMARY KEY,
    product_id  INTEGER NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    change_qty  INTEGER NOT NULL,
    change_type VARCHAR(20) NOT NULL CHECK (change_type IN ('IN','OUT','ADJUST')),
    note        VARCHAR(255),
    changed_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- TRIGGER: update updated_at ----------
CREATE OR REPLACE FUNCTION trg_set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_product_updated
BEFORE UPDATE ON product
FOR EACH ROW EXECUTE FUNCTION trg_set_updated_at();

-- ---------- TRIGGER: sinkron composite info ----------
CREATE OR REPLACE FUNCTION trg_sync_product_info() RETURNS TRIGGER AS $$
DECLARE
    cat_name VARCHAR(80);
BEGIN
    SELECT name INTO cat_name FROM category WHERE id = NEW.category_id;
    NEW.info = ROW(NEW.name, cat_name, NEW.price, NEW.stock)::product_info;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_product_info
BEFORE INSERT OR UPDATE ON product
FOR EACH ROW EXECUTE FUNCTION trg_sync_product_info();

-- ---------- SEED DATA ----------
INSERT INTO category (name, description) VALUES
('Beras', 'Beras organik & premium'),
('Minyak & Lemak', 'Minyak goreng, mentega'),
('Gula', 'Gula pasir, gula aren'),
('Telur', 'Telur ayam, bebek'),
('Tepung', 'Tepung terigu, tapioka'),
('Makanan Instan', 'Mie instan, sarden, dll.');

INSERT INTO product (sku, name, category_id, description, price, stock, unit) VALUES
('BR-001','Beras Organik 5kg',1,'Beras organik premium, tanpa pestisida.',65000,45,'pcs'),
('MG-002','Minyak Goreng 2L',2,'Minyak goreng kelapa sawit.',18000,22,'pcs'),
('GL-003','Gula Pasir 1kg',3,'Gula pasir putih bersih.',14000,0,'pcs'),
('TL-004','Telur Ayam 1kg',4,'Telur ayam segar.',25000,25,'kg'),
('TP-005','Tepung Terigu 1kg',5,'Tepung terigu serbaguna.',12000,40,'pcs'),
('MI-006','Mie Instan 1 dus',6,'Mie instan isi 40 pcs.',75000,60,'dus');

INSERT INTO stock_history (product_id, change_qty, change_type, note) VALUES
(1, 50, 'IN',  'Stok awal masuk'),
(1, -5, 'OUT', 'Penjualan harian'),
(2, 30, 'IN',  'Stok awal masuk'),
(2, -8, 'OUT', 'Penjualan harian'),
(3, 20, 'IN',  'Stok awal masuk'),
(3, -20,'OUT', 'Penjualan habis');