package com.awesome.plugins.json2bean

import com.awesome.utils.RegexText
import com.awesome.utils.regex
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import java.io.File

//匹配到class的bean类的字段
private const val FILED_REGEX = "[a-zA-Z0-9\\?\\<\\>\\,_]+ [0-9a-zA-Z_]*(?=;)"

class CloneJsonGenerateAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val file = File(psiFile?.virtualFile?.path)
        if (file.exists()) {
            val lines = file.readLines()
            val regex = Regex(RegexText.variableRegex)
            var isEnable = false
            for (line in lines) {
                if (line.matches(regex)) {
                    isEnable = true
                    break
                }
            }
            e.presentation.setEnabledAndVisible(isEnable)
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor? = e.getData(CommonDataKeys.EDITOR)
        val selectionModel = editor?.selectionModel
        if (selectionModel != null) {
            e.runWriteCmd {
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
                val cloneMethod = StringBuilder("  $className clone() => $className(\n")
                classContent.regex(FILED_REGEX) {
                    val results = it.split(" ")
                    if (results.size == 2) {
                        var type = results[0].trim()
                        type = type.substring(0, results[0].length - 1)
                        val fieldName = results[1]
                        generate(type, fieldName, cloneMethod)
                    }
                }
                cloneMethod.append("      );\n")
                editor.document.insertString(selectionModel.selectionStart, cloneMethod.toString())
            }
        }
    }

    private fun generate(type: String, fieldName: String, cloneMethod: StringBuilder) {
        if (type.isNormalType()) {
            cloneMethod.append("        $fieldName: $fieldName,\n")
        } else if (type.startsWith("List<")) {
            val newType = type.replace("List<", "").replace(">", "")
            if (newType.isNormalType()) {
                cloneMethod.append("        $fieldName: List<$newType>.from($fieldName??[]),\n")
            } else {
                cloneMethod.append("        $fieldName: $fieldName?.map((v) => v.clone()).toList(),\n")
            }
        } else {
            cloneMethod.append("        $fieldName: $fieldName?.clone(),\n")
        }

    }

    private fun String.isNormalType(): Boolean {
        val type = this
        return type == "String" || type == "num" || type == "int" || type == "double" || type == "bool"
    }
}
