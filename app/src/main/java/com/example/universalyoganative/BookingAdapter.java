package com.example.universalyoganative;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<Booking> bookings;
    private Context context;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookings, OnBookingClickListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        holder.tvBookingId.setText("Booking #" + booking.getBookingId());
        holder.tvUserEmail.setText("User: " + booking.getUserEmail());
        holder.tvClassDate.setText("Date: " + booking.getClassDate());
        holder.tvCourseTime.setText("Time: " + booking.getCourseTime());
        holder.tvPrice.setText("Price: $" + String.format("%.2f", booking.getPrice()));
        holder.tvDuration.setText("Duration: " + booking.getDuration() + " minutes");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookings != null ? bookings.size() : 0;
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookings = newBookings;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId;
        TextView tvUserEmail;
        TextView tvClassDate;
        TextView tvCourseTime;
        TextView tvPrice;
        TextView tvDuration;

        ViewHolder(View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvClassDate = itemView.findViewById(R.id.tvClassDate);
            tvCourseTime = itemView.findViewById(R.id.tvCourseTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
} 