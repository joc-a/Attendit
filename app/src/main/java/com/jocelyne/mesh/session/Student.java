package com.jocelyne.mesh.session;

import com.hypelabs.hype.Instance;

public class Student extends User {

    // to enable communication between instances, not just resolve and display
    private Instance instance;

    private String studentID;

    public Student(String fname, String lname, String email, String studentID) {
        super(fname, lname, email);
        this.studentID = studentID;
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
