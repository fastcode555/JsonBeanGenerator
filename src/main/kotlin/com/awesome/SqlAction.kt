package com.awesome

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class SqlAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.getData<Project>(PlatformDataKeys.PROJECT)
        val editor: Editor = e.getRequiredData<Editor>(CommonDataKeys.EDITOR)
        if (project != null && editor != null) {
            val mDialog = SqlDialog(project,editor)
            mDialog.showDialog()
        }
    }
}