package com.jocelyne.mesh.session_management;

import com.hypelabs.hype.Instance;

public class Student extends User {

    // to enable communication between instances, not just resolve and display
    private Instance instance;

    //to detect if found then lost
    public boolean lost;

    private String studentID;

    public Student() {
        lost = false;
    }

    public Student(String fname, String lname, String email, String studentID) {
        super(fname, lname, email);
        this.studentID = studentID;
        lost = false;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
}
