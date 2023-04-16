package com.azuriom.azlink.common.data;

public class UserInfo {

    private final int id;
    private final String name;
    private double money;

    public UserInfo(int id, String name, double money) {
        this.id = id;
        this.name = name;
        this.money = money;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public double getMoney() {
        return money;
    }
}
