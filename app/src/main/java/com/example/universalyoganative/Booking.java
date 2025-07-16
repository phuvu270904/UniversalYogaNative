package com.example.universalyoganative;

public class Booking {
    private int bookingId;
    private int classId;
    private long userId;
    private String userEmail;
    private String classDate;
    private String courseTime;
    private double price;
    private int duration;
    private int syncStatus; // 0 = not synced, 1 = synced

    public Booking() {
    }

    public Booking(int bookingId, int classId, long userId, String userEmail, String classDate, String courseTime, double price, int duration) {
        this.bookingId = bookingId;
        this.classId = classId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.classDate = classDate;
        this.courseTime = courseTime;
        this.price = price;
        this.duration = duration;
        this.syncStatus = 0; // Default to not synced
    }

    public Booking(int bookingId, int classId, long userId, String userEmail, String classDate, String courseTime, double price, int duration, int syncStatus) {
        this.bookingId = bookingId;
        this.classId = classId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.classDate = classDate;
        this.courseTime = courseTime;
        this.price = price;
        this.duration = duration;
        this.syncStatus = syncStatus;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getClassDate() {
        return classDate;
    }

    public void setClassDate(String classDate) {
        this.classDate = classDate;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String courseTime) {
        this.courseTime = courseTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    public boolean isSynced() {
        return syncStatus == 1;
    }
} 