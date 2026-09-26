package com.walletwise.model;

import java.time.LocalDate;

public class Income extends AccountItem {

    public Income(int id, double amount, Category category, LocalDate date, String note, String receiptPath) {
        super(id, amount, category, date, note, receiptPath);
    }

    public Income(double amount, Category category, LocalDate date, String note, String receiptPath) {
        super(0, amount, category, date, note, receiptPath);
    }

    @Override
    public TransactionType getType() {
        return TransactionType.INCOME;
    }

    @Override
    public String getDisplay() {
        return "Income: " + getAmount();
    }
}