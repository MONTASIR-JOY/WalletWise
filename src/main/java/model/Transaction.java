package com.walletwise.model;

import java.time.LocalDate;

public class Transaction {

    private int id;
    private double amount;
    private TransactionType type;
    private Category category;
    private LocalDate date;
    private String note;
    private String receiptPath;

    public Transaction(int id,
                       double amount,
                       TransactionType type,
                       Category category,
                       LocalDate date,
                       String note,
                       String receiptPath) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.note = note;
        this.receiptPath = receiptPath;
    }

    public Transaction(double amount,
                       TransactionType type,
                       Category category,
                       LocalDate date,
                       String note,
                       String receiptPath) {
        this(0, amount, type, category, date, note, receiptPath);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReceiptPath() {
        return receiptPath;
    }

    public void setReceiptPath(String receiptPath) {
        this.receiptPath = receiptPath;
    }
}