package com.collab.productivity.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.collab.productivity.data.model.Group;
import com.collab.productivity.utils.FirebaseManager;
import com.collab.productivity.utils.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * GroupViewModel - Manages group data across the app
 * Provides LiveData for observing group changes
 * Uses MVVM architecture pattern with Firebase integration
 */
public class GroupViewModel extends ViewModel {
    private static final String TAG = "GroupViewModel";

    private MutableLiveData<List<Group>> groupsLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private List<Group> groupList;
    private FirebaseManager firebaseManager;

    /**
     * Constructor - Initializes with Firebase
     */
    public GroupViewModel() {
        groupList = new ArrayList<>();
        groupsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        firebaseManager = FirebaseManager.getInstance();

        // Load groups from Firebase
        loadGroupsFromFirebase();
    }

    /**
     * Returns LiveData of groups for observation
     */
    public LiveData<List<Group>> getGroups() {
        return groupsLiveData;
    }

    /**
     * Returns LiveData for errors
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Returns LiveData for loading state
     */
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    /**
     * Load groups from Firebase
     */
    public void loadGroupsFromFirebase() {
        loadingLiveData.setValue(true);
        firebaseManager.getUserGroups(new FirebaseManager.GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groups) {
                Logger.d(TAG, "Groups loaded successfully: " + groups.size());
                groupList.clear();
                groupList.addAll(groups);
                groupsLiveData.postValue(groupList);
                loadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "Error loading groups: " + error, null);
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Adds a new group to Firebase
     * @param group Group object to add
     */
    public void addGroup(Group group, GroupCreationCallback callback) {
        loadingLiveData.setValue(true);
        firebaseManager.createGroup(group, new FirebaseManager.GroupCallback() {
            @Override
            public void onSuccess(Group createdGroup) {
                Logger.d(TAG, "Group created successfully: " + createdGroup.getId());
                groupList.add(createdGroup);
                groupsLiveData.postValue(groupList);
                loadingLiveData.postValue(false);
                if (callback != null) {
                    callback.onSuccess(createdGroup);
                }
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "Error creating group: " + error, null);
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Join a group using invite code
     */
    public void joinGroup(String inviteCode, GroupCreationCallback callback) {
        loadingLiveData.setValue(true);
        firebaseManager.joinGroup(inviteCode, new FirebaseManager.GroupCallback() {
            @Override
            public void onSuccess(Group group) {
                Logger.d(TAG, "Joined group successfully: " + group.getId());
                boolean exists = false;
                for (Group g : groupList) {
                    if (g.getId().equals(group.getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    groupList.add(group);
                    groupsLiveData.postValue(groupList);
                }
                loadingLiveData.postValue(false);
                if (callback != null) {
                    callback.onSuccess(group);
                }
            }

            @Override
            public void onError(String error) {
                Logger.e(TAG, "Error joining group: " + error, null);
                errorLiveData.postValue(error);
                loadingLiveData.postValue(false);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Get group by ID
     */
    public void getGroupById(String groupId, GroupCreationCallback callback) {
        firebaseManager.getGroupById(groupId, new FirebaseManager.GroupCallback() {
            @Override
            public void onSuccess(Group group) {
                if (callback != null) {
                    callback.onSuccess(group);
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
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
     * Clears all groups (useful for logout or reset)
     */
    public void clearGroups() {
        groupList.clear();
        groupsLiveData.setValue(groupList);
    }

    /**
     * Callback interface for group operations
     */
    public interface GroupCreationCallback {
        void onSuccess(Group group);
        void onError(String error);
    }
}

