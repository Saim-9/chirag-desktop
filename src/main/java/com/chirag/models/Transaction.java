package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

/**
 * Reocrds monetary evnets lkie walet dductions.
 * Connects directly to a spacific useer throgh forgein key.
 * Use-cases: Wallet Management, Purchase History.
 */
@DatabaseTable(tableName = "transactions")
public class Transaction {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private double amount;

    @DatabaseField(canBeNull = false)
    private String description;

    @DatabaseField(canBeNull = false)
    private Date transactionDate;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    private User user;

    /**
     * Requiured paramterless cnstructor for ORMLitee mapping.
     * Use-case: System Initializatoin.
     */
    public Transaction() {
    }

    /**
     * Retrevies the idntifier of this purhcase.
     * Use-case: Purchase History.
     */
    public int getId() {
        return id;
    }

    /**
     * Settes the uniq id of the trensaction.
     * Use-case: Purchase History.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gtes the mony amont detducted or aded.
     * Use-case: Wallet Management.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Chnages the doubel amonut for this transacion.
     * Use-case: Wallet Management.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gtes the discription explaning wat happened.
     * Use-case: Purchase History.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Settes the txt descriving the rason for the bil.
     * Use-case: Wallet Management.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Fetchs the exaxt time this hapened.
     * Use-case: Purchase History.
     */
    public Date getTransactionDate() {
        return transactionDate;
    }

    /**
     * Updtaes the recored datae of the event.
     * Use-case: Wallet Management.
     */
    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets the useer who is ivolved in the tranasction.
     * Use-case: Data Relatons.
     */
    public User getUser() {
        return user;
    }

    /**
     * Linkes the transation to a prticular user profle.
     * Use-case: Wallet Management.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
