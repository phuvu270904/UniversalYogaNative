package com.example.universalyoganative;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private FirebaseFirestore db;
    private DatabaseHelper localDb;
    
    // Collection names
    private static final String COLLECTION_COURSES = "yoga_courses";
    private static final String COLLECTION_INSTANCES = "class_instances";
    private static final String COLLECTION_USERS = "users";
    
    public interface SyncCallback {
        void onSyncProgress(String message, int progress);
        void onSyncComplete(boolean success, String message);
        void onSyncError(String error);
    }
    
    public FirebaseService(DatabaseHelper localDb) {
        this.localDb = localDb;
        this.db = FirebaseFirestore.getInstance();
    }
    
    /**
     * Check if Firebase connection is available
     */
    public void checkConnection(SyncCallback callback) {
        callback.onSyncProgress("Checking Firebase connection...", 0);
        
        // First check if we can access the database
        db.collection(COLLECTION_COURSES).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSyncComplete(true, "Firebase connection successful - Database accessible");
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
                        callback.onSyncError("Firebase connection failed: Permission denied. Please check Firestore security rules.");
                    } else if (errorMessage != null && errorMessage.contains("UNAVAILABLE")) {
                        callback.onSyncError("Firebase connection failed: Service unavailable. Please check your internet connection.");
                    } else {
                        callback.onSyncError("Firebase connection failed: " + errorMessage);
                    }
                });
    }
    
    /**
     * Sync local data to Firebase (upload)
     */
    public void syncToCloud(SyncCallback callback) {
        callback.onSyncProgress("Starting sync to cloud...", 0);
        
        // First check if we have permission to write to Firestore
        db.collection(COLLECTION_COURSES).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Permission check passed, proceed with sync
                    performSyncToCloud(callback);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
                        callback.onSyncError("Sync failed: Permission denied. Please check Firestore security rules.");
                    } else {
                        callback.onSyncError("Sync failed: " + errorMessage);
                    }
                });
    }
    
    private void performSyncToCloud(SyncCallback callback) {
        // Get all data to sync (courses, instances, users, bookings)
        android.database.Cursor unsyncedCourses = localDb.getUnsyncedCourses();
        android.database.Cursor unsyncedInstances = localDb.getUnsyncedInstances();
        android.database.Cursor allUsers = localDb.getAllUsers();
        List<Booking> allBookings = localDb.getAllBookings();
        
        int totalItems = (unsyncedCourses != null ? unsyncedCourses.getCount() : 0) + 
                        (unsyncedInstances != null ? unsyncedInstances.getCount() : 0) +
                        (allUsers != null ? allUsers.getCount() : 0) +
                        (allBookings != null ? allBookings.size() : 0);
        
        if (totalItems == 0) {
            callback.onSyncComplete(true, "No data to sync");
            return;
        }
        
        AtomicInteger completedItems = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // Sync courses
        if (unsyncedCourses != null && unsyncedCourses.moveToFirst()) {
            do {
                YogaCourse course = createCourseFromCursor(unsyncedCourses);
                uploadCourseToFirestore(course, new SyncCallback() {
                    @Override
                    public void onSyncProgress(String message, int progress) {
                        // Not used for individual uploads
                    }
                    
                    @Override
                    public void onSyncComplete(boolean success, String message) {
                        int completed = completedItems.incrementAndGet();
                        if (success) {
                            successCount.incrementAndGet();
                            // Mark as synced in local DB
                            localDb.markCourseSynced(course.getId());
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing courses... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                        }
                    }
                    
                    @Override
                    public void onSyncError(String error) {
                        int completed = completedItems.incrementAndGet();
                        errorCount.incrementAndGet();
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing courses... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(false, resultMessage);
                        }
                    }
                });
            } while (unsyncedCourses.moveToNext());
            unsyncedCourses.close();
        }
        
        // Sync instances
        if (unsyncedInstances != null && unsyncedInstances.moveToFirst()) {
            do {
                ClassInstance instance = createInstanceFromCursor(unsyncedInstances);
                uploadInstanceToFirestore(instance, new SyncCallback() {
                    @Override
                    public void onSyncProgress(String message, int progress) {
                        // Not used for individual uploads
                    }
                    
                    @Override
                    public void onSyncComplete(boolean success, String message) {
                        int completed = completedItems.incrementAndGet();
                        if (success) {
                            successCount.incrementAndGet();
                            // Mark as synced in local DB
                            localDb.markInstanceSynced(instance.getId());
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing instances... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                        }
                    }
                    
                    @Override
                    public void onSyncError(String error) {
                        int completed = completedItems.incrementAndGet();
                        errorCount.incrementAndGet();
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing instances... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(false, resultMessage);
                        }
                    }
                });
            } while (unsyncedInstances.moveToNext());
            unsyncedInstances.close();
        }
        
        // Sync users
        System.out.println("allUsers: " + allUsers.getCount());
        System.out.println("isMoveToFirst: " + allUsers.moveToFirst());
        if (allUsers != null && allUsers.moveToFirst()) {
            do {
                User user = createUserFromCursor(allUsers);
                uploadUserToFirestore(user, new SyncCallback() {
                    @Override
                    public void onSyncProgress(String message, int progress) {
                        // Not used for individual uploads
                    }
                    
                    @Override
                    public void onSyncComplete(boolean success, String message) {
                        int completed = completedItems.incrementAndGet();
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing users... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                        }
                    }
                    
                    @Override
                    public void onSyncError(String error) {
                        int completed = completedItems.incrementAndGet();
                        errorCount.incrementAndGet();
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing users... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(false, resultMessage);
                        }
                    }
                });
            } while (allUsers.moveToNext());
            allUsers.close();
        }
        
        // Sync bookings
        if (allBookings != null && !allBookings.isEmpty()) {
            for (Booking booking : allBookings) {
                uploadBookingToFirestore(booking, new SyncCallback() {
                    @Override
                    public void onSyncProgress(String message, int progress) {
                        // Not used for individual uploads
                    }
                    
                    @Override
                    public void onSyncComplete(boolean success, String message) {
                        int completed = completedItems.incrementAndGet();
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing bookings... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                        }
                    }
                    
                    @Override
                    public void onSyncError(String error) {
                        int completed = completedItems.incrementAndGet();
                        errorCount.incrementAndGet();
                        
                        int progress = (completed * 100) / totalItems;
                        callback.onSyncProgress("Syncing bookings... " + completed + "/" + totalItems, progress);
                        
                        if (completed == totalItems) {
                            String resultMessage = "Sync completed. Success: " + successCount.get() + 
                                                 ", Errors: " + errorCount.get();
                            callback.onSyncComplete(false, resultMessage);
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Sync data from Firebase to local (download)
     */
    public void syncFromCloud(SyncCallback callback) {
        callback.onSyncProgress("Starting sync from cloud...", 0);
        
        // First check if we have permission to read from Firestore
        db.collection(COLLECTION_COURSES).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Permission check passed, proceed with download
                    performSyncFromCloud(callback);
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && errorMessage.contains("PERMISSION_DENIED")) {
                        callback.onSyncError("Download failed: Permission denied. Please check Firestore security rules.");
                    } else {
                        callback.onSyncError("Download failed: " + errorMessage);
                    }
                });
    }
    
    private void performSyncFromCloud(SyncCallback callback) {
        AtomicInteger totalItems = new AtomicInteger(0);
        AtomicInteger completedItems = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // First, count total items from all collections
        db.collection(COLLECTION_COURSES).get().addOnSuccessListener(courseSnapshots -> {
            db.collection(COLLECTION_INSTANCES).get().addOnSuccessListener(instanceSnapshots -> {
                db.collection(COLLECTION_USERS).get().addOnSuccessListener(userSnapshots -> {
                    db.collection("bookings").get().addOnSuccessListener(bookingSnapshots -> {
                        totalItems.set(courseSnapshots.size() + instanceSnapshots.size() + 
                                     userSnapshots.size() + bookingSnapshots.size());
                        
                        if (totalItems.get() == 0) {
                            callback.onSyncComplete(true, "No data in cloud to sync");
                            return;
                        }
                
                // Download courses
                for (QueryDocumentSnapshot document : courseSnapshots) {
                    downloadCourseFromFirestore(document, new SyncCallback() {
                        @Override
                        public void onSyncProgress(String message, int progress) {
                            // Not used for individual downloads
                        }
                        
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            int completed = completedItems.incrementAndGet();
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading courses... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                            }
                        }
                        
                        @Override
                        public void onSyncError(String error) {
                            int completed = completedItems.incrementAndGet();
                            errorCount.incrementAndGet();
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading courses... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(false, resultMessage);
                            }
                        }
                    });
                }
                
                // Download instances
                for (QueryDocumentSnapshot document : instanceSnapshots) {
                    downloadInstanceFromFirestore(document, new SyncCallback() {
                        @Override
                        public void onSyncProgress(String message, int progress) {
                            // Not used for individual downloads
                        }
                        
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            int completed = completedItems.incrementAndGet();
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading instances... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                            }
                        }
                        
                        @Override
                        public void onSyncError(String error) {
                            int completed = completedItems.incrementAndGet();
                            errorCount.incrementAndGet();
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading instances... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(false, resultMessage);
                            }
                        }
                    });
                }
                
                // Download users
                for (QueryDocumentSnapshot document : userSnapshots) {
                    downloadUserFromFirestore(document, new SyncCallback() {
                        @Override
                        public void onSyncProgress(String message, int progress) {
                            // Not used for individual downloads
                        }
                        
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            int completed = completedItems.incrementAndGet();
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading users... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                            }
                        }
                        
                        @Override
                        public void onSyncError(String error) {
                            int completed = completedItems.incrementAndGet();
                            errorCount.incrementAndGet();
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading users... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(false, resultMessage);
                            }
                        }
                    });
                }
                
                // Download bookings
                for (QueryDocumentSnapshot document : bookingSnapshots) {
                    downloadBookingFromFirestore(document, new SyncCallback() {
                        @Override
                        public void onSyncProgress(String message, int progress) {
                            // Not used for individual downloads
                        }
                        
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            int completed = completedItems.incrementAndGet();
                            if (success) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading bookings... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(errorCount.get() == 0, resultMessage);
                            }
                        }
                        
                        @Override
                        public void onSyncError(String error) {
                            int completed = completedItems.incrementAndGet();
                            errorCount.incrementAndGet();
                            
                            int progress = (completed * 100) / totalItems.get();
                            callback.onSyncProgress("Downloading bookings... " + completed + "/" + totalItems.get(), progress);
                            
                            if (completed == totalItems.get()) {
                                String resultMessage = "Download completed. Success: " + successCount.get() + 
                                                     ", Errors: " + errorCount.get();
                                callback.onSyncComplete(false, resultMessage);
                            }
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                callback.onSyncError("Failed to get bookings from cloud: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            callback.onSyncError("Failed to get users from cloud: " + e.getMessage());
        });
    }).addOnFailureListener(e -> {
        callback.onSyncError("Failed to get instances from cloud: " + e.getMessage());
    });
}).addOnFailureListener(e -> {
    callback.onSyncError("Failed to get courses from cloud: " + e.getMessage());
});
    }
    
    /**
     * Full bidirectional sync
     */
    public void fullSync(SyncCallback callback) {
        callback.onSyncProgress("Starting full bidirectional sync...", 0);
        
        // First sync to cloud, then from cloud
        syncToCloud(new SyncCallback() {
            @Override
            public void onSyncProgress(String message, int progress) {
                callback.onSyncProgress("Upload: " + message, progress / 2);
            }
            
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    callback.onSyncProgress("Upload completed, starting download...", 50);
                    syncFromCloud(new SyncCallback() {
                        @Override
                        public void onSyncProgress(String message, int progress) {
                            callback.onSyncProgress("Download: " + message, 50 + (progress / 2));
                        }
                        
                        @Override
                        public void onSyncComplete(boolean success, String message) {
                            callback.onSyncComplete(success, "Full sync completed. " + message);
                        }
                        
                        @Override
                        public void onSyncError(String error) {
                            callback.onSyncError("Download failed: " + error);
                        }
                    });
                } else {
                    callback.onSyncError("Upload failed: " + message);
                }
            }
            
            @Override
            public void onSyncError(String error) {
                callback.onSyncError("Upload failed: " + error);
            }
        });
    }
    
    // Helper methods for uploading to Firestore
    private void uploadCourseToFirestore(YogaCourse course, SyncCallback callback) {
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("dayOfWeek", course.getDayOfWeek());
        courseData.put("time", course.getTime());
        courseData.put("capacity", course.getCapacity());
        courseData.put("duration", course.getDuration());
        courseData.put("price", course.getPrice());
        courseData.put("type", course.getType());
        courseData.put("description", course.getDescription());
        courseData.put("difficulty", course.getDifficulty());
        courseData.put("location", course.getLocation());
        courseData.put("createdDate", course.getCreatedDate());
        courseData.put("localId", course.getId());
        courseData.put("lastModified", System.currentTimeMillis());
        
        db.collection(COLLECTION_COURSES).add(courseData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSyncComplete(true, "Course uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onSyncError("Failed to upload course: " + e.getMessage());
                });
    }
    
    private void uploadInstanceToFirestore(ClassInstance instance, SyncCallback callback) {
        Map<String, Object> instanceData = new HashMap<>();
        instanceData.put("courseId", instance.getCourseId());
        instanceData.put("date", instance.getDate());
        instanceData.put("teacher", instance.getTeacher());
        instanceData.put("comments", instance.getComments());
        instanceData.put("photoPath", instance.getPhotoPath());
        instanceData.put("latitude", instance.getLatitude());
        instanceData.put("longitude", instance.getLongitude());
        instanceData.put("localId", instance.getId());
        instanceData.put("lastModified", System.currentTimeMillis());
        
        db.collection(COLLECTION_INSTANCES).add(instanceData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSyncComplete(true, "Instance uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onSyncError("Failed to upload instance: " + e.getMessage());
                });
    }
    
    private void uploadUserToFirestore(User user, SyncCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("password", user.getPassword());
        userData.put("role", user.getRole());
        userData.put("createdDate", user.getCreatedDate());
        userData.put("localId", user.getId());
        userData.put("lastModified", System.currentTimeMillis());
        
        db.collection(COLLECTION_USERS).add(userData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSyncComplete(true, "User uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onSyncError("Failed to upload user: " + e.getMessage());
                });
    }
    
    private void uploadBookingToFirestore(Booking booking, SyncCallback callback) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("classId", booking.getClassId());
        bookingData.put("userId", booking.getUserId());
        bookingData.put("localId", booking.getBookingId());
        bookingData.put("lastModified", System.currentTimeMillis());
        
        db.collection("bookings").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSyncComplete(true, "Booking uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    callback.onSyncError("Failed to upload booking: " + e.getMessage());
                });
    }
    
    // Helper methods for downloading from Firestore
    private void downloadCourseFromFirestore(QueryDocumentSnapshot document, SyncCallback callback) {
        try {
            String dayOfWeek = document.getString("dayOfWeek");
            String time = document.getString("time");
            Long capacity = document.getLong("capacity");
            Long duration = document.getLong("duration");
            Double price = document.getDouble("price");
            String type = document.getString("type");
            String description = document.getString("description");
            String difficulty = document.getString("difficulty");
            String location = document.getString("location");
            String createdDate = document.getString("createdDate");
            Long localId = document.getLong("localId");
            
            if (dayOfWeek != null && time != null && capacity != null && duration != null && 
                price != null && type != null) {
                
                // Check if course already exists locally
                if (localId != null && localId > 0) {
                    // Update existing course
                    localDb.updateYogaCourse(localId, dayOfWeek, time, capacity.intValue(), 
                                           duration.intValue(), price.floatValue(), type, 
                                           description, difficulty, location);
                } else {
                    // Create new course
                    long newId = localDb.createNewYogaCourse(dayOfWeek, time, capacity.intValue(), 
                                                           duration.intValue(), price.floatValue(), 
                                                           type, description, difficulty, location);
                    if (newId > 0) {
                        // Mark as synced
                        localDb.markCourseSynced(newId);
                    }
                }
                callback.onSyncComplete(true, "Course downloaded successfully");
            } else {
                callback.onSyncError("Invalid course data in document");
            }
        } catch (Exception e) {
            callback.onSyncError("Error processing course: " + e.getMessage());
        }
    }
    
    private void downloadInstanceFromFirestore(QueryDocumentSnapshot document, SyncCallback callback) {
        try {
            Long courseId = document.getLong("courseId");
            String date = document.getString("date");
            String teacher = document.getString("teacher");
            String comments = document.getString("comments");
            String photoPath = document.getString("photoPath");
            Double latitude = document.getDouble("latitude");
            Double longitude = document.getDouble("longitude");
            Long localId = document.getLong("localId");
            
            if (courseId != null && date != null && teacher != null) {
                // Check if instance already exists locally
                if (localId != null && localId > 0) {
                    // Update existing instance
                    localDb.updateClassInstance(localId, date, teacher, comments, 
                                              photoPath, latitude != null ? latitude : 0.0, 
                                              longitude != null ? longitude : 0.0);
                } else {
                    // Create new instance
                    long newId = localDb.createClassInstance(courseId, date, teacher, comments, 
                                                           photoPath, latitude != null ? latitude : 0.0, 
                                                           longitude != null ? longitude : 0.0);
                    if (newId > 0) {
                        // Mark as synced
                        localDb.markInstanceSynced(newId);
                    }
                }
                callback.onSyncComplete(true, "Instance downloaded successfully");
            } else {
                callback.onSyncError("Invalid instance data in document");
            }
        } catch (Exception e) {
            callback.onSyncError("Error processing instance: " + e.getMessage());
        }
    }
    
    private void downloadUserFromFirestore(QueryDocumentSnapshot document, SyncCallback callback) {
        try {
            String name = document.getString("name");
            String email = document.getString("email");
            String password = document.getString("password");
            String role = document.getString("role");
            String createdDate = document.getString("createdDate");
            Long localId = document.getLong("localId");
            
            if (name != null && email != null && password != null && role != null) {
                // Check if user already exists locally
                if (localId != null && localId > 0) {
                    // Update existing user
                    localDb.updateUser(localId, name, email, role);
                    // Note: We don't update password for security reasons
                } else {
                    // Create new user
                    long newId = localDb.registerUser(name, email, password, role);
                    if (newId > 0) {
                        // User created successfully
                    } else if (newId == -1) {
                        // Email already exists, try to update
                        User existingUser = localDb.getUserByEmail(email);
                        if (existingUser != null) {
                            localDb.updateUser(existingUser.getId(), name, email, role);
                        }
                    }
                }
                callback.onSyncComplete(true, "User downloaded successfully");
            } else {
                callback.onSyncError("Invalid user data in document");
            }
        } catch (Exception e) {
            callback.onSyncError("Error processing user: " + e.getMessage());
        }
    }
    
    private void downloadBookingFromFirestore(QueryDocumentSnapshot document, SyncCallback callback) {
        try {
            Long classId = document.getLong("classId");
            Long userId = document.getLong("userId");
            Long localId = document.getLong("localId");
            
            if (classId != null && userId != null) {
                // Check if booking already exists locally
                if (localId != null && localId > 0) {
                    // Booking already exists, skip
                    callback.onSyncComplete(true, "Booking already exists locally");
                } else {
                    // Create new booking
                    long newId = localDb.createBooking(classId.intValue(), userId);
                    if (newId > 0) {
                        // Booking created successfully
                        callback.onSyncComplete(true, "Booking downloaded successfully");
                    } else {
                        callback.onSyncError("Failed to create booking locally");
                    }
                }
            } else {
                callback.onSyncError("Invalid booking data in document");
            }
        } catch (Exception e) {
            callback.onSyncError("Error processing booking: " + e.getMessage());
        }
    }
    
    // Helper methods to create objects from cursors
    @android.annotation.SuppressLint("Range")
    private YogaCourse createCourseFromCursor(android.database.Cursor cursor) {
        YogaCourse course = new YogaCourse();
        course.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        course.setDayOfWeek(cursor.getString(cursor.getColumnIndex("dayofweek")));
        course.setTime(cursor.getString(cursor.getColumnIndex("time")));
        course.setCapacity(cursor.getInt(cursor.getColumnIndex("capacity")));
        course.setDuration(cursor.getInt(cursor.getColumnIndex("duration")));
        course.setPrice(cursor.getFloat(cursor.getColumnIndex("price")));
        course.setType(cursor.getString(cursor.getColumnIndex("type")));
        course.setDescription(cursor.getString(cursor.getColumnIndex("description")));
        course.setDifficulty(cursor.getString(cursor.getColumnIndex("difficulty")));
        course.setLocation(cursor.getString(cursor.getColumnIndex("location")));
        course.setCreatedDate(cursor.getString(cursor.getColumnIndex("created_date")));
        course.setSyncStatus(cursor.getInt(cursor.getColumnIndex("sync_status")));
        return course;
    }
    
    @android.annotation.SuppressLint("Range")
    private ClassInstance createInstanceFromCursor(android.database.Cursor cursor) {
        ClassInstance instance = new ClassInstance();
        instance.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        instance.setCourseId(cursor.getLong(cursor.getColumnIndex("course_id")));
        instance.setDate(cursor.getString(cursor.getColumnIndex("date")));
        instance.setTeacher(cursor.getString(cursor.getColumnIndex("teacher")));
        instance.setComments(cursor.getString(cursor.getColumnIndex("comments")));
        instance.setPhotoPath(cursor.getString(cursor.getColumnIndex("photo_path")));
        instance.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
        instance.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        instance.setSyncStatus(cursor.getInt(cursor.getColumnIndex("sync_status")));
        return instance;
    }
    
    @android.annotation.SuppressLint("Range")
    private User createUserFromCursor(android.database.Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        user.setName(cursor.getString(cursor.getColumnIndex("name")));
        user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
        user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
        user.setRole(cursor.getString(cursor.getColumnIndex("role")));
        user.setCreatedDate(cursor.getString(cursor.getColumnIndex("created_date")));
        return user;
    }
} 