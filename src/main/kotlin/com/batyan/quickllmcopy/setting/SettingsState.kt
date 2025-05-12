package com.batyan.quickllmcopy.setting

/** Stores the settings state for the Quick LLM Copy plugin. */
class SettingsState {
    /**
     * Default list of excluded directories. Users can modify this list in the settings UI. Stored
     * as a comma-separated string.
     */
    var excludedDirectories: String = ".idea,.git,.gradle,build,out,node_modules"

    /** The text that appears before the copied code content. */
    var prefixText: String = "Provided code:"

    /** The text that appears before the codebase structure. */
    var codebaseText: String = "You can ask for other files from the codebase if needed:"

    /**
     * Comma-separated list of file extensions to include (e.g., .kt,.java). If empty, all file
     * extensions are included.
     */
    var includedFileExtensions: String = "" // Default to empty, meaning include all
}
