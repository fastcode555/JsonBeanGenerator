package com.awesome.plugins.json2bean.utils

import com.awesome.core.generators.DartJsonGenerator
import com.awesome.core.generators.PythonJsonGenerator
import com.awesome.core.generators.TsJsonGenerator
import com.awesome.core.generators.kt.KtFastJsonGenerator
import com.awesome.core.generators.kt.KtGsonGenerator
import com.awesome.core.generators.kt.MapKtJsonGenerator
import com.intellij.psi.PsiDirectory

object GeneratorHelper {

    /**
     * json转成bean的方法（预览或非 Dart 路径用，返回单个字符串）
     **/
    fun json2Bean(
        fileType: String, content: String,
        className: String,
        extendName: String,
        impName: String,
        isSqliteEnable: Boolean,
        primaryKey: String,
        depType: String,
        psiDirectory: PsiDirectory,
        needClone: Boolean,
        splitGFile: Boolean = false,
    ): String {
        if (fileType == ".dart") {
            return DartJsonGenerator(
                content,
                className,
                extendName,
                impName,
                isSqliteEnable,
                primaryKey,
                needClone,
                splitGFile,
            ).toString()
        } else if (fileType == ".ts") {
            return TsJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        } else if (fileType == ".py") {
            return PythonJsonGenerator(
                content,
                className,
                extendName,
                impName
            ).toString()
        }
        return DartJsonGenerator(
            content,
            className,
            extendName,
            impName,
            isSqliteEnable,
            primaryKey,
            needClone,
            splitGFile,
        ).toString()
    }

    /**
     * Dart 双文件生成入口：写文件路径用，返回主文件与可选 part 文件的内容。
     */
    fun dartGenerate(
        content: String,
        className: String,
        extendName: String,
        impName: String,
        isSqliteEnable: Boolean,
        primaryKey: String,
        needClone: Boolean,
        splitGFile: Boolean,
    ): DartJsonGenerator.Output {
        return DartJsonGenerator(
            content,
            className,
            extendName,
            impName,
            isSqliteEnable,
            primaryKey,
            needClone,
            splitGFile,
        ).generate()
    }

    fun json2KtOrJava(
        fileType: String,
        fileName: String,
        content: String,
        extendName: String,
        impName: String,
        depType: String,
        psiDirectory: PsiDirectory
    ) {
        if (fileType == ".kt") {
            if (depType == "none") {
                MapKtJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "gson") {
                KtGsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            } else if (depType == "fastjson") {
                KtFastJsonGenerator(content, fileName, extendName, impName, psiDirectory).generate()
            }
        } else if (fileType == ".java") {

        }
    }

}
