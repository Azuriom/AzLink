package com.azuriom.azlink.common.data;

public class UserInfo {

    private final String name;
    private final double money;

    public UserInfo(String name, double money) {
        this.name = name;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }
}
