package com.batyan.quickllmcopy.utils

import com.batyan.quickllmcopy.setting.SettingsService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileVisitor
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

/** Utility object providing file operations for the QuickLLMCopy plugin. */
object FileUtils {

    /**
     * Collects files that match the specified extensions.
     *
     * @param rootFile The starting file or directory to process
     * @param includedExtensions List of file extensions to include with dot prefix (e.g., ".kt")
     *                          If empty, all files are included
     * @return
     * List of all matching files
     */
    fun collectFiles(
        rootFile: VirtualFile,
        includedExtensions: List<String> = emptyList()
    ): List<VirtualFile> = buildList {
        VfsUtilCore.visitChildrenRecursively(
            rootFile,
            object : VirtualFileVisitor<Unit>() {
                override fun visitFile(file: VirtualFile): Boolean {
                    if (!file.isDirectory && file.matchesExtensions(includedExtensions)) {
                        add(file)
                    }
                    return true // Continue visiting
                }
            }
        )
    }

    /** Extension function to check if a file matches the given extensions. */
    private fun VirtualFile.matchesExtensions(includedExtensions: List<String>): Boolean =
        includedExtensions.isEmpty() || extension?.lowercase()?.let { ".$it" } in includedExtensions

    /**
     * Gets the relative path of a file with respect to the project's base path.
     *
     * @param project The current project
     * @param file The file for which to get the relative path
     * @return The relative path string, or the file name if it's not under the project base path
     */
    fun getRelativePath(project: Project, file: VirtualFile): String =
        project.basePath?.let { basePath ->
            if (file.path.startsWith(basePath)) {
                file.path.removePrefix("$basePath${File.separator}")
            } else {
                file.name
            }
        }
            ?: file.name

    /**
     * Builds a string containing the content of selected files, formatted for LLM.
     *
     * @param project The current project.
     * @param initialVirtualFiles The initially selected virtual files by the user.
     * @param settings The plugin settings service instance.
     * @return A string with formatted file contents.
     */
    fun buildSelectedFilesContent(
        project: Project,
        initialVirtualFiles: Array<VirtualFile>,
        settings: SettingsService
    ): String {
        val allCollectedFiles = mutableListOf<VirtualFile>()

        for (file in initialVirtualFiles) {
            allCollectedFiles.addAll(collectFiles(file, settings.getIncludedFileExtensions()))
        }

        val resultText = StringBuilder("${settings.getPrefixText()}\n\n")

        ApplicationManager.getApplication().runReadAction {
            for (file in allCollectedFiles) {
                if (file.isDirectory) continue

                val relativePath = getRelativePath(project, file)
                resultText.append("File: $relativePath\n")
                resultText.append("```\n")
                try {
                    val document = FileDocumentManager.getInstance().getDocument(file)
                    if (document != null) {
                        resultText.append(document.text)
                    } else {
                        // Show a message if the file is binary
                        if (file.fileType.isBinary) {
                            resultText.append("[Binary file: content not displayed]")
                        } else {
                            resultText.append(
                                String(file.contentsToByteArray(), StandardCharsets.UTF_8)
                            )
                        }
                    }
                } catch (ex: IOException) {
                    resultText.append("Error reading file: ${ex.message}")
                    // Notify user about the error for this specific file
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Quick LLM Copy Notification")
                        .createNotification(
                            "Error reading file: ${file.name} - ${ex.message}",
                            NotificationType.WARNING
                        )
                        .notify(project)
                }
                resultText.append("\n```\n\n")
            }
        }
        return resultText.toString()
    }
}
