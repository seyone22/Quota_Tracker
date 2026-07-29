package dev.seyone.quotatracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.quotatracker.data.local.entity.QuotaEntity
import dev.seyone.quotatracker.data.model.ResetStrategy
import dev.seyone.quotatracker.data.repository.QuotaRepository
import dev.seyone.quotatracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsRepository: SettingsRepository,
    private val quotaRepository: QuotaRepository
) : ViewModel() {

    fun completeOnboarding(
        sleepHours: Int,
        workHours: Int,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            // Save settings & baseline
            settingsRepository.setHasCompletedOnboarding(
                completed = true,
                sleepHours = sleepHours,
                workHours = workHours
            )

            // Inject good default quota if database is empty
            val existingQuotas = quotaRepository.getQuotasWithCurrentWeekProgress().firstOrNull()
            if (existingQuotas.isNullOrEmpty()) {
                val sampleQuota = QuotaEntity(
                    title = "📖 Reading",
                    targetMinutes = 120, // 2 hours/week
                    resetStrategy = ResetStrategy.CLEAN,
                    isPinned = true
                )
                quotaRepository.addQuota(sampleQuota)
            }

            onFinished()
        }
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val quotaRepository: QuotaRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                return OnboardingViewModel(settingsRepository, quotaRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
