package com.vlnabatov.alabaster.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font

/**
 * Stores per-theme editor colour overrides and applies them to the bundled
 * Alabaster colour schemes.
 *
 * The bundled schemes are read-only, so the overrides are kept in the plugin's
 * own settings and re-applied whenever the IDE starts or the active colour
 * scheme changes. Only the keys listed in [AlabasterSettingsConfigurable] are
 * overridden; everything else keeps the theme's own colours.
 */
@Service(Service.Level.APP)
@State(name = "AlabasterThemeSettings", storages = [Storage("alabaster-theme-settings.xml")])
class AlabasterSettingsService : PersistentStateComponent<AlabasterSettingsService.State> {

    data class ColorOverride(
        var theme: String = "",
        var key: String = "",
        var color: String = "",
    )

    data class State(
        var overrides: MutableList<ColorOverride> = mutableListOf(),
    )

    private var myState = State()

    @Volatile
    private var applying = false

    private val connection = ApplicationManager.getApplication().messageBus.connect()
    init {
        connection.subscribe(
            EditorColorsManager.TOPIC,
            object : EditorColorsListener {
                override fun globalSchemeChange(scheme: EditorColorsScheme?) {
                    applyAll()
                }
            },
        )
    }

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun overrides(theme: String): Map<String, String> =
        myState.overrides.asSequence()
            .filter { it.theme == theme }
            .associate { it.key to it.color }

    fun updateOverrides(overrides: Map<String, Map<String, String>>) {
        myState.overrides = overrides.entries.flatMap { (theme, values) ->
            values.map { (key, color) -> ColorOverride(theme, key, color) }
        }.toMutableList()
        applyAll()
    }

    /**
     * Applies the stored overrides to all three schemes. Re-applying is
     * idempotent: the non-foreground attributes of each key are preserved.
     */
    fun applyAll() {
        if (applying) return
        applying = true
        try {
            val manager = EditorColorsManager.getInstance()
            for (theme in THEMES) {
                val scheme = manager.getScheme(theme) ?: continue
                for ((keyName, value) in overrides(theme)) {
                    val key = TextAttributesKey.find(keyName) ?: continue
                    val color = parseColor(value) ?: continue
                    runCatching {
                        scheme.setAttributes(key, withForeground(scheme.getAttributes(key), color))
                    }.onFailure {
                        LOG.warn("Cannot apply Alabaster colour override $keyName to ${scheme.name}", it)
                    }
                }
            }
            // Notify the editor so the new colours are painted immediately. The
            // listener above is re-entrancy guarded by `applying`.
            manager.setGlobalScheme(manager.globalScheme)
        } finally {
            applying = false
        }
    }

    private fun withForeground(existing: TextAttributes?, color: Color) =
        TextAttributes(
            color,
            existing?.backgroundColor,
            existing?.effectColor,
            existing?.effectType,
            existing?.fontType ?: Font.PLAIN,
        )

    private fun parseColor(value: String): Color? =
        runCatching { Color(Integer.parseInt(value, 16)) }.getOrNull()

    companion object {
        val THEMES = listOf("Alabaster", "Alabaster BG", "Alabaster Dark")

        private val LOG = Logger.getInstance(AlabasterSettingsService::class.java)

        fun getInstance(): AlabasterSettingsService =
            ApplicationManager.getApplication().getService(AlabasterSettingsService::class.java)

        fun toHex(color: Color): String =
            String.format("%06x", color.rgb and 0xFFFFFF)
    }
}
