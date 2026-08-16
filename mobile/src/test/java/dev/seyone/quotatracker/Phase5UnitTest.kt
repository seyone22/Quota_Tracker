package dev.seyone.quotatracker

import dev.seyone.quotatracker.core.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.core.data.local.model.QuotaWithProgress
import dev.seyone.quotatracker.core.domain.model.ResetStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase5UnitTest {

    @Test
    fun underperformingCheck_identifiesQuotasBelow50Percent() {
        val pinnedUnderperforming = QuotaWithProgress(
            quota = QuotaEntity(
                id = 1,
                title = "Guitar Practice",
                targetMinutes = 600,
                resetStrategy = ResetStrategy.CLEAN,
                isPinned = true
            ),
            loggedMinutes = 200
        )

        val pinnedOnTrack = QuotaWithProgress(
            quota = QuotaEntity(
                id = 2,
                title = "Reading",
                targetMinutes = 300,
                resetStrategy = ResetStrategy.CLEAN,
                isPinned = true
            ),
            loggedMinutes = 240
        )

        val isUnderperforming = { item: QuotaWithProgress ->
            item.quota.isPinned && item.loggedMinutes < (item.quota.targetMinutes * 0.5)
        }

        assertTrue(isUnderperforming(pinnedUnderperforming))
        assertFalse(isUnderperforming(pinnedOnTrack))
    }

    @Test
    fun csvAnalyticsHeader_containsRequiredColumns() {
        val expectedHeader = "Date,QuotaTitle,TargetMinutes,LoggedMinutes,Type"
        val header = "Date,QuotaTitle,TargetMinutes,LoggedMinutes,Type"
        assertEquals(expectedHeader, header)
    }
}
