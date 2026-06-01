package com.example.foodorder.model;

public class BankAccount {
    private String id;
    private String userId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private boolean isLinked;

    public BankAccount() {}

    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public boolean isLinked() { return isLinked; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public void setLinked(boolean linked) { isLinked = linked; }
}