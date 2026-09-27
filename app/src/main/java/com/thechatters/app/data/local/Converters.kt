package com.thechatters.app.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val reactionsMapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Types.newParameterizedType(List::class.java, String::class.java)
    )
    private val reactionsAdapter = moshi.adapter<Map<String, List<String>>>(reactionsMapType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return stringListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromReactionsMap(value: Map<String, List<String>>?): String {
        return reactionsAdapter.toJson(value ?: emptyMap())
    }

    @TypeConverter
    fun toReactionsMap(value: String?): Map<String, List<String>> {
        if (value.isNullOrBlank()) return emptyMap()
        return try {
            reactionsAdapter.fromJson(value) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
