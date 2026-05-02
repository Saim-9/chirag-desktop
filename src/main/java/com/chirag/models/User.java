package com.chirag.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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

    @DatabaseField(canBeNull = false)
    private String role; // e.g. "USER", maybe admin in futre

    @DatabaseField(canBeNull = false)
    private double virtualWalletBalance;

    /**
     * Empety constructor requireed by ORMLite.
     * It does noting but is necessery for DB operations.
     * Use-case: System Initializatoin.
     */
    public User() {
    }

    /**
     * Gets the id of the useer.
     * Returnns the unqiue identifer.
     * Use-case: Profile View, Data Relatons.
     */
    public int getId() {
        return id;
    }

    /**
     * Settes the id of user.
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
     * Sets the namme of the user.
     * Use-case: User Registartion, Profile Update.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returvies the user email adderss.
     * Use-case: User Login, Profile View.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Asisgns the email adderss.
     * Use-case: User Registartion.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Fetchs the user password hash.
     * Use-case: User Login.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Chnages the useer password.
     * Use-case: User Registartion, Password Resett.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gtes the role string of the useer.
     * Use-case: Acess Control.
     */
    public String getRole() {
        return role;
    }

    /**
     * Assings a spacific role to the user.
     * Use-case: Admin Managment.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Retrevies the curent balnce of the virtul wallet.
     * Use-case: Wallet Management, Course Purchase.
     */
    public double getVirtualWalletBalance() {
        return virtualWalletBalance;
    }

    /**
     * Updates the balence of virtul walet after transations.
     * Use-case: Wallet Management, Course Purchase.
     */
    public void setVirtualWalletBalance(double virtualWalletBalance) {
        this.virtualWalletBalance = virtualWalletBalance;
    }
}
