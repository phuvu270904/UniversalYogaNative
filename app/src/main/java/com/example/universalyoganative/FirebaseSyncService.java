package com.example.universalyoganative;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSyncService {
    private static final String TAG = "FirebaseSyncService";
    
    // Firestore collection names
    private static final String COLLECTION_YOGA_COURSES = "yoga_courses";
    private static final String COLLECTION_CLASS_INSTANCES = "class_instances";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_BOOKINGS = "bookings";
    
    private final FirebaseFirestore db;
    private final DatabaseHelper databaseHelper;
    private final Context context;
    private final Handler timeoutHandler;
    private static final long SYNC_TIMEOUT_MS = 30000; // 30 seconds timeout
    
    // Sync callbacks
    public interface SyncCallback {
        void onSyncStarted();
        void onSyncProgress(String message);
        void onSyncCompleted(boolean success, String message);
        void onSyncError(String error);
    }
    
    public FirebaseSyncService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.databaseHelper = new DatabaseHelper(context);
        this.timeoutHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Check if network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * Perform complete bidirectional sync
     */
    public void performFullSync(SyncCallback callback) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            if (callback != null) {
                callback.onSyncError("No network connection available");
            }
            return;
        }
        
        if (callback != null) {
            callback.onSyncStarted();
        }
        
        // Set timeout for sync operation
        timeoutHandler.postDelayed(() -> {
            if (callback != null) {
                callback.onSyncError("Sync operation timed out after 30 seconds");
            }
        }, SYNC_TIMEOUT_MS);
        
        // Step 1: Pull data from Firestore to local database
        pullFromFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pulling data from cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                if (success) {
                    // Step 2: Push local data to Firestore
                    pushToFirestore(new SyncCallback() {
                        @Override
                        public void onSyncStarted() {
                            if (callback != null) {
                                callback.onSyncProgress("Pushing data to cloud...");
                            }
                        }
                        
                        @Override
                        public void onSyncProgress(String message) {
                            if (callback != null) {
                                callback.onSyncProgress(message);
                            }
                        }
                        
                                                            @Override
                                    public void onSyncCompleted(boolean success, String message) {
                                        if (success) {
                                            // Step 3: Clean up deleted records
                                            cleanupDeletedRecords(new SyncCallback() {
                                                @Override
                                                public void onSyncStarted() {
                                                    if (callback != null) {
                                                        callback.onSyncProgress("Cleaning up deleted records...");
                                                    }
                                                }
                                                
                                                @Override
                                                public void onSyncProgress(String message) {
                                                    if (callback != null) {
                                                        callback.onSyncProgress(message);
                                                    }
                                                }
                                                
                                                @Override
                                                public void onSyncCompleted(boolean success, String message) {
                                                    // Clear timeout
                                                    timeoutHandler.removeCallbacksAndMessages(null);
                                                    if (callback != null) {
                                                        callback.onSyncCompleted(true, "Sync completed successfully");
                                                    }
                                                }
                                                
                                                @Override
                                                public void onSyncError(String error) {
                                                    // Clear timeout
                                                    timeoutHandler.removeCallbacksAndMessages(null);
                                                    if (callback != null) {
                                                        callback.onSyncError("Cleanup error: " + error);
                                                    }
                                                }
                                            });
                                        } else {
                                            // Clear timeout
                                            timeoutHandler.removeCallbacksAndMessages(null);
                                            if (callback != null) {
                                                callback.onSyncError("Push failed: " + message);
                                            }
                                        }
                                    }
                        
                        @Override
                        public void onSyncError(String error) {
                            if (callback != null) {
                                callback.onSyncError("Push error: " + error);
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onSyncError("Pull failed: " + message);
                    }
                }
            }
            
            @Override
            public void onSyncError(String error) {
                // Clear timeout
                timeoutHandler.removeCallbacksAndMessages(null);
                if (callback != null) {
                    callback.onSyncError("Pull error: " + error);
                }
            }
        });
    }
    
    /**
     * Pull all data from Firestore to local database
     */
    private void pullFromFirestore(SyncCallback callback) {
        final int[] completedOperations = {0};
        final int totalOperations = 4; // 4 collections to sync
        
        // Pull Users
        pullUsersFromFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pulling users from cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedOperations[0]++;
                if (completedOperations[0] == totalOperations) {
                    if (callback != null) {
                        callback.onSyncCompleted(true, "Pull completed");
                    }
                }
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Users pull error: " + error);
                }
            }
        });
        
        // Pull Yoga Courses
        pullYogaCoursesFromFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pulling yoga courses from cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedOperations[0]++;
                if (completedOperations[0] == totalOperations) {
                    if (callback != null) {
                        callback.onSyncCompleted(true, "Pull completed");
                    }
                }
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Yoga courses pull error: " + error);
                }
            }
        });
        
        // Pull Class Instances
        pullClassInstancesFromFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pulling class instances from cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedOperations[0]++;
                if (completedOperations[0] == totalOperations) {
                    if (callback != null) {
                        callback.onSyncCompleted(true, "Pull completed");
                    }
                }
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Class instances pull error: " + error);
                }
            }
        });
        
        // Pull Bookings
        pullBookingsFromFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pulling bookings from cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedOperations[0]++;
                if (completedOperations[0] == totalOperations) {
                    if (callback != null) {
                        callback.onSyncCompleted(true, "Pull completed");
                    }
                }
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Bookings pull error: " + error);
                }
            }
        });
    }
    
    /**
     * Pull users from Firestore
     */
    private void pullUsersFromFirestore(SyncCallback callback) {
        db.collection(COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int updatedCount = 0;
                        int errorCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> userData = document.getData();
                                String firestoreId = document.getId();
                                
                                // Check if user exists locally by Firestore ID first
                                User existingUser = databaseHelper.getUserByFirestoreId(firestoreId);
                                
                                if (existingUser == null) {
                                    // Check if user exists by email (for backward compatibility)
                                    String email = (String) userData.get("email");
                                    if (email != null) {
                                        existingUser = databaseHelper.getUserByEmail(email);
                                    }
                                    
                                    if (existingUser == null) {
                                        // Create new user
                                        long userId = databaseHelper.registerUser(
                                                (String) userData.get("name"),
                                                email,
                                                (String) userData.get("password"),
                                                (String) userData.get("role")
                                        );
                                        if (userId > 0) {
                                            // Mark as synced and store Firestore ID
                                            databaseHelper.markUserSynced(userId);
                                            databaseHelper.updateUserFirestoreId(userId, firestoreId);
                                            updatedCount++;
                                        }
                                    } else {
                                        // Update existing user and store Firestore ID
                                        databaseHelper.updateUser(
                                                existingUser.getId(),
                                                (String) userData.get("name"),
                                                email,
                                                (String) userData.get("role")
                                        );
                                        databaseHelper.markUserSynced(existingUser.getId());
                                        databaseHelper.updateUserFirestoreId(existingUser.getId(), firestoreId);
                                        updatedCount++;
                                    }
                                } else {
                                    // User exists with this Firestore ID, update if needed
                                    String firestoreCreatedDate = (String) userData.get("created_date");
                                    if (firestoreCreatedDate != null && 
                                        Long.parseLong(firestoreCreatedDate) > Long.parseLong(existingUser.getCreatedDate())) {
                                        
                                        databaseHelper.updateUser(
                                                existingUser.getId(),
                                                (String) userData.get("name"),
                                                (String) userData.get("email"),
                                                (String) userData.get("role")
                                        );
                                        databaseHelper.markUserSynced(existingUser.getId());
                                        updatedCount++;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing user document: " + e.getMessage());
                                errorCount++;
                            }
                        }
                        Log.d(TAG, "Pulled " + updatedCount + " users from Firestore, " + errorCount + " errors");
                        if (callback != null) {
                            String message = "Users pulled: " + updatedCount;
                            if (errorCount > 0) {
                                message += " (" + errorCount + " errors)";
                            }
                            callback.onSyncCompleted(true, message);
                        }
                    } else {
                        Log.e(TAG, "Error getting users from Firestore", task.getException());
                        if (callback != null) {
                            callback.onSyncError("Failed to get users: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    /**
     * Pull yoga courses from Firestore
     */
    private void pullYogaCoursesFromFirestore(SyncCallback callback) {
        db.collection(COLLECTION_YOGA_COURSES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int updatedCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> courseData = document.getData();
                                String firestoreId = document.getId();
                                
                                // Check if course exists locally by Firestore ID
                                Cursor existingCourse = databaseHelper.getCourseByFirestoreId(firestoreId);
                                
                                if (existingCourse == null || existingCourse.getCount() == 0) {
                                    // Course doesn't exist, create new one
                                    String dayOfWeek = (String) courseData.get("dayofweek");
                                    String time = (String) courseData.get("time");
                                    String type = (String) courseData.get("type");
                                    
                                    if (dayOfWeek != null && time != null && type != null) {
                                        long courseId = databaseHelper.createNewYogaCourse(
                                                dayOfWeek,
                                                time,
                                                ((Number) courseData.get("capacity")).intValue(),
                                                ((Number) courseData.get("duration")).intValue(),
                                                ((Number) courseData.get("price")).floatValue(),
                                                type,
                                                (String) courseData.get("description"),
                                                (String) courseData.get("difficulty"),
                                                (String) courseData.get("location")
                                        );
                                        
                                        if (courseId > 0) {
                                            databaseHelper.markCourseSynced(courseId);
                                            databaseHelper.updateCourseFirestoreId(courseId, firestoreId);
                                            updatedCount++;
                                        }
                                    }
                                } else {
                                    // Course exists, update if needed
                                    existingCourse.close();
                                    // For now, we'll skip updating existing courses to avoid conflicts
                                    // In a production app, you'd implement conflict resolution logic
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing course document: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "Pulled " + updatedCount + " yoga courses from Firestore");
                        if (callback != null) {
                            callback.onSyncCompleted(true, "Courses pulled: " + updatedCount);
                        }
                    } else {
                        Log.e(TAG, "Error getting courses from Firestore", task.getException());
                        if (callback != null) {
                            callback.onSyncError("Failed to get courses: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    /**
     * Pull class instances from Firestore
     */
    private void pullClassInstancesFromFirestore(SyncCallback callback) {
        db.collection(COLLECTION_CLASS_INSTANCES)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int updatedCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> instanceData = document.getData();
                                String firestoreId = document.getId();
                                
                                // Check if instance exists locally by Firestore ID
                                Cursor existingInstance = databaseHelper.getInstanceByFirestoreId(firestoreId);
                                
                                if (existingInstance == null || existingInstance.getCount() == 0) {
                                    // Instance doesn't exist, create new one
                                    String firestoreCourseId = (String) instanceData.get("course_id");
                                    String date = (String) instanceData.get("date");
                                    String teacher = (String) instanceData.get("teacher");
                                    
                                    // Convert Firestore course ID to local course ID
                                    long localCourseId = databaseHelper.getCourseIdByFirestoreId(firestoreCourseId);
                                    
                                    if (localCourseId > 0 && date != null && teacher != null) {
                                        long instanceId = databaseHelper.createClassInstance(
                                                localCourseId, // Use local course ID
                                                date,
                                                teacher,
                                                (String) instanceData.get("comments"),
                                                (String) instanceData.get("photo_path"),
                                                ((Number) instanceData.get("latitude")).doubleValue(),
                                                ((Number) instanceData.get("longitude")).doubleValue()
                                        );
                                        
                                        if (instanceId > 0) {
                                            databaseHelper.markInstanceSynced(instanceId);
                                            databaseHelper.updateInstanceFirestoreId(instanceId, firestoreId);
                                            updatedCount++;
                                        }
                                    } else if (localCourseId <= 0) {
                                        Log.w(TAG, "Cannot create class instance - course with Firestore ID " + firestoreCourseId + " not found locally. Course may need to be synced first.");
                                    }
                                } else {
                                    // Instance exists, skip to avoid conflicts
                                    existingInstance.close();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing instance document: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "Pulled " + updatedCount + " class instances from Firestore");
                        if (callback != null) {
                            callback.onSyncCompleted(true, "Instances pulled: " + updatedCount);
                        }
                    } else {
                        Log.e(TAG, "Error getting instances from Firestore", task.getException());
                        if (callback != null) {
                            callback.onSyncError("Failed to get instances: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    /**
     * Pull bookings from Firestore
     */
    private void pullBookingsFromFirestore(SyncCallback callback) {
        db.collection(COLLECTION_BOOKINGS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int updatedCount = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Map<String, Object> bookingData = document.getData();
                                String firestoreId = document.getId();
                                
                                // Check if booking exists locally by Firestore ID
                                Cursor existingBooking = databaseHelper.getBookingByFirestoreId(firestoreId);
                                
                                if (existingBooking == null || existingBooking.getCount() == 0) {
                                    // Booking doesn't exist, create new one
                                    String firestoreClassId = (String) bookingData.get("class_id");
                                    String firestoreUserId = (String) bookingData.get("user_id");
                                    
                                    // Convert Firestore IDs to local IDs
                                    long localClassId = databaseHelper.getInstanceIdByFirestoreId(firestoreClassId);
                                    long localUserId = databaseHelper.getUserIdByFirestoreId(firestoreUserId);
                                    
                                    if (localClassId > 0 && localUserId > 0) {
                                        long bookingId = databaseHelper.createBooking((int) localClassId, localUserId);
                                        if (bookingId > 0) {
                                            databaseHelper.markBookingSynced(bookingId);
                                            databaseHelper.updateBookingFirestoreId(bookingId, firestoreId);
                                            updatedCount++;
                                        }
                                    } else {
                                        if (localClassId <= 0) {
                                            Log.w(TAG, "Cannot create booking - class instance with Firestore ID " + firestoreClassId + " not found locally.");
                                        }
                                        if (localUserId <= 0) {
                                            Log.w(TAG, "Cannot create booking - user with Firestore ID " + firestoreUserId + " not found locally.");
                                        }
                                    }
                                } else {
                                    // Booking exists, skip to avoid conflicts
                                    existingBooking.close();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing booking document: " + e.getMessage());
                            }
                        }
                        Log.d(TAG, "Pulled " + updatedCount + " bookings from Firestore");
                        if (callback != null) {
                            callback.onSyncCompleted(true, "Bookings pulled: " + updatedCount);
                        }
                    } else {
                        Log.e(TAG, "Error getting bookings from Firestore", task.getException());
                        if (callback != null) {
                            callback.onSyncError("Failed to get bookings: " + task.getException().getMessage());
                        }
                    }
                });
    }
    
    /**
     * Push local data to Firestore (sequential to maintain dependencies)
     */
    private void pushToFirestore(SyncCallback callback) {
        // Step 1: Push Users and Courses first (can be parallel)
        final int[] completedBasics = {0};
        final int basicsCount = 2;
        
        pushUsersToFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pushing users to cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedBasics[0]++;
                checkBasicsCompleted(callback, completedBasics[0], basicsCount);
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Users push error: " + error);
                }
            }
        });
        
        pushYogaCoursesToFirestore(new SyncCallback() {
            @Override
            public void onSyncStarted() {
                if (callback != null) {
                    callback.onSyncProgress("Pushing yoga courses to cloud...");
                }
            }
            
            @Override
            public void onSyncProgress(String message) {
                if (callback != null) {
                    callback.onSyncProgress(message);
                }
            }
            
            @Override
            public void onSyncCompleted(boolean success, String message) {
                completedBasics[0]++;
                checkBasicsCompleted(callback, completedBasics[0], basicsCount);
            }
            
            @Override
            public void onSyncError(String error) {
                if (callback != null) {
                    callback.onSyncError("Yoga courses push error: " + error);
                }
            }
        });
    }
    
    /**
     * Check if basic entities (users and courses) are completed, then proceed with dependent entities
     */
    private void checkBasicsCompleted(SyncCallback callback, int completed, int total) {
        if (completed == total) {
            // Step 2: Push Class Instances (depends on courses)
            pushClassInstancesToFirestore(new SyncCallback() {
                @Override
                public void onSyncStarted() {
                    if (callback != null) {
                        callback.onSyncProgress("Pushing class instances to cloud...");
                    }
                }
                
                @Override
                public void onSyncProgress(String message) {
                    if (callback != null) {
                        callback.onSyncProgress(message);
                    }
                }
                
                @Override
                public void onSyncCompleted(boolean success, String message) {
                    if (success) {
                        // Step 3: Push Bookings (depends on class instances and users)
                        pushBookingsToFirestore(new SyncCallback() {
                            @Override
                            public void onSyncStarted() {
                                if (callback != null) {
                                    callback.onSyncProgress("Pushing bookings to cloud...");
                                }
                            }
                            
                            @Override
                            public void onSyncProgress(String message) {
                                if (callback != null) {
                                    callback.onSyncProgress(message);
                                }
                            }
                            
                            @Override
                            public void onSyncCompleted(boolean success, String message) {
                                if (callback != null) {
                                    callback.onSyncCompleted(true, "Push completed");
                                }
                            }
                            
                            @Override
                            public void onSyncError(String error) {
                                if (callback != null) {
                                    callback.onSyncError("Bookings push error: " + error);
                                }
                            }
                        });
                    } else {
                        if (callback != null) {
                            callback.onSyncError("Class instances push failed: " + message);
                        }
                    }
                }
                
                @Override
                public void onSyncError(String error) {
                    if (callback != null) {
                        callback.onSyncError("Class instances push error: " + error);
                    }
                }
            });
        }
    }
    
    /**
     * Push users to Firestore
     */
    private void pushUsersToFirestore(SyncCallback callback) {
        Cursor cursor = databaseHelper.getUnsyncedUsers();
        if (cursor == null || cursor.getCount() == 0) {
            if (callback != null) {
                callback.onSyncCompleted(true, "No users to push");
            }
            return;
        }
        
        WriteBatch batch = db.batch();
        final int[] pushedCount = {0};
        final int[] deletedCount = {0};
        final List<Long> syncedUserIds = new ArrayList<>();
        final List<Long> deletedUserIds = new ArrayList<>();
        final Map<Long, String> userIdToFirestoreId = new HashMap<>();
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    User user = databaseHelper.createUserFromCursor(cursor);
                    String syncStatus = user.getSyncStatus();
                    
                    if (SyncStatus.EDITED.getValue().equals(syncStatus)) {
                        // Create or update user in Firestore
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", user.getName());
                        userData.put("email", user.getEmail());
                        userData.put("password", user.getPassword());
                        userData.put("role", user.getRole());
                        userData.put("created_date", user.getCreatedDate());
                        userData.put("sync_status", SyncStatus.SYNC.getValue());
                        
                        DocumentReference userRef;
                        String firestoreId = user.getFirestoreId();
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            // Update existing document
                            userRef = db.collection(COLLECTION_USERS).document(firestoreId);
                        } else {
                            // Create new document
                            userRef = db.collection(COLLECTION_USERS).document();
                        }
                        
                        batch.set(userRef, userData);
                        syncedUserIds.add(user.getId());
                        userIdToFirestoreId.put(user.getId(), userRef.getId());
                        pushedCount[0]++;
                        
                    } else if (SyncStatus.DELETED.getValue().equals(syncStatus)) {
                        // Delete from Firestore if it has a Firestore ID
                        String firestoreId = user.getFirestoreId();
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            DocumentReference userRef = db.collection(COLLECTION_USERS).document(firestoreId);
                            batch.delete(userRef);
                            deletedCount[0]++;
                        }
                        // Add to list for local deletion after successful batch commit
                        deletedUserIds.add(user.getId());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing user for push: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        if (pushedCount[0] > 0 || deletedCount[0] > 0) {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mark local records as synced and store Firestore IDs after successful commit
                        for (Long userId : syncedUserIds) {
                            databaseHelper.markUserSynced(userId);
                            String firestoreId = userIdToFirestoreId.get(userId);
                            if (firestoreId != null) {
                                databaseHelper.updateUserFirestoreId(userId, firestoreId);
                            }
                        }
                        
                        // Delete local records that were successfully deleted from Firestore
                        for (Long userId : deletedUserIds) {
                            databaseHelper.getWritableDatabase().delete(
                                    DatabaseHelper.TABLE_USER,
                                    "_id = ?",
                                    new String[]{String.valueOf(userId)}
                            );
                        }
                        
                        Log.d(TAG, "Successfully pushed " + pushedCount[0] + " users and deleted " + deletedCount[0] + " users");
                        if (callback != null) {
                            String message = "Users: " + pushedCount[0] + " pushed";
                            if (deletedCount[0] > 0) {
                                message += ", " + deletedCount[0] + " deleted";
                            }
                            callback.onSyncCompleted(true, message);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error pushing users to Firestore", e);
                        if (callback != null) {
                            callback.onSyncError("Failed to push users: " + e.getMessage());
                        }
                    });
        } else {
            if (callback != null) {
                callback.onSyncCompleted(true, "No users to push");
            }
        }
    }
    
    /**
     * Push yoga courses to Firestore
     */
    private void pushYogaCoursesToFirestore(SyncCallback callback) {
        Cursor cursor = databaseHelper.getUnsyncedCourses();
        if (cursor == null || cursor.getCount() == 0) {
            if (callback != null) {
                callback.onSyncCompleted(true, "No courses to push");
            }
            return;
        }
        
        WriteBatch batch = db.batch();
        final int[] pushedCount = {0};
        final int[] deletedCount = {0};
        final List<Long> syncedCourseIds = new ArrayList<>();
        final List<Long> deletedCourseIds = new ArrayList<>();
        final Map<Long, String> courseIdToFirestoreId = new HashMap<>();
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    long id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String syncStatus = cursor.getString(cursor.getColumnIndex("sync_status"));
                    
                    if (SyncStatus.EDITED.getValue().equals(syncStatus)) {
                        Map<String, Object> courseData = new HashMap<>();
                        courseData.put("dayofweek", cursor.getString(cursor.getColumnIndex("dayofweek")));
                        courseData.put("time", cursor.getString(cursor.getColumnIndex("time")));
                        courseData.put("capacity", cursor.getInt(cursor.getColumnIndex("capacity")));
                        courseData.put("duration", cursor.getInt(cursor.getColumnIndex("duration")));
                        courseData.put("price", cursor.getFloat(cursor.getColumnIndex("price")));
                        courseData.put("type", cursor.getString(cursor.getColumnIndex("type")));
                        courseData.put("description", cursor.getString(cursor.getColumnIndex("description")));
                        courseData.put("difficulty", cursor.getString(cursor.getColumnIndex("difficulty")));
                        courseData.put("location", cursor.getString(cursor.getColumnIndex("location")));
                        courseData.put("created_date", cursor.getString(cursor.getColumnIndex("created_date")));
                        courseData.put("sync_status", SyncStatus.SYNC.getValue());
                        
                        DocumentReference courseRef;
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            // Update existing document
                            courseRef = db.collection(COLLECTION_YOGA_COURSES).document(firestoreId);
                        } else {
                            // Create new document
                            courseRef = db.collection(COLLECTION_YOGA_COURSES).document();
                        }
                        
                        batch.set(courseRef, courseData);
                        syncedCourseIds.add(id);
                        courseIdToFirestoreId.put(id, courseRef.getId());
                        pushedCount[0]++;
                        
                    } else if (SyncStatus.DELETED.getValue().equals(syncStatus)) {
                        // Delete from Firestore if it has a Firestore ID
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            DocumentReference courseRef = db.collection(COLLECTION_YOGA_COURSES).document(firestoreId);
                            batch.delete(courseRef);
                            deletedCount[0]++;
                        }
                        // Add to list for local deletion after successful batch commit
                        deletedCourseIds.add(id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing course for push: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        if (pushedCount[0] > 0 || deletedCount[0] > 0) {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mark local records as synced and store Firestore IDs after successful commit
                        for (Long courseId : syncedCourseIds) {
                            databaseHelper.markCourseSynced(courseId);
                            String firestoreId = courseIdToFirestoreId.get(courseId);
                            if (firestoreId != null) {
                                databaseHelper.updateCourseFirestoreId(courseId, firestoreId);
                            }
                        }
                        
                        // Delete local records that were successfully deleted from Firestore
                        for (Long courseId : deletedCourseIds) {
                            databaseHelper.getWritableDatabase().delete(
                                    "YogaCourse",
                                    "_id = ?",
                                    new String[]{String.valueOf(courseId)}
                            );
                        }
                        
                        Log.d(TAG, "Successfully pushed " + pushedCount[0] + " courses and deleted " + deletedCount[0] + " courses");
                        if (callback != null) {
                            String message = "Courses: " + pushedCount[0] + " pushed";
                            if (deletedCount[0] > 0) {
                                message += ", " + deletedCount[0] + " deleted";
                            }
                            callback.onSyncCompleted(true, message);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error pushing courses to Firestore", e);
                        if (callback != null) {
                            callback.onSyncError("Failed to push courses: " + e.getMessage());
                        }
                    });
        } else {
            if (callback != null) {
                callback.onSyncCompleted(true, "No courses to push");
            }
        }
    }
    
    /**
     * Push class instances to Firestore
     */
    private void pushClassInstancesToFirestore(SyncCallback callback) {
        Cursor cursor = databaseHelper.getUnsyncedInstances();
        if (cursor == null || cursor.getCount() == 0) {
            if (callback != null) {
                callback.onSyncCompleted(true, "No instances to push");
            }
            return;
        }
        
        WriteBatch batch = db.batch();
        final int[] pushedCount = {0};
        final int[] deletedCount = {0};
        final List<Long> syncedInstanceIds = new ArrayList<>();
        final List<Long> deletedInstanceIds = new ArrayList<>();
        final Map<Long, String> instanceIdToFirestoreId = new HashMap<>();
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    long id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String syncStatus = cursor.getString(cursor.getColumnIndex("sync_status"));
                    
                    if (SyncStatus.EDITED.getValue().equals(syncStatus)) {
                        Map<String, Object> instanceData = new HashMap<>();
                        
                        // Get local course ID and convert it to Firestore course ID
                        long localCourseId = cursor.getLong(cursor.getColumnIndex("course_id"));
                        String firestoreCourseId = databaseHelper.getCourseFirestoreId(localCourseId);
                        
                        if (firestoreCourseId == null || firestoreCourseId.isEmpty()) {
                            Log.w(TAG, "Cannot push class instance - course with local ID " + localCourseId + " has no Firestore ID. Course may need to be synced first.");
                            continue; // Skip this instance if course isn't synced yet
                        }
                        
                        instanceData.put("course_id", firestoreCourseId); // Use Firestore course ID instead of local ID
                        instanceData.put("date", cursor.getString(cursor.getColumnIndex("date")));
                        instanceData.put("teacher", cursor.getString(cursor.getColumnIndex("teacher")));
                        instanceData.put("comments", cursor.getString(cursor.getColumnIndex("comments")));
                        instanceData.put("photo_path", cursor.getString(cursor.getColumnIndex("photo_path")));
                        instanceData.put("latitude", cursor.getDouble(cursor.getColumnIndex("latitude")));
                        instanceData.put("longitude", cursor.getDouble(cursor.getColumnIndex("longitude")));
                        instanceData.put("sync_status", SyncStatus.SYNC.getValue());
                        
                        DocumentReference instanceRef;
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            // Update existing document
                            instanceRef = db.collection(COLLECTION_CLASS_INSTANCES).document(firestoreId);
                        } else {
                            // Create new document
                            instanceRef = db.collection(COLLECTION_CLASS_INSTANCES).document();
                        }
                        
                        batch.set(instanceRef, instanceData);
                        syncedInstanceIds.add(id);
                        instanceIdToFirestoreId.put(id, instanceRef.getId());
                        pushedCount[0]++;
                        
                    } else if (SyncStatus.DELETED.getValue().equals(syncStatus)) {
                        // Delete from Firestore if it has a Firestore ID
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            DocumentReference instanceRef = db.collection(COLLECTION_CLASS_INSTANCES).document(firestoreId);
                            batch.delete(instanceRef);
                            deletedCount[0]++;
                        }
                        // Add to list for local deletion after successful batch commit
                        deletedInstanceIds.add(id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing instance for push: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        if (pushedCount[0] > 0 || deletedCount[0] > 0) {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mark local records as synced and store Firestore IDs after successful commit
                        for (Long instanceId : syncedInstanceIds) {
                            databaseHelper.markInstanceSynced(instanceId);
                            String firestoreId = instanceIdToFirestoreId.get(instanceId);
                            if (firestoreId != null) {
                                databaseHelper.updateInstanceFirestoreId(instanceId, firestoreId);
                            }
                        }
                        
                        // Delete local records that were successfully deleted from Firestore
                        for (Long instanceId : deletedInstanceIds) {
                            databaseHelper.getWritableDatabase().delete(
                                    "ClassInstance",
                                    "_id = ?",
                                    new String[]{String.valueOf(instanceId)}
                            );
                        }
                        
                        Log.d(TAG, "Successfully pushed " + pushedCount[0] + " instances and deleted " + deletedCount[0] + " instances");
                        if (callback != null) {
                            String message = "Instances: " + pushedCount[0] + " pushed";
                            if (deletedCount[0] > 0) {
                                message += ", " + deletedCount[0] + " deleted";
                            }
                            callback.onSyncCompleted(true, message);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error pushing instances to Firestore", e);
                        if (callback != null) {
                            callback.onSyncError("Failed to push instances: " + e.getMessage());
                        }
                    });
        } else {
            if (callback != null) {
                callback.onSyncCompleted(true, "No instances to push");
            }
        }
    }
    
    /**
     * Push bookings to Firestore
     */
    private void pushBookingsToFirestore(SyncCallback callback) {
        Cursor cursor = databaseHelper.getUnsyncedBookings();
        if (cursor == null || cursor.getCount() == 0) {
            if (callback != null) {
                callback.onSyncCompleted(true, "No bookings to push");
            }
            return;
        }
        
        WriteBatch batch = db.batch();
        final int[] pushedCount = {0};
        final int[] deletedCount = {0};
        final List<Long> syncedBookingIds = new ArrayList<>();
        final List<Long> deletedBookingIds = new ArrayList<>();
        final Map<Long, String> bookingIdToFirestoreId = new HashMap<>();
        
        if (cursor.moveToFirst()) {
            do {
                try {
                    long id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String syncStatus = cursor.getString(cursor.getColumnIndex("sync_status"));
                    
                    if (SyncStatus.EDITED.getValue().equals(syncStatus)) {
                        Map<String, Object> bookingData = new HashMap<>();
                        
                        // Get local class instance ID and user ID, convert them to Firestore IDs
                        long localClassId = cursor.getLong(cursor.getColumnIndex("class_id"));
                        long localUserId = cursor.getLong(cursor.getColumnIndex("user_id"));
                        
                        String firestoreClassId = databaseHelper.getInstanceFirestoreId(localClassId);
                        String firestoreUserId = databaseHelper.getUserFirestoreId(localUserId);
                        
                        if (firestoreClassId == null || firestoreClassId.isEmpty()) {
                            Log.w(TAG, "Cannot push booking - class instance with local ID " + localClassId + " has no Firestore ID. Instance may need to be synced first.");
                            continue; // Skip this booking if class instance isn't synced yet
                        }
                        
                        if (firestoreUserId == null || firestoreUserId.isEmpty()) {
                            Log.w(TAG, "Cannot push booking - user with local ID " + localUserId + " has no Firestore ID. User may need to be synced first.");
                            continue; // Skip this booking if user isn't synced yet
                        }
                        
                        bookingData.put("class_id", firestoreClassId); // Use Firestore class instance ID
                        bookingData.put("user_id", firestoreUserId); // Use Firestore user ID
                        bookingData.put("sync_status", SyncStatus.SYNC.getValue());
                        
                        DocumentReference bookingRef;
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            // Update existing document
                            bookingRef = db.collection(COLLECTION_BOOKINGS).document(firestoreId);
                        } else {
                            // Create new document
                            bookingRef = db.collection(COLLECTION_BOOKINGS).document();
                        }
                        
                        batch.set(bookingRef, bookingData);
                        syncedBookingIds.add(id);
                        bookingIdToFirestoreId.put(id, bookingRef.getId());
                        pushedCount[0]++;
                        
                    } else if (SyncStatus.DELETED.getValue().equals(syncStatus)) {
                        // Delete from Firestore if it has a Firestore ID
                        String firestoreId = cursor.getString(cursor.getColumnIndex("firestore_id"));
                        if (firestoreId != null && !firestoreId.isEmpty()) {
                            DocumentReference bookingRef = db.collection(COLLECTION_BOOKINGS).document(firestoreId);
                            batch.delete(bookingRef);
                            deletedCount[0]++;
                        }
                        // Add to list for local deletion after successful batch commit
                        deletedBookingIds.add(id);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing booking for push: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        if (pushedCount[0] > 0 || deletedCount[0] > 0) {
            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mark local records as synced and store Firestore IDs after successful commit
                        for (Long bookingId : syncedBookingIds) {
                            databaseHelper.markBookingSynced(bookingId);
                            String firestoreId = bookingIdToFirestoreId.get(bookingId);
                            if (firestoreId != null) {
                                databaseHelper.updateBookingFirestoreId(bookingId, firestoreId);
                            }
                        }
                        
                        // Delete local records that were successfully deleted from Firestore
                        for (Long bookingId : deletedBookingIds) {
                            databaseHelper.getWritableDatabase().delete(
                                    "Bookings",
                                    "_id = ?",
                                    new String[]{String.valueOf(bookingId)}
                            );
                        }
                        
                        Log.d(TAG, "Successfully pushed " + pushedCount[0] + " bookings and deleted " + deletedCount[0] + " bookings");
                        if (callback != null) {
                            String message = "Bookings: " + pushedCount[0] + " pushed";
                            if (deletedCount[0] > 0) {
                                message += ", " + deletedCount[0] + " deleted";
                            }
                            callback.onSyncCompleted(true, message);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error pushing bookings to Firestore", e);
                        if (callback != null) {
                            callback.onSyncError("Failed to push bookings: " + e.getMessage());
                        }
                    });
        } else {
            if (callback != null) {
                callback.onSyncCompleted(true, "No bookings to push");
            }
        }
    }
    
    /**
     * Clean up deleted records that were never synced to Firestore
     */
    private void cleanupDeletedRecords(SyncCallback callback) {
        // Delete records with sync_status = 'deleted' that don't have firestore_id
        // (these were created locally but deleted before being synced)
        int deletedCount = 0;
        
        // Delete locally-created deleted users
        Cursor userCursor = databaseHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USER,
                new String[]{"_id"},
                "sync_status = ? AND (firestore_id IS NULL OR firestore_id = '')",
                new String[]{SyncStatus.DELETED.getValue()},
                null, null, null
        );
        
        if (userCursor != null && userCursor.moveToFirst()) {
            do {
                long userId = userCursor.getLong(0);
                databaseHelper.getWritableDatabase().delete(
                        DatabaseHelper.TABLE_USER,
                        "_id = ?",
                        new String[]{String.valueOf(userId)}
                );
                deletedCount++;
            } while (userCursor.moveToNext());
            userCursor.close();
        }
        
        // Delete locally-created deleted courses
        Cursor courseCursor = databaseHelper.getReadableDatabase().query(
                "YogaCourse",
                new String[]{"_id"},
                "sync_status = ? AND (firestore_id IS NULL OR firestore_id = '')",
                new String[]{SyncStatus.DELETED.getValue()},
                null, null, null
        );
        
        if (courseCursor != null && courseCursor.moveToFirst()) {
            do {
                long courseId = courseCursor.getLong(0);
                databaseHelper.getWritableDatabase().delete(
                        "YogaCourse",
                        "_id = ?",
                        new String[]{String.valueOf(courseId)}
                );
                deletedCount++;
            } while (courseCursor.moveToNext());
            courseCursor.close();
        }
        
        // Delete locally-created deleted instances
        Cursor instanceCursor = databaseHelper.getReadableDatabase().query(
                "ClassInstance",
                new String[]{"_id"},
                "sync_status = ? AND (firestore_id IS NULL OR firestore_id = '')",
                new String[]{SyncStatus.DELETED.getValue()},
                null, null, null
        );
        
        if (instanceCursor != null && instanceCursor.moveToFirst()) {
            do {
                long instanceId = instanceCursor.getLong(0);
                databaseHelper.getWritableDatabase().delete(
                        "ClassInstance",
                        "_id = ?",
                        new String[]{String.valueOf(instanceId)}
                );
                deletedCount++;
            } while (instanceCursor.moveToNext());
            instanceCursor.close();
        }
        
        // Delete locally-created deleted bookings
        Cursor bookingCursor = databaseHelper.getReadableDatabase().query(
                "Bookings",
                new String[]{"_id"},
                "sync_status = ? AND (firestore_id IS NULL OR firestore_id = '')",
                new String[]{SyncStatus.DELETED.getValue()},
                null, null, null
        );
        
        if (bookingCursor != null && bookingCursor.moveToFirst()) {
            do {
                long bookingId = bookingCursor.getLong(0);
                databaseHelper.getWritableDatabase().delete(
                        "Bookings",
                        "_id = ?",
                        new String[]{String.valueOf(bookingId)}
                );
                deletedCount++;
            } while (bookingCursor.moveToNext());
            bookingCursor.close();
        }
        
        Log.d(TAG, "Cleaned up " + deletedCount + " locally-created deleted records");
        if (callback != null) {
            String message = deletedCount > 0 ? 
                "Cleanup completed: " + deletedCount + " local records removed" :
                "Cleanup completed: no local cleanup needed";
            callback.onSyncCompleted(true, message);
        }
    }
} 