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
class FlutterTailWindAction : AnAction() {

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
            //创建tailwind_ext.g.dart 的文件
            val text = psiFile.text.regexOne(RegexText.variableConstStringRegex)
            val color = psiFile.text.regexOne(RegexText.colorConstRegex)
            val builder = StringBuilder("part of 'tailwind_ext.dart';\n\n")
            if (text != null) {
                builder.append(parseColorText(psiFile.text))
            } else if (color != null) {
                builder.append(parseColor(psiFile.text))
            }
            val file = File(psiFile.parent?.virtualFile?.path, "tailwind_ext.g.dart")
            file.writeText(builder.toString())

            //创建tailwind_ext.dart
            val tailFile = File(psiFile.parent?.virtualFile?.path, "tailwind_ext.dart")
            if (!tailFile.exists()) {
                tailFile.writeText(FlutterTailwind.fluterTailWindConst)
            }
        }
    }

    /**
     * 需要解析出颜色的名字，并生成文件tailwind extension的文件
     **/
    private fun parseColor(text: String): StringBuilder {
        return parseText(
            text,
            "  T get %s => this..color(Colours.%s);\n",
            "  T get border%s => this..border(Colours.%s);\n",
            "  T get text%s => this..textColor(Colours.%s);\n",
            RegexText.colorConstNameRegex
        )
    }

    /**
     * 需要解析出颜色的名字，生成tailwind extension的文件
     **/
    private fun parseColorText(text: String): StringBuilder {
        return parseText(
            text,
            "  T get %s => this..color(Colours.%s.cr);\n",
            "  T get border%s => this..border(Colours.%s.cr);\n",
            "  T get border%s => this..textColor(Colours.%s.cr);\n",
            RegexText.variableConstNameRegex
        )
    }

    private fun getFilterName(name: String): String {
        if (FlutterTailwind.colors.contains(name)) {
            return "${name}x"
        }
        return name
    }

    private fun parseText(
        text: String, string1: String, string2: String, string3: String, regex: String
    ): StringBuilder {
        val colorExt = StringBuilder()
        val borderColorExt = StringBuilder()
        val textColorExt = StringBuilder()
        val filedNames = text.regexAll(regex)

        for (i in filedNames.indices) {
            val filedName = filedNames[i]
            val colorName = getFilterName(filedName)
            colorExt.append(string1.format(colorName, filedName))
            borderColorExt.append(string2.format(colorName.firstUpperCamel(), filedName))
            textColorExt.append(string3.format(colorName.firstUpperCamel(), filedName))
            if (i != filedNames.indices.length - 1) {
                colorExt.append("\n")
                borderColorExt.append("\n")
                textColorExt.append("\n")
            }
        }
        val builder = StringBuilder("extension ColorExt<T extends ColorBuilder> on T {\n")
        builder.append(colorExt).append("}\n\n")

        builder.append("extension BorderColorExt<T extends BorderColorBuilder> on T {\n")
        builder.append(borderColorExt).append("}\n\n")

        builder.append("extension TextColorExt<T extends TextColorBuilder> on T {\n")
        builder.append(textColorExt).append("}")
        return builder
    }
}
