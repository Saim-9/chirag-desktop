package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Records monetary events like wallet deductions.
 * Connects directly to a specific user through foreign key.
 * Use-cases: Wallet Management, Purchase History.
 */
@DatabaseTable(tableName = "transactions")
public class Transaction {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, dataType = DataType.BIG_DECIMAL)
    private BigDecimal amount = BigDecimal.ZERO;

    @DatabaseField(canBeNull = false, dataType = DataType.BIG_DECIMAL)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @DatabaseField(canBeNull = false, dataType = DataType.BIG_DECIMAL)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @DatabaseField(canBeNull = false)
    private String description;

    @DatabaseField(canBeNull = false)
    private Date transactionDate;

    @DatabaseField(foreign = true, columnName = "buyer_id", canBeNull = true)
    private User buyer;

    @DatabaseField(foreign = true, columnName = "instructor_id", canBeNull = true)
    private User instructor;

    /**
     * Required parameterless constructor for ORMLite mapping.
     * Use-case: System Initialization.
     */
    public Transaction() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount != null ? amount.doubleValue() : 0.0; }
    public void setAmount(double amount) { this.amount = BigDecimal.valueOf(amount); }

    public double getPlatformFee() { return platformFee != null ? platformFee.doubleValue() : 0.0; }
    public void setPlatformFee(double platformFee) { this.platformFee = BigDecimal.valueOf(platformFee); }

    public double getNetAmount() { return netAmount != null ? netAmount.doubleValue() : 0.0; }
    public void setNetAmount(double netAmount) { this.netAmount = BigDecimal.valueOf(netAmount); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Date transactionDate) { this.transactionDate = transactionDate; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    public User getInstructor() { return instructor; }
    public void setInstructor(User instructor) { this.instructor = instructor; }
}
