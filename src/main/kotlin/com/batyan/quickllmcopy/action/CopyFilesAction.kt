package com.batyan.quickllmcopy.action

import com.batyan.quickllmcopy.setting.SettingsService
import com.batyan.quickllmcopy.utils.FileUtils
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

class CopyFilesAction : AnAction() {
    private val LOG = Logger.getInstance(CopyFilesAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        if (virtualFiles.isEmpty()) {
            return
        }

        // Append File Copy
        val settings = SettingsService.Companion.getInstance()
        val content = FileUtils.buildSelectedFilesContent(project, virtualFiles, settings)

        CopyPasteManager.getInstance().setContents(StringSelection(content))
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Quick LLM Copy Notification")
                .createNotification("Code copied to clipboard!", NotificationType.INFORMATION)
                .notify(project)
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
