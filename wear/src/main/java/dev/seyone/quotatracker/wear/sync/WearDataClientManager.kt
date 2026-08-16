package dev.seyone.quotatracker.wear.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

import dev.seyone.quotatracker.core.data.sync.LogTimeMessagePayload
import dev.seyone.quotatracker.core.data.sync.WearQuotaItem
import dev.seyone.quotatracker.core.data.sync.WearQuotaStatePayload

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
    addOnCanceledListener { cont.cancel() }
}

class WearDataClientManager(private val context: Context) : DataClient.OnDataChangedListener {

    private val dataClient = Wearable.getDataClient(context)
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)
    private val gson = Gson()

    private val _quotas = MutableStateFlow<List<WearQuotaItem>>(emptyList())
    val quotas: StateFlow<List<WearQuotaItem>> = _quotas.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    fun startListening() {
        dataClient.addListener(this)
        fetchInitialData()
    }

    fun stopListening() {
        dataClient.removeListener(this)
    }

    suspend fun fetchQuotasDirectly(): List<WearQuotaItem> {
        return try {
            val dataItems = dataClient.dataItems.awaitTask()
            var list: List<WearQuotaItem> = emptyList()
            dataItems.forEach { item ->
                if (item.uri.path == "/quotas_state") {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val json = dataMap.getString("payload") ?: return@forEach
                    val payload = gson.fromJson(json, WearQuotaStatePayload::class.java)
                    list = payload.quotas
                }
            }
            dataItems.release()
            list
        } catch (e: Exception) {
            Log.e("WearDataClient", "Failed to fetch initial Wear data", e)
            emptyList()
        }
    }

    private fun fetchInitialData() {
        scope.launch {
            _quotas.value = fetchQuotasDirectly()
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/quotas_state") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val json = dataMap.getString("payload") ?: return@forEach
                val payload = gson.fromJson(json, WearQuotaStatePayload::class.java)
                _quotas.value = payload.quotas
            }
        }
    }

    suspend fun sendLogTimeMessage(quotaId: Int, durationMinutes: Int): Boolean {
        return try {
            val nodes = nodeClient.connectedNodes.awaitTask()
            if (nodes.isEmpty()) {
                Log.w("WearDataClient", "No connected phone nodes found")
                return false
            }
            val payload = gson.toJson(LogTimeMessagePayload(quotaId, durationMinutes))
            val bytes = payload.toByteArray(Charsets.UTF_8)

            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/log_time", bytes).awaitTask()
            }
            Log.d("WearDataClient", "Sent /log_time ($durationMinutes m) for quotaId=$quotaId to ${nodes.size} nodes")
            true
        } catch (e: Exception) {
            Log.e("WearDataClient", "Failed to send /log_time message", e)
            false
        }
    }
}
