package lenderjava;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws SQLException {

        // Log koneksi database
        if (DatabaseConnection.getConnection() != null) {
            System.out.println("Koneksi database berhasil");
        }

        // Set primaryStage ke SceneManager
        SceneManager.setStage(primaryStage);

        // Panggil scene awal
        SceneManager.switchScene("/view/Peminjaman.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
