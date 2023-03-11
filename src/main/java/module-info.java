module com.markokroselj.uv_dn2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires FXTrayIcon;
    requires java.scripting;
    requires java.desktop;
    requires java.prefs;


    opens com.markokroselj.uv_dn2 to javafx.fxml;
    exports com.markokroselj.uv_dn2;
}