package com.example.universalyoganative;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ClassInstanceAdapter extends RecyclerView.Adapter<ClassInstanceAdapter.ClassInstanceViewHolder> {
    private List<ClassInstance> instanceList;
    private OnInstanceActionListener listener;

    public interface OnInstanceActionListener {
        void onEditInstance(ClassInstance instance);
        void onDeleteInstance(ClassInstance instance);
        void onInstanceClick(ClassInstance instance);
    }

    public ClassInstanceAdapter(List<ClassInstance> instanceList, OnInstanceActionListener listener) {
        this.instanceList = instanceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassInstanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_instance_item, parent, false);
        return new ClassInstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassInstanceViewHolder holder, int position) {
        ClassInstance instance = instanceList.get(position);
        holder.bind(instance);
    }

    @Override
    public int getItemCount() {
        return instanceList.size();
    }

    public void updateInstances(List<ClassInstance> newInstances) {
        this.instanceList = newInstances;
        notifyDataSetChanged();
    }

    class ClassInstanceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvTeacher, tvComments, tvLocation;
        private ImageView ivPhoto, ivLocationIcon, ivSyncStatus;
        private MaterialButton btnEdit, btnDelete;
        private View photoContainer, locationContainer;

        public ClassInstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize views
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvComments = itemView.findViewById(R.id.tvComments);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            ivLocationIcon = itemView.findViewById(R.id.ivLocationIcon);
            ivSyncStatus = itemView.findViewById(R.id.ivSyncStatus);
            
            photoContainer = itemView.findViewById(R.id.photoContainer);
            locationContainer = itemView.findViewById(R.id.locationContainer);
            
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInstanceClick(instanceList.get(getAdapterPosition()));
                }
            });
            
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditInstance(instanceList.get(getAdapterPosition()));
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteInstance(instanceList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(ClassInstance instance) {
            tvDate.setText(instance.getFormattedDate());
            tvTeacher.setText(instance.getTeacher());
            
            // Handle comments
            String comments = instance.getComments();
            if (comments != null && !comments.trim().isEmpty()) {
                tvComments.setText(comments);
                tvComments.setVisibility(View.VISIBLE);
            } else {
                tvComments.setText("No comments");
                tvComments.setVisibility(View.VISIBLE);
            }
            
            // Handle photo
            if (instance.hasPhoto()) {
                photoContainer.setVisibility(View.VISIBLE);
                Glide.with(itemView.getContext())
                        .load(instance.getPhotoPath())
                        .placeholder(R.drawable.ic_photo_placeholder)
                        .error(R.drawable.ic_photo_error)
                        .centerCrop()
                        .into(ivPhoto);
            } else {
                photoContainer.setVisibility(View.GONE);
            }
            
            // Handle location
            if (instance.hasLocation()) {
                locationContainer.setVisibility(View.VISIBLE);
                tvLocation.setText(instance.getFormattedLocation());
                ivLocationIcon.setVisibility(View.VISIBLE);
            } else {
                locationContainer.setVisibility(View.GONE);
            }
            
            // Handle sync status
            if (instance.isSynced()) {
                ivSyncStatus.setImageResource(R.drawable.ic_cloud_done);
                ivSyncStatus.setColorFilter(itemView.getContext().getColor(R.color.success_color));
            } else {
                ivSyncStatus.setImageResource(R.drawable.ic_cloud_off);
                ivSyncStatus.setColorFilter(itemView.getContext().getColor(R.color.warning_color));
            }
        }
    }
} 