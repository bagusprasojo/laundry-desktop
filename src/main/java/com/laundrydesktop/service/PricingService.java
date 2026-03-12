package com.laundrydesktop.service;

import com.laundrydesktop.model.ServicePrice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PricingService {
    public int findPrice(List<ServicePrice> prices, String serviceName, String speedName, String unitName) {
        return prices.stream()
                .filter(p -> p.serviceName().equals(serviceName)
                        && p.speedName().equals(speedName)
                        && p.unitName().equals(unitName))
                .findFirst()
                .map(ServicePrice::price)
                .orElseThrow(() -> new IllegalArgumentException("Harga layanan belum diatur."));
    }

    public String generateInvoiceNo() {
        return "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }
}
