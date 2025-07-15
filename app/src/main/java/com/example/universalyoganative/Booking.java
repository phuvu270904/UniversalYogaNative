package com.example.universalyoganative;

public class Booking {
    private int bookingId;
    private int classId;
    private int userId;
    private String userEmail;
    private String classDate;
    private String courseTime;
    private double price;
    private int duration;

    public Booking() {
    }

    public Booking(int bookingId, int classId, int userId, String userEmail, String classDate, String courseTime, double price, int duration) {
        this.bookingId = bookingId;
        this.classId = classId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.classDate = classDate;
        this.courseTime = courseTime;
        this.price = price;
        this.duration = duration;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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
} 