package com.laundrydesktop.model;

public record MasterItem(int id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
