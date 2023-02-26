package com.azuriom.azlink.common.users;

import com.google.gson.annotations.SerializedName;

public class EditMoneyResult {

    @SerializedName("old_balance")
    private final double oldBalance;
    @SerializedName("new_balance")
    private final double newBalance;

    public EditMoneyResult(double oldBalance, double newBalance) {
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }
}
