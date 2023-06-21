package com.example.recipe.model;

import com.google.firebase.Timestamp;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Comment implements Serializable {
    private String key, content, uid, uname, uphoto;
    private long created;

    public Comment(String content, long created, String uid, String uname, String uphoto) {
        this.content = content;
        this.uid = uid;
        this.uname = uname;
        this.uphoto = uphoto;
        this.created = created;
    }

    public Comment() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUphoto() {
        return uphoto;
    }

    public void setUphoto(String uphoto) {
        this.uphoto = uphoto;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}