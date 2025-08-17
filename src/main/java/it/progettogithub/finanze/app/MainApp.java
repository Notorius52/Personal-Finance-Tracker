package it.progettogithub.finanze.app;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class MainApp extends Application {

    private Label incomeLabel = new Label();
    private Label expensesLabel = new Label();
    private Label balanceLabel = new Label();
    
    private Transaction currentlyEditing = null;

    @Override
    public void start(Stage primaryStage) {
        try {
            DatabaseManager.createNewTable();

            BorderPane root = new BorderPane();
            
            // --- BARRA DEI MENU ---
            MenuBar menuBar = new MenuBar();
            Menu fileMenu = new Menu("File");
            MenuItem exportItem = new MenuItem("Esporta CSV...");
            MenuItem printItem = new MenuItem("Stampa...");
            MenuItem exitItem = new MenuItem("Esci");
            fileMenu.getItems().addAll(exportItem, printItem, new SeparatorMenuItem(), exitItem);
            menuBar.getMenus().add(fileMenu);
            
            // --- TABELLA ---
            TableView<Transaction> transactionTable = new TableView<>();
            setupTableColumns(transactionTable);
            
            ObservableList<Transaction> data = DatabaseManager.loadTransactions();
            transactionTable.setItems(data);
            
            // --- PANNELLO INPUT ---
            DatePicker dateInput = new DatePicker(LocalDate.now());
            ComboBox<String> typeInput = new ComboBox<>();
            typeInput.getItems().addAll("Uscita", "Entrata");
            typeInput.setValue("Uscita");
            TextField descInput = new TextField();
            descInput.setPromptText("Descrizione");
            TextField amountInput = new TextField();
            amountInput.setPromptText("Importo");
            TextField categoryInput = new TextField();
            categoryInput.setPromptText("Categoria");
            Button addButton = new Button("Aggiungi");
            HBox inputBox = new HBox(10, new Label("Nuova:"), dateInput, typeInput, descInput, amountInput, categoryInput, addButton);
            inputBox.setPadding(new Insets(10));
            
            VBox topContainer = new VBox(menuBar, inputBox);
            
            // --- PANNELLO LATERALE ---
            Button editButton = new Button("Modifica Selezionato");
            Button deleteButton = new Button("Elimina Selezionato");
            VBox sideButtons = new VBox(10, editButton, deleteButton);
            sideButtons.setPadding(new Insets(0, 10, 0, 10));
            sideButtons.setAlignment(Pos.CENTER);
            
            // --- BARRA RIEPILOGO ---
            HBox summaryBox = new HBox(30);
            setupSummaryBox(summaryBox);

            // --- ASSEMBLAGGIO INTERFACCIA ---
            root.setTop(topContainer);
            root.setCenter(transactionTable);
            root.setRight(sideButtons);
            root.setBottom(summaryBox);
            
            // --- LOGICA PULSANTI E MENU ---
            
            // Logica Aggiungi / Aggiorna
            addButton.setOnAction(event -> {
                try {
                    double amount = Double.parseDouble(amountInput.getText());
                    if (typeInput.getValue().equals("Uscita") && amount > 0) amount *= -1;

                    if (currentlyEditing == null) {
                        Transaction newTransaction = new Transaction(dateInput.getValue(), descInput.getText(), amount, categoryInput.getText());
                        DatabaseManager.saveTransaction(newTransaction);
                    } else {
                        currentlyEditing.setDate(dateInput.getValue());
                        currentlyEditing.setDescription(descInput.getText());
                        currentlyEditing.setAmount(amount);
                        currentlyEditing.setCategory(categoryInput.getText());
                        DatabaseManager.updateTransaction(currentlyEditing);
                        currentlyEditing = null;
                        addButton.setText("Aggiungi");
                    }
                    data.setAll(DatabaseManager.loadTransactions());
                    clearInputFields(dateInput, typeInput, descInput, amountInput, categoryInput);
                } catch (NumberFormatException e) {
                    showAlert("Errore di Formato", "L'importo deve essere un numero valido.");
                }
            });

            // Logica Elimina
            deleteButton.setOnAction(event -> {
                Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    DatabaseManager.deleteTransaction(selected.getId());
                    data.remove(selected);
                } else {
                    showAlert("Nessuna Selezione", "Per favore, seleziona una transazione da eliminare.");
                }
            });

            // Logica Modifica
            editButton.setOnAction(event -> {
                Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    currentlyEditing = selected;
                    dateInput.setValue(selected.getDate());
                    descInput.setText(selected.getDescription());
                    categoryInput.setText(selected.getCategory());
                    if (selected.getAmount() < 0) {
                        typeInput.setValue("Uscita");
                        amountInput.setText(String.valueOf(selected.getAmount() * -1));
                    } else {
                        typeInput.setValue("Entrata");
                        amountInput.setText(String.valueOf(selected.getAmount()));
                    }
                    addButton.setText("Aggiorna");
                } else {
                    showAlert("Nessuna Selezione", "Per favore, seleziona una transazione da modificare.");
                }
            });
            
            // Logica Menu
            exportItem.setOnAction(event -> exportToCsv(primaryStage, data));
            printItem.setOnAction(event -> printTable(primaryStage, transactionTable));
            exitItem.setOnAction(event -> primaryStage.close());
            
            // Listener per aggiornamento automatico
            data.addListener((ListChangeListener<Transaction>) c -> updateSummary(data));
            updateSummary(data);

            // --- FASE FINALE ---
            Scene scene = new Scene(root, 950, 600);
            primaryStage.setTitle("Personal Finance Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    // ==================================================================
    // METODI DI UTILITÀ (QUELLI CHE MANCAVANO PRIMA)
    // ==================================================================
    
    private void updateSummary(ObservableList<Transaction> transactions) {
        double totalIncome = transactions.stream().filter(t -> t.getAmount() > 0).mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = transactions.stream().filter(t -> t.getAmount() < 0).mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome + totalExpenses;
        incomeLabel.setText(String.format("%.2f €", totalIncome));
        expensesLabel.setText(String.format("%.2f €", totalExpenses));
        balanceLabel.setText(String.format("%.2f €", balance));
    }
    
    @SuppressWarnings("unchecked")
    private void setupTableColumns(TableView<Transaction> table) {
        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<Transaction, String> descCol = new TableColumn<>("Descrizione");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Importo");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        
        amountCol.setCellFactory(column -> {
            return new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(String.format("%.2f €", item));
                        if (item < 0) {
                            setTextFill(Color.RED);
                        } else {
                            setTextFill(Color.GREEN);
                        }
                    }
                }
            };
        });

        TableColumn<Transaction, String> catCol = new TableColumn<>("Categoria");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        
        table.getColumns().addAll(dateCol, descCol, amountCol, catCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }
    
    private void setupSummaryBox(HBox summaryBox) {
        summaryBox.setPadding(new Insets(10));
        summaryBox.setAlignment(Pos.CENTER);
        incomeLabel.setTextFill(Color.GREEN);
        expensesLabel.setTextFill(Color.RED);
        balanceLabel.setFont(Font.font("System", 14));
        summaryBox.getChildren().addAll(new Label("Entrate Totali:"), incomeLabel, new Label("Uscite Totali:"), expensesLabel, new Label("Saldo:"), balanceLabel);
    }
    
    private void clearInputFields(DatePicker date, ComboBox<String> type, TextField... fields) {
        date.setValue(LocalDate.now());
        type.setValue("Uscita");
        for (TextField field : fields) field.clear();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void exportToCsv(Stage stage, ObservableList<Transaction> data) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Transazioni come CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Data,Descrizione,Importo,Categoria\n");
                for (Transaction t : data) {
                    writer.append(t.getDate().toString()).append(",");
                    writer.append("\"").append(t.getDescription()).append("\",");
                    writer.append(String.valueOf(t.getAmount())).append(",");
                    writer.append("\"").append(t.getCategory()).append("\"\n");
                }
            } catch (IOException e) {
                System.err.println("Errore durante il salvataggio del file CSV: " + e.getMessage());
            }
        }
    }
    
    private void printTable(Stage stage, TableView<Transaction> table) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(stage)) {
            boolean success = job.printPage(table);
            if (success) {
                job.endJob();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}