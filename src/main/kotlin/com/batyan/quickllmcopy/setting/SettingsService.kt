package com.batyan.quickllmcopy.setting

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Service for managing Quick LLM Copy plugin settings. Uses PersistentStateComponent to store and
 * load settings.
 */
@State(
    name = "com.batyan.quickllmcopy.setting.QuickLLMCopySettings",
    storages = [Storage("quickLLMCopySettings.xml")]
)
class SettingsService : PersistentStateComponent<SettingsState> {
    private var myState = SettingsState()

    override fun getState(): SettingsState {
        return myState
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    /**
     * Gets the list of excluded directories.
     * @return List of excluded directory names.
     */
    fun getExcludedDirectories(): List<String> {
        return myState.excludedDirectories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    /**
     * Gets the prefix text for the copied code.
     * @return The prefix text.
     */
    fun getPrefixText(): String {
        return myState.prefixText
    }

    /**
     * Gets the text that appears before the codebase structure.
     * @return The codebase introduction text.
     */
    fun getCodebaseText(): String {
        return myState.codebaseText
    }

    /**
     * Gets the list of included file extensions. Extensions should be in the format ".ext" (e.g.,
     * ".kt"). An empty list means all files are included.
     * @return List of included file extensions.
     */
    fun getIncludedFileExtensions(): List<String> {
        if (myState.includedFileExtensions.isBlank()) {
            return emptyList() // Include all if blank
        }
        return myState.includedFileExtensions.split(",").map { it.trim().lowercase() }.filter {
            it.isNotEmpty() && it.startsWith(".")
        }
    }

    companion object {
        /**
         * Gets the instance of the settings service.
         * @return The settings service instance.
         */
        fun getInstance(): SettingsService {
            return ApplicationManager.getApplication().getService(SettingsService::class.java)
        }
    }
}
