package com.example.recipe.model;

import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe implements Serializable {
    private String key, title, description, imageUrl, ingredients, steps, uid, uname, uphoto;
    private int cookingTime;
    private float difficulty;
    private long created;
    private Map<String, Comment> comments = new HashMap<>();

    public Recipe(String title, String description, String imageUrl, String ingredients, String steps, int cookingTime, float difficulty, long created, Map<String, Comment> comments, String uid, String uname, String uphoto) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.ingredients = ingredients;
        this.steps = steps;
        this.uid = uid;
        this.uname = uname;
        this.uphoto = uphoto;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.created = created;
        this.comments = comments;
    }

    public Recipe() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
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

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

    public List<Comment> getListComments() {
        List<Comment> list = new ArrayList<>();
        comments.forEach((key, value) -> {
            Comment comment = value;
            comment.setKey(key);
            list.add(comment);
        });
        list.sort(new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                if (o1.getCreated() > o2.getCreated()) return 1;
                else if (o1.getCreated() < o2.getCreated()) return -1;
                return 0;
            }
        });
        return list;
    }
}