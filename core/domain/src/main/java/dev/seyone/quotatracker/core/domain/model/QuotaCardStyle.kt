package dev.seyone.quotatracker.core.domain.model

enum class QuotaCardStyle(
    val displayName: String,
    val description: String
) {
    DUAL_TONE("M3 Dual-Tone & Mode Pill", "Dynamic meter with pill switcher and layered gold overtime bar"),
    SEGMENTED_STEPPER("M3 Segmented Stepper", "Discrete step blocks with tactile stepper dial and flame badge"),
    GLOW_BANNER("M3 Glow & Overachiever", "Dark glass card with top overachiever banner and neon glow")
}
