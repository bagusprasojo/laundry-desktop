package com.laundrydesktop.view;

import com.laundrydesktop.model.Customer;
import com.laundrydesktop.repo.CustomerRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class CustomerView {
    private static final int PAGE_SIZE = 10;

    private final CustomerRepository customerRepository = new CustomerRepository();

    private final TextField customerNameField = new TextField();
    private final TextField customerPhoneField = new TextField();
    private final TextField customerAddressField = new TextField();
    private final TextField customerNoteField = new TextField();
    private final Label customerFormInfo = new Label();
    private final Button customerSaveButton = new Button("Simpan Pelanggan");
    private final TableView<Customer> customerTable = new TableView<>();
    private final TextField customerSearchField = new TextField();
    private final Label customerPageInfoLabel = new Label("Halaman 1/1");
    private final Button customerPrevPageButton = new Button("Sebelumnya");
    private final Button customerNextPageButton = new Button("Berikutnya");

    private Integer editingCustomerId = null;
    private int customerCurrentPage = 0;
    private int customerTotalPages = 1;
    private String customerKeyword = "";
    private Parent root;

    public Parent build() {
        if (root != null) {
            return root;
        }

        VBox root = new VBox(12);
        root.setPadding(new Insets(12));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Nama"), customerNameField);
        form.addRow(1, new Label("Telepon"), customerPhoneField);
        form.addRow(2, new Label("Alamat"), customerAddressField);
        form.addRow(3, new Label("Catatan"), customerNoteField);

        Button customerResetButton = new Button("Reset Form");
        customerSaveButton.setOnAction(e -> saveOrUpdateCustomer());
        customerResetButton.setOnAction(e -> clearCustomerForm());

        HBox topActions = new HBox(10, customerSaveButton, customerResetButton, customerFormInfo);
        topActions.setAlignment(Pos.CENTER_LEFT);
        VBox topPanel = new VBox(10, new Label("Input / Edit Pelanggan"), form, topActions);
        topPanel.setPadding(new Insets(12));
        topPanel.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");

        setupCustomerTable();
        customerSearchField.setPromptText("Cari nama atau telepon...");
        Button searchBtn = new Button("Cari");
        Button clearSearchBtn = new Button("Reset Cari");
        searchBtn.setOnAction(e -> applyCustomerSearch());
        clearSearchBtn.setOnAction(e -> {
            customerSearchField.clear();
            applyCustomerSearch();
        });
        customerSearchField.setOnAction(e -> applyCustomerSearch());

        customerPrevPageButton.setOnAction(e -> {
            if (customerCurrentPage > 0) {
                loadCustomerPage(customerCurrentPage - 1);
            }
        });
        customerNextPageButton.setOnAction(e -> {
            if (customerCurrentPage < customerTotalPages - 1) {
                loadCustomerPage(customerCurrentPage + 1);
            }
        });

        HBox searchRow = new HBox(10, new Label("Pencarian"), customerSearchField, searchBtn, clearSearchBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        HBox paginationRow = new HBox(10, customerPrevPageButton, customerNextPageButton, customerPageInfoLabel);
        paginationRow.setAlignment(Pos.CENTER_LEFT);

        VBox bottomPanel = new VBox(10, new Label("Daftar Pelanggan"), searchRow, customerTable, paginationRow);
        bottomPanel.setPadding(new Insets(12));
        bottomPanel.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
        VBox.setVgrow(customerTable, Priority.ALWAYS);

        VBox.setVgrow(bottomPanel, Priority.ALWAYS);
        root.getChildren().addAll(topPanel, bottomPanel);

        this.root = root;
        refreshData();
        return this.root;
    }

    public void refreshData() {
        refreshCustomerList();
    }

    private void setupCustomerTable() {
        TableColumn<Customer, String> nameCol = new TableColumn<>("Nama");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().name())));

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Telepon");
        phoneCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().phone())));

        TableColumn<Customer, String> addressCol = new TableColumn<>("Alamat");
        addressCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().address())));

        TableColumn<Customer, String> noteCol = new TableColumn<>("Catatan");
        noteCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().note())));

        customerTable.getColumns().setAll(nameCol, phoneCol, addressCol, noteCol);
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        customerTable.setPrefHeight(280);
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    startEditCustomer(row.getItem());
                }
            });
            return row;
        });
    }

    private void saveOrUpdateCustomer() {
        String name = customerNameField.getText().trim();
        if (name.isBlank()) {
            customerFormInfo.setText("Nama pelanggan wajib diisi.");
            return;
        }

        String phone = customerPhoneField.getText().trim();
        String address = customerAddressField.getText().trim();
        String note = customerNoteField.getText().trim();

        if (editingCustomerId == null) {
            customerRepository.create(name, phone, address, note);
            customerFormInfo.setText("Pelanggan baru berhasil disimpan.");
        } else {
            customerRepository.update(editingCustomerId, name, phone, address, note);
            customerFormInfo.setText("Data pelanggan berhasil diperbarui.");
        }

        clearCustomerForm();
        refreshCustomerList();
    }

    private void startEditCustomer(Customer customer) {
        editingCustomerId = customer.id();
        customerNameField.setText(safeText(customer.name()));
        customerPhoneField.setText(safeText(customer.phone()));
        customerAddressField.setText(safeText(customer.address()));
        customerNoteField.setText(safeText(customer.note()));
        customerSaveButton.setText("Update Pelanggan");
        customerFormInfo.setText("Mode edit aktif untuk: " + customer.name());
    }

    private void clearCustomerForm() {
        editingCustomerId = null;
        customerNameField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
        customerNoteField.clear();
        customerSaveButton.setText("Simpan Pelanggan");
    }

    private void applyCustomerSearch() {
        customerKeyword = customerSearchField.getText() == null ? "" : customerSearchField.getText().trim();
        loadCustomerPage(0);
    }

    private void refreshCustomerList() {
        loadCustomerPage(customerCurrentPage);
    }

    private void loadCustomerPage(int pageIndex) {
        int totalData = customerRepository.countByKeyword(customerKeyword);
        customerTotalPages = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));
        customerCurrentPage = Math.min(Math.max(0, pageIndex), customerTotalPages - 1);

        int offset = customerCurrentPage * PAGE_SIZE;
        List<Customer> pageData = customerRepository.findPagedByKeyword(customerKeyword, PAGE_SIZE, offset);
        customerTable.setItems(FXCollections.observableArrayList(pageData));

        customerPageInfoLabel.setText("Halaman " + (customerCurrentPage + 1) + "/" + customerTotalPages + " | Total: " + totalData);
        customerPrevPageButton.setDisable(customerCurrentPage <= 0);
        customerNextPageButton.setDisable(customerCurrentPage >= customerTotalPages - 1);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
