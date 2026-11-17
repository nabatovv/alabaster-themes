package com.vlnabatov.alabaster.settings

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

/**
 * Settings page that lets the user change the editor colours of the three
 * Alabaster colour schemes. The values are stored by
 * [AlabasterSettingsService], which applies them to the read-only bundled
 * schemes and re-applies them on startup.
 */
class AlabasterSettingsConfigurable : SearchableConfigurable {

    private var rootComponent: JComponent? = null
    private val colorPanels = linkedMapOf<String, LinkedHashMap<String, ColorPanel>>()
    private val initialColors = linkedMapOf<String, Color>()

    override fun getId(): String = ID

    override fun getDisplayName(): String = "Alabaster Themes"

    override fun createComponent(): JComponent {
        if (rootComponent == null) {
            rootComponent = buildUi()
        }
        reset()
        return rootComponent!!
    }

    override fun isModified(): Boolean =
        colorPanels.any { (theme, panels) ->
            panels.any { (key, panel) ->
                panel.selectedColor != initialColors[themeKey(theme, key)]
            }
        }

    override fun apply() {
        val values = linkedMapOf<String, MutableMap<String, String>>()
        val service = AlabasterSettingsService.getInstance()
        for (theme in AlabasterSettingsService.THEMES) {
            val themeValues = service.overrides(theme).toMutableMap()
            colorPanels[theme]?.forEach { (key, panel) ->
                val color = panel.selectedColor ?: return@forEach
                if (color != initialColors[themeKey(theme, key)]) {
                    themeValues[key] = AlabasterSettingsService.toHex(color)
                }
            }
            values[theme] = themeValues
        }
        service.updateOverrides(values)
        initialColors.clear()
        initialColors.putAll(captureColors())
    }

    override fun reset() {
        if (rootComponent == null) return
        AlabasterSettingsService.getInstance().applyAll()
        initialColors.clear()
        val manager = EditorColorsManager.getInstance()
        for (theme in AlabasterSettingsService.THEMES) {
            val scheme = manager.getScheme(theme)
            colorPanels[theme]?.forEach { (keyName, panel) ->
                val color = TextAttributesKey.find(keyName)
                    ?.let { scheme?.getAttributes(it)?.foregroundColor }
                    ?: scheme?.defaultForeground
                    ?: Color.BLACK
                panel.selectedColor = color
                initialColors[themeKey(theme, keyName)] = color
            }
        }
    }

    override fun disposeUIResources() {
        rootComponent = null
        colorPanels.clear()
        initialColors.clear()
    }

    private fun buildUi(): JComponent {
        val tabs = JTabbedPane()
        val manager = EditorColorsManager.getInstance()
        for (theme in AlabasterSettingsService.THEMES) {
            val scheme = manager.getScheme(theme)
            val grid = JPanel(GridLayout(0, 2, 16, 6))
            grid.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
            val panels = linkedMapOf<String, ColorPanel>()
            for ((keyName, label) in COLOR_KEYS) {
                val color = TextAttributesKey.find(keyName)
                    ?.let { scheme?.getAttributes(it)?.foregroundColor }
                    ?: scheme?.defaultForeground
                    ?: Color.BLACK
                val picker = ColorPanel().apply {
                    selectedColor = color
                    toolTipText = "$keyName — $label"
                }
                val cell = JPanel(BorderLayout(8, 0)).apply {
                    add(JLabel(label), BorderLayout.WEST)
                    add(picker, BorderLayout.EAST)
                }
                grid.add(cell)
                panels[keyName] = picker
            }
            colorPanels[theme] = panels
            tabs.addTab(theme, JBScrollPane(grid))
        }
        return JPanel(BorderLayout(0, 8)).apply {
            border = JBUI.Borders.empty(8)
            add(
                JLabel(
                    "<html>Change the editor colours of the three Alabaster colour schemes. " +
                        "The plugin stores the values and re-applies them when the IDE starts. " +
                        "Elements the theme intentionally renders as default text " +
                        "(keywords, variables and call sites) follow the Text colour.</html>",
                ),
                BorderLayout.NORTH,
            )
            add(tabs, BorderLayout.CENTER)
        }
    }

    private fun captureColors(): Map<String, Color> =
        colorPanels.flatMap { (theme, panels) ->
            panels.mapNotNull { (key, panel) ->
                panel.selectedColor?.let { themeKey(theme, key) to it }
            }
        }.toMap()

    private fun themeKey(theme: String, key: String) = "$theme|$key"

    companion object {
        const val ID = "com.vlnabatov.alabaster.settings"

        private val COLOR_KEYS = linkedMapOf(
            "TEXT" to "Text",
            "DEFAULT_STRING" to "String",
            "DEFAULT_NUMBER" to "Number",
            "DEFAULT_CONSTANT" to "Constant",
            "DEFAULT_LINE_COMMENT" to "Line comment",
            "DEFAULT_BLOCK_COMMENT" to "Block comment",
            "DEFAULT_DOC_COMMENT" to "Doc comment",
            "DEFAULT_DOC_COMMENT_TAG" to "Doc comment tag",
            "DEFAULT_FUNCTION_DECLARATION" to "Function declaration",
            "DEFAULT_CLASS_NAME" to "Class name",
            "DEFAULT_INTERFACE_NAME" to "Interface name",
            "DEFAULT_INSTANCE_FIELD" to "Instance field",
            "DEFAULT_STATIC_FIELD" to "Static field",
            "DEFAULT_OPERATION_SIGN" to "Operator",
            "DEFAULT_BRACES" to "Braces",
            "DEFAULT_BRACKETS" to "Brackets",
            "DEFAULT_PARENTHESES" to "Parentheses",
            "DEFAULT_COMMA" to "Comma",
            "DEFAULT_SEMICOLON" to "Semicolon",
            "DEFAULT_DOT" to "Dot",
            "DEFAULT_VALID_STRING_ESCAPE" to "String escape",
            "DEFAULT_INVALID_STRING_ESCAPE" to "Invalid escape",
            "DEFAULT_METADATA" to "Annotation",
            "DEFAULT_TAG" to "Tag",
            "DEFAULT_ATTRIBUTE" to "Attribute",
            "DEFAULT_ENTITY" to "Entity",
            "DEFAULT_LABEL" to "Label",
        )
    }
}
