package lenderjava;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

public class ManagementPeminjamanController {

    @FXML
    private TableView<Peminjaman> tblPeminjamanManagement;

    @FXML
    private Button RefreshData;

    @FXML
    private TextField fltrNJ;
    @FXML
    private DatePicker fltrPeminjaman;
    @FXML
    private DatePicker fltrPengembalian;

    @FXML
    private Button prntPeminjamanManagement;

    @FXML
    private Button btnLogout;
    @FXML
    private ComboBox<String> fltrBulanPeminjaman;

    private ObservableList<Peminjaman> peminjamanList = FXCollections.observableArrayList();
    private FilteredList<Peminjaman> filteredData;

    @FXML
    public void refreshData() {
        peminjamanList.clear();
        loadPeminjamanData();
        fltrNJ.clear();
        fltrPeminjaman.setValue(null);
        fltrPengembalian.setValue(null);
    }

    public void initialize() {
        setupTableColumns();
        loadPeminjamanData();

        for (Month month : Month.values()) {
            fltrBulanPeminjaman.getItems().add(month.name());
        }

        btnLogout.setOnAction(event -> {
            LoginController.logout();
            SceneManager.switchScene("/view/Login.fxml");
        });

        fltrBulanPeminjaman.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Month selectedMonth = Month.valueOf(newValue);
                int currentYear = YearMonth.now().getYear();
                filteredData.setPredicate(peminjaman -> {
                    LocalDate tanggalPeminjaman = peminjaman.getTanggalPeminjaman();
                    return tanggalPeminjaman.getYear() == currentYear && tanggalPeminjaman.getMonth() == selectedMonth;
                });
            } else {
                filteredData.setPredicate(p -> true);
            }
        });

        RefreshData.setOnAction(event -> refreshData());
        prntPeminjamanManagement.setOnAction(event -> {
            try {
                printToPDF();
            } catch (IOException e) {
                System.out.println("Error printing to PDF: " + e.getMessage());
            }
        });

        filteredData = new FilteredList<>(peminjamanList, p -> true);
        tblPeminjamanManagement.setItems(filteredData);

        fltrNJ.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(peminjaman -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return peminjaman.getNamaLengkap().toLowerCase().contains(lowerCaseFilter) ||
                       peminjaman.getJudulBuku().toLowerCase().contains(lowerCaseFilter);
            });
        });

        fltrPeminjaman.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(peminjaman -> {
                if (newValue == null) {
                    return true;
                }
                return peminjaman.getTanggalPeminjaman().equals(newValue);
            });
        });

        fltrPengembalian.valueProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(peminjaman -> {
                if (newValue == null) {
                    return true;
                }
                return peminjaman.getTanggalPengembalian().equals(newValue);
            });
        });
    }

    private void setupTableColumns() {
        TableColumn<Peminjaman, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Peminjaman, String> namaLengkapColumn = new TableColumn<>("Nama Lengkap");
        namaLengkapColumn.setCellValueFactory(new PropertyValueFactory<>("namaLengkap"));

        TableColumn<Peminjaman, String> judulBukuColumn = new TableColumn<>("Judul Buku");
        judulBukuColumn.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));

        TableColumn<Peminjaman, LocalDate> tanggalPeminjamanColumn = new TableColumn<>("Tanggal Peminjaman");
        tanggalPeminjamanColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalPeminjaman"));

        TableColumn<Peminjaman, LocalDate> tanggalPengembalianColumn = new TableColumn<>("Tanggal Pengembalian");
        tanggalPengembalianColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalPengembalian"));

        TableColumn<Peminjaman, Integer> qtyColumn = new TableColumn<>("Qty");
        qtyColumn.setCellValueFactory(new PropertyValueFactory<>("qty"));

        TableColumn<Peminjaman, String> createByColumn = new TableColumn<>("Create By");
        createByColumn.setCellValueFactory(new PropertyValueFactory<>("createBy"));

        TableColumn<Peminjaman, Integer> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblPeminjamanManagement.getColumns().addAll(idColumn, namaLengkapColumn, judulBukuColumn, tanggalPeminjamanColumn, tanggalPengembalianColumn, qtyColumn, createByColumn, statusColumn);
        tblPeminjamanManagement.setItems(peminjamanList);
    }

    private void loadPeminjamanData() {
        String query = "SELECT p.id, p.id_member, m.nama_lengkap, p.id_buku, b.judul, p.tanggal_peminjaman, p.tanggal_pengembalian, p.qty, p.create_by, p.status " +
                       "FROM peminjaman p " +
                       "JOIN member m ON p.id_member = m.id " +
                       "JOIN buku b ON p.id_buku = b.id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int idMember = resultSet.getInt("id_member");
                String namaLengkap = resultSet.getString("nama_lengkap");
                int idBuku = resultSet.getInt("id_buku");
                String judulBuku = resultSet.getString("judul");
                LocalDate tanggalPeminjaman = resultSet.getDate("tanggal_peminjaman").toLocalDate();
                LocalDate tanggalPengembalian = resultSet.getDate("tanggal_pengembalian").toLocalDate();
                int qty = resultSet.getInt("qty");
                String createBy = resultSet.getString("create_by");
                int status = resultSet.getInt("status");

                Peminjaman peminjaman = new Peminjaman(id, idMember, namaLengkap, idBuku, judulBuku, tanggalPeminjaman, tanggalPengembalian, qty, createBy, status);
                peminjamanList.add(peminjaman);
            }
        } catch (SQLException e) {
            System.out.println("Error loading peminjaman data: " + e.getMessage());
        }
    }
    private void printToPDF() throws IOException {
        if (filteredData.isEmpty()) {
            System.out.println("Tidak ada data untuk dicetak.");
            return;
        }

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 700);

            contentStream.showText("Peminjaman Management Report");
            contentStream.newLine();
            contentStream.newLine();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.showText(String.format("%-5s | %-20s | %-20s | %-15s | %-15s | %-5s", "ID", "Nama", "Judul", "Tgl Peminjaman", "Tgl Pengembalian", "Qty"));
            contentStream.newLine();
            contentStream.newLine();

            int lineCount = 0;

            for (Peminjaman peminjaman : filteredData) {
                if (lineCount >= 45) {
                    contentStream.endText();
                    contentStream.close();

                    page = new PDPage();
                    document.addPage(page);
                    try (PDPageContentStream newContentStream = new PDPageContentStream(document, page)) {
                        newContentStream.setFont(PDType1Font.HELVETICA, 10);
                        newContentStream.beginText();
                        newContentStream.setLeading(14.5f);
                        newContentStream.newLineAtOffset(25, 700);
                        lineCount = 0;
                    }
                }

                contentStream.showText(String.format("%-5d | %-20s | %-20s | %-15s | %-15s | %-5d",
                        peminjaman.getId(),
                        peminjaman.getNamaLengkap(),
                        peminjaman.getJudulBuku(),
                        peminjaman.getTanggalPeminjaman(),
                        peminjaman.getTanggalPengembalian(),
                        peminjaman.getQty()));
                contentStream.newLine();
                lineCount++;
            }

            contentStream.endText();
        }

        String userHome = System.getProperty("user.home");
        document.save(userHome + "/Documents/PeminjamanManagementReport.pdf");
        document.close();
        System.out.println("PDF berhasil disimpan di: " + userHome + "/Documents/PeminjamanManagementReport.pdf");
    }
    
    public class Peminjaman {
        private int id;
        private int idMember;
        private String namaLengkap;
        private int idBuku;
        private String judulBuku;
        private LocalDate tanggalPeminjaman;
        private LocalDate tanggalPengembalian;
        private int qty;
        private String createBy;
        private int status;

        public Peminjaman(int id, int idMember, String namaLengkap, int idBuku, String judulBuku, LocalDate tanggalPeminjaman, LocalDate tanggalPengembalian, int qty, String createBy, int status) {
            this.id = id;
            this.idMember = idMember;
            this.namaLengkap = namaLengkap;
            this.idBuku = idBuku;
            this.judulBuku = judulBuku;
            this.tanggalPeminjaman = tanggalPeminjaman;
            this.tanggalPengembalian = tanggalPengembalian;
            this.qty = qty;
            this.createBy = createBy;
            this.status = status;
        }

        public int getId() { return id; }
        public int getIdMember() { return idMember; }
        public String getNamaLengkap() { return namaLengkap; }
        public int getIdBuku() { return idBuku; }
        public String getJudulBuku() { return judulBuku; }
        public LocalDate getTanggalPeminjaman() { return tanggalPeminjaman; }
        public LocalDate getTanggalPengembalian() { return tanggalPengembalian; }
        public int getQty() { return qty; }
        public String getCreateBy() { return createBy; }
        public int getStatus() { return status; }
    }
}
