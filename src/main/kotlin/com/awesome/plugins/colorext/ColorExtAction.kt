package com.awesome.plugins.colorext

import ai.grazie.nlp.utils.length
import com.awesome.utils.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import firstUpperCamel
import java.io.File

/**
 * 根据Colours这个文件，创建出扩展
 **/
class ColorExtAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val psiFile = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (psiFile is PsiFile) {
            val isDartFile = psiFile.virtualFile.extension == "dart"
            val text = psiFile.text.regexOne(RegexText.variableConstStringRegex)
            val color = psiFile.text.regexOne(RegexText.colorConstRegex)
            e.presentation.isEnabledAndVisible = isDartFile && (text != null || color != null)
        } else {
            e.presentation.isEnabledAndVisible = false
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val psiFile = e.getData<PsiElement>(CommonDataKeys.PSI_ELEMENT)
        if (psiFile != null && psiFile is PsiFile) {
            val text = psiFile.text.regexOne(RegexText.variableConstStringRegex)
            val color = psiFile.text.regexOne(RegexText.colorConstRegex)
            val builder = StringBuilder("import 'colours.dart';\nimport 'package:infinity_core/core.dart';\n\n")
            if (text != null) {
                builder.append(parseColorText(psiFile.text))
            } else if (color != null) {
                builder.append(parseColor(psiFile.text))
            }
            val file = File(psiFile.parent?.virtualFile?.path, "tailwind_ext.dart")
            file.writeText(builder.toString())
        }
    }

    /**
     * 需要解析出颜色的名字，并生成文件tailwind extension的文件
     **/
    private fun parseColor(text: String): StringBuilder {
        return parseText(
            text,
            "  T get %s => this..color = Colours.%s;\n",
            "  T get border%s => this..border(Colours.%s);\n",
            RegexText.colorConstNameRegex
        )
    }

    /**
     * 需要解析出颜色的名字，生成tailwind extension的文件
     **/
    private fun parseColorText(text: String): StringBuilder {
        return parseText(
            text,
            "  T get %s => this..color = Colours.%s.cr;\n",
            "  T get border%s => this..border(Colours.%s.cr);\n",
            RegexText.variableConstNameRegex
        )
    }

    private fun parseText(text: String, string1: String, string2: String, regex: String): StringBuilder {
        val firstClass = StringBuilder()
        val secondClass = StringBuilder()
        val filedNames = text.regexAll(regex)

        for (i in filedNames.indices) {
            val filedName = filedNames[i]
            firstClass.append(string1.format(filedName, filedName))
            secondClass.append(string2.format(filedName.firstUpperCamel(), filedName))
            if (i != filedNames.indices.length - 1) {
                firstClass.append("\n")
                secondClass.append("\n")
            }
        }
        val builder = StringBuilder("extension TailWindExt<T extends ColorBuilder> on T {\n")
        builder.append(firstClass).append("}\n\n")
        builder.append("extension TailWindBD<T extends BoxDecorationBuilder> on T {\n")
        builder.append(secondClass).append("}")
        println(builder)
        return builder
    }
}
