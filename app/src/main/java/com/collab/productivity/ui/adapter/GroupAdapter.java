package com.collab.productivity.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.collab.productivity.R;
import com.collab.productivity.data.model.Group;
import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final Context context;
    private List<Group> groups;
    private OnGroupClickListener onGroupClickListener;

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    public GroupAdapter(Context context) {
        this.context = context;
        this.groups = new ArrayList<>();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged(); // Using notifyDataSetChanged for simplicity in Phase 1
    }

    public void submitList(List<Group> groups) {
        setGroups(groups);
    }

    public void setOnGroupClickListener(OnGroupClickListener listener) {
        this.onGroupClickListener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.tvGroupName.setText(group.getName());
        holder.tvGroupDescription.setText(group.getDescription());
        holder.tvGroupDate.setText(group.getCreatedDate());
        holder.tvMemberCount.setText(
            context.getString(R.string.member_count, group.getMembers().size())
        );

        holder.itemView.setOnClickListener(v -> {
            if (onGroupClickListener != null) {
                onGroupClickListener.onGroupClick(group);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvGroupName;
        private final TextView tvGroupDescription;
        private final TextView tvGroupDate;
        private final TextView tvMemberCount;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupDescription = itemView.findViewById(R.id.tv_group_description);
            tvGroupDate = itemView.findViewById(R.id.tv_group_date);
            tvMemberCount = itemView.findViewById(R.id.tv_member_count);
        }
    }
}
