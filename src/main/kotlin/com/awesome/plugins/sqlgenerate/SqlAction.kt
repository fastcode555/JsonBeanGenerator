package com.awesome.plugins.sqlgenerate

import com.awesome.SqlDialog
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

class SqlAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.getData<Project>(PlatformDataKeys.PROJECT)
        val mDirectory = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (mDirectory != null && mDirectory is PsiDirectory) {
            val mDialog = SqlDialog(project, mDirectory)
            mDialog.showDialog()
        }
    }
}
