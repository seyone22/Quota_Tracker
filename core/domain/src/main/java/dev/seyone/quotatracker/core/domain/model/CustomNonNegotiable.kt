package dev.seyone.quotatracker.core.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class CustomNonNegotiable(
    val id: String,
    val name: String,
    val emoji: String = "⭐️",
    val hoursPerWeek: Int
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("emoji", emoji)
            put("hoursPerWeek", hoursPerWeek)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CustomNonNegotiable {
            return CustomNonNegotiable(
                id = json.optString("id", java.util.UUID.randomUUID().toString()),
                name = json.optString("name", "Custom"),
                emoji = json.optString("emoji", "⭐️"),
                hoursPerWeek = json.optInt("hoursPerWeek", 0)
            )
        }

        fun listToJsonString(list: List<CustomNonNegotiable>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJsonString(jsonStr: String): List<CustomNonNegotiable> {
            if (jsonStr.isBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val result = mutableListOf<CustomNonNegotiable>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(fromJson(obj))
                }
                result
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
