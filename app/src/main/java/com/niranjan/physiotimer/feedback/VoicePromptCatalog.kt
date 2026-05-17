package com.niranjan.physiotimer.feedback

import java.util.Locale

internal object VoicePromptCatalog {
    private enum class VoiceCategory {
        System,
        Step,
        Motivation,
        Completion,
        Counting
    }

    private data class VoiceEntry(
        val prompt: String,
        val assets: List<String>,
        val category: VoiceCategory,
        val aliases: Set<String> = emptySet(),
        val stepSuggestionLabel: String? = null
    )

    private val entries = listOf(
        VoiceEntry("paused", listOf("voices/Paused.mp3"), VoiceCategory.System),
        VoiceEntry("resuming", listOf("voices/Resuming.mp3"), VoiceCategory.System),
        VoiceEntry("starting exercise", listOf("voices/Starting the exercise.mp3"), VoiceCategory.System, aliases = setOf("starting the exercise")),

        VoiceEntry("lift", listOf("voices/Lift.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Lift"),
        VoiceEntry("hold", listOf("voices/Hold.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Hold"),
        VoiceEntry("relax", listOf("voices/Relax.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Relax"),
        VoiceEntry("rest", listOf("voices/Rest.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Rest"),
        VoiceEntry("breathe", listOf("voices/Breathe.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Breathe"),
        VoiceEntry("release", listOf("voices/Release.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Release"),
        VoiceEntry(
            "switch side",
            listOf("voices/Switchside.mp3"),
            VoiceCategory.Step,
            aliases = setOf("switchside"),
            stepSuggestionLabel = "Switch side"
        ),
        VoiceEntry("left", listOf("voices/Left.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Left"),
        VoiceEntry("right", listOf("voices/Right.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Right"),
        VoiceEntry("pull", listOf("voices/Pull.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Pull"),
        VoiceEntry("push", listOf("voices/Push.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Push"),
        VoiceEntry("squeeze", listOf("voices/Squeeze.mp3"), VoiceCategory.Step, stepSuggestionLabel = "Squeeze"),

        VoiceEntry("keep going", listOf("voices/motivation/Keep_going_voice.mp3"), VoiceCategory.Motivation),
        VoiceEntry("nice steady pace", listOf("voices/motivation/Nice_Steady_pace.mp3"), VoiceCategory.Motivation),
        VoiceEntry("breathe and move", listOf("voices/motivation/Breathe_Move.mp3"), VoiceCategory.Motivation),
        VoiceEntry("almost there", listOf("voices/motivation/Almost_there.mp3"), VoiceCategory.Motivation),
        VoiceEntry("lie down or sit", listOf("voices/motivation/Lie down or sit.mp3"), VoiceCategory.Motivation),

        VoiceEntry("completed", listOf("voices/Over_nice.mp3", "voices/Well_done.mp3"), VoiceCategory.Completion),
        VoiceEntry("well done", listOf("voices/Well_done.mp3"), VoiceCategory.Completion),
        VoiceEntry("last one", listOf("voices/Last one.mp3"), VoiceCategory.Completion),

        VoiceEntry("count 10 seconds", listOf("voices/10sec counter from 1 to 10.mp3"), VoiceCategory.Counting),
        VoiceEntry("count 20 seconds", listOf("voices/20sec counter from 1 to 10.mp3"), VoiceCategory.Counting)
    )

    private val promptIndex: Map<String, VoiceEntry> = buildMap {
        this@VoicePromptCatalog.entries.forEach { entry ->
            put(normalize(entry.prompt), entry)
            entry.aliases.forEach { alias ->
                put(normalize(alias), entry)
            }
        }
    }

    private val stepEntries = entries.filter { it.category == VoiceCategory.Step }

    private val stepSuggestionNames: List<String> = stepEntries
        .mapNotNull { it.stepSuggestionLabel }
        .distinct()

    private val configurableRunningMotivationCues = setOf(
        normalize("keep going")
    )

    private val configurableCompletionMotivationCues = emptySet<String>()
    private val counterPauseCues = setOf(
        normalize("last one"),
        normalize("completed"),
        normalize("well done")
    )

    fun assetsFor(text: String): List<String> {
        val normalized = normalize(text)
        if (normalized.isBlank()) return emptyList()

        promptIndex[normalized]?.let { return it.assets }

        // If the cue includes a known step phrase (e.g., "left leg"), use that recorded clip.
        stepEntries.firstOrNull { stepEntry ->
            val key = normalize(stepEntry.prompt)
            normalized == key || normalized.contains(key)
        }?.let { return it.assets }

        return if (normalized.startsWith("starting exercise")) {
            listOf("voices/Starting the exercise.mp3")
        } else {
            emptyList()
        }
    }

    fun isMotivationText(text: String): Boolean {
        val entry = promptIndex[normalize(text)]
        return entry?.category == VoiceCategory.Motivation
    }

    fun isConfigurableMotivationCue(text: String): Boolean {
        val normalized = normalize(text)
        return normalized in configurableRunningMotivationCues ||
            normalized in configurableCompletionMotivationCues
    }

    fun usesCompletionMotivationVoice(text: String): Boolean {
        return normalize(text) in configurableCompletionMotivationCues
    }

    fun shouldPauseCounterDuringCue(text: String): Boolean {
        val normalized = normalize(text)
        return normalized in counterPauseCues ||
            isMotivationText(text) ||
            isConfigurableMotivationCue(text)
    }

    fun hasRecordedVoiceForStep(stepName: String): Boolean {
        val normalized = normalize(stepName)
        if (normalized.isBlank()) return false

        return stepEntries.any { stepEntry ->
            val key = normalize(stepEntry.prompt)
            normalized == key || normalized.contains(key)
        }
    }

    fun suggestedStepNames(query: String, maxItems: Int = 5): List<String> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        val startsWith = stepSuggestionNames.filter { normalize(it).startsWith(normalizedQuery) }
        val contains = stepSuggestionNames
            .filter { normalize(it).contains(normalizedQuery) && it !in startsWith }

        return (startsWith + contains).take(maxItems)
    }

    private fun normalize(text: String): String {
        return text
            .trim()
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
    }
}
