package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements YogaCourseAdapter.OnCourseActionListener {
    private DatabaseHelper helper;
    private RecyclerView recyclerViewCourses;
    private YogaCourseAdapter courseAdapter;
    private TextView tvCourseCount, tvInstanceCount, tvEmptyState;
    private List<YogaCourse> courseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        helper = MainActivity.helper;
        if (helper == null) {
            helper = new DatabaseHelper(getContext());
        }

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners(view);
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourses();
        updateStatistics();
    }

    private void initializeViews(View view) {
        recyclerViewCourses = view.findViewById(R.id.recyclerViewCourses);
        tvCourseCount = view.findViewById(R.id.tvCourseCount);
        tvInstanceCount = view.findViewById(R.id.tvInstanceCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        courseAdapter = new YogaCourseAdapter(courseList, this);
        recyclerViewCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCourses.setAdapter(courseAdapter);
    }

    private void setupClickListeners(View view) {
        FloatingActionButton fabAddCourse = view.findViewById(R.id.fabAddCourse);
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateYogaCourse.class);
            startActivity(intent);
        });

        MaterialButton btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            startActivity(intent);
        });

        MaterialButton btnSync = view.findViewById(R.id.btnSync);
        btnSync.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SyncActivity.class);
            startActivity(intent);
        });

        MaterialButton btnResetDb = view.findViewById(R.id.btnResetDb);
        btnResetDb.setOnClickListener(v -> showResetDatabaseDialog());
    }

    private void loadCourses() {
        courseList.clear();
        Cursor cursor = helper.readAllYogaCourse();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                YogaCourse course = createCourseFromCursor(cursor);
                courseList.add(course);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        courseAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @SuppressLint("Range")
    private YogaCourse createCourseFromCursor(Cursor cursor) {
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
        course.setInstructor(cursor.getString(cursor.getColumnIndex("instructor")));
        return course;
    }

    private void updateStatistics() {
        Cursor courseCursor = helper.readAllYogaCourse();
        int courseCount = courseCursor != null ? courseCursor.getCount() : 0;
        if (courseCursor != null) courseCursor.close();
        tvCourseCount.setText(String.valueOf(courseCount));

        Cursor instanceCursor = helper.readAllClassInstances();
        int instanceCount = instanceCursor != null ? instanceCursor.getCount() : 0;
        if (instanceCursor != null) instanceCursor.close();
        tvInstanceCount.setText(String.valueOf(instanceCount));
    }

    private void updateEmptyState() {
        if (courseList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewCourses.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewCourses.setVisibility(View.VISIBLE);
        }
    }

    private void showResetDatabaseDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Reset Database")
                .setMessage(R.string.confirm_reset_database)
                .setPositiveButton("Reset", (dialog, which) -> {
                    helper.resetDatabase();
                    loadCourses();
                    updateStatistics();
                    Toast.makeText(getContext(), "Database reset successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditCourse(YogaCourse course) {
        Intent intent = new Intent(getActivity(), EditYogaCourse.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteCourse(YogaCourse course) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Course")
                .setMessage(R.string.confirm_delete_course)
                .setPositiveButton("Delete", (dialog, which) -> {
                    helper.deleteYogaCourse(course.getId());
                    loadCourses();
                    updateStatistics();
                    Toast.makeText(getContext(), R.string.course_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onViewInstances(YogaCourse course) {
        Intent intent = new Intent(getActivity(), ClassInstanceActivity.class);
        intent.putExtra("course_id", course.getId());
        intent.putExtra("course_name", course.getType());
        startActivity(intent);
    }

    @Override
    public void onCourseClick(YogaCourse course) {
        Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
        intent.putExtra("course_id", course.getId());
        startActivity(intent);
    }
} 