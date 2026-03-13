package com.laundrydesktop.view;

import com.laundrydesktop.model.Order;
import com.laundrydesktop.repo.OrderRepository;
import com.laundrydesktop.repo.PaymentTransactionRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class PickupView {
    private final OrderRepository orderRepository = new OrderRepository();
    private final PaymentTransactionRepository paymentTransactionRepository = new PaymentTransactionRepository();

    private final TableView<Order> table = new TableView<>();
    private final ComboBox<String> paymentMethodCombo = new ComboBox<>();
    private final TextField additionalPaymentField = new TextField("0");
    private final Label infoLabel = new Label();
    private Parent root;

    public Parent build() {
        if (root != null) {
            return root;
        }

        VBox container = new VBox(12);
        container.setPadding(new Insets(12));

        setupTable();

        paymentMethodCombo.setItems(FXCollections.observableArrayList("Tunai", "Digital", "DP"));
        paymentMethodCombo.getSelectionModel().selectFirst();
        additionalPaymentField.setPromptText("Tambahan pembayaran");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Metode Bayar"), paymentMethodCombo);
        form.addRow(1, new Label("Tambahan Bayar"), additionalPaymentField);

        Button processBtn = new Button("Proses Pengambilan");
        Button refreshBtn = new Button("Refresh");
        processBtn.setOnAction(e -> processPickup());
        refreshBtn.setOnAction(e -> refreshData());

        HBox actions = new HBox(10, processBtn, refreshBtn, infoLabel);
        actions.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(new Label("Daftar Order Belum Diambil"), table, form, actions);

        root = container;
        refreshData();
        return root;
    }

    public void refreshData() {
        table.setItems(FXCollections.observableArrayList(orderRepository.findNotPickedUp()));
    }

    private void setupTable() {
        TableColumn<Order, String> invoiceCol = new TableColumn<>("Invoice");
        invoiceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().invoiceNo()));

        TableColumn<Order, String> customerCol = new TableColumn<>("Pelanggan");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().customerName()));

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().totalPrice()));

        TableColumn<Order, String> paidCol = new TableColumn<>("Sudah Bayar");
        paidCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Rp" + data.getValue().paidAmount()));

        TableColumn<Order, String> statusCol = new TableColumn<>("Status Order");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().orderStatus()));

        TableColumn<Order, String> payStatusCol = new TableColumn<>("Status Bayar");
        payStatusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().paymentStatus()));

        table.getColumns().setAll(invoiceCol, customerCol, totalCol, paidCol, statusCol, payStatusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(360);
    }

    private void processPickup() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            infoLabel.setText("Pilih order yang akan diproses.");
            return;
        }

        int additional;
        try {
            additional = Integer.parseInt(additionalPaymentField.getText().trim());
        } catch (Exception e) {
            infoLabel.setText("Tambahan pembayaran harus angka.");
            return;
        }
        if (additional < 0) {
            infoLabel.setText("Tambahan pembayaran tidak boleh negatif.");
            return;
        }

        int newPaid = selected.paidAmount() + additional;
        String paymentStatus = newPaid >= selected.totalPrice() ? "Lunas" : "Belum Lunas";

        orderRepository.updatePickup(
                selected.invoiceNo(),
                newPaid,
                paymentStatus,
                "Sudah Diambil",
                paymentMethodCombo.getValue()
        );
        if (additional > 0) {
            paymentTransactionRepository.create(
                    selected.invoiceNo(),
                    LocalDate.now().toString(),
                    additional,
                    paymentMethodCombo.getValue(),
                    "Pembayaran saat pengambilan"
            );
        }
        infoLabel.setText("Pengambilan berhasil diproses.");
        additionalPaymentField.setText("0");
        refreshData();
    }
}
