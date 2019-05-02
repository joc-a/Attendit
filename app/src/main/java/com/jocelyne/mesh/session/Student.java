package com.jocelyne.mesh.session;

public class Student extends User {

    private String studentID;

    public Student(String fname, String lname, String email, String studentID) {
        super(fname, lname, email);
        this.studentID = studentID;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
}
