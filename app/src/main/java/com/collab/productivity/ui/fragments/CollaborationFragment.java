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
import com.collab.productivity.R;
import com.google.android.material.button.MaterialButton;
import java.util.UUID;

public class CollaborationFragment extends Fragment {

    private MaterialButton btnGenerateLink;
    private TextView textLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collaboration, container, false);

        btnGenerateLink = view.findViewById(R.id.btn_generate_link);
        textLink = view.findViewById(R.id.text_link);

        btnGenerateLink.setOnClickListener(v -> generateAndCopyLink());

        return view;
    }

    private void generateAndCopyLink() {
        // Generate mock collaboration link
        String mockLink = "https://notionary.app/collaborate/" + UUID.randomUUID().toString();

        // Show the link
        textLink.setText(mockLink);
        textLink.setVisibility(View.VISIBLE);

        // Copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Collaboration Link", mockLink);
        clipboard.setPrimaryClip(clip);

        // Show success message
        Toast.makeText(requireContext(), R.string.link_generated, Toast.LENGTH_SHORT).show();
    }
}
