package com.tagari.smartpantry;

public class PantryItem {
    public long id;
    public String name;
    public String category;
    public int quantity;
    public String unit;
    public String expiry;

    public PantryItem(long id, String name, String category, int quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }
}
