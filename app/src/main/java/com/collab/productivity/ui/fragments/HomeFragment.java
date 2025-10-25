package com.collab.productivity.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.ui.adapter.GroupAdapter;
import com.collab.productivity.viewmodel.GroupViewModel;

/**
 * HomeFragment - Displays list of groups
 * Shows a RecyclerView with dummy group data
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;
    private GroupViewModel groupViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_groups);

        // Set up RecyclerView
        setupRecyclerView();

        // Initialize ViewModel
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Observe group data
        observeViewModel();

        return view;
    }

    /**
     * Sets up RecyclerView with layout manager and adapter
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(requireContext());
        recyclerView.setAdapter(groupAdapter);
    }

    /**
     * Observes ViewModel data changes and updates UI
     */
    private void observeViewModel() {
        groupViewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                groupAdapter.setGroups(groups);
            }
        });
    }
}
