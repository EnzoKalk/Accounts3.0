package com.example.accounts;

import java.io.Serializable;
import java.util.List;

public class Account implements Serializable {
    private String name;
    private String category;
    private List<AccountElement> list;

    public Account(String name, String category, List<AccountElement> list) {
        this.name = name;
        this.category = category;
        this.list = list;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    List<AccountElement> getList() {
        return list;
    }

    void setList(List<AccountElement> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object object) {
        boolean bool = false;
        if (object instanceof Account) {
            bool = (((this.getName().toLowerCase().equals(((Account) object).getName().toLowerCase()))));
        }
        return bool;
    }

    boolean equals(String str) {
        return this.getName().toLowerCase().compareTo(str.toLowerCase()) == 0;
    }
}
