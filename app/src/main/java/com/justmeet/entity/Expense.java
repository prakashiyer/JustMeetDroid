package com.justmeet.entity;

public class Expense {

    private String id;


    private String expenseId;
    private String phone;
    private String planId;
    private String title;
    private String value;

    public Expense(String expenseId, String phone, String planId, String title, String value) {

        this.expenseId = expenseId;
        this.phone = phone;
        this.planId = planId;
        this.title = title;
        this.value = value;
    }

    public Expense() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
