package com.awesome.plugins.json2bean

import com.awesome.utils.regex
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor

//匹配到class的bean类的字段
private const val FILED_REGEX = "[a-zA-Z0-9\\?\\<\\>\\,_]+ [0-9a-zA-Z_]*(?=;)"

class CopyJsonGenerateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val selectionModel = editor?.selectionModel
        if (selectionModel != null) {
            WriteCommandAction.runWriteCommandAction(e.project) {
                val content = editor.document.text
                var targetIndex = 0
                var className = ""
                //获取选中的class跟选中的位置
                content.regex("class .*?\\{") {
                    val index = content.indexOf(it)
                    if (index > 0 && index < selectionModel.selectionStart) {
                        //获取到最接近选中位置的完整的class
                        targetIndex = index
                        //获取到className
                        className = it.replace("class ", "").trim().split(" ")[0]
                    }
                }
                val classContent = content.substring(targetIndex, selectionModel.selectionStart)
                val fromJsonBuilder = StringBuilder("  $className.copy($className copy) {\n")
                classContent.regex(FILED_REGEX) {
                    val results = it.split(" ")
                    if (results.size == 2) {
                        val fieldName = results[1]
                        fromJsonBuilder.append("    $fieldName = copy.$fieldName;\n")
                    }
                }
                fromJsonBuilder.append("  }\n")
                editor.document.insertString(selectionModel.selectionStart, fromJsonBuilder.toString())
            }
        }
    }
}