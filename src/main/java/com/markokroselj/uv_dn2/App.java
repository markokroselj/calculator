package com.markokroselj.uv_dn2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

public class App extends Application {
    public static final String WINDOW_PREFS_KEY = "window preferences";

    @Override
    public void start(Stage stage) throws IOException {
        //Load the position and if it was maximized from registry
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        AtomicReference<String> windowPrefs = new AtomicReference<>(prefs.get(WINDOW_PREFS_KEY, null));
        double x, y;
        boolean maximized;
        if (windowPrefs.get() != null) {
            String[] parts = windowPrefs.get().split(",");
            if (parts.length > 2) {
                x = Double.parseDouble(parts[0]);
                y = Double.parseDouble(parts[1]);
                maximized = Boolean.parseBoolean(parts[2]);
                if (x >= 0 && y >= 0) {
                    stage.setX(x);
                    stage.setY(y);

                    stage.setMaximized(maximized);

                }
            }
        }

        //Get the default position value
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (primScreenBounds.getWidth() - 600) / 2;
        double centerY = (primScreenBounds.getHeight() - 450) / 2;

        //Save the window position and if it is maximized to  preferences when the window is closed
        stage.setOnCloseRequest(event -> {
            if (!stage.isMaximized())
                windowPrefs.set(stage.getX() + "," + stage.getY() + "," + false);
            else
                windowPrefs.set(centerX + "," + centerY + "," + true);
            prefs.put(WINDOW_PREFS_KEY, windowPrefs.get());
        });

        stage.setOpacity(0);
        stage.getIcons().add(new Image(String.valueOf(App.class.getResource("images/icon.png"))));
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 585);
        scene.getStylesheets().add(String.valueOf(App.class.getResource("style.css")));
        scene.getStylesheets().add(String.valueOf(App.class.getResource("themes/dark.css")));

        stage.setTitle("Kalkulator");
        stage.setScene(scene);
        stage.show();
        Thread test = new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> stage.setOpacity(1));
        });
        test.start();


    }

    public static void main(String[] args) {
        launch();
    }
}