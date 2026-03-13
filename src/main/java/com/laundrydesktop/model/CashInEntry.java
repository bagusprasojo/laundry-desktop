package com.laundrydesktop.model;

public record CashInEntry(
        String paymentDate,
        String invoiceNo,
        String customerName,
        String paymentMethod,
        int amount,
        String note
) {
}
