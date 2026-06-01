package com.example.foodorder.model;

import java.io.Serializable;

public class BankAccount implements Serializable {
    private String id;
    private String userId;
    private String bankName; // Ví dụ: "Vietcombank", "MB Bank",...
    private String accountNumber;
    private String accountHolder;
    private boolean isLinked; // mặc định true khi tạo
    private long linkedAt;

    public BankAccount() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
    public boolean isLinked() { return isLinked; }
    public void setLinked(boolean linked) { isLinked = linked; }
    public long getLinkedAt() { return linkedAt; }
    public void setLinkedAt(long linkedAt) { this.linkedAt = linkedAt; }
}