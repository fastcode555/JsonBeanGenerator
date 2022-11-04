package com.awesome.plugins.testaction

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.awesome.ideatool.showNotify

class TestAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.showNotify("我的第一个插件1", "Hello, World" + System.currentTimeMillis())
    }
}