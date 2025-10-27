package com.collab.productivity.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Group Model - Represents a collaborative group
 * Contains group information and list of members
 */
public class Group {
    private String id;
    private String name;
    private String description;
    private String createdDate;
    private String collaborativeLink;
    private List<String> members;
    private List<String> memberIds;
    private int itemCount;
    private String creatorId;
    private String inviteCode;

    // Constructor
    public Group(String id, String name, String description, String createdDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
        this.members = new ArrayList<>();
        this.memberIds = new ArrayList<>();
        this.itemCount = 0;
        this.collaborativeLink = "";
        this.inviteCode = "";
    }

    // Empty constructor for Firestore
    public Group() {
        this.members = new ArrayList<>();
        this.memberIds = new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getCollaborativeLink() {
        return collaborativeLink;
    }

    public List<String> getMembers() {
        return members;
    }

    public int getItemCount() {
        return itemCount;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setCollaborativeLink(String collaborativeLink) {
        this.collaborativeLink = collaborativeLink;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    // Helper methods
    public void addMember(String member) {
        if (!members.contains(member)) {
            members.add(member);
        }
    }

    public void addMemberId(String memberId) {
        if (!memberIds.contains(memberId)) {
            memberIds.add(memberId);
        }
    }

    public void removeMember(String member) {
        members.remove(member);
    }
}
