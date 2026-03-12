package com.laundrydesktop.model;

public record ServicePrice(String serviceName, String speedName, String unitName, int price) {
    @Override
    public String toString() {
        return serviceName + " | " + speedName + " | " + unitName + " = Rp" + price;
    }
}
