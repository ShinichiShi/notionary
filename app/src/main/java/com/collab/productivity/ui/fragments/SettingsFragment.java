package com.collab.productivity.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.collab.productivity.R;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        try {
            View view = inflater.inflate(R.layout.fragment_settings, container, false);
            initializeViews(view);
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating settings fragment", e);
            return new View(requireContext());
        }
    }

    private void initializeViews(View view) {
        try {
            // Initialize your views here
            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
    }
}
