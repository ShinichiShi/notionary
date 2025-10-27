package com.collab.productivity.data.model;

import java.util.ArrayList;
import java.util.List;

public class FileTreeNode {
    private FileItem item;
    private List<FileTreeNode> children;
    private FileTreeNode parent;

    public FileTreeNode(FileItem item) {
        this.item = item;
        this.children = new ArrayList<>();
    }

    public void addChild(FileTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(FileTreeNode child) {
        children.remove(child);
        child.setParent(null);
    }

    public List<FileTreeNode> getChildren() {
        return children;
    }

    public FileItem getItem() {
        return item;
    }

    public FileTreeNode getParent() {
        return parent;
    }

    public void setParent(FileTreeNode parent) {
        this.parent = parent;
    }

    public String getFullPath() {
        if (parent == null || parent.getItem() == null) {
            return "/" + item.getName();
        }
        return parent.getFullPath() + "/" + item.getName();
    }

    public List<FileItem> getFlattenedList() {
        List<FileItem> result = new ArrayList<>();

        // Add parent navigation if not root
        if (parent != null && parent.getItem() != null) {
            FileItem parentNav = new FileItem(
                "..",
                parent.getItem().getPath(),
                "Parent Directory",
                null,
                true
            );
            result.add(parentNav);
        }

        // Add folders first
        for (FileTreeNode child : children) {
            if (child.getItem().isFolder()) {
                result.add(child.getItem());
            }
        }

        // Add files second
        for (FileTreeNode child : children) {
            if (!child.getItem().isFolder()) {
                result.add(child.getItem());
            }
        }

        return result;
    }
}
