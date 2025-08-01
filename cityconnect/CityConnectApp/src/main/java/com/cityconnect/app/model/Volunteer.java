package com.cityconnect.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String contact;
    private String areaOfInterest;
    private LocalDate date;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAreaOfInterest() { return areaOfInterest; }
    public void setAreaOfInterest(String areaOfInterest) { this.areaOfInterest = areaOfInterest; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
