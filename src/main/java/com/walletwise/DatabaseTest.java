package com.walletwise;

import com.walletwise.dao.Database;

public class DatabaseTest {

    public static void main(String[] args) {
        Database.initialize();
        System.out.println("Test finished.");
    }
}