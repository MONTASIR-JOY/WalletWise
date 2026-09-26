package com.walletwise.util;

public interface Notifiable {

    void notify(String message);

    default void notifyError(String message) {
        notify("Error: " + message);
    }
}