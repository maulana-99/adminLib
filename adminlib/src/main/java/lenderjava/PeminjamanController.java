package lenderjava;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.DateCell;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

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

    @FXML
    private TextField dataQtyBuku;

    @FXML
    private DatePicker dataPengembalian;

    @FXML
    private Button Pinjam;

    @FXML
    private Button hapusPeminjaman;

    @FXML
    private Button konfimasiPeminjaman;

    @FXML
    private TableView<Peminjaman> tblPeminjaman;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lError;

    private ObservableList<Member> memberList = FXCollections.observableArrayList();
    private ObservableList<Buku> bukuList = FXCollections.observableArrayList();
    private ObservableList<Peminjaman> peminjamanList = FXCollections.observableArrayList();

    private int selectedMemberId;
    private int selectedBukuId;

    @FXML
    public void initialize() {
        btnSrcMemberPeminjaman.setOnAction(event -> searchMember());
        btnSrcBukuPeminjaman.setOnAction(event -> searchBuku());
        Pinjam.setOnAction(event -> addPeminjaman());
        hapusPeminjaman.setOnAction(event -> deletePeminjaman());
        konfimasiPeminjaman.setOnAction(event -> confirmPeminjaman());

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

        TableColumn<Buku, Integer> stockColumn = new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));

        tblSrcBuku.getColumns().addAll(bukuIdColumn, judulColumn, pengarangColumn, stockColumn);
        tblSrcBuku.setItems(bukuList);

        // Set up the peminjaman table columns
        TableColumn<Peminjaman, Integer> peminjamanMemberIdColumn = new TableColumn<>("Member ID");
        peminjamanMemberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));

        TableColumn<Peminjaman, String> peminjamanMemberNameColumn = new TableColumn<>("Nama Member");
        peminjamanMemberNameColumn.setCellValueFactory(new PropertyValueFactory<>("namaMember"));

        TableColumn<Peminjaman, Integer> peminjamanBukuIdColumn = new TableColumn<>("Buku ID");
        peminjamanBukuIdColumn.setCellValueFactory(new PropertyValueFactory<>("bukuId"));

        TableColumn<Peminjaman, String> peminjamanBukuTitleColumn = new TableColumn<>("Judul Buku");
        peminjamanBukuTitleColumn.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));

        TableColumn<Peminjaman, Integer> peminjamanQtyColumn = new TableColumn<>("Qty");
        peminjamanQtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));

        tblPeminjaman.getColumns().addAll(peminjamanMemberIdColumn, peminjamanMemberNameColumn, peminjamanBukuIdColumn, peminjamanBukuTitleColumn, peminjamanQtyColumn);
        tblPeminjaman.setItems(peminjamanList);

        // Load all members and books initially
        loadAllMembers();
        loadAllBooks();

        btnLogout.setOnAction(event -> {
            LoginController.logout();
            SceneManager.switchScene("/view/Login.fxml");
        });

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

        // Set initial value and add listener for dataQtyBuku
        dataQtyBuku.setText("1");
        dataQtyBuku.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dataQtyBuku.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (!dataQtyBuku.getText().isEmpty()) {
                int value = Integer.parseInt(dataQtyBuku.getText());
                if (value < 1) {
                    dataQtyBuku.setText("1");
                } else if (value > 3) {
                    dataQtyBuku.setText("3");
                }
            }
        });

        // Set initial date for dataPengembalian to today
        dataPengembalian.setValue(LocalDate.now());

        // Restrict date selection to today and future dates
        dataPengembalian.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #EEEEEE;");
                        }
                    }
                };
            }
        });
    }

    private void addPeminjaman() {
        String namaMember = dataMember.getText();
        String judulBuku = dataBuku.getText();
        int qty = Integer.parseInt(dataQtyBuku.getText());

        if (!namaMember.isEmpty() && !judulBuku.isEmpty() && qty > 0) {
            Peminjaman peminjaman = new Peminjaman(selectedMemberId, namaMember, selectedBukuId, judulBuku, qty);
            peminjamanList.add(peminjaman);
        }
    }

    private void deletePeminjaman() {
        Peminjaman selectedPeminjaman = tblPeminjaman.getSelectionModel().getSelectedItem();
        if (selectedPeminjaman != null) {
            peminjamanList.remove(selectedPeminjaman);
        }
    }

    private void confirmPeminjaman() {
        String query = "INSERT INTO peminjaman (id_member, id_buku, tanggal_peminjaman, tanggal_pengembalian, qty, create_by) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (Peminjaman peminjaman : peminjamanList) {
                preparedStatement.setInt(1, peminjaman.getMemberId());
                preparedStatement.setInt(2, peminjaman.getBukuId());
                preparedStatement.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                preparedStatement.setDate(4, java.sql.Date.valueOf(dataPengembalian.getValue()));
                preparedStatement.setInt(5, peminjaman.getQty());
                preparedStatement.setString(6, LoginController.getLoggedInUserEmail());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            peminjamanList.clear();
            lError.setText("");
        } catch (SQLException e) {
            lError.setText("Error confirming peminjaman: " + e.getMessage());
        }
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
        String query = "SELECT id, judul, pengarang, stock FROM buku WHERE stock > 0";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String judul = resultSet.getString("judul");
                String pengarang = resultSet.getString("pengarang");
                int stock = resultSet.getInt("stock");
                bukuList.add(new Buku(id, judul, pengarang, stock));
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

        String query = "SELECT id, judul, pengarang, stock FROM buku WHERE LOWER(judul) LIKE ? OR LOWER(pengarang) LIKE ? ";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, "%" + searchText + "%");
            preparedStatement.setString(2, "%" + searchText + "%");

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String judul = resultSet.getString("judul");
                String pengarang = resultSet.getString("pengarang");
                int stock = resultSet.getInt("stock");
                bukuList.add(new Buku(id, judul, pengarang, stock));
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
        private int stock;

        public Buku(int id, String judul, String pengarang, int stock) {
            this.id = id;
            this.judul = judul;
            this.pengarang = pengarang;
            this.stock = stock;
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

        public int getStock() {
            return stock;
        }
    }

    public static class Peminjaman {
        private int memberId;
        private String namaMember;
        private int bukuId;
        private String judulBuku;
        private int qty;

        public Peminjaman(int memberId, String namaMember, int bukuId, String judulBuku, int qty) {
            this.memberId = memberId;
            this.namaMember = namaMember;
            this.bukuId = bukuId;
            this.judulBuku = judulBuku;
            this.qty = qty;
        }

        public int getMemberId() {
            return memberId;
        }

        public String getNamaMember() {
            return namaMember;
        }

        public int getBukuId() {
            return bukuId;
        }

        public String getJudulBuku() {
            return judulBuku;
        }

        public int getQty() {
            return qty;
        }
    }
}
