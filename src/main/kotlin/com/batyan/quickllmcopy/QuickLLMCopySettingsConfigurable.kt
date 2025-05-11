package com.batyan.QuickLLMCopy

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent
import javax.swing.JTextField

/**
 * Provides the settings UI for the Quick LLM Copy plugin.
 */
class QuickLLMCopySettingsConfigurable : Configurable {
    // private var myPanel: JPanel? = null // No longer a JPanel field
    private lateinit var excludedDirsField: JTextField // Changed to lateinit

    override fun getDisplayName(): String {
        return "Quick LLM Copy"
    }

    override fun createComponent(): JComponent {
        // Initialize the field before it's used in the panel builder
        excludedDirsField = JTextField()
        val ui = panel {
            row("Excluded directories (comma-separated):") {
                cell(excludedDirsField)
                    .columns(COLUMNS_LARGE)
                    .align(AlignX.FILL)
            }
        }
        // Load current settings into the field after UI creation
        reset()
        return ui
    }

    override fun isModified(): Boolean {
        val settingsState = QuickLLMCopySettingsService.getInstance().state
        return excludedDirsField.text != settingsState.excludedDirectories
    }

    override fun apply() {
        val settingsState = QuickLLMCopySettingsService.getInstance().state
        settingsState.excludedDirectories = excludedDirsField.text ?: ""
    }

    override fun reset() {
        val settingsState = QuickLLMCopySettingsService.getInstance().state
        // Ensure excludedDirsField is initialized before calling reset if createComponent hasn't run yet.
        // However, reset() is typically called after createComponent() has prepared the UI elements.
        if (this::excludedDirsField.isInitialized) {
            excludedDirsField.text = settingsState.excludedDirectories
        }
    }
} 