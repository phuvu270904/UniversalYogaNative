package com.example.universalyoganative;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SyncProgressDialog extends Dialog {
    private TextView tvProgress;
    private ProgressBar progressBar;
    private boolean isIndeterminate = true;

    public SyncProgressDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sync_progress, null);
        setContentView(view);

        tvProgress = view.findViewById(R.id.tvProgress);
        progressBar = view.findViewById(R.id.progressBar);

        // Set initial message
        updateProgress("Initializing sync...");
    }

    public void updateProgress(String message) {
        if (tvProgress != null) {
            tvProgress.setText(message);
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        this.isIndeterminate = indeterminate;
        if (progressBar != null) {
            progressBar.setIndeterminate(indeterminate);
        }
    }

    public void setProgress(int progress) {
        if (progressBar != null && !isIndeterminate) {
            progressBar.setProgress(progress);
        }
    }
} 