package com.jocelyne.mesh.instructor.classes;

import com.jocelyne.mesh.session.Student;

import java.util.ArrayList;

public class Class {

    // name variables
    public String prefix;
    public String number;
    public String name;
    public String CRN;

    // schedule variables
    public String startTime;
    public String endTime;
    public String daysOfTheWeek;

    // student list
    public ArrayList<Student> students;

    public Class() {}

    // without students
    public Class(String prefix, String number, String name, String CRN,
                 String startTime, String endTime, String daysOfTheWeek) {
        this.prefix = prefix;
        this.number = number;
        this.name = name;
        this.CRN = CRN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfTheWeek = daysOfTheWeek;
        this.students = new ArrayList<>(); // empty student list
    }

    // with students
    public Class(String prefix, String number, String name, String CRN,
                 String startTime, String endTime, String daysOfTheWeek,
                 ArrayList<Student> students) {
        this.prefix = prefix;
        this.number = number;
        this.name = name;
        this.CRN = CRN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfTheWeek = daysOfTheWeek;
        this.students = students;
    }
}
