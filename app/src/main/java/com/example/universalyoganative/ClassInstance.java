package com.example.universalyoganative;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ClassInstance implements Serializable {
    private long id;
    private long courseId;
    private String date;
    private String teacher;
    private String comments;
    private String photoPath;
    private double latitude;
    private double longitude;
    private int syncStatus;

    // Date format for display
    private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    // Default constructor
    public ClassInstance() {
    }

    // Constructor with essential fields
    public ClassInstance(long courseId, String date, String teacher, String comments) {
        this.courseId = courseId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
        this.syncStatus = 0; // Not synced by default
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    // Helper methods
    public String getFormattedDate() {
        try {
            Date dateObj = INPUT_DATE_FORMAT.parse(date);
            return DISPLAY_DATE_FORMAT.format(dateObj);
        } catch (ParseException e) {
            return date; // Return original if parsing fails
        }
    }

    public String getFormattedLocation() {
        if (latitude != 0.0 && longitude != 0.0) {
            return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
        }
        return "No location";
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.trim().isEmpty();
    }

    public boolean hasLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public boolean isSynced() {
        return syncStatus == 1;
    }

    public String getDayOfWeek() {
        try {
            Date dateObj = INPUT_DATE_FORMAT.parse(date);
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            return dayFormat.format(dateObj);
        } catch (ParseException e) {
            return "";
        }
    }

    /**
     * Validates if the date matches the expected day of the week from the course
     * @param expectedDayOfWeek The day of week from the YogaCourse (e.g., "Tuesday")
     * @return true if the date matches the expected day of week, false otherwise
     */
    public boolean isDateMatchingDayOfWeek(String expectedDayOfWeek) {
        if (expectedDayOfWeek == null || expectedDayOfWeek.trim().isEmpty()) {
            return false;
        }
        
        String actualDayOfWeek = getDayOfWeek();
        return expectedDayOfWeek.trim().equalsIgnoreCase(actualDayOfWeek.trim());
    }

    /**
     * Gets a formatted string showing the validation result
     * @param expectedDayOfWeek The day of week from the YogaCourse
     * @return A formatted string indicating if the date matches the day of week
     */
    public String getDayOfWeekValidationMessage(String expectedDayOfWeek) {
        if (isDateMatchingDayOfWeek(expectedDayOfWeek)) {
            return "✓ Date matches " + expectedDayOfWeek;
        } else {
            return "✗ Date should be on " + expectedDayOfWeek + " (current: " + getDayOfWeek() + ")";
        }
    }

    @Override
    public String toString() {
        return "ClassInstance{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", date='" + date + '\'' +
                ", teacher='" + teacher + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassInstance that = (ClassInstance) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
} 