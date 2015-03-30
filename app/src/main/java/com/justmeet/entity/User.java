package com.justmeet.entity;

import java.util.List;

public class User {

    private int id;
    private String name;
    private String phone;
    private List<String> groupIds;
    private byte[] image;
    private boolean selected;

    public User() {

    }

    public User(int id, String name, String phone, List<String> groupIds, byte[] image, boolean selected) {
        this.id = id;
        this.name = name;
        this.groupIds = groupIds;
        this.image = image;
        this.selected = selected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

}
