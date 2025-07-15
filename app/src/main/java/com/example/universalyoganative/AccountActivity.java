package com.example.universalyoganative;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends BaseActivity implements UserAdapter.OnUserActionListener {
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private TextView tvUserCount, tvEmptyState;
    private List<User> userList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        setupRecyclerView();
        loadUsers();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            int statusBarHeight = 0;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
            
            v.setPadding(systemBars.left, statusBarHeight, systemBars.right, 0);
            return insets;
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_account;
    }

    private void initializeViews() {
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        tvUserCount = findViewById(R.id.tvUserCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(userAdapter);
    }

    @SuppressLint("Range")
    private void loadUsers() {
        userList.clear();
        Cursor cursor = dbHelper.readAllUsers();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)));
                user.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL)));
                userList.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }

        userAdapter.updateData(userList);
        updateUI();
    }

    private void updateUI() {
        tvUserCount.setText("Total Users: " + userList.size());
        
        if (userList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewUsers.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteUser(user.getId());
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
} 