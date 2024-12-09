package lenderjava;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button btnlogin;

    @FXML
    private Text messageText;

    private static String loggedInUserEmail;
    private static String loggedInUsername;

    @FXML
    public void initialize() {
        btnlogin.setOnAction(event -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageText.setText("Email dan password harus diisi.");
            return;
        }

        if (!isValidEmail(email)) {
            messageText.setText("Format email tidak valid.");
            return;
        }

        if (password.length() < 8) {
            messageText.setText("Password harus minimal 8 karakter.");
            return;
        }

        if (authenticateUser(email, password)) {
            loggedInUserEmail = email;

            messageText.setText("Login berhasil!");

            SceneManager.switchScene("/view/Peminjaman.fxml");
        } else {
            messageText.setText("Email atau password salah.");
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\."
                + "[a-zA-Z0-9_+&*-]+)*@"
                + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
                + "A-Z]{2,7}$";
        return email.matches(regex);
    }

    private boolean authenticateUser(String email, String password) {
        String query = "SELECT username FROM user WHERE email = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                loggedInUserEmail = email;
                loggedInUsername = resultSet.getString("username");
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error during authentication: " + e.getMessage());
        }
        return false;
    }

    public static String getLoggedInUserEmail() {
        return loggedInUserEmail;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void logout() {
        loggedInUserEmail = null;
        loggedInUsername = null;
        System.out.println("User telah logout dan data telah dibersihkan.");
    }
}
