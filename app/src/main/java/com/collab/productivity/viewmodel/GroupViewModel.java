package com.collab.productivity.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.collab.productivity.data.model.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupViewModel - Manages group data across the app
 * Provides LiveData for observing group changes
 * Uses MVVM architecture pattern
 */
public class GroupViewModel extends ViewModel {

    private MutableLiveData<List<Group>> groupsLiveData;
    private List<Group> groupList;

    /**
     * Constructor - Initializes with dummy data
     */
    public GroupViewModel() {
        groupList = new ArrayList<>();
        groupsLiveData = new MutableLiveData<>();

        // Load initial dummy data
        loadDummyData();
    }

    /**
     * Returns LiveData of groups for observation
     */
    public LiveData<List<Group>> getGroups() {
        return groupsLiveData;
    }

    /**
     * Adds a new group to the list
     * @param group Group object to add
     */
    public void addGroup(Group group) {
        groupList.add(group);
        groupsLiveData.setValue(groupList);
    }

    /**
     * Removes a group from the list
     * @param group Group object to remove
     */
    public void removeGroup(Group group) {
        groupList.remove(group);
        groupsLiveData.setValue(groupList);
    }

    /**
     * Updates an existing group
     * @param updatedGroup Updated group object
     */
    public void updateGroup(Group updatedGroup) {
        for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getId().equals(updatedGroup.getId())) {
                groupList.set(i, updatedGroup);
                groupsLiveData.setValue(groupList);
                break;
            }
        }
    }

    /**
     * Loads dummy data for testing and demonstration
     * This will be replaced with actual API calls in Phase 3
     */
    private void loadDummyData() {
        // Create dummy groups
        Group group1 = new Group("1", "Project Alpha",
                "Marketing campaign planning", "Oct 20, 2025");
        group1.addMember("Alice");
        group1.addMember("Bob");
        group1.setItemCount(5);

        Group group2 = new Group("2", "Team Meeting",
                "Weekly sync-up notes", "Oct 18, 2025");
        group2.addMember("Charlie");
        group2.addMember("Diana");
        group2.setItemCount(3);

        Group group3 = new Group("3", "Design Sprint",
                "UI/UX design collaboration", "Oct 15, 2025");
        group3.addMember("Eve");
        group3.setItemCount(8);

        // Add to list
        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);

        // Notify observers
        groupsLiveData.setValue(groupList);
    }

    /**
     * Clears all groups (useful for logout or reset)
     */
    public void clearGroups() {
        groupList.clear();
        groupsLiveData.setValue(groupList);
    }
}
