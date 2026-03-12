package com.laundrydesktop.view;

import com.laundrydesktop.model.Customer;
import com.laundrydesktop.model.Order;
import com.laundrydesktop.model.ServicePrice;
import com.laundrydesktop.repo.CustomerRepository;
import com.laundrydesktop.repo.OrderRepository;
import com.laundrydesktop.repo.ServicePriceRepository;
import com.laundrydesktop.service.PdfInvoiceService;
import com.laundrydesktop.service.PricingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionView {
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final ServicePriceRepository servicePriceRepository = new ServicePriceRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final PricingService pricingService = new PricingService();
    private final PdfInvoiceService pdfInvoiceService = new PdfInvoiceService();
    private final Runnable onChanged;

    private final TextField customerField = new TextField();
    private final Button selectCustomerButton = new Button("Cari Pelanggan");
    private Customer selectedCustomer;

    private final ComboBox<String> serviceCombo = new ComboBox<>();
    private final ComboBox<String> speedCombo = new ComboBox<>();
    private final ComboBox<String> unitCombo = new ComboBox<>();
    private final TextField qtyField = new TextField();
    private final ComboBox<String> paymentMethodCombo = new ComboBox<>();
    private final TextField dpField = new TextField("0");
    private final Label unitPriceLabel = new Label("Rp0");
    private final Label totalPriceLabel = new Label("Rp0");
    private final Label remainingLabel = new Label("Rp0");
    private final Label infoLabel = new Label();
    private final TableView<Order> orderTable = new TableView<>();

    private final List<ServicePrice> cachedPrices = new ArrayList<>();
    private Parent root;
    private int currentUnitPrice = 0;
    private int currentTotal = 0;

    public TransactionView() {
        this(null);
    }

    public TransactionView(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public Parent build() {
        if (root != null) {
            return root;
        }

        VBox container = new VBox(12);
        container.setPadding(new Insets(12));

        customerField.setEditable(false);
        customerField.setPromptText("Belum ada pelanggan terpilih");
        selectCustomerButton.setOnAction(e -> openCustomerPicker());

        qtyField.setPromptText("contoh: 2 atau 3.5");
        paymentMethodCombo.setItems(FXCollections.observableArrayList("Tunai", "Digital", "DP"));
        paymentMethodCombo.getSelectionModel().selectFirst();

        serviceCombo.setOnAction(e -> updateUnitPriceAndTotals());
        speedCombo.setOnAction(e -> updateUnitPriceAndTotals());
        unitCombo.setOnAction(e -> updateUnitPriceAndTotals());
        qtyField.textProperty().addListener((obs, oldV, newV) -> updateTotals());
        dpField.textProperty().addListener((obs, oldV, newV) -> updateTotals());

        GridPane leftForm = new GridPane();
        leftForm.setHgap(10);
        leftForm.setVgap(10);
        HBox customerRow = new HBox(8, customerField, selectCustomerButton);
        HBox.setHgrow(customerField, Priority.ALWAYS);
        leftForm.addRow(0, new Label("Pelanggan"), customerRow);
        leftForm.addRow(1, new Label("Layanan"), serviceCombo);
        leftForm.addRow(2, new Label("Kecepatan"), speedCombo);
        leftForm.addRow(3, new Label("Satuan"), unitCombo);
        leftForm.addRow(4, new Label("Quantity"), qtyField);

        GridPane rightForm = new GridPane();
        rightForm.setHgap(10);
        rightForm.setVgap(10);
        rightForm.addRow(0, new Label("Metode Bayar"), paymentMethodCombo);
        rightForm.addRow(1, new Label("DP (opsional)"), dpField);
        rightForm.addRow(2, new Label("Harga Satuan"), unitPriceLabel);
        rightForm.addRow(3, new Label("Total"), totalPriceLabel);
        rightForm.addRow(4, new Label("Sisa Bayar"), remainingLabel);

        HBox twoColumns = new HBox(20, leftForm, rightForm);
        HBox.setHgrow(leftForm, Priority.ALWAYS);
        HBox.setHgrow(rightForm, Priority.ALWAYS);

        Button saveBtn = new Button("Simpan Transaksi + Nota PDF");
        saveBtn.setOnAction(e -> saveTransaction());

        HBox actions = new HBox(10, saveBtn, infoLabel);
        actions.setAlignment(Pos.CENTER_LEFT);

        setupOrderTable();
        Button reloadBtn = new Button("Refresh Order");
        reloadBtn.setOnAction(e -> refreshOrders());

        container.getChildren().addAll(twoColumns, actions, new Separator(), new Label("Riwayat Order"), orderTable, reloadBtn);

        root = container;
        refreshData();
        return root;
    }

    public void refreshData() {
        reloadCachedPrices();
        refreshOrders();
    }

    private void setupOrderTable() {
        TableColumn<Order, String> invoiceCol = new TableColumn<>("Invoice");
        invoiceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().invoiceNo()));

        TableColumn<Order, String> customerCol = new TableColumn<>("Pelanggan");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().customerName()));

        TableColumn<Order, String> serviceCol = new TableColumn<>("Layanan");
        serviceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().serviceName()));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().totalPrice()));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().orderStatus()));

        TableColumn<Order, String> paymentCol = new TableColumn<>("Pembayaran");
        paymentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().paymentStatus()));

        orderTable.getColumns().setAll(invoiceCol, customerCol, serviceCol, totalCol, statusCol, paymentCol);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        orderTable.setPrefHeight(320);
    }

    private void openCustomerPicker() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            infoLabel.setText("Belum ada data pelanggan. Tambahkan pelanggan dulu.");
            return;
        }

        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Pilih Pelanggan");
        dialog.setHeaderText("Cari dan pilih pelanggan");

        ButtonType selectBtn = new ButtonType("Pilih", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);

        TextField searchField = new TextField();
        searchField.setPromptText("Cari nama atau telepon...");

        TableView<Customer> table = new TableView<>();
        TableColumn<Customer, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().name())));
        TableColumn<Customer, String> phoneCol = new TableColumn<>("Telepon");
        phoneCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().phone())));
        table.getColumns().setAll(nameCol, phoneCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        ObservableList<Customer> allData = FXCollections.observableArrayList(customers);
        table.setItems(allData);
        table.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String keyword = newV == null ? "" : newV.trim().toLowerCase();
            table.setItems(FXCollections.observableArrayList(
                    customers.stream()
                            .filter(c -> safeText(c.name()).toLowerCase().contains(keyword)
                                    || safeText(c.phone()).toLowerCase().contains(keyword))
                            .toList()
            ));
            if (!table.getItems().isEmpty()) {
                table.getSelectionModel().selectFirst();
            }
        });

        table.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    dialog.setResult(row.getItem());
                    dialog.close();
                }
            });
            return row;
        });

        VBox content = new VBox(10, searchField, table);
        content.setPadding(new Insets(8));
        content.setPrefSize(460, 360);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == selectBtn ? table.getSelectionModel().getSelectedItem() : null);
        Customer picked = dialog.showAndWait().orElse(null);
        if (picked != null) {
            selectedCustomer = picked;
            customerField.setText(picked.toString());
            infoLabel.setText("Pelanggan terpilih: " + safeText(picked.name()));
        }
    }

    private void updateUnitPriceAndTotals() {
        String service = serviceCombo.getValue();
        String speed = speedCombo.getValue();
        String unit = unitCombo.getValue();

        if (service == null || speed == null || unit == null) {
            currentUnitPrice = 0;
            unitPriceLabel.setText("Rp0");
            updateTotals();
            return;
        }

        currentUnitPrice = cachedPrices.stream()
                .filter(p -> p.serviceName().equals(service)
                        && p.speedName().equals(speed)
                        && p.unitName().equals(unit))
                .findFirst()
                .map(ServicePrice::price)
                .orElse(0);

        unitPriceLabel.setText("Rp" + currentUnitPrice);
        updateTotals();
    }

    private void updateTotals() {
        double qty = parseDoubleOrZero(qtyField.getText());
        currentTotal = (int) Math.round(currentUnitPrice * qty);
        totalPriceLabel.setText("Rp" + currentTotal);

        int dp = parseIntOrZero(dpField.getText());
        int remaining = Math.max(0, currentTotal - dp);
        remainingLabel.setText("Rp" + remaining);
    }

    private void saveTransaction() {
        try {
            if (selectedCustomer == null) {
                throw new IllegalArgumentException("Pelanggan wajib dipilih.");
            }

            double qty = parseDoubleOrZero(qtyField.getText());
            if (qty <= 0) {
                throw new IllegalArgumentException("Quantity harus lebih dari 0.");
            }
            if (currentUnitPrice <= 0) {
                throw new IllegalArgumentException("Harga layanan tidak ditemukan untuk kombinasi terpilih.");
            }

            String method = paymentMethodCombo.getValue();
            int dp = parseIntOrZero(dpField.getText());
            int paidAmount = "DP".equals(method) ? dp : currentTotal;
            String paymentStatus = paidAmount >= currentTotal ? "Lunas" : "Belum Lunas";

            LocalDate now = LocalDate.now();
            int addHours = estimateHours(speedCombo.getValue());
            String estimate = now.atStartOfDay().plusHours(addHours).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            Order order = new Order(
                    pricingService.generateInvoiceNo(),
                    selectedCustomer.id(),
                    selectedCustomer.name(),
                    serviceCombo.getValue(),
                    speedCombo.getValue(),
                    unitCombo.getValue(),
                    qty,
                    currentUnitPrice,
                    currentTotal,
                    "Diterima",
                    paymentStatus,
                    method,
                    dp,
                    paidAmount,
                    now.toString(),
                    estimate
            );

            orderRepository.create(order);
            Path pdf = pdfInvoiceService.generate(order);
            infoLabel.setText("Transaksi tersimpan. Nota: " + pdf);

            refreshOrders();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (Exception e) {
            infoLabel.setText("Gagal simpan transaksi: " + e.getMessage());
        }
    }

    private void reloadCachedPrices() {
        cachedPrices.clear();
        cachedPrices.addAll(servicePriceRepository.findAll());

        serviceCombo.setItems(FXCollections.observableArrayList(cachedPrices.stream().map(ServicePrice::serviceName).distinct().toList()));
        speedCombo.setItems(FXCollections.observableArrayList(cachedPrices.stream().map(ServicePrice::speedName).distinct().toList()));
        unitCombo.setItems(FXCollections.observableArrayList(cachedPrices.stream().map(ServicePrice::unitName).distinct().toList()));

        if (!serviceCombo.getItems().isEmpty()) {
            serviceCombo.getSelectionModel().selectFirst();
        }
        if (!speedCombo.getItems().isEmpty()) {
            speedCombo.getSelectionModel().selectFirst();
        }
        if (!unitCombo.getItems().isEmpty()) {
            unitCombo.getSelectionModel().selectFirst();
        }

        updateUnitPriceAndTotals();
    }

    private void refreshOrders() {
        orderTable.setItems(FXCollections.observableArrayList(orderRepository.findAll()));
    }

    private int parseIntOrZero(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int estimateHours(String speed) {
        return switch (speed) {
            case "Super Express" -> 6;
            case "Express" -> 24;
            default -> 48;
        };
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
