package com.example.universalyoganative;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment implements UserAdapter.OnUserActionListener {
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private TextView tvUserCount, tvEmptyState;
    private List<User> userList;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        dbHelper = MainActivity.helper;
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext());
        }

        initializeViews(view);
        setupRecyclerView();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initializeViews(View view) {
        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers);
        tvUserCount = view.findViewById(R.id.tvUserCount);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupRecyclerView() {
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
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
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteUser(user.getId());
                    Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 