package com.laundrydesktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LaundryApp extends Application {
    @Override
    public void start(Stage stage) {
        MainShellView mainView = new MainShellView();
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(mainView.build(stage), 1200, 760);
        stage.setTitle("WashManager");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
