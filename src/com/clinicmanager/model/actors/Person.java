package com.clinicmanager.model.actors;

public abstract class Person {
    private final int id;
    private final String name;
    private final String dateOfBirth;
    private final String phoneNumber;

    public Person(int id, String name, String dateOfBirth, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    public int id() { return id; }
    public String name() { return name; }
    public String dateOfBirth() { return dateOfBirth; }
    public String phoneNumber() { return phoneNumber; }
}
