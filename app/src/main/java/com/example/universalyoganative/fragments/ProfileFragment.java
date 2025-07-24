package com.example.universalyoganative;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class ProfileFragment extends Fragment {
    private SessionManager sessionManager;
    private TextView tvName, tvEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        sessionManager = new SessionManager(getContext());

        initializeViews(view);
        displayUserInfo();
        setupClickListeners(view);
        
        return view;
    }

    private void initializeViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
    }

    private void displayUserInfo() {
        User user = sessionManager.getLoggedInUser();
        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());
        }
    }

    private void setupClickListeners(View view) {
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
} 