package com.walletwise.model;

public class Category {

    private int id;
    private String name;
    private TransactionType type;

    public Category(int id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Category(String name, TransactionType type) {
        this(0, name, type);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }
}