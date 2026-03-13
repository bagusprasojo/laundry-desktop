package com.laundrydesktop.view;

import com.laundrydesktop.service.JasperCashInReportService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class CashInReportView {
    private final JasperCashInReportService jasperCashInReportService = new JasperCashInReportService();

    private final DatePicker startDatePicker = new DatePicker(LocalDate.now().minusDays(7));
    private final DatePicker endDatePicker = new DatePicker(LocalDate.now());
    private final Label infoLabel = new Label();
    private Parent root;

    public Parent build() {
        if (root != null) {
            return root;
        }

        GridPane filterForm = new GridPane();
        filterForm.setHgap(10);
        filterForm.setVgap(10);
        filterForm.addRow(0, new Label("Periode Awal"), startDatePicker);
        filterForm.addRow(1, new Label("Periode Akhir"), endDatePicker);

        Button showBtn = new Button("Tampilkan");
        showBtn.setOnAction(e -> previewReport());

        HBox actions = new HBox(10, showBtn, infoLabel);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12,
                new Label("Laporan Kas Masuk"),
                filterForm,
                actions
        );
        box.setPadding(new Insets(12));
        root = box;
        return root;
    }

    public void refreshData() {
        // Tidak ada data grid yang perlu di-refresh pada mode preview.
    }

    private void previewReport() {
        if (!isPeriodValid()) {
            return;
        }
        try {
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            jasperCashInReportService.previewCashInReport(start, end);
            infoLabel.setText("Preview laporan kas masuk berhasil dibuka.");
        } catch (Exception e) {
            infoLabel.setText("Gagal menampilkan preview: " + e.getMessage());
        }
    }

    private boolean isPeriodValid() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            infoLabel.setText("Periode awal dan akhir wajib diisi.");
            return false;
        }
        if (start.isAfter(end)) {
            infoLabel.setText("Periode awal tidak boleh lebih besar dari periode akhir.");
            return false;
        }
        return true;
    }
}
