package com.walletwise.model;

import java.time.LocalDate;

public class Income extends AccountItem {

    public Income(int id, double amount, Category category, LocalDate date, String note) {
        super(id, amount, category, date, note);
    }

    public Income(double amount, Category category, LocalDate date, String note) {
        super(0, amount, category, date, note);
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