package com.laundrydesktop.view;

import com.laundrydesktop.model.MasterItem;
import com.laundrydesktop.model.ServicePrice;
import com.laundrydesktop.repo.MasterItemRepository;
import com.laundrydesktop.repo.ServicePriceRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicePriceView {
    private static final int PAGE_SIZE = 10;

    private final MasterItemRepository masterItemRepository = new MasterItemRepository();
    private final ServicePriceRepository servicePriceRepository = new ServicePriceRepository();
    private final Runnable onChanged;

    private final ComboBox<String> serviceCombo = new ComboBox<>();
    private final ComboBox<String> speedCombo = new ComboBox<>();
    private final ComboBox<String> unitCombo = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final Label formInfo = new Label();
    private final Button saveButton = new Button("Simpan Harga");
    private final TableView<ServicePrice> table = new TableView<>();
    private final TextField searchField = new TextField();
    private final Label pageInfoLabel = new Label("Halaman 1/1");
    private final Button prevPageButton = new Button("Sebelumnya");
    private final Button nextPageButton = new Button("Berikutnya");

    private final List<ServicePrice> cachedPrices = new ArrayList<>();
    private String editingService;
    private String editingSpeed;
    private String editingUnit;
    private int currentPage = 0;
    private int totalPages = 1;
    private String keyword = "";
    private Parent root;

    public ServicePriceView(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    public Parent build() {
        if (root != null) {
            return root;
        }

        VBox container = new VBox(12);
        container.setPadding(new Insets(12));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Layanan"), serviceCombo);
        form.addRow(1, new Label("Kecepatan"), speedCombo);
        form.addRow(2, new Label("Satuan"), unitCombo);
        form.addRow(3, new Label("Harga"), amountField);

        amountField.setPromptText("contoh: 7000");
        Button resetButton = new Button("Reset Form");
        Button deleteButton = new Button("Hapus Terpilih");
        saveButton.setOnAction(e -> saveOrUpdate());
        resetButton.setOnAction(e -> clearForm());
        deleteButton.setOnAction(e -> deleteSelected());

        HBox topActions = new HBox(10, saveButton, resetButton, deleteButton, formInfo);
        topActions.setAlignment(Pos.CENTER_LEFT);
        VBox topPanel = new VBox(10, new Label("Input / Edit Harga Layanan"), form, topActions);
        topPanel.setPadding(new Insets(12));
        topPanel.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");

        setupTable();
        searchField.setPromptText("Cari layanan, kecepatan, atau satuan...");
        Button searchBtn = new Button("Cari");
        Button clearSearchBtn = new Button("Reset Cari");
        searchBtn.setOnAction(e -> applySearch());
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            applySearch();
        });
        searchField.setOnAction(e -> applySearch());

        prevPageButton.setOnAction(e -> {
            if (currentPage > 0) {
                loadPage(currentPage - 1);
            }
        });
        nextPageButton.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                loadPage(currentPage + 1);
            }
        });

        HBox searchRow = new HBox(10, new Label("Pencarian"), searchField, searchBtn, clearSearchBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        HBox paginationRow = new HBox(10, prevPageButton, nextPageButton, pageInfoLabel);
        paginationRow.setAlignment(Pos.CENTER_LEFT);

        VBox bottomPanel = new VBox(10, new Label("Daftar Harga Layanan"), searchRow, table, paginationRow);
        bottomPanel.setPadding(new Insets(12));
        bottomPanel.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox.setVgrow(bottomPanel, Priority.ALWAYS);
        container.getChildren().addAll(topPanel, bottomPanel);

        root = container;
        refreshData();
        return root;
    }

    public void refreshData() {
        refreshMasterChoices();
        reloadCachedPrices();
        loadPage(currentPage);
    }

    public void refreshMasterChoices() {
        List<String> services = masterItemRepository.findAll("services").stream().map(MasterItem::name).toList();
        List<String> speeds = masterItemRepository.findAll("speeds").stream().map(MasterItem::name).toList();
        List<String> units = masterItemRepository.findAll("units").stream().map(MasterItem::name).toList();

        serviceCombo.setItems(FXCollections.observableArrayList(services));
        speedCombo.setItems(FXCollections.observableArrayList(speeds));
        unitCombo.setItems(FXCollections.observableArrayList(units));

        if ((serviceCombo.getValue() == null || !services.contains(serviceCombo.getValue())) && !services.isEmpty()) {
            serviceCombo.getSelectionModel().selectFirst();
        }
        if ((speedCombo.getValue() == null || !speeds.contains(speedCombo.getValue())) && !speeds.isEmpty()) {
            speedCombo.getSelectionModel().selectFirst();
        }
        if ((unitCombo.getValue() == null || !units.contains(unitCombo.getValue())) && !units.isEmpty()) {
            unitCombo.getSelectionModel().selectFirst();
        }
    }

    private void setupTable() {
        TableColumn<ServicePrice, String> serviceCol = new TableColumn<>("Layanan");
        serviceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().serviceName())));

        TableColumn<ServicePrice, String> speedCol = new TableColumn<>("Kecepatan");
        speedCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().speedName())));

        TableColumn<ServicePrice, String> unitCol = new TableColumn<>("Satuan");
        unitCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().unitName())));

        TableColumn<ServicePrice, String> priceCol = new TableColumn<>("Harga");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(formatRupiah(data.getValue().price())));

        table.getColumns().setAll(serviceCol, speedCol, unitCol, priceCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);
        table.setRowFactory(tv -> {
            TableRow<ServicePrice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    startEdit(row.getItem());
                }
            });
            return row;
        });
    }

    private void saveOrUpdate() {
        try {
            String service = safeText(serviceCombo.getValue()).trim();
            String speed = safeText(speedCombo.getValue()).trim();
            String unit = safeText(unitCombo.getValue()).trim();
            String amountRaw = amountField.getText().trim();

            if (service.isBlank() || speed.isBlank() || unit.isBlank()) {
                formInfo.setText("Layanan, kecepatan, dan satuan wajib dipilih.");
                return;
            }
            if (amountRaw.isBlank()) {
                formInfo.setText("Harga wajib diisi.");
                return;
            }

            int amount = Integer.parseInt(amountRaw);
            if (amount <= 0) {
                formInfo.setText("Harga harus lebih besar dari 0.");
                return;
            }
            if (amount > 100000000) {
                formInfo.setText("Harga terlalu besar.");
                return;
            }

            if (editingService != null) {
                boolean keyChanged = !editingService.equals(service)
                        || !editingSpeed.equals(speed)
                        || !editingUnit.equals(unit);
                if (keyChanged) {
                    servicePriceRepository.delete(editingService, editingSpeed, editingUnit);
                }
            } else {
                boolean exists = cachedPrices.stream().anyMatch(p ->
                        p.serviceName().equals(service) && p.speedName().equals(speed) && p.unitName().equals(unit));
                if (exists) {
                    formInfo.setText("Kombinasi sudah ada. Harga akan diperbarui.");
                }
            }

            servicePriceRepository.upsert(service, speed, unit, amount);
            if (formInfo.getText() == null || formInfo.getText().isBlank() || !formInfo.getText().contains("diperbarui")) {
                formInfo.setText("Harga layanan tersimpan: " + formatRupiah(amount));
            }
            clearForm();
            refreshData();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (NumberFormatException ex) {
            formInfo.setText("Harga harus berupa angka.");
        } catch (Exception ex) {
            formInfo.setText("Gagal menyimpan harga: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        ServicePrice selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            formInfo.setText("Pilih harga layanan yang ingin dihapus.");
            return;
        }
        try {
            servicePriceRepository.delete(selected.serviceName(), selected.speedName(), selected.unitName());
            formInfo.setText("Harga layanan berhasil dihapus.");
            clearForm();
            refreshData();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (Exception ex) {
            formInfo.setText("Gagal menghapus harga: " + ex.getMessage());
        }
    }

    private void startEdit(ServicePrice item) {
        editingService = item.serviceName();
        editingSpeed = item.speedName();
        editingUnit = item.unitName();
        serviceCombo.setValue(item.serviceName());
        speedCombo.setValue(item.speedName());
        unitCombo.setValue(item.unitName());
        amountField.setText(String.valueOf(item.price()));
        saveButton.setText("Update Harga");
        formInfo.setText("Mode edit aktif untuk kombinasi terpilih.");
    }

    private void clearForm() {
        editingService = null;
        editingSpeed = null;
        editingUnit = null;
        if (!serviceCombo.getItems().isEmpty()) {
            serviceCombo.getSelectionModel().selectFirst();
        }
        if (!speedCombo.getItems().isEmpty()) {
            speedCombo.getSelectionModel().selectFirst();
        }
        if (!unitCombo.getItems().isEmpty()) {
            unitCombo.getSelectionModel().selectFirst();
        }
        amountField.clear();
        saveButton.setText("Simpan Harga");
    }

    private void applySearch() {
        keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        loadPage(0);
    }

    private void loadPage(int pageIndex) {
        int totalData = servicePriceRepository.countByKeyword(keyword);
        totalPages = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));
        currentPage = Math.min(Math.max(0, pageIndex), totalPages - 1);

        int offset = currentPage * PAGE_SIZE;
        List<ServicePrice> pageData = servicePriceRepository.findPagedByKeyword(keyword, PAGE_SIZE, offset);
        table.setItems(FXCollections.observableArrayList(pageData));

        pageInfoLabel.setText("Halaman " + (currentPage + 1) + "/" + totalPages + " | Total: " + totalData);
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    private void reloadCachedPrices() {
        cachedPrices.clear();
        cachedPrices.addAll(servicePriceRepository.findAll());
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String formatRupiah(int amount) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return "Rp" + decimalFormat.format(amount);
    }
}
