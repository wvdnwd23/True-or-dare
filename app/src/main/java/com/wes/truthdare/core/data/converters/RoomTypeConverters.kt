package com.wes.truthdare.core.data.converters

import androidx.room.TypeConverter
import com.wes.truthdare.core.selector.GameMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Type converters for Room database
 */
class RoomTypeConverters {
    /**
     * Convert a list of strings to a JSON string
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val jsonArray = JSONArray()
        value.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }
    
    /**
     * Convert a JSON string to a list of strings
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val jsonArray = JSONArray(value)
        val result = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            result.add(jsonArray.getString(i))
        }
        return result
    }
    
    /**
     * Convert a map of string to float to a JSON string
     */
    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String {
        val jsonObject = JSONObject()
        value.forEach { (key, value) -> jsonObject.put(key, value) }
        return jsonObject.toString()
    }
    
    /**
     * Convert a JSON string to a map of string to float
     */
    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        val jsonObject = JSONObject(value)
        val result = mutableMapOf<String, Float>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = jsonObject.getDouble(key).toFloat()
        }
        return result
    }
    
    /**
     * Convert a GameMode enum to a string
     */
    @TypeConverter
    fun fromGameMode(value: GameMode): String {
        return value.name
    }
    
    /**
     * Convert a string to a GameMode enum
     */
    @TypeConverter
    fun toGameMode(value: String): GameMode {
        return GameMode.valueOf(value)
    }
}