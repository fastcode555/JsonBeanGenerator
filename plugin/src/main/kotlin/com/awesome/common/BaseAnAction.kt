package com.awesome.common

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ReadAction

abstract class BaseAnAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        ReadAction.run<Throwable> {
            val psiFile = e.getData(CommonDataKeys.PSI_FILE)
            val types = fileType()
            if (types.isEmpty()) return@run
            e.presentation.isEnabledAndVisible = psiFile != null && types.contains(psiFile.virtualFile.extension)
        }
    }

    abstract fun fileType(): ArrayList<String>
}
