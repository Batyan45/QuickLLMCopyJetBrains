package com.batyan.QuickLLMCopy

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Service for managing Quick LLM Copy plugin settings.
 * Uses PersistentStateComponent to store and load settings.
 */
@State(
    name = "com.batyan.QuickLLMCopy.QuickLLMCopySettings",
    storages = [Storage("quickLLMCopySettings.xml")]
)
class QuickLLMCopySettingsService : PersistentStateComponent<QuickLLMCopySettingsState> {
    private var myState = QuickLLMCopySettingsState()

    override fun getState(): QuickLLMCopySettingsState {
        return myState
    }

    override fun loadState(state: QuickLLMCopySettingsState) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    /**
     * Gets the list of excluded directories.
     * @return List of excluded directory names.
     */
    fun getExcludedDirectories(): List<String> {
        return myState.excludedDirectories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    companion object {
        /**
         * Gets the instance of the settings service.
         * @return The settings service instance.
         */
        fun getInstance(): QuickLLMCopySettingsService {
            return ApplicationManager.getApplication().getService(QuickLLMCopySettingsService::class.java)
        }
    }
} 