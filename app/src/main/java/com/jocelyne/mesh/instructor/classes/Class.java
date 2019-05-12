package com.jocelyne.mesh.instructor.classes;

import com.jocelyne.mesh.session_management.Student;

import java.util.HashMap;
import java.util.Map;

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

    // hashmap for student id and student object
    public Map<String, Student> studentsMap;

    public Class() {}

    // without studentsMap
    public Class(String prefix, String number, String name, String CRN,
                 String startTime, String endTime, String daysOfTheWeek) {
        this.prefix = prefix;
        this.number = number;
        this.name = name;
        this.CRN = CRN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfTheWeek = daysOfTheWeek;
        this.studentsMap = new HashMap<>(); // empty student list
    }

    // with studentsMap
    public Class(String prefix, String number, String name, String CRN,
                 String startTime, String endTime, String daysOfTheWeek,
                 HashMap<String, Student> studentsMap) {
        this.prefix = prefix;
        this.number = number;
        this.name = name;
        this.CRN = CRN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.daysOfTheWeek = daysOfTheWeek;
        this.studentsMap = studentsMap;
    }
}
