package com.awesome.common

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

abstract class BaseAnAction : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val types = fileType()
        if (types.isEmpty()) return
        e.presentation.setEnabledAndVisible(psiFile != null && types.contains(psiFile.virtualFile.extension))
    }

    abstract fun fileType(): ArrayList<String>
}
