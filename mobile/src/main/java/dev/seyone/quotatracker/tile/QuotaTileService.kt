package dev.seyone.quotatracker.tile

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.seyone.quotatracker.QuotaApplication
import dev.seyone.quotatracker.ui.quicklog.QuickLogActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class QuotaTileService : TileService() {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val app = applicationContext as QuotaApplication
        scope.launch {
            val quotas = app.repository.getQuotasWithCurrentWeekProgress().firstOrNull() ?: emptyList()
            val topPinned = quotas.firstOrNull { it.quota.isPinned } ?: quotas.firstOrNull()

            val tile = qsTile ?: return@launch
            if (topPinned != null) {
                val logged = topPinned.loggedMinutes
                val target = topPinned.quota.targetMinutes
                val pct = (logged.toFloat() / target.toFloat() * 100).toInt()

                tile.label = topPinned.quota.title
                tile.subtitle = "$pct% logged"
                tile.state = Tile.STATE_ACTIVE
            } else {
                tile.label = "Quota Tracker"
                tile.subtitle = "No quotas"
                tile.state = Tile.STATE_INACTIVE
            }
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, QuickLogActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivityAndCollapse(intent)
    }
}
