package lenderjava;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML
    private Label pesanKesalahan;

    @FXML
    private TextField tfSrc;

    @FXML
    private ListView<String> lvSrc;

    @FXML
    private Button btnSrc;

    @FXML
    private TextField fxSrcMember;

    @FXML
    private ListView<String> lvSrcMember;

    @FXML
    private Button btnSrcMember;

    @FXML
    private TextField qty;

    @FXML
    private DatePicker tglPengembalian;

    @FXML
    private TableView<PeminjamanSementara> tblPeminjamanSementara;

    @FXML
    private Button konfirmasiPeminjaman;

    private List<String> namaMember = new ArrayList<>();
    private List<String> emailMember = new ArrayList<>();
    private List<String> judulBuku = new ArrayList<>();

    // Inner class for PeminjamanSementara
    public static class PeminjamanSementara {
        private String member;
        private String book;
        private int qty;
        private LocalDate tanggalPeminjaman;
        private LocalDate tanggalPengembalian;

        public PeminjamanSementara(String member, String book, int qty, LocalDate tanggalPeminjaman, LocalDate tanggalPengembalian) {
            this.member = member;
            this.book = book;
            this.qty = qty;
            this.tanggalPeminjaman = tanggalPeminjaman;
            this.tanggalPengembalian = tanggalPengembalian;
        }

        public String getMember() {
            return member;
        }

        public String getBook() {
            return book;
        }

        public int getQty() {
            return qty;
        }

        public LocalDate getTanggalPeminjaman() {
            return tanggalPeminjaman;
        }

        public LocalDate getTanggalPengembalian() {
            return tanggalPengembalian;
        }
    }

    @FXML
    public void initialize() {
        loadBookTitlesFromDatabase();

        loadMemberNamesAndEmailsFromDatabase();

        btnSrc.setOnAction(event -> searchBooks());

        btnSrcMember.setOnAction(event -> searchMembers());

        tglPengembalian.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        tglPengembalian.setValue(LocalDate.now().plusDays(1));
    }

    private void loadBookTitlesFromDatabase() {
        String query = "SELECT judul FROM buku";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                judulBuku.add(resultSet.getString("judul"));
                lvSrc.getItems().add(resultSet.getString("judul"));
            }
        } catch (SQLException e) {
            System.out.println("Error loading buku: " + e.getMessage());
        }
    }

    private void loadMemberNamesAndEmailsFromDatabase() {
        String query = "SELECT nama_lengkap, email FROM member";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                namaMember.add(resultSet.getString("nama_lengkap"));
                emailMember.add(resultSet.getString("email"));
                lvSrcMember.getItems().add(resultSet.getString("nama_lengkap") + " (" + resultSet.getString("email") + ")");
            }
        } catch (SQLException e) {
            System.out.println("Error loading member: " + e.getMessage());
        }
    }

    private void searchBooks() {
        String query = tfSrc.getText().toLowerCase();
        lvSrc.getItems().clear();

        for (String judul : judulBuku) {
            if (judul.toLowerCase().contains(query)) {
                lvSrc.getItems().add(judul);
            }
        }
    }

    private void searchMembers() {
        String query = fxSrcMember.getText().toLowerCase();
        lvSrcMember.getItems().clear();

        for (int i = 0; i < namaMember.size(); i++) {
            String nama = namaMember.get(i);
            String email = emailMember.get(i);
            if (nama.toLowerCase().contains(query) || email.toLowerCase().contains(query)) {
                lvSrcMember.getItems().add(nama + " (" + email + ")");
            }
        }
    }

    @FXML
    private void checkQty() {
        int qtyValue = Integer.parseInt(qty.getText());
        if (qtyValue < 1 || qtyValue > 3) {
            qty.setText("1");
        }
    }

    @FXML
    private void addToPeminjamanSementara() {
        String selectedMember = lvSrcMember.getSelectionModel().getSelectedItem();
        String selectedBook = lvSrc.getSelectionModel().getSelectedItem();
        int quantity = Integer.parseInt(qty.getText());
        LocalDate returnDate = tglPengembalian.getValue();

        if (selectedMember != null && selectedBook != null && quantity > 0) {
            PeminjamanSementara peminjaman = new PeminjamanSementara(selectedMember, selectedBook, quantity, LocalDate.now(), returnDate);
            tblPeminjamanSementara.getItems().add(peminjaman);
        } else {
            pesanKesalahan.setText("Please select a member and a book, and ensure quantity is valid.");
        }
    }

    @FXML
    private void confirmPeminjaman() {
        for (PeminjamanSementara peminjaman : tblPeminjamanSementara.getItems()) {
            savePeminjamanToDatabase(peminjaman);
        }
        tblPeminjamanSementara.getItems().clear();
    }

    private void savePeminjamanToDatabase(PeminjamanSementara peminjaman) {
        String query = "INSERT INTO peminjaman (id_member, id_buku, qty, tanggal_peminjaman, tanggal_pengembalian) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, getIdFromMember(peminjaman.getMember()));
            preparedStatement.setInt(2, getIdFromBook(peminjaman.getBook()));
            preparedStatement.setInt(3, peminjaman.getQty());
            preparedStatement.setDate(4, java.sql.Date.valueOf(peminjaman.getTanggalPeminjaman()));
            preparedStatement.setDate(5, java.sql.Date.valueOf(peminjaman.getTanggalPengembalian()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving peminjaman: " + e.getMessage());
        }
    }

    private int getIdFromMember(String member) {
        // Implement logic to extract ID from member string
        return 0; // Placeholder
    }

    private int getIdFromBook(String book) {
        // Implement logic to extract ID from book string
        return 0; // Placeholder
    }
}