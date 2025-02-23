package com.example.myapplication;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * TypeConverter class for String Lists and JSON strings.
 * <br>
 * It handles conversion between a list of image paths and a JSON string to enable storage
 * and retrieval of image paths in the Room database.
 */
public class StringListConverter {

    /** Converts a given List of String objects into a single JSON string
     * @param list The list to be converted */
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    /** Converts a given JSON string into a List of String objects
     * @param json The JSON string to be converted */
    @TypeConverter
    public static List<String> toStringList(String json) {
        if (json == null) return null;
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
