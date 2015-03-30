package com.justmeet.entity;

import java.util.List;

public class ExpenseReport {

    private List<ExpenseRow> expenseRows;

    public ExpenseReport() {

    }

    public List<ExpenseRow> getExpenseRows() {
        return expenseRows;
    }

    public void setExpenseRows(List<ExpenseRow> expenseRows) {
        this.expenseRows = expenseRows;
    }


}
