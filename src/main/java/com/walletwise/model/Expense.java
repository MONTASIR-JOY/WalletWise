package com.walletwise.model;

import java.time.LocalDate;

public class Expense extends AccountItem {

    public Expense(int id, double amount, Category category, LocalDate date, String note, String receiptPath) {
        super(id, amount, category, date, note, receiptPath);
    }

    public Expense(double amount, Category category, LocalDate date, String note, String receiptPath) {
        super(0, amount, category, date, note, receiptPath);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.EXPENSE;
    }

    @Override
    public String getDisplay() {
        return "Expense: " + getAmount();
    }
}