package dev.seyone.quotatracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dev.seyone.quotatracker.R
import dev.seyone.quotatracker.core.data.local.entity.QuotaEntity

object DynamicShortcutManager {

    fun updateTopPinnedGoalShortcut(context: Context, topPinnedGoal: QuotaEntity?) {
        if (topPinnedGoal == null) {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf("shortcut_top_pinned_goal"))
            return
        }

        val intent = Intent(context, dev.seyone.quotatracker.ui.MainActivity::class.java).apply {
            action = "dev.seyone.quotatracker.ACTION_LOG_GOAL"
            putExtra("quota_id", topPinnedGoal.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcut = ShortcutInfoCompat.Builder(context, "shortcut_top_pinned_goal")
            .setShortLabel("Log ${topPinnedGoal.title}")
            .setLongLabel("Log time for ${topPinnedGoal.title}")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_shortcut_pinned_goal))
            .setIntent(intent)
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }
}
