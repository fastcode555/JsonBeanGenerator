package com.awesome.plugins.json2bean

import com.awesome.common.BaseAnAction
import com.awesome.utils.RegexText
import com.awesome.utils.regex
import com.awesome.utils.regexOne
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import org.intellij.lang.annotations.Pattern
import java.io.File

//匹配到class的bean类的字段
private const val FILED_REGEX = "[a-zA-Z0-9\\?\\<\\>\\,_]+ [0-9a-zA-Z_]*(?=;)"

class ToJsonGenerateAction : AnAction() {

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
                //获取选中的class跟选中的位置
                content.regex("class .*?\\{") {
                    val index = content.indexOf(it)
                    if (index > 0 && index < selectionModel.selectionStart) {
                        //获取到最接近选中位置的完整的class
                        targetIndex = index
                    }
                }
                val classContent = content.substring(targetIndex, selectionModel.selectionStart)
                val toJsonBuilder = StringBuilder("  Map<String, dynamic> toJson() => {\n")
                classContent.regex(FILED_REGEX) {
                    val results = it.split(" ")
                    if (results.size == 2) {
                        val fieldName = results[1]
                        toJsonBuilder.append("\t\t\t'$fieldName': ${getParseType(results[0], fieldName)},\n")
                    }
                }

                editor.document.insertString(
                    selectionModel.selectionStart,
                    "${toJsonBuilder.append("\t\t}")};\n"
                )
            }
        }
    }

    private fun getParseType(s: String, fieldName: String): String {
        val typeName = s.replace("?", "")
        if (typeName == "String" || typeName == "int" || typeName == "num" || typeName == "double" || typeName == "bool") {
            return fieldName
        } else if (typeName.startsWith("List<")) {
            val innerType = getInnerType(typeName)
            if (innerType == "String" || innerType == "int" || innerType == "num" || innerType == "double" || innerType == "bool") {
                return "$fieldName"
            }
            return "$fieldName?.map((v) => v.toJson()).toList()"
        } else {
            return "$fieldName?.toJson()"
        }
    }

    private fun getInnerType(typeName: String): String {
        return typeName.substring("List<".length, typeName.length - 1)
    }
}
