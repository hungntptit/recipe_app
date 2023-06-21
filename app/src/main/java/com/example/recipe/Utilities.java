package com.example.recipe;

import com.example.recipe.model.Recipe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utilities {
    public static SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
    public static String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    public static String getISOCurrentDateTime() {
        String now = ISO_8601_FORMAT.format(new Date());
        return now;
    }

    
}
