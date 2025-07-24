package com.example.universalyoganative;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private DatabaseHelper dbHelper;
    private TextView tvNoBookings;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        tvNoBookings = view.findViewById(R.id.tvNoBookings);

        // Initialize database helper and session manager
        dbHelper = MainActivity.helper;
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getContext());
        }
        
        sessionManager = new SessionManager(getContext());

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        // Get all bookings from all users
        List<Booking> bookings = dbHelper.getAllBookings();
        
        if (bookings.isEmpty()) {
            tvNoBookings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoBookings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            
            adapter = new BookingAdapter(getContext(), bookings, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Handle booking click if needed
        // For example, show booking details or cancellation dialog
    }
} 