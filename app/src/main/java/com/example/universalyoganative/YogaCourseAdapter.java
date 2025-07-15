package com.example.universalyoganative;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class YogaCourseAdapter extends RecyclerView.Adapter<YogaCourseAdapter.YogaCourseViewHolder> {
    private List<YogaCourse> courseList;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEditCourse(YogaCourse course);
        void onDeleteCourse(YogaCourse course);
        void onViewInstances(YogaCourse course);
        void onCourseClick(YogaCourse course);
    }

    public YogaCourseAdapter(List<YogaCourse> courseList, OnCourseActionListener listener) {
        this.courseList = courseList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public YogaCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.yoga_course_item, parent, false);
        return new YogaCourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaCourseViewHolder holder, int position) {
        YogaCourse course = courseList.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public void updateCourses(List<YogaCourse> newCourses) {
        this.courseList = newCourses;
        notifyDataSetChanged();
    }

    class YogaCourseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvType, tvPrice, tvDow, tvTime, tvDuration, tvCapacity, tvLocation;
        private TextView tvDifficulty, tvDescription;
        private MaterialButton btnEdit, btnDelete, btnViewInstances;

        public YogaCourseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views
            tvType = itemView.findViewById(R.id.tvType);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDow = itemView.findViewById(R.id.tvDow);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewInstances = itemView.findViewById(R.id.btnViewInstances);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(courseList.get(getAdapterPosition()));
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCourse(courseList.get(getAdapterPosition()));
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCourse(courseList.get(getAdapterPosition()));
                }
            });
            
            btnViewInstances.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewInstances(courseList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(YogaCourse course) {
            tvType.setText(course.getType());
            tvPrice.setText(course.getFormattedPrice());
            tvDow.setText(course.getDayOfWeek());
            tvTime.setText(course.getTime());
            tvDuration.setText(course.getFormattedDuration());
            tvCapacity.setText(course.getFormattedCapacity());
            tvLocation.setText(course.getLocation() != null ? course.getLocation() : "No location");
            tvDifficulty.setText(course.getDifficulty() != null ? course.getDifficulty() : "All Levels");
            
            // Handle description
            String description = course.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                tvDescription.setText(description);
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setText("No description available");
                tvDescription.setVisibility(View.VISIBLE);
            }
        }
    }
} 