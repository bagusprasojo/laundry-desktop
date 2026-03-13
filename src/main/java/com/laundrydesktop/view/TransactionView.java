package com.laundrydesktop.view;

import com.laundrydesktop.model.Customer;
import com.laundrydesktop.model.Order;
import com.laundrydesktop.model.ServicePrice;
import com.laundrydesktop.repo.CustomerRepository;
import com.laundrydesktop.repo.OrderRepository;
import com.laundrydesktop.repo.PaymentTransactionRepository;
import com.laundrydesktop.repo.ServicePriceRepository;
import com.laundrydesktop.repo.SpeedRepository;
import com.laundrydesktop.service.JasperWorkReceiptService;
import com.laundrydesktop.service.PricingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionView {
    private final CustomerRepository customerRepository = new CustomerRepository();
    private final ServicePriceRepository servicePriceRepository = new ServicePriceRepository();
    private final SpeedRepository speedRepository = new SpeedRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final PaymentTransactionRepository paymentTransactionRepository = new PaymentTransactionRepository();
    private final PricingService pricingService = new PricingService();
    private final JasperWorkReceiptService jasperWorkReceiptService = new JasperWorkReceiptService();
    private final Runnable onChanged;

    private final TextField customerField = new TextField();
    private final Button selectCustomerButton = new Button("Cari Pelanggan");
    private Customer selectedCustomer;

    private final ComboBox<String> serviceCombo = new ComboBox<>();
    private final ComboBox<String> speedCombo = new ComboBox<>();
    private final ComboBox<String> unitCombo = new ComboBox<>();
    private final DatePicker receivedDatePicker = new DatePicker(LocalDate.now());
    private final TextField qtyField = new TextField();
    private final TextArea noteArea = new TextArea();
    private final TextField paidNowField = new TextField("0");
    private final Label unitPriceLabel = new Label("Rp0");
    private final Label totalPriceLabel = new Label("Rp0");
    private final Label remainingLabel = new Label("Rp0");
    private final Label estimateDurationLabel = new Label("-");
    private final Label estimateDonePreviewLabel = new Label("-");
    private final Label infoLabel = new Label();
    private final TableView<Order> orderTable = new TableView<>();
    private final TextField orderSearchField = new TextField();
    private final Label orderPageInfoLabel = new Label("Halaman 1/1");
    private final Button orderPrevPageButton = new Button("Sebelumnya");
    private final Button orderNextPageButton = new Button("Berikutnya");
    private final Button saveButton = new Button("Simpan + Tampilkan Nota");
    private final Button cancelEditButton = new Button("Batal Edit");

    private final List<ServicePrice> cachedPrices = new ArrayList<>();
    private Parent root;
    private int currentUnitPrice = 0;
    private int currentTotal = 0;
    private int orderCurrentPage = 0;
    private int orderTotalPages = 1;
    private String orderKeyword = "";
    private boolean editMode = false;
    private String editingInvoiceNo;
    private String editingOrderStatus = "Diterima";
    private static final int ORDER_PAGE_SIZE = 10;

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
        noteArea.setPromptText("Keterangan pekerjaan (opsional)");
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);
        serviceCombo.setOnAction(e -> updateUnitPriceAndTotals());
        speedCombo.setOnAction(e -> {
            updateUnitPriceAndTotals();
            updateEstimatePreview();
        });
        unitCombo.setOnAction(e -> updateUnitPriceAndTotals());
        qtyField.textProperty().addListener((obs, oldV, newV) -> updateTotals());
        paidNowField.textProperty().addListener((obs, oldV, newV) -> updateTotals());
        receivedDatePicker.valueProperty().addListener((obs, oldV, newV) -> updateEstimatePreview());

        GridPane leftForm = new GridPane();
        leftForm.setHgap(10);
        leftForm.setVgap(10);
        HBox customerRow = new HBox(8, customerField, selectCustomerButton);
        HBox.setHgrow(customerField, Priority.ALWAYS);
        HBox unitRow = new HBox(10, unitCombo, new Label("Harga Satuan"), unitPriceLabel);
        HBox.setHgrow(unitCombo, Priority.ALWAYS);
        leftForm.addRow(0, new Label("Pelanggan"), customerRow);
        leftForm.addRow(1, new Label("Tanggal Terima"), receivedDatePicker);
        leftForm.addRow(2, new Label("Layanan"), serviceCombo);
        leftForm.addRow(3, new Label("Kecepatan"), speedCombo);
        leftForm.addRow(4, new Label("Satuan"), unitRow);
        leftForm.addRow(5, new Label("Quantity"), qtyField);

        GridPane rightForm = new GridPane();
        rightForm.setHgap(10);
        rightForm.setVgap(10);
        rightForm.addRow(0, new Label("Total"), totalPriceLabel);
        rightForm.addRow(1, new Label("Dibayar Saat Ini"), paidNowField);
        rightForm.addRow(2, new Label("Sisa Bayar"), remainingLabel);
        rightForm.addRow(3, new Label("Estimasi Durasi"), estimateDurationLabel);
        rightForm.addRow(4, new Label("Estimasi Selesai"), estimateDonePreviewLabel);
        rightForm.addRow(5, new Label("Keterangan"), noteArea);

        HBox twoColumns = new HBox(20, leftForm, rightForm);
        HBox.setHgrow(leftForm, Priority.ALWAYS);
        HBox.setHgrow(rightForm, Priority.ALWAYS);

        saveButton.setOnAction(e -> saveTransaction());
        cancelEditButton.setOnAction(e -> exitEditMode());
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);

        HBox actions = new HBox(10, saveButton, cancelEditButton, infoLabel);
        actions.setAlignment(Pos.CENTER_LEFT);

        setupOrderTable();
        orderSearchField.setPromptText("Cari invoice, pelanggan, layanan, tanggal, status, keterangan...");
        Button searchButton = new Button("Cari");
        Button resetSearchButton = new Button("Reset Cari");
        searchButton.setOnAction(e -> applyOrderSearch());
        resetSearchButton.setOnAction(e -> {
            orderSearchField.clear();
            applyOrderSearch();
        });
        orderSearchField.setOnAction(e -> applyOrderSearch());

        orderPrevPageButton.setOnAction(e -> {
            if (orderCurrentPage > 0) {
                loadOrderPage(orderCurrentPage - 1);
            }
        });
        orderNextPageButton.setOnAction(e -> {
            if (orderCurrentPage < orderTotalPages - 1) {
                loadOrderPage(orderCurrentPage + 1);
            }
        });

        HBox searchRow = new HBox(10, new Label("Pencarian"), orderSearchField, searchButton, resetSearchButton);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        HBox paginationRow = new HBox(10, orderPrevPageButton, orderNextPageButton, orderPageInfoLabel);
        paginationRow.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(
                twoColumns,
                actions,
                new Separator(),
                new Label("Riwayat Order"),
                searchRow,
                orderTable,
                paginationRow
        );

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

        TableColumn<Order, String> receivedDateCol = new TableColumn<>("Tanggal Terima");
        receivedDateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().orderDate()));

        TableColumn<Order, String> estimateDoneCol = new TableColumn<>("Estimasi Selesai");
        estimateDoneCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().estimateDone())));

        TableColumn<Order, String> serviceCol = new TableColumn<>("Layanan");
        serviceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().serviceName()));

        TableColumn<Order, String> noteCol = new TableColumn<>("Keterangan");
        noteCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().note())));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().totalPrice()));

        TableColumn<Order, String> paidCol = new TableColumn<>("Terbayar");
        paidCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().paidAmount()));

        TableColumn<Order, String> remainingCol = new TableColumn<>("Sisa Bayar");
        remainingCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                "Rp" + Math.max(0, data.getValue().totalPrice() - data.getValue().paidAmount())));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().orderStatus()));

        TableColumn<Order, String> paymentCol = new TableColumn<>("Pembayaran");
        paymentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().paymentStatus()));

        TableColumn<Order, Void> actionCol = new TableColumn<>("Aksi");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Hapus");
            private final Button printButton = new Button("Cetak");
            private final HBox box = new HBox(6, editButton, deleteButton, printButton);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                editButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    enterEditMode(order);
                });
                deleteButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
                printButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    printOrderReceipt(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        orderTable.getColumns().setAll(invoiceCol, receivedDateCol, estimateDoneCol, customerCol, serviceCol, noteCol, totalCol, paidCol, remainingCol, statusCol, paymentCol, actionCol);
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

        int paidNow = parseIntOrZero(paidNowField.getText());
        int remaining = Math.max(0, currentTotal - paidNow);
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

            int paidAmount = parseIntOrZero(paidNowField.getText());
            if (paidAmount < 0) {
                throw new IllegalArgumentException("Nilai dibayar tidak boleh negatif.");
            }
            String paymentStatus = paidAmount >= currentTotal ? "Lunas" : "Belum Lunas";

            LocalDate now = receivedDatePicker.getValue();
            if (now == null) {
                throw new IllegalArgumentException("Tanggal terima wajib dipilih.");
            }
            int addHours = estimateHours(speedCombo.getValue());
            if (addHours <= 0) {
                throw new IllegalArgumentException("Durasi master kecepatan belum valid. Periksa Master Kecepatan.");
            }
            String estimate = buildEstimateDone(now, addHours);
            String orderStatus = editMode ? editingOrderStatus : "Diterima";

            Order order = new Order(
                    editMode ? editingInvoiceNo : pricingService.generateInvoiceNo(),
                    selectedCustomer.id(),
                    selectedCustomer.name(),
                    serviceCombo.getValue(),
                    speedCombo.getValue(),
                    unitCombo.getValue(),
                    qty,
                    currentUnitPrice,
                    currentTotal,
                    orderStatus,
                    paymentStatus,
                    "",
                    paidAmount,
                    paidAmount,
                    safeText(noteArea.getText()),
                    now.toString(),
                    estimate
            );

            if (editMode) {
                orderRepository.updateOrder(order);
                infoLabel.setText("Transaksi berhasil diperbarui.");
            } else {
                orderRepository.create(order);
                if (paidAmount > 0) {
                    paymentTransactionRepository.create(
                            order.invoiceNo(),
                            now.toString(),
                            paidAmount,
                            "",
                            "Pembayaran awal saat terima pekerjaan"
                    );
                }
                jasperWorkReceiptService.previewReceipt(order);
                infoLabel.setText("Transaksi tersimpan. Nota ditampilkan.");
            }

            refreshOrders();
            exitEditMode();
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
        updateEstimatePreview();
    }

    private void refreshOrders() {
        loadOrderPage(orderCurrentPage);
    }

    private void applyOrderSearch() {
        orderKeyword = orderSearchField.getText() == null ? "" : orderSearchField.getText().trim();
        loadOrderPage(0);
    }

    private void loadOrderPage(int pageIndex) {
        int totalData = orderRepository.countByKeyword(orderKeyword);
        orderTotalPages = Math.max(1, (int) Math.ceil((double) totalData / ORDER_PAGE_SIZE));
        orderCurrentPage = Math.min(Math.max(0, pageIndex), orderTotalPages - 1);

        int offset = orderCurrentPage * ORDER_PAGE_SIZE;
        List<Order> pageData = orderRepository.findPagedByKeyword(orderKeyword, ORDER_PAGE_SIZE, offset);
        orderTable.setItems(FXCollections.observableArrayList(pageData));

        orderPageInfoLabel.setText("Halaman " + (orderCurrentPage + 1) + "/" + orderTotalPages + " | Total: " + totalData);
        orderPrevPageButton.setDisable(orderCurrentPage <= 0);
        orderNextPageButton.setDisable(orderCurrentPage >= orderTotalPages - 1);
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
        return speedRepository.findDurationHoursByName(speed);
    }

    private String buildEstimateDone(LocalDate receivedDate, int durationHours) {
        LocalTime baseTime = LocalTime.now().withSecond(0).withNano(0);
        return receivedDate.atTime(baseTime)
                .plusHours(durationHours)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void updateEstimatePreview() {
        LocalDate receivedDate = receivedDatePicker.getValue();
        int durationHours = estimateHours(speedCombo.getValue());

        if (durationHours > 0) {
            estimateDurationLabel.setText(durationHours + " jam");
        } else {
            estimateDurationLabel.setText("Durasi tidak valid");
        }

        if (receivedDate == null || durationHours <= 0) {
            estimateDonePreviewLabel.setText("-");
            return;
        }
        estimateDonePreviewLabel.setText(buildEstimateDone(receivedDate, durationHours));
    }

    private void enterEditMode(Order order) {
        if (order == null) {
            return;
        }
        editMode = true;
        editingInvoiceNo = order.invoiceNo();
        editingOrderStatus = order.orderStatus();
        selectedCustomer = new Customer(order.customerId(), order.customerName(), "", "", "");

        customerField.setText(safeText(order.customerName()));
        serviceCombo.setValue(order.serviceName());
        speedCombo.setValue(order.speedName());
        unitCombo.setValue(order.unitName());
        qtyField.setText(String.valueOf(order.quantity()));
        paidNowField.setText(String.valueOf(order.paidAmount()));
        noteArea.setText(safeText(order.note()));
        receivedDatePicker.setValue(LocalDate.parse(order.orderDate()));

        updateUnitPriceAndTotals();
        updateEstimatePreview();
        saveButton.setText("Update Transaksi");
        cancelEditButton.setVisible(true);
        cancelEditButton.setManaged(true);
        infoLabel.setText("Mode edit aktif: " + order.invoiceNo());
    }

    private void exitEditMode() {
        editMode = false;
        editingInvoiceNo = null;
        editingOrderStatus = "Diterima";
        selectedCustomer = null;

        customerField.clear();
        receivedDatePicker.setValue(LocalDate.now());
        qtyField.clear();
        paidNowField.setText("0");
        noteArea.clear();

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
        updateEstimatePreview();

        saveButton.setText("Simpan + Tampilkan Nota");
        cancelEditButton.setVisible(false);
        cancelEditButton.setManaged(false);
    }

    private void deleteOrder(Order order) {
        if (order == null) {
            return;
        }
        if ("Sudah Diambil".equalsIgnoreCase(safeText(order.orderStatus()))) {
            infoLabel.setText("Order yang sudah diambil tidak bisa dihapus.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus transaksi " + order.invoiceNo() + "?");
        confirm.setContentText("Pelanggan: " + safeText(order.customerName()));
        boolean yes = confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        if (!yes) {
            return;
        }

        try {
            paymentTransactionRepository.deleteByInvoiceNo(order.invoiceNo());
            orderRepository.deleteByInvoiceNo(order.invoiceNo());
            infoLabel.setText("Transaksi berhasil dihapus: " + order.invoiceNo());
            if (editMode && order.invoiceNo().equals(editingInvoiceNo)) {
                exitEditMode();
            }
            refreshOrders();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (Exception e) {
            infoLabel.setText("Gagal hapus transaksi: " + e.getMessage());
        }
    }

    private void printOrderReceipt(Order order) {
        if (order == null) {
            return;
        }
        try {
            jasperWorkReceiptService.previewReceipt(order);
            infoLabel.setText("Preview nota dibuka untuk: " + order.invoiceNo());
        } catch (Exception e) {
            infoLabel.setText("Gagal membuka nota: " + e.getMessage());
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
