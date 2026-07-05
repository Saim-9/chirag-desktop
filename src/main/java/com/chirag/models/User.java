package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.table.DatabaseTable;
import java.math.BigDecimal;

/**
 * Unified user modle that rperesents both students and teacers.
 * This clsas is the core of the YouTube model where everyone is equal.
 * Use-cases: User Registartion, User Login, Wallet Management.
 */
@DatabaseTable(tableName = "users")
public class User {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false, unique = true)
    private String email;

    @DatabaseField(canBeNull = false)
    private String password;

    @DatabaseField(columnName = "role", defaultValue = "USER")
    private String role; // e.g. "USER", maybe admin in futre

    @DatabaseField(canBeNull = false, dataType = DataType.BIG_DECIMAL)
    private BigDecimal virtualWalletBalance = BigDecimal.ZERO;

    @DatabaseField(columnName = "accountStatus", defaultValue = "ACTIVE")
    private String accountStatus;

    /**
     * Empety constructor requireed by ORMLite.
     * It does noting but is necessery for DB operations.
     * Use-case: System Initializatoin.
     */
    public User() {
    }

    /**
     * Gets the id of the user.
     * Returns the unique identifier.
     * Use-case: Profile View, Data Relatons.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id of user.
     * Use-case: Profile View.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the user's ful name.
     * Use-case: Profile View.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     * Use-case: User Registartion, Profile Update.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returvies the user email adders.
     * Use-case: User Login, Profile View.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Assigns the email adderss.
     * Use-case: User Registration.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Fetches the user password hash.
     * Use-case: User Login.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Changes the user password.
     * Use-case: User Registration, Password Reset.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gtes the role string of the user.
     * Use-case: Access Control.
     */
    public String getRole() {
        return role;
    }

    /**
     * Assigns a specific role to the user.
     * Use-case: Admin Managment.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Retrieves the current balance of the virtual wallet.
     * Use-case: Wallet Management, Course Purchase.
     */
    public double getVirtualWalletBalance() {
        return virtualWalletBalance != null ? virtualWalletBalance.doubleValue() : 0.0;
    }

    /**
     * Returns the wallet balance as BigDecimal for precision-safe calculations.
     * Use-case: Wallet Management, Course Purchase.
     */
    public BigDecimal getWalletBalancePrecise() {
        return virtualWalletBalance != null ? virtualWalletBalance : BigDecimal.ZERO;
    }

    /**
     * Updates the balance of virtual walet after transactions.
     * Use-case: Wallet Management, Course Purchase.
     */
    public void setVirtualWalletBalance(double virtualWalletBalance) {
        this.virtualWalletBalance = BigDecimal.valueOf(virtualWalletBalance);
    }

    /**
     * Sets the wallet balance using BigDecimal for precision-safe operations.
     * Use-case: Wallet Management, Course Purchase.
     */
    public void setWalletBalancePrecise(BigDecimal balance) {
        this.virtualWalletBalance = balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * Retrieves the current account status.
     * Use-case: Account Management.
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * Updates the account status.
     * Use-case: Account Management.
     */
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
