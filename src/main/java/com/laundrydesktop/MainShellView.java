package com.laundrydesktop;

import com.laundrydesktop.db.DatabaseInitializer;
import com.laundrydesktop.repo.UserRepository;
import com.laundrydesktop.view.CustomerView;
import com.laundrydesktop.view.DashboardView;
import com.laundrydesktop.view.BusinessProfileView;
import com.laundrydesktop.view.CashInReportView;
import com.laundrydesktop.view.JobListView;
import com.laundrydesktop.view.MasterItemView;
import com.laundrydesktop.view.PickupView;
import com.laundrydesktop.view.ServicePriceView;
import com.laundrydesktop.view.SpeedView;
import com.laundrydesktop.view.TransactionView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MainShellView {
    private final UserRepository userRepository = new UserRepository();
    private final TabPane mdiTabs = new TabPane();
    private final BusinessProfileView businessProfileView = new BusinessProfileView();
    private final Label authStatusLabel = new Label();
    private MenuItem loginLogoutMenuItem;
    private Menu masterDataMenu;
    private Menu transaksiMenu;
    private Menu laporanMenu;
    private Button pelangganToolbarButton;
    private Button penerimaanToolbarButton;
    private Button pengambilanToolbarButton;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean loggedIn;
    private int childCounter = 1;

    public Parent build(Stage stage) {
        new DatabaseInitializer().initialize();
        if (!showLoginDialog()) {
            throw new RuntimeException("Login gagal atau dibatalkan.");
        }

        BorderPane root = new BorderPane();
        root.setTop(new VBox(buildWindowBar(stage), buildMenuBar(stage), buildToolBar(stage)));
        root.setCenter(buildMdiArea());
        refreshAuthUiState();
        return root;
    }

    private Parent buildWindowBar(Stage stage) {
        Label title = new Label("WashManager");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minBtn = new Button("-");
        Button maxBtn = new Button("[ ]");
        Button closeBtn = new Button("X");
        styleWindowButton(minBtn);
        styleWindowButton(maxBtn);
        styleWindowButton(closeBtn);
        closeBtn.setStyle("-fx-background-color: #b42318; -fx-text-fill: white; -fx-font-size: 12px;");

        minBtn.setOnAction(e -> stage.setIconified(true));
        maxBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        closeBtn.setOnAction(e -> stage.close());

        HBox bar = new HBox(8, title, spacer, minBtn, maxBtn, closeBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 10, 8, 10));
        bar.setStyle("-fx-background-color: #1f2937;");

        bar.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });
        bar.setOnMouseDragged(e -> {
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - dragOffsetX);
                stage.setY(e.getScreenY() - dragOffsetY);
            }
        });

        return bar;
    }

    private Parent buildMenuBar(Stage mainStage) {
        Menu fileMenu = new Menu("File");
        loginLogoutMenuItem = new MenuItem("Login");
        MenuItem businessProfileItem = new MenuItem("Profil Usaha");
        MenuItem exitItem = new MenuItem("Exit");
        loginLogoutMenuItem.setOnAction(e -> handleMenuAction("Login/Logout", mainStage));
        businessProfileItem.setOnAction(e -> handleMenuAction("Profil Usaha", mainStage));
        exitItem.setOnAction(e -> handleMenuAction("Exit", mainStage));
        fileMenu.getItems().addAll(loginLogoutMenuItem, businessProfileItem, exitItem);

        masterDataMenu = new Menu("Master Data");
        MenuItem pelangganItem = new MenuItem("Pelanggan");
        MenuItem layananItem = new MenuItem("Layanan");
        MenuItem satuanItem = new MenuItem("Satuan");
        MenuItem kecepatanItem = new MenuItem("Kecepatan");
        pelangganItem.setOnAction(e -> handleMenuAction("Pelanggan", mainStage));
        layananItem.setOnAction(e -> handleMenuAction("Layanan", mainStage));
        satuanItem.setOnAction(e -> handleMenuAction("Satuan", mainStage));
        kecepatanItem.setOnAction(e -> handleMenuAction("Kecepatan", mainStage));
        masterDataMenu.getItems().addAll(pelangganItem, layananItem, satuanItem, kecepatanItem);

        transaksiMenu = new Menu("Transaksi");
        MenuItem hargaItem = new MenuItem("Setting harga layanan");
        MenuItem terimaItem = new MenuItem("Terima pekerjaan");
        MenuItem pengambilanItem = new MenuItem("Pengambilan");
        hargaItem.setOnAction(e -> handleMenuAction("Setting harga layanan", mainStage));
        terimaItem.setOnAction(e -> handleMenuAction("Terima pekerjaan", mainStage));
        pengambilanItem.setOnAction(e -> handleMenuAction("Pengambilan", mainStage));
        transaksiMenu.getItems().addAll(hargaItem, terimaItem, pengambilanItem);

        laporanMenu = new Menu("Laporan");
        MenuItem dashboardItem = new MenuItem("Dashboard");
        MenuItem daftarPekerjaanItem = new MenuItem("Daftar Pekerjaan");
        MenuItem kasMasukItem = new MenuItem("Kas Masuk");
        dashboardItem.setOnAction(e -> handleMenuAction("Dashboard", mainStage));
        daftarPekerjaanItem.setOnAction(e -> handleMenuAction("Daftar Pekerjaan", mainStage));
        kasMasukItem.setOnAction(e -> handleMenuAction("Kas Masuk", mainStage));
        laporanMenu.getItems().addAll(dashboardItem, daftarPekerjaanItem, kasMasukItem);

        MenuBar menuBar = new MenuBar(fileMenu, masterDataMenu, transaksiMenu, laporanMenu);
        return menuBar;
    }

    private Parent buildToolBar(Stage mainStage) {
        pelangganToolbarButton = new Button("Pelanggan");
        pelangganToolbarButton.setGraphic(createCustomerIcon());
        pelangganToolbarButton.setOnAction(e -> handleMenuAction("Pelanggan", mainStage));

        penerimaanToolbarButton = new Button("Penerimaan Pekerjaan");
        penerimaanToolbarButton.setGraphic(createReceiveIcon());
        penerimaanToolbarButton.setOnAction(e -> handleMenuAction("Terima pekerjaan", mainStage));

        pengambilanToolbarButton = new Button("Pengambilan");
        pengambilanToolbarButton.setGraphic(createPickupIcon());
        pengambilanToolbarButton.setOnAction(e -> handleMenuAction("Pengambilan", mainStage));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        ToolBar toolBar = new ToolBar(
                pelangganToolbarButton,
                penerimaanToolbarButton,
                pengambilanToolbarButton,
                spacer,
                authStatusLabel
        );
        return toolBar;
    }

    private Parent buildWelcomePanel() {
        VBox box = new VBox(8,
                new Label("Gunakan menu bar di bagian atas."),
                new Label("Setiap item menu membuka MDI child di dalam form utama.")
        );
        box.setPadding(new Insets(16));
        return box;
    }

    private Parent buildMdiArea() {
        mdiTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        Tab welcomeTab = new Tab("Beranda", buildWelcomePanel());
        welcomeTab.setClosable(false);
        mdiTabs.getTabs().setAll(welcomeTab);
        return mdiTabs;
    }

    private void handleMenuAction(String menu, Stage mainStage) {
        switch (menu) {
            case "Login/Logout" -> toggleLogin();
            case "Exit" -> mainStage.close();
            case "Profil Usaha" -> {
                if (ensureLoggedIn()) openMdiChild("Profil Usaha", businessProfileView.build());
            }
            case "Pelanggan" -> {
                if (ensureLoggedIn()) openMdiChild("Pelanggan", new CustomerView().build());
            }
            case "Layanan" -> {
                if (ensureLoggedIn()) openMdiChild("Master Layanan", new MasterItemView("services", "Master Layanan", null).build());
            }
            case "Satuan" -> {
                if (ensureLoggedIn()) openMdiChild("Master Satuan", new MasterItemView("units", "Master Satuan", null).build());
            }
            case "Kecepatan" -> {
                if (ensureLoggedIn()) openMdiChild("Master Kecepatan", new SpeedView(null).build());
            }
            case "Setting harga layanan" -> {
                if (ensureLoggedIn()) openMdiChild("Setting Harga Layanan", new ServicePriceView(null).build());
            }
            case "Terima pekerjaan" -> {
                if (ensureLoggedIn()) openMdiChild("Terima Pekerjaan", new TransactionView(null).build());
            }
            case "Pengambilan" -> {
                if (ensureLoggedIn()) openMdiChild("Pengambilan", new PickupView().build());
            }
            case "Dashboard" -> {
                if (ensureLoggedIn()) openMdiChild("Dashboard", new DashboardView().build());
            }
            case "Daftar Pekerjaan" -> {
                if (ensureLoggedIn()) openMdiChild("Daftar Pekerjaan", new JobListView().build());
            }
            case "Kas Masuk" -> {
                if (ensureLoggedIn()) openMdiChild("Kas Masuk", new CashInReportView().build());
            }
            default -> {
            }
        }
    }

    private void openMdiChild(String title, Parent content) {
        Tab tab = new Tab(title + " #" + childCounter++, content);
        tab.setClosable(true);
        mdiTabs.getTabs().add(tab);
        mdiTabs.getSelectionModel().select(tab);
    }

    private boolean ensureLoggedIn() {
        if (loggedIn) {
            return true;
        }
        return showLoginDialog();
    }

    private void toggleLogin() {
        if (loggedIn) {
            loggedIn = false;
            mdiTabs.getTabs().removeIf(Tab::isClosable);
            refreshAuthUiState();
            return;
        }
        showLoginDialog();
    }

    private void styleWindowButton(Button button) {
        button.setPrefSize(30, 24);
        button.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-size: 12px;");
    }

    private Parent createCustomerIcon() {
        Circle head = new Circle(4.5, Color.web("#0f766e"));
        head.setTranslateY(-4);
        Rectangle body = new Rectangle(12, 8, Color.web("#14b8a6"));
        body.setArcWidth(5);
        body.setArcHeight(5);
        body.setTranslateY(4);
        return new StackPane(body, head);
    }

    private Parent createReceiveIcon() {
        Rectangle paper = new Rectangle(12, 14, Color.web("#2563eb"));
        paper.setArcWidth(2);
        paper.setArcHeight(2);
        Rectangle line1 = new Rectangle(8, 1.5, Color.WHITE);
        Rectangle line2 = new Rectangle(8, 1.5, Color.WHITE);
        line1.setTranslateY(-3);
        line2.setTranslateY(1);
        return new StackPane(paper, line1, line2);
    }

    private Parent createPickupIcon() {
        Rectangle box = new Rectangle(13, 10, Color.web("#ca8a04"));
        box.setArcWidth(2);
        box.setArcHeight(2);
        Rectangle lid = new Rectangle(13, 2.5, Color.web("#f59e0b"));
        lid.setTranslateY(-4);
        return new StackPane(box, lid);
    }

    private boolean showLoginDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Masuk sebagai pengguna sistem");

        ButtonType loginBtn = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField userField = new TextField("admin");
        PasswordField passField = new PasswordField();
        passField.setText("admin123");

        grid.addRow(0, new Label("Username"), userField);
        grid.addRow(1, new Label("Password"), passField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == loginBtn && userRepository.authenticate(userField.getText(), passField.getText()));

        Boolean ok = dialog.showAndWait().orElse(false);
        if (!ok) {
            loggedIn = false;
            refreshAuthUiState();
            return false;
        }
        loggedIn = true;
        refreshAuthUiState();
        return true;
    }

    private void refreshAuthUiState() {
        if (loginLogoutMenuItem != null) {
            loginLogoutMenuItem.setText(loggedIn ? "Logout" : "Login");
        }
        if (masterDataMenu != null) {
            masterDataMenu.setDisable(!loggedIn);
        }
        if (transaksiMenu != null) {
            transaksiMenu.setDisable(!loggedIn);
        }
        if (laporanMenu != null) {
            laporanMenu.setDisable(!loggedIn);
        }
        if (pelangganToolbarButton != null) {
            pelangganToolbarButton.setDisable(!loggedIn);
        }
        if (penerimaanToolbarButton != null) {
            penerimaanToolbarButton.setDisable(!loggedIn);
        }
        if (pengambilanToolbarButton != null) {
            pengambilanToolbarButton.setDisable(!loggedIn);
        }
        authStatusLabel.setText(loggedIn ? "Status: Login" : "Status: Logout");
    }
}
