package com.example.universalyoganative;

import java.io.Serializable;

public class YogaCourse implements Serializable {
    private long id;
    private String dayOfWeek;
    private String time;
    private int capacity;
    private int duration;
    private float price;
    private String type;
    private String description;
    private String difficulty;
    private String location;
    private String instructor;
    private String createdDate;
    private int syncStatus;

    // Default constructor
    public YogaCourse() {
    }

    // Constructor with essential fields
    public YogaCourse(String dayOfWeek, String time, int capacity, int duration, 
                     float price, String type, String description, String difficulty, 
                     String location, String instructor) {
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.capacity = capacity;
        this.duration = duration;
        this.price = price;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
        this.location = location;
        this.instructor = instructor;
        this.syncStatus = 0; // Not synced by default
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("£%.2f", price);
    }

    public String getFormattedCapacity() {
        return capacity + " people";
    }

    public String getFormattedDuration() {
        return duration + " min";
    }

    public String getScheduleInfo() {
        return dayOfWeek + " at " + time;
    }

    public boolean isSynced() {
        return syncStatus == 1;
    }

    @Override
    public String toString() {
        return type + " - " + dayOfWeek + " " + time;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        YogaCourse that = (YogaCourse) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
} 