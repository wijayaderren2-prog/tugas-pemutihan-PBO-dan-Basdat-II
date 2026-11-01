package com.kopdes.kmp.model;

public class Category extends BaseEntity {
    private String name;
    private String description;

    public Category() {}
    public Category(Integer id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String getDisplayName() { return name; }
    @Override
    public String toString() { return name; }
}