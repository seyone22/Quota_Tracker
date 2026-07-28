package dev.seyone.quotatracker.sync

import com.google.gson.Gson

data class WearQuotaItem(
    val id: Int,
    val title: String,
    val targetMinutes: Int,
    val loggedMinutes: Int,
    val isCompleted: Boolean,
    val isPinned: Boolean
)

data class WearQuotaStatePayload(
    val timestamp: Long,
    val quotas: List<WearQuotaItem>
)

data class LogTimeMessagePayload(
    val quotaId: Int,
    val durationMinutes: Int
)

object WearSyncJson {
    private val gson = Gson()

    fun toJson(obj: Any): String = gson.toJson(obj)
    fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)
}
