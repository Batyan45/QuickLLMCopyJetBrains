package com.batyan.QuickLLMCopy

/**
 * Stores the settings state for the Quick LLM Copy plugin.
 */
class QuickLLMCopySettingsState {
    /**
     * Default list of excluded directories.
     * Users can modify this list in the settings UI.
     * Stored as a comma-separated string.
     */
    var excludedDirectories: String = ".idea,.git,.gradle,build,out,node_modules";
}
