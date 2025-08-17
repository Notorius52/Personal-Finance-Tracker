package it.progettogithub.finanze.app;

import java.time.LocalDate;

public class Transaction {
    private int id; // NUOVO: Campo per l'ID univoco dal database
    private LocalDate date;
    private String description;
    private double amount;
    private String category;
    
    // Costruttore per nuove transazioni (l'ID verrà generato dal DB)
    public Transaction(LocalDate date, String description, double amount, String category) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.category = category;
    }

    // NUOVO: Costruttore per transazioni caricate dal DB (che hanno già un ID)
    public Transaction(int id, LocalDate date, String description, double amount, String category) {
        this(date, description, amount, category);
        this.id = id;
    }

    // Getters
    public int getId() { return id; } // NUOVO
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }

    // Setters
    public void setDate(LocalDate date) { this.date = date; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
}