package com.example.rifkinurfaiz.githubusers;

/**
 * Created by rifkinurfaiz on 10/22/2017.
 */

public class ListItem {
    private String username;
    private String avatar;

    public ListItem(String username, String avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUsername() {
        return username;
    }
}
