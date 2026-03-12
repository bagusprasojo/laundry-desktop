package com.laundrydesktop.service;

import com.laundrydesktop.model.Order;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReportService {
    public int totalRevenue(List<Order> orders) {
        return orders.stream().mapToInt(Order::totalPrice).sum();
    }

    public String mostPopularService(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::serviceName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    public String mostActiveCustomer(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::customerName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
    }

    public long unfinishedOrders(List<Order> orders) {
        return orders.stream().filter(o -> !"Sudah Diambil".equals(o.orderStatus())).count();
    }

    public Map<String, Long> dailyOrderCount(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::orderDate, Collectors.counting()));
    }
}
