package com.walletwise.model;

public class Budget {

    private int id;
    private Category category;
    private String month;
    private double limitAmount;

    public Budget(int id, Category category, String month, double limitAmount) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    public Budget(Category category, String month, double limitAmount) {
        this(0, category, month, limitAmount);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }
}