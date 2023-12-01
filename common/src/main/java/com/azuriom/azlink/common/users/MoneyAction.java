package com.azuriom.azlink.common.users;

import java.util.Locale;

public enum MoneyAction {
    ADD, REMOVE, SET;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static MoneyAction fromString(String action) {
        try {
            return MoneyAction.valueOf(action.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}