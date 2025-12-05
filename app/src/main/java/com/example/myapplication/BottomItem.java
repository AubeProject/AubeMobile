package com.example.myapplication;

public class BottomItem {
    private final String title;
    private final int iconRes;

    public BottomItem(String title, int iconRes) {
        this.title = title;
        this.iconRes = iconRes;
    }
    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
}

