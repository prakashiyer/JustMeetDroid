package com.justmeet.entity;

import java.util.List;

public class Plan {

    private String id;
    private String title;
    private String startTime;
    private String endTime;
    private String location;
    private List<String> membersAttending;
    private List<String> membersInvited;
    private List<String> groupsInvited;
    private String creator;

    public Plan() {

    }

    public Plan(String id, String title, String startTime, String location,
                List<String> membersAttending,  String creator,String endTime,
                List<String> groupsInvited, List<String> membersInvited) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.membersAttending = membersAttending;
        this.membersInvited = membersInvited;
        this.groupsInvited = groupsInvited;
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getMembersAttending() {
        return membersAttending;
    }

    public void setMembersAttending(List<String> membersAttending) {
        this.membersAttending = membersAttending;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getMembersInvited() {
        return membersInvited;
    }

    public void setMembersInvited(List<String> membersInvited) {
        this.membersInvited = membersInvited;
    }

    public List<String> getGroupsInvited() {
        return groupsInvited;
    }

    public void setGroupsInvited(List<String> groupsInvited) {
        this.groupsInvited = groupsInvited;
    }
}
