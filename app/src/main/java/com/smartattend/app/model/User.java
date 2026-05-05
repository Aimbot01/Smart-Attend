package com.smartattend.app.model;

import java.util.Objects;

public class User {
    private String uid = "";
    private String name = "";
    private String email = "";
    private String role = "";
    private String studentId = "";
    private String department = "";
    private int semester = 0;
    private String faceImageUrl = "";
    private long createdAt = System.currentTimeMillis();

    public User() {}

    public User(String uid, String name, String email, String role, String studentId, String department, int semester, String faceImageUrl, long createdAt) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.studentId = studentId;
        this.department = department;
        this.semester = semester;
        this.faceImageUrl = faceImageUrl;
        this.createdAt = createdAt;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getFaceImageUrl() { return faceImageUrl; }
    public void setFaceImageUrl(String faceImageUrl) { this.faceImageUrl = faceImageUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return semester == user.semester && createdAt == user.createdAt && Objects.equals(uid, user.uid) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(role, user.role) && Objects.equals(studentId, user.studentId) && Objects.equals(department, user.department) && Objects.equals(faceImageUrl, user.faceImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, email, role, studentId, department, semester, faceImageUrl, createdAt);
    }
}
