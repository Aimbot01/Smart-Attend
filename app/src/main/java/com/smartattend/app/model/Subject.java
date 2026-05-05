package com.smartattend.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Subject {
    private String code = "";
    private String name = "";
    private String teacherId = "";
    private String teacherName = "";
    private String room = "";
    private String schedule = "";
    private List<String> enrolledStudents = new ArrayList<>();

    public Subject() {}

    public Subject(String code, String name, String teacherId, String teacherName, String room, String schedule, List<String> enrolledStudents) {
        this.code = code;
        this.name = name;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.room = room;
        this.schedule = schedule;
        this.enrolledStudents = enrolledStudents;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public List<String> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(List<String> enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(code, subject.code) && Objects.equals(name, subject.name) && Objects.equals(teacherId, subject.teacherId) && Objects.equals(teacherName, subject.teacherName) && Objects.equals(room, subject.room) && Objects.equals(schedule, subject.schedule) && Objects.equals(enrolledStudents, subject.enrolledStudents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, teacherId, teacherName, room, schedule, enrolledStudents);
    }
}
