package com.smartattend.app.model;

public class ScheduleItem {
    private String day; // MON, TUE, etc.
    private String time; // 09:10 AM - 09:55 AM
    private String subject; // MAD
    private String room; // NB 102
    private String type; // Lecture, Lab, etc.

    public ScheduleItem() {}

    public ScheduleItem(String day, String time, String subject, String room, String type) {
        this.day = day;
        this.time = time;
        this.subject = subject;
        this.room = room;
        this.type = type;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
