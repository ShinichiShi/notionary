package com.collab.productivity.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.collab.productivity.utils.MoodHelper;
import java.util.List;

public class MoodSelectorDialog extends DialogFragment {

    public interface OnMoodSelectedListener {
        void onMoodSelected(MoodHelper.Mood mood);
    }

    private OnMoodSelectedListener listener;

    public static MoodSelectorDialog newInstance() {
        return new MoodSelectorDialog();
    }

    public void setOnMoodSelectedListener(OnMoodSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        List<MoodHelper.Mood> moods = MoodHelper.getAllMoods();
        String[] moodDisplayTexts = new String[moods.size()];

        for (int i = 0; i < moods.size(); i++) {
            moodDisplayTexts[i] = moods.get(i).getDisplayText();
        }

        return new AlertDialog.Builder(requireContext())
                .setTitle("How are you feeling?")
                .setItems(moodDisplayTexts, (dialog, which) -> {
                    if (listener != null && which >= 0 && which < moods.size()) {
                        listener.onMoodSelected(moods.get(which));
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}
