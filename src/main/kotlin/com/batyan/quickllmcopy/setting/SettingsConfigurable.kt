package com.batyan.quickllmcopy.setting

import com.intellij.openapi.options.Configurable
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField

/**
 * Provides the settings UI for the Quick LLM Copy plugin.
 */
class SettingsConfigurable : Configurable {
    private lateinit var excludedDirsField: JTextField
    private lateinit var prefixTextField: JTextField
    private lateinit var codebaseTextField: JTextField
    private lateinit var includedExtensionsField: JTextField

    override fun getDisplayName(): String {
        return "Quick LLM Copy"
    }

    override fun createComponent(): JComponent {
        excludedDirsField = JTextField()
        prefixTextField = JTextField()
        codebaseTextField = JTextField()
        includedExtensionsField = JTextField()

        val ui = panel {
            row("Excluded directories (comma-separated):") {
                cell(excludedDirsField).columns(COLUMNS_LARGE).align(AlignX.FILL)
            }
            row("Prefix text for copied code:") {
                cell(prefixTextField).columns(COLUMNS_LARGE).align(AlignX.FILL)
            }
            row("Text before codebase structure:") {
                cell(codebaseTextField).columns(COLUMNS_LARGE).align(AlignX.FILL)
            }
            row("Included file extensions (e.g., .kt,.java. Empty for all):") {
                cell(includedExtensionsField).columns(COLUMNS_LARGE).align(AlignX.FILL)
            }
        }
        reset()
        return ui
    }

    override fun isModified(): Boolean {
        val settingsState = SettingsService.Companion.getInstance().state
        return excludedDirsField.text != settingsState.excludedDirectories ||
                prefixTextField.text != settingsState.prefixText ||
                codebaseTextField.text != settingsState.codebaseText ||
                includedExtensionsField.text != settingsState.includedFileExtensions
    }

    override fun apply() {
        val settingsState = SettingsService.Companion.getInstance().state
        settingsState.excludedDirectories = excludedDirsField.text ?: ""
        settingsState.prefixText = prefixTextField.text ?: ""
        settingsState.codebaseText = codebaseTextField.text ?: ""
        settingsState.includedFileExtensions = includedExtensionsField.text ?: ""
    }

    override fun reset() {
        val settingsState = SettingsService.Companion.getInstance().state
        if (this::excludedDirsField.isInitialized) {
            excludedDirsField.text = settingsState.excludedDirectories
        }
        if (this::prefixTextField.isInitialized) {
            prefixTextField.text = settingsState.prefixText
        }
        if (this::codebaseTextField.isInitialized) {
            codebaseTextField.text = settingsState.codebaseText
        }
        if (this::includedExtensionsField.isInitialized) {
            includedExtensionsField.text = settingsState.includedFileExtensions
        }
    }
}
