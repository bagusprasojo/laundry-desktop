package com.laundrydesktop.view;

import com.laundrydesktop.model.SpeedMaster;
import com.laundrydesktop.repo.SpeedRepository;
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

public class SpeedView {
    private static final int PAGE_SIZE = 10;

    private final SpeedRepository repository = new SpeedRepository();
    private final Runnable onChanged;

    private final TextField nameField = new TextField();
    private final TextField durationField = new TextField();
    private final TextField searchField = new TextField();
    private final Label formInfoLabel = new Label();
    private final Label pageInfoLabel = new Label("Halaman 1/1");
    private final Button saveButton = new Button("Simpan");
    private final Button prevPageButton = new Button("Sebelumnya");
    private final Button nextPageButton = new Button("Berikutnya");
    private final TableView<SpeedMaster> table = new TableView<>();

    private Integer editingId = null;
    private int currentPage = 0;
    private int totalPages = 1;
    private String keyword = "";
    private Parent root;

    public SpeedView(Runnable onChanged) {
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
        form.addRow(0, new Label("Nama Kecepatan"), nameField);
        form.addRow(1, new Label("Estimasi (Jam)"), durationField);

        Button resetButton = new Button("Reset Form");
        Button deleteButton = new Button("Hapus Terpilih");
        saveButton.setOnAction(e -> saveOrUpdate());
        resetButton.setOnAction(e -> clearForm());
        deleteButton.setOnAction(e -> deleteSelected());

        HBox topActions = new HBox(10, saveButton, resetButton, deleteButton, formInfoLabel);
        topActions.setAlignment(Pos.CENTER_LEFT);
        VBox topPanel = new VBox(10, new Label("Input / Edit Master Kecepatan"), form, topActions);
        topPanel.setPadding(new Insets(12));
        topPanel.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6;");

        setupTable();
        searchField.setPromptText("Cari nama kecepatan...");
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

        VBox bottomPanel = new VBox(10, new Label("Daftar Master Kecepatan"), searchRow, table, paginationRow);
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
        loadPage(currentPage);
    }

    private void setupTable() {
        TableColumn<SpeedMaster, String> nameCol = new TableColumn<>("Nama Kecepatan");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safeText(data.getValue().name())));
        TableColumn<SpeedMaster, String> durationCol = new TableColumn<>("Estimasi (Jam)");
        durationCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().durationHours())));
        table.getColumns().setAll(nameCol, durationCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(280);
        table.setRowFactory(tv -> {
            TableRow<SpeedMaster> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    startEdit(row.getItem());
                }
            });
            return row;
        });
    }

    private void saveOrUpdate() {
        String name = nameField.getText().trim();
        int duration = parsePositiveDuration(durationField.getText());
        if (name.isBlank()) {
            formInfoLabel.setText("Nama kecepatan wajib diisi.");
            return;
        }
        if (duration <= 0) {
            formInfoLabel.setText("Estimasi durasi harus angka lebih dari 0.");
            return;
        }
        try {
            if (editingId == null) {
                repository.create(name, duration);
                formInfoLabel.setText("Master kecepatan baru berhasil disimpan.");
            } else {
                repository.update(editingId, name, duration);
                formInfoLabel.setText("Master kecepatan berhasil diperbarui.");
            }
            clearForm();
            refreshData();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (Exception ex) {
            formInfoLabel.setText("Gagal menyimpan master kecepatan: " + ex.getMessage());
        }
    }

    private void deleteSelected() {
        SpeedMaster selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            formInfoLabel.setText("Pilih kecepatan yang ingin dihapus.");
            return;
        }
        try {
            repository.delete(selected.id());
            formInfoLabel.setText("Master kecepatan berhasil dihapus.");
            clearForm();
            refreshData();
            if (onChanged != null) {
                onChanged.run();
            }
        } catch (Exception ex) {
            formInfoLabel.setText("Gagal menghapus master kecepatan: " + ex.getMessage());
        }
    }

    private void startEdit(SpeedMaster item) {
        editingId = item.id();
        nameField.setText(safeText(item.name()));
        durationField.setText(String.valueOf(item.durationHours()));
        saveButton.setText("Update");
        formInfoLabel.setText("Mode edit aktif untuk kecepatan \"" + safeText(item.name()) + "\"");
    }

    private void clearForm() {
        editingId = null;
        nameField.clear();
        durationField.clear();
        saveButton.setText("Simpan");
    }

    private void applySearch() {
        keyword = searchField.getText() == null ? "" : searchField.getText().trim();
        loadPage(0);
    }

    private void loadPage(int pageIndex) {
        int totalData = repository.countByKeyword(keyword);
        totalPages = Math.max(1, (int) Math.ceil((double) totalData / PAGE_SIZE));
        currentPage = Math.min(Math.max(0, pageIndex), totalPages - 1);

        int offset = currentPage * PAGE_SIZE;
        List<SpeedMaster> pageData = repository.findPagedByKeyword(keyword, PAGE_SIZE, offset);
        table.setItems(FXCollections.observableArrayList(pageData));

        pageInfoLabel.setText("Halaman " + (currentPage + 1) + "/" + totalPages + " | Total: " + totalData);
        prevPageButton.setDisable(currentPage <= 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);
    }

    private int parsePositiveDuration(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
