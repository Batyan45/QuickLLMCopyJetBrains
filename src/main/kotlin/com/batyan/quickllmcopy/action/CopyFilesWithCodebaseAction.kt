package com.batyan.quickllmcopy.action

import com.batyan.quickllmcopy.setting.SettingsService
import com.batyan.quickllmcopy.utils.FileUtils
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection

class CopyFilesWithCodebaseAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        if (virtualFiles.isEmpty()) {
            return
        }
        // Append File Copy
        val settings = SettingsService.Companion.getInstance()
        val baseContent = FileUtils.buildSelectedFilesContent(project, virtualFiles, settings)

        val resultText = StringBuilder(baseContent)

        // Append codebase specific parts
        val codebaseText = settings.getCodebaseText()
        resultText.append("$codebaseText\n\n")
        resultText.append("```\n")
        resultText.append("Project structure:\n")
        project.basePath?.let {
            val projectRoot = LocalFileSystem.getInstance().findFileByPath(it)
            if (projectRoot != null) {
                resultText.append(generateDirectoryTree(projectRoot, project))
            }
        }
        resultText.append("```\n")

        CopyPasteManager.getInstance().setContents(StringSelection(resultText.toString()))
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Quick LLM Copy Notification")
                .createNotification(
                        "Code with codebase structure copied to clipboard!",
                        NotificationType.INFORMATION
                )
                .notify(project)
    }

    private fun generateDirectoryTree(
            rootFile: VirtualFile,
            project: Project,
            indent: String = ""
    ): String {
        val tree = StringBuilder()
        val children =
                rootFile.children?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
                        ?: return ""

        // Get excluded directories from settings
        val excludedDirs = SettingsService.Companion.getInstance().getExcludedDirectories()

        children.forEachIndexed { index, child ->
            // Skip excluded directories
            if (excludedDirs.contains(child.name)) {
                return@forEachIndexed
            }

            val isLast = index == children.size - 1
            val prefix = if (isLast) "└── " else "├── "
            tree.append("$indent$prefix${child.name}${if (child.isDirectory) "/" else ""}\n")

            if (child.isDirectory) {
                val childIndent = if (isLast) "$indent    " else "$indent│   "
                tree.append(generateDirectoryTree(child, project, childIndent))
            }
        }
        return tree.toString()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isEnabledAndVisible =
                project != null && virtualFiles != null && virtualFiles.isNotEmpty()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
