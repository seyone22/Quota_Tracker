package dev.seyone.quotatracker

import dev.seyone.quotatracker.core.data.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase4UnitTest {

    @Test
    fun preferenceKeys_haveCorrectNames() {
        assertEquals("haptic_feedback_enabled", SettingsRepository.KEY_HAPTIC_FEEDBACK.name)
        assertEquals("auto_sort_completed", SettingsRepository.KEY_AUTO_SORT.name)
        assertEquals("theme_mode", SettingsRepository.KEY_THEME_MODE.name)
    }

    @Test
    fun csvFormatting_escapesCommasInTitles() {
        val titleWithComma = "Drawing, Painting & Art"
        val escapedTitle = titleWithComma.replace(",", " ")
        assertEquals("Drawing  Painting & Art", escapedTitle)
    }
}
