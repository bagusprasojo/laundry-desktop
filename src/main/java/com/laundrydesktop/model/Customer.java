package com.laundrydesktop.model;

public record Customer(int id, String name, String phone, String address, String note) {
    @Override
    public String toString() {
        return name + (phone == null || phone.isBlank() ? "" : " (" + phone + ")");
    }
}
