package com.walletwise.model;

import java.time.LocalDate;

public class Expense extends AccountItem {

    public Expense(int id, double amount, Category category, LocalDate date, String note) {
        super(id, amount, category, date, note);
    }

    public Expense(double amount, Category category, LocalDate date, String note) {
        super(0, amount, category, date, note);
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