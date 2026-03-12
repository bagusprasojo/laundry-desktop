package com.laundrydesktop.view;

import com.laundrydesktop.model.BusinessProfile;
import com.laundrydesktop.repo.BusinessProfileRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BusinessProfileView {
    private final BusinessProfileRepository repository = new BusinessProfileRepository();

    private final TextField businessNameField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField ownerField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label infoLabel = new Label();
    private Parent root;

    public Parent build() {
        if (root != null) {
            return root;
        }

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Nama Usaha"), businessNameField);
        form.addRow(1, new Label("Alamat"), addressField);
        form.addRow(2, new Label("Penanggung Jawab"), ownerField);
        form.addRow(3, new Label("No. Telepon"), phoneField);

        Button saveBtn = new Button("Simpan");
        Button resetBtn = new Button("Reset");
        saveBtn.setOnAction(e -> save());
        resetBtn.setOnAction(e -> loadData());

        HBox actions = new HBox(10, saveBtn, resetBtn, infoLabel);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(12,
                new Label("Profil Usaha"),
                form,
                actions
        );
        box.setPadding(new Insets(12));

        root = box;
        loadData();
        return root;
    }

    public void refreshData() {
        loadData();
    }

    private void loadData() {
        BusinessProfile profile = repository.get();
        businessNameField.setText(profile.businessName());
        addressField.setText(profile.address());
        ownerField.setText(profile.ownerName());
        phoneField.setText(profile.phone());
    }

    private void save() {
        String businessName = businessNameField.getText() == null ? "" : businessNameField.getText().trim();
        if (businessName.isBlank()) {
            infoLabel.setText("Nama usaha wajib diisi.");
            return;
        }

        BusinessProfile profile = new BusinessProfile(
                businessName,
                safe(addressField.getText()),
                safe(ownerField.getText()),
                safe(phoneField.getText())
        );

        repository.upsert(profile);
        infoLabel.setText("Profil usaha berhasil disimpan.");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
