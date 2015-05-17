package com.justmeet.entity;

import java.util.List;

public class Group {

    private String id;
    private String name;
    private List<String> members;
    private String admin;
    private byte[] image;
    private boolean selected;

    public Group() {

    }

    public Group(String id, String name, List<String> members, String admin, byte[] image, boolean selected) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.admin = admin;
        this.image = image;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
