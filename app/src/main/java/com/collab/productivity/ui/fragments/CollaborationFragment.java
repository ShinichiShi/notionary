package com.collab.productivity.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Group;
import com.collab.productivity.ui.adapter.GroupAdapter;
import com.collab.productivity.viewmodel.GroupViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class CollaborationFragment extends Fragment {

    private MaterialButton btnGenerateLink;
    private MaterialButton btnJoinGroup;
    private TextView textLink;
    private RecyclerView recyclerViewShared;
    private TextView textEmptyShared;
    private GroupViewModel groupViewModel;
    private GroupAdapter groupAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collaboration, container, false);

        btnGenerateLink = view.findViewById(R.id.btn_generate_link);
        btnJoinGroup = view.findViewById(R.id.btn_join_group);
        textLink = view.findViewById(R.id.text_link);
        recyclerViewShared = view.findViewById(R.id.recycler_view_shared);
        textEmptyShared = view.findViewById(R.id.text_empty_shared);

        // Initialize ViewModel
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Observe groups
        observeGroups();

        btnGenerateLink.setOnClickListener(v -> showGroupSelectionDialog());

        if (btnJoinGroup != null) {
            btnJoinGroup.setOnClickListener(v -> showJoinGroupDialog());
        }

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewShared.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupAdapter = new GroupAdapter(requireContext());
        recyclerViewShared.setAdapter(groupAdapter);
    }

    private void observeGroups() {
        groupViewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null && !groups.isEmpty()) {
                groupAdapter.submitList(new ArrayList<>(groups));
                recyclerViewShared.setVisibility(View.VISIBLE);
                textEmptyShared.setVisibility(View.GONE);
            } else {
                recyclerViewShared.setVisibility(View.GONE);
                textEmptyShared.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showGroupSelectionDialog() {
        List<Group> groups = groupViewModel.getGroups().getValue();
        if (groups == null || groups.isEmpty()) {
            Toast.makeText(requireContext(), "No groups available. Create a group first!",
                Toast.LENGTH_SHORT).show();
            return;
        }

        String[] groupNames = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            groupNames[i] = groups.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Group to Share")
            .setItems(groupNames, (dialog, which) -> {
                Group selectedGroup = groups.get(which);
                generateAndCopyLink(selectedGroup);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void generateAndCopyLink(Group group) {
        // Generate collaboration link with invite code
        String inviteCode = group.getInviteCode();
        String shareableLink = "https://notionary.app/join?code=" + inviteCode;

        // Show the link
        textLink.setText("Invite Code: " + inviteCode + "\n\nLink: " + shareableLink);
        textLink.setVisibility(View.VISIBLE);

        // Copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Collaboration Link", shareableLink);
        clipboard.setPrimaryClip(clip);

        // Show success message
        Toast.makeText(requireContext(),
            "Invite link copied! Share it with your team.",
            Toast.LENGTH_LONG).show();
    }

    private void showJoinGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_join_group, null);
        TextInputEditText codeInput = dialogView.findViewById(R.id.et_invite_code);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Join Group")
            .setMessage("Enter the invite code shared with you")
            .setView(dialogView)
            .setPositiveButton("Join", (dialog, which) -> {
                String code = codeInput.getText().toString().trim().toUpperCase();
                if (!code.isEmpty()) {
                    joinGroupWithCode(code);
                } else {
                    Toast.makeText(requireContext(), "Please enter an invite code",
                        Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void joinGroupWithCode(String inviteCode) {
        groupViewModel.joinGroup(inviteCode, new GroupViewModel.GroupCreationCallback() {
            @Override
            public void onSuccess(Group group) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                        "Successfully joined: " + group.getName(),
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                        "Error joining group: " + error,
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
