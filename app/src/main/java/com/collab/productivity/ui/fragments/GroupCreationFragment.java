package com.collab.productivity.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Group;
import com.collab.productivity.viewmodel.GroupViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class GroupCreationFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private TextInputLayout tilGroupName;
    private TextInputLayout tilGroupDescription;
    private TextInputEditText etGroupName;
    private TextInputEditText etGroupDescription;
    private MaterialButton btnCreateGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_creation, container, false);

        // Initialize views
        tilGroupName = view.findViewById(R.id.til_group_name);
        tilGroupDescription = view.findViewById(R.id.til_group_description);
        etGroupName = view.findViewById(R.id.et_group_name);
        etGroupDescription = view.findViewById(R.id.et_group_description);
        btnCreateGroup = view.findViewById(R.id.btn_create_group);

        // Initialize ViewModel
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Set click listener for create button
        btnCreateGroup.setOnClickListener(v -> createGroup());

        return view;
    }

    private void createGroup() {
        String name = etGroupName.getText().toString().trim();
        String description = etGroupDescription.getText().toString().trim();

        // Validate input
        if (name.isEmpty()) {
            tilGroupName.setError(getString(R.string.error_empty_name));
            return;
        }

        // Clear any previous errors
        tilGroupName.setError(null);
        tilGroupDescription.setError(null);

        // Disable button while creating
        btnCreateGroup.setEnabled(false);
        btnCreateGroup.setText("Creating...");

        // Generate unique ID and format current date
        String id = UUID.randomUUID().toString();
        String date = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date());

        // Create new group
        Group newGroup = new Group(id, name, description, date);

        // Add to ViewModel with callback
        groupViewModel.addGroup(newGroup, new com.collab.productivity.viewmodel.GroupViewModel.GroupCreationCallback() {
            @Override
            public void onSuccess(Group createdGroup) {
                if (isAdded()) {
                    // Show success message with invite code
                    Toast.makeText(requireContext(),
                        "Group created! Invite code: " + createdGroup.getInviteCode(),
                        Toast.LENGTH_LONG).show();

                    // Clear inputs
                    etGroupName.setText("");
                    etGroupDescription.setText("");

                    // Re-enable button
                    btnCreateGroup.setEnabled(true);
                    btnCreateGroup.setText(R.string.btn_create_group);

                    // Navigate back to home
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                        "Error creating group: " + error,
                        Toast.LENGTH_SHORT).show();

                    // Re-enable button
                    btnCreateGroup.setEnabled(true);
                    btnCreateGroup.setText(R.string.btn_create_group);
                }
            }
        });
    }
}

