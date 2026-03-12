package com.laundrydesktop.model;

public record Order(
        String invoiceNo,
        int customerId,
        String customerName,
        String serviceName,
        String speedName,
        String unitName,
        double quantity,
        int unitPrice,
        int totalPrice,
        String orderStatus,
        String paymentStatus,
        String paymentMethod,
        int downPayment,
        int paidAmount,
        String orderDate,
        String estimateDone
) {
}
