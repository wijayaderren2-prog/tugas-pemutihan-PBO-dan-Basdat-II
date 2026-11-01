package com.kopdes.kmp.model;

import java.math.BigDecimal;

public class Product extends BaseEntity {
    private String sku;
    private String name;
    private Integer categoryId;
    private String categoryName;   // join helper
    private String description;
    private BigDecimal price;
    private int stock;
    private String unit;
    private ProductInfo info;      // composite type ORDBMS

    public Product() {}

    public Product(Integer id, String sku, String name, Integer categoryId,
                   String categoryName, String description, BigDecimal price,
                   int stock, String unit) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.unit = unit;
    }

    // ----- Getters / Setters (Encapsulation) -----
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public ProductInfo getInfo() { return info; }
    public void setInfo(ProductInfo info) { this.info = info; }

    public StockStatus getStatus() { return StockStatus.fromStock(stock); }

    @Override
    public String getDisplayName() { return name; }

    @Override
    public String toString() {
        return String.format("%s (%s) - Rp%s - stok %d %s",
                name, sku, price, stock, unit);
    }
}