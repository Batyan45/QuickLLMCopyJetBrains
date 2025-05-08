package com.batyan.QuickLLMCopy

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.datatransfer.StringSelection
import java.io.IOException
import java.nio.charset.StandardCharsets

class CopyFilesWithCodebaseAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        if (virtualFiles.isEmpty()) {
            return
        }

        val allFiles = mutableListOf<VirtualFile>()
        for (file in virtualFiles) {
            collectFiles(file, allFiles)
        }

        // TODO: Get prefix and codebase text from settings
        val prefixText = "Provided code:"
        val codebaseText = "You can ask for other files from the codebase if needed:"
        val resultText = StringBuilder("$prefixText\n\n")

        ApplicationManager.getApplication().runReadAction {
            for (file in allFiles) {
                if (file.isDirectory) continue

                val relativePath = getRelativePath(project, file)
                resultText.append("File: $relativePath\n")
                resultText.append("```\n")
                try {
                    val document = FileDocumentManager.getInstance().getDocument(file)
                    if (document != null) {
                        resultText.append(document.text)
                    } else {
                        resultText.append(String(file.contentsToByteArray(), StandardCharsets.UTF_8))
                    }
                } catch (ex: IOException) {
                    resultText.append("Error reading file: ${ex.message}")
                    // Optionally, notify about the error using the notification system as well
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Quick LLM Copy Notification")
                        .createNotification("Error reading file: ${file.name} - ${ex.message}", NotificationType.WARNING)
                        .notify(project)
                }
                resultText.append("\n```\n\n")
            }

            resultText.append("$codebaseText\n\n")
            resultText.append("```\n")
            resultText.append("Project structure:\n")
            project.basePath?.let {
                val projectRoot = com.intellij.openapi.vfs.LocalFileSystem.getInstance().findFileByPath(it)
                if (projectRoot != null) {
                    resultText.append(generateDirectoryTree(projectRoot, project))
                }
            }
            resultText.append("```\n")
        }

        CopyPasteManager.getInstance().setContents(StringSelection(resultText.toString()))
        // Messages.showInfoMessage("Code with codebase structure copied to clipboard!", "Quick LLM Copy") // Replaced
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Quick LLM Copy Notification")
            .createNotification("Code with codebase structure copied to clipboard!", NotificationType.INFORMATION)
            .notify(project)
    }

    private fun collectFiles(file: VirtualFile, allFiles: MutableList<VirtualFile>) {
        if (file.isDirectory) {
            file.children?.forEach { child -> collectFiles(child, allFiles) }
        } else {
            allFiles.add(file)
        }
    }

    private fun getRelativePath(project: Project, file: VirtualFile): String {
        val basePath = project.basePath
        if (basePath != null && file.path.startsWith(basePath)) {
            return file.path.substring(basePath.length + 1)
        }
        return file.name
    }

    private fun generateDirectoryTree(rootFile: VirtualFile, project: Project, indent: String = ""): String {
        val tree = StringBuilder()
        val children = rootFile.children?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: return ""

        children.forEachIndexed { index, child ->
            // Skip .idea, .git, .gradle, build directories commonly found in IntelliJ projects
            if (child.name == ".idea" || child.name == ".git" || child.name == ".gradle" || child.name == "build" || child.name == "out") {
                // continue to next iteration
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
        e.presentation.isEnabledAndVisible = project != null && virtualFiles != null && virtualFiles.isNotEmpty()
    }
} 