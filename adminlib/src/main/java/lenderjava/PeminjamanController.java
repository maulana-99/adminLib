package lenderjava;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PeminjamanController {
    @FXML
    private TextField srcMemberPeminjaman;

    @FXML 
    private Button btnSrcMemberPeminjaman;
    
    @FXML
    private TableView<Member> tblSrcMember;

    @FXML
    private TextField srcBukuPeminjaman;

    @FXML
    private Button btnSrcBukuPeminjaman;

    @FXML
    private TableView<Buku> tblSrcBuku;

    @FXML
    private TextField dataMember;

    @FXML
    private TextField dataBuku;

    private ObservableList<Member> memberList = FXCollections.observableArrayList();
    private ObservableList<Buku> bukuList = FXCollections.observableArrayList();

    private int selectedMemberId;
    private int selectedBukuId;

    @FXML
    public void initialize() {
        btnSrcMemberPeminjaman.setOnAction(event -> searchMember());
        btnSrcBukuPeminjaman.setOnAction(event -> searchBuku());

        // Set up the member table columns
        TableColumn<Member, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Member, String> nameColumn = new TableColumn<>("Nama Lengkap");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));

        TableColumn<Member, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        tblSrcMember.getColumns().addAll(idColumn, nameColumn, emailColumn);
        tblSrcMember.setItems(memberList);

        // Set up the book table columns
        TableColumn<Buku, Integer> bukuIdColumn = new TableColumn<>("ID");
        bukuIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Buku, String> judulColumn = new TableColumn<>("Judul");
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judul"));

        TableColumn<Buku, String> pengarangColumn = new TableColumn<>("Pengarang");
        pengarangColumn.setCellValueFactory(new PropertyValueFactory<>("pengarang"));

        tblSrcBuku.getColumns().addAll(bukuIdColumn, judulColumn, pengarangColumn);
        tblSrcBuku.setItems(bukuList);

        // Load all members and books initially
        loadAllMembers();
        loadAllBooks();

        // Add listener for member selection
        tblSrcMember.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Member>() {
            @Override
            public void changed(ObservableValue<? extends Member> observable, Member oldValue, Member newValue) {
                if (newValue != null) {
                    selectedMemberId = newValue.getId();
                    dataMember.setText(newValue.getNamaLengkap());
                }
            }
        });

        // Add listener for book selection
        tblSrcBuku.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Buku>() {
            @Override
            public void changed(ObservableValue<? extends Buku> observable, Buku oldValue, Buku newValue) {
                if (newValue != null) {
                    selectedBukuId = newValue.getId();
                    dataBuku.setText(newValue.getJudul());
                }
            }
        });
    }

    private void loadAllMembers() {
        memberList.clear();
        String query = "SELECT id, nama_lengkap, email FROM member";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String namaLengkap = resultSet.getString("nama_lengkap");
                String email = resultSet.getString("email");
                memberList.add(new Member(id, namaLengkap, email));
            }
        } catch (SQLException e) {
            System.out.println("Error loading members: " + e.getMessage());
        }
    }

    private void loadAllBooks() {
        bukuList.clear();
        String query = "SELECT id, judul, pengarang FROM buku";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String judul = resultSet.getString("judul");
                String pengarang = resultSet.getString("pengarang");
                bukuList.add(new Buku(id, judul, pengarang));
            }
        } catch (SQLException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    private void searchMember() {
        String searchText = srcMemberPeminjaman.getText().toLowerCase();
        memberList.clear();

        String query = "SELECT id, nama_lengkap, email FROM member WHERE LOWER(nama_lengkap) LIKE ? OR LOWER(email) LIKE ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, "%" + searchText + "%");
            preparedStatement.setString(2, "%" + searchText + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String namaLengkap = resultSet.getString("nama_lengkap");
                String email = resultSet.getString("email");
                memberList.add(new Member(id, namaLengkap, email));
            }
        } catch (SQLException e) {
            System.out.println("Error searching member: " + e.getMessage());
        }
    }

    private void searchBuku() {
        String searchText = srcBukuPeminjaman.getText().toLowerCase();
        bukuList.clear();

        String query = "SELECT id, judul, pengarang FROM buku WHERE LOWER(judul) LIKE ? OR LOWER(pengarang) LIKE ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, "%" + searchText + "%");
            preparedStatement.setString(2, "%" + searchText + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String judul = resultSet.getString("judul");
                String pengarang = resultSet.getString("pengarang");
                bukuList.add(new Buku(id, judul, pengarang));
            }
        } catch (SQLException e) {
            System.out.println("Error searching book: " + e.getMessage());
        }
    }

    public static class Member {
        private int id;
        private String namaLengkap;
        private String email;

        public Member(int id, String namaLengkap, String email) {
            this.id = id;
            this.namaLengkap = namaLengkap;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getNamaLengkap() {
            return namaLengkap;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class Buku {
        private int id;
        private String judul;
        private String pengarang;

        public Buku(int id, String judul, String pengarang) {
            this.id = id;
            this.judul = judul;
            this.pengarang = pengarang;
        }

        public int getId() {
            return id;
        }

        public String getJudul() {
            return judul;
        }

        public String getPengarang() {
            return pengarang;
        }
    }
}
