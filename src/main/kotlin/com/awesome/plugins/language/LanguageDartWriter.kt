package com.awesome.plugins.language

import com.awesome.utils.regexOne
import com.intellij.openapi.editor.SelectionModel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import toCamel
import java.io.File

class LanguageDartWriter(
    val mapValue: HashMap<String, String?>,
    val idKey: String,
    val dirPath: String,
    val psiElement: PsiElement,
    val rawText: String,
    val selectionModel: SelectionModel?
) {
    var moduleName = ""

    fun startWrite() {
        writeKey2Index()
    }

    //将key写入到strings.dart文件中
    fun writeKey2Index() {
        val stringsFile = File("${dirPath}/strings.dart")
        var dartBuilder: java.lang.StringBuilder?
        if (!stringsFile.exists()) {
            dartBuilder = java.lang.StringBuilder()
            stringsFile.parentFile.mkdirs()
            stringsFile.createNewFile()
            writeImportAndHeader(dartBuilder)
            writeTranslationService()
        } else {
            dartBuilder = StringBuilder(stringsFile.readText())
            for ((k, v) in mapValue) {
                val fileName = generateFileName(k)
                writeValue2Dart(fileName, k, v)
            }
        }
        val index = dartBuilder.lastIndexOf("}")
        dartBuilder.insert(index, "\tstatic const String $idKey = '$idKey';\n")
        stringsFile.writeText(dartBuilder.toString())
    }

    //写入导包和导头部语言部分
    private fun writeImportAndHeader(dartBuilder: StringBuilder) {
        val method = StringBuilder()
        val currentPackageName = getCurrentPackageName()
        method.append("\nconst Map<String, Map<String, String>> localizedSimpleValues = {\n")
        for ((k, v) in mapValue) {
            val fileName = generateFileName(k)
            dartBuilder.append("import '$currentPackageName$fileName';\n")
            method.append("\t'$k':${generateVariableName(k)},\n")
            writeValue2Dart(fileName, k, v)
        }
        method.append("};\n")
        dartBuilder.append(method.toString())
        dartBuilder.append("\nclass Ids {\n")
        dartBuilder.append("}\n")
    }

    //获取当前导包的名字前缀
    private fun getCurrentPackageName(): String {
        if (moduleName.isEmpty()) {
            val pubspecFile = File("${psiElement.project.basePath}/pubspec.yaml").readText()
            moduleName = pubspecFile.regexOne("(?<=name\\:).*?(?=\\n)")?.trim() ?: ""
        }
        val filePath = dirPath
        val content = "${moduleName}/lib";
        val index = filePath.indexOf(content) + content.length
        val importResult = filePath.substring(index, filePath.length)
        return "package:$moduleName/$importResult/".replace("//", "/")
    }

    //生成translation_service 的服务文件
    private fun writeTranslationService() {
        val translationFile = File("${dirPath}/translation_service.dart")
        translationFile.writeText(
            "import 'package:flutter/material.dart';\nimport 'package:get/get.dart';\n" +
                    "import '${getCurrentPackageName()}strings.dart';\n\n" +
                    "class TranslationService extends Translations {\n" +
                    "  static Locale? get locale => Get.deviceLocale;\n" +
                    "  static final fallbackLocale = Locale('en', 'US');\n\n" +
                    "  @override\n" +
                    "  Map<String, Map<String, String>> get keys => localizedSimpleValues;\n" +
                    "}\n"
        )
    }

    //将翻译的值生成到各个文件中
    fun writeValue2Dart(fileName: String, code: String, value: String?) {
        val importStringDart = "import '${getCurrentPackageName()}strings.dart';\n"
        var builder: java.lang.StringBuilder?
        val stringsFile = File("${dirPath}/$fileName")
        if (!stringsFile.exists()) {
            stringsFile.createNewFile()
            builder = java.lang.StringBuilder()
            builder.append(importStringDart)
            builder.append("\nconst Map<String, String> ${generateVariableName(code)} = {\n")
            builder.append("};")
        } else {
            builder = java.lang.StringBuilder(stringsFile.readText())
        }
        val index = builder.lastIndexOf("};")
        val idValue = "\tIds.${idKey}:'$value',\n"
        builder.insert(index, idValue)
        stringsFile.writeText(builder.toString())
        if (psiElement is PsiFile) {
            if (selectionModel != null) {
                selectionModel.editor.document.replaceString(
                    selectionModel.selectionStart,
                    selectionModel.selectionEnd,
                    "Ids.${idKey}.tr"
                )
                //判断当前文件夹是否导包，如果没有，需要进行导包
                val text = selectionModel.editor.document.text
                if (!text.contains(importStringDart)) {
                    selectionModel.editor.document.insertString(0, importStringDart)
                }
            } else {
                var text = psiElement.text
                if (text.contains("'$rawText'")) {
                    text = text.replace("'$rawText'", "Ids.${idKey}.tr")
                }
                if (text.contains("\"$rawText\"")) {
                    text = text.replace("\"$rawText\"", "Ids.${idKey}.tr")
                }
                File(psiElement.virtualFile.path).writeText(text)
            }
        }
    }

    //生成文件的文件名
    private fun generateFileName(code: String): String {
        return "string_${code.replace("-", "_").toLowerCase()}.dart"
    }

    //生成每个语言的文件的变量名
    private fun generateVariableName(code: String): String {
        return "${"language_$code".toCamel()}"
    }
}