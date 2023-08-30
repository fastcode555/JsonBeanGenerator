package com.awesome.plugins.json2bean

import com.awesome.common.BaseAnAction
import com.awesome.utils.regex
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import org.apache.http.util.TextUtils

//匹配到class的bean类的字段
private const val FILED_REGEX = "[a-zA-Z0-9\\?\\<\\>\\,_]+ [0-9a-zA-Z_]*(?=;)"

class ToAndFromJsonGenerateAction : BaseAnAction() {
    override fun fileType(): ArrayList<String> = arrayListOf("dart")
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
                val fromJsonBuilder = StringBuilder("  $className.fromJson(Map json) {\n")
                val toJsonBuilder = StringBuilder("  Map<String, dynamic> toJson() => {\n")
                classContent.regex(FILED_REGEX) {
                    val results = it.split(" ")
                    if (results.size == 2) {
                        val fieldName = results[1]
                        val typeName = results[0].replace("?", "").trim()
                        if (TextUtils.isEmpty(typeName)) return@regex
                        fromJsonBuilder.append("    $fieldName = ${getParseType(typeName, fieldName)};\n")
                        toJsonBuilder.append("\t\t\t'$fieldName': ${getToParseType(typeName, fieldName)},\n")
                    }
                }

                fromJsonBuilder.append("  }\n")
                editor.document.insertString(selectionModel.selectionStart, fromJsonBuilder.toString())
                editor.document.insertString(
                    selectionModel.selectionStart,
                    "${toJsonBuilder.append("\t\t};")}\n\n"
                )
            }
        }
    }

    private fun getToParseType(typeName: String, fieldName: String): String {
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

    private fun getParseType(typeName: String, fieldName: String): String {
        if (typeName == "String") {
            return "json.asString('$fieldName')"
        } else if (typeName == "int") {
            return "json.asInt('$fieldName')"
        } else if (typeName == "double") {
            return "json.asDouble('$fieldName')"
        } else if (typeName == "num") {
            return "json.asNum('$fieldName')"
        } else if (typeName == "bool") {
            return "json.asBool('$fieldName')"
        } else if (typeName.startsWith("List<")) {
            val innerType = getInnerType(typeName)
            if (innerType == "String" || innerType == "int" || innerType == "double" || innerType == "bool" || innerType == "num") {
                return "json.as$typeName('$fieldName')"
            } else {
                return "json.as$typeName('$fieldName',(v) => $innerType.fromJson(v))"
            }
        } else {
            return "json.asBean<$typeName>('$fieldName',(v) => $typeName.fromJson(v))"
        }
    }

    private fun getInnerType(typeName: String): String {
        return typeName.substring("List<".length, typeName.length - 1)
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.setEnabledAndVisible(psiFile != null && "dart" == psiFile.virtualFile.extension)
    }
}
