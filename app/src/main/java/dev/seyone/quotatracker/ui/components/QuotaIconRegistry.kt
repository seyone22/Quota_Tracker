package dev.seyone.quotatracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Podcasts
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

data class QuotaIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object QuotaIconRegistry {
    val availableIcons: List<QuotaIconOption> = listOf(
        QuotaIconOption("book", "Reading", Icons.Outlined.MenuBook),
        QuotaIconOption("fitness_center", "Gym", Icons.Outlined.FitnessCenter),
        QuotaIconOption("directions_run", "Running", Icons.Outlined.DirectionsRun),
        QuotaIconOption("directions_bike", "Cycling", Icons.Outlined.DirectionsBike),
        QuotaIconOption("code", "Coding", Icons.Outlined.Code),
        QuotaIconOption("edit", "Writing", Icons.Outlined.Edit),
        QuotaIconOption("palette", "Art", Icons.Outlined.Palette),
        QuotaIconOption("music_note", "Music", Icons.Outlined.MusicNote),
        QuotaIconOption("language", "Language", Icons.Outlined.Language),
        QuotaIconOption("self_improvement", "Meditation", Icons.Outlined.SelfImprovement),
        QuotaIconOption("school", "Study", Icons.Outlined.School),
        QuotaIconOption("work", "Work", Icons.Outlined.WorkOutline),
        QuotaIconOption("water_drop", "Water", Icons.Outlined.WaterDrop),
        QuotaIconOption("restaurant", "Cooking", Icons.Outlined.Restaurant),
        QuotaIconOption("sports_esports", "Gaming", Icons.Outlined.SportsEsports),
        QuotaIconOption("cleaning_services", "Cleaning", Icons.Outlined.CleaningServices),
        QuotaIconOption("lightbulb", "Projects", Icons.Outlined.Lightbulb),
        QuotaIconOption("savings", "Finances", Icons.Outlined.Savings),
        QuotaIconOption("podcasts", "Audiobooks", Icons.Outlined.Podcasts),
        QuotaIconOption("park", "Outdoors", Icons.Outlined.Park)
    )

    fun getIcon(key: String?): ImageVector? {
        if (key.isNullOrBlank() || key == "none") return null
        return availableIcons.find { it.key == key }?.icon
    }
}
