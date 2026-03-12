package com.laundrydesktop.view;

import com.laundrydesktop.model.Order;
import com.laundrydesktop.repo.OrderRepository;
import com.laundrydesktop.service.ReportService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardView {
    private final OrderRepository orderRepository = new OrderRepository();
    private final ReportService reportService = new ReportService();

    private final Label orderTodayLabel = new Label("0");
    private final Label unfinishedLabel = new Label("0");
    private final Label revenueTodayLabel = new Label("Rp0");
    private final Label revenueMonthLabel = new Label("Rp0");
    private final Label popularServiceLabel = new Label("-");
    private final Label activeCustomerLabel = new Label("-");
    private Parent root;

    public Parent build() {
        if (root != null) {
            return root;
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(12));

        grid.addRow(0, new Label("Order Hari Ini"), orderTodayLabel);
        grid.addRow(1, new Label("Order Belum Selesai"), unfinishedLabel);
        grid.addRow(2, new Label("Pendapatan Hari Ini"), revenueTodayLabel);
        grid.addRow(3, new Label("Pendapatan Bulan Ini"), revenueMonthLabel);
        grid.addRow(4, new Label("Layanan Paling Laku"), popularServiceLabel);
        grid.addRow(5, new Label("Pelanggan Paling Aktif"), activeCustomerLabel);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshData());

        VBox box = new VBox(10, grid, refreshBtn);
        box.setPadding(new Insets(12));
        root = box;
        refreshData();
        return root;
    }

    public void refreshData() {
        List<Order> all = orderRepository.findAll();
        List<Order> today = orderRepository.findByDate(LocalDate.now());

        orderTodayLabel.setText(String.valueOf(today.size()));
        unfinishedLabel.setText(String.valueOf(reportService.unfinishedOrders(all)));
        revenueTodayLabel.setText("Rp" + reportService.totalRevenue(today));

        int monthRevenue = all.stream()
                .filter(o -> o.orderDate().startsWith(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))))
                .mapToInt(Order::totalPrice)
                .sum();
        revenueMonthLabel.setText("Rp" + monthRevenue);
        popularServiceLabel.setText(reportService.mostPopularService(all));
        activeCustomerLabel.setText(reportService.mostActiveCustomer(all));
    }
}
